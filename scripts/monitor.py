#!/usr/bin/env python3
"""
Main Monitoring Daemon for Fire TV App
Orchestrates error detection and auto-fixing
"""

import asyncio
import logging
import signal
import sys
import time
import json
import os
from typing import Dict, List, Optional, Any
from dataclasses import dataclass, asdict
from datetime import datetime, timedelta
from concurrent.futures import ThreadPoolExecutor

from adb_manager import ADBManager
from error_detector import ErrorDetector, DetectedError, ErrorSeverity
from auto_fixer import AutoFixer, FixAction, FixResult

@dataclass
class MonitoringStats:
    """Statistics for monitoring session"""
    start_time: float
    total_errors_detected: int
    total_fixes_attempted: int
    successful_fixes: int
    failed_fixes: int
    current_status: str
    last_error_time: Optional[float] = None
    last_fix_time: Optional[float] = None

class AppMonitor:
    """Main monitoring daemon for Fire TV app"""
    
    def __init__(self, config_path: str = "monitor_config.json"):
        self.config_path = config_path
        self.config = self._load_config()
        
        # Setup logging
        self._setup_logging()
        
        # Initialize components
        self.adb = ADBManager(
            self.config["device_ip"], 
            self.config["device_port"]
        )
        self.error_detector = ErrorDetector(self.config)
        self.auto_fixer = AutoFixer(self.adb)
        
        # Monitoring state
        self.is_running = False
        self.stats = MonitoringStats(
            start_time=time.time(),
            total_errors_detected=0,
            total_fixes_attempted=0,
            successful_fixes=0,
            failed_fixes=0,
            current_status="initializing"
        )
        
        # Background tasks
        self.logcat_task = None
        self.health_check_task = None
        self.maintenance_task = None
        
        # Logcat buffer
        self.logcat_buffer = []
        self.max_buffer_size = 1000
        
        # Thread pool for blocking operations
        self.executor = ThreadPoolExecutor(max_workers=3)
        
    def _load_config(self) -> Dict[str, Any]:
        """Load monitoring configuration"""
        default_config = {
            "device_ip": "192.168.4.94",
            "device_port": 5555,
            "monitoring_interval": 5,
            "logcat_buffer_size": 1000,
            "health_check_interval": 30,
            "maintenance_interval": 3600,  # 1 hour
            "max_fix_attempts_per_hour": 10,
            "enable_auto_fix": True,
            "enable_emergency_recovery": True,
            "log_level": "INFO",
            "log_file": "monitor.log",
            "report_file": "monitor_report.json"
        }
        
        if os.path.exists(self.config_path):
            try:
                with open(self.config_path, 'r') as f:
                    config = json.load(f)
                # Merge with defaults
                default_config.update(config)
            except Exception as e:
                print(f"Error loading config: {e}, using defaults")
        
        return default_config
    
    def _setup_logging(self):
        """Setup logging configuration"""
        log_level = getattr(logging, self.config["log_level"].upper(), logging.INFO)
        
        # Configure logger
        logging.basicConfig(
            level=log_level,
            format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler(self.config["log_file"]),
                logging.StreamHandler(sys.stdout)
            ]
        )
        
        self.logger = logging.getLogger(__name__)
        self.logger.info("Monitor logging initialized")
    
    async def start(self):
        """Start the monitoring daemon"""
        self.logger.info("Starting Fire TV App Monitor")
        self.is_running = True
        self.stats.current_status = "starting"
        
        # Setup signal handlers
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
        
        try:
            # Initialize connection
            if not await self._initialize_connection():
                self.logger.error("Failed to initialize ADB connection")
                return False
            
            # Start background tasks
            self.logcat_task = asyncio.create_task(self._logcat_monitor())
            self.health_check_task = asyncio.create_task(self._health_check_loop())
            self.maintenance_task = asyncio.create_task(self._maintenance_loop())
            
            self.stats.current_status = "monitoring"
            self.logger.info("Monitor started successfully")
            
            # Wait for tasks
            await asyncio.gather(
                self.logcat_task,
                self.health_check_task,
                self.maintenance_task,
                return_exceptions=True
            )
            
        except Exception as e:
            self.logger.error(f"Monitor error: {e}")
        finally:
            await self._cleanup()
    
    async def stop(self):
        """Stop the monitoring daemon"""
        self.logger.info("Stopping monitor...")
        self.is_running = False
        self.stats.current_status = "stopping"
        
        # Cancel tasks
        if self.logcat_task:
            self.logcat_task.cancel()
        if self.health_check_task:
            self.health_check_task.cancel()
        if self.maintenance_task:
            self.maintenance_task.cancel()
    
    def _signal_handler(self, signum, frame):
        """Handle shutdown signals"""
        self.logger.info(f"Received signal {signum}, shutting down...")
        asyncio.create_task(self.stop())
    
    async def _initialize_connection(self) -> bool:
        """Initialize ADB connection to Fire TV"""
        loop = asyncio.get_event_loop()
        
        try:
            # Connect to device
            connected = await loop.run_in_executor(self.executor, self.adb.connect)
            if not connected:
                return False
            
            # Get device info
            device_info = await loop.run_in_executor(self.executor, self.adb.get_device_info)
            if device_info:
                self.logger.info(f"Connected to {device_info.model} (Android {device_info.android_version})")
            
            # Check app installation
            app_installed = await loop.run_in_executor(self.executor, self.adb.is_app_installed)
            if not app_installed:
                self.logger.warning("Fire TV app not installed")
                return False
            
            return True
            
        except Exception as e:
            self.logger.error(f"Connection initialization error: {e}")
            return False
    
    async def _logcat_monitor(self):
        """Monitor logcat output for errors"""
        self.logger.info("Starting logcat monitoring")
        
        try:
            process = await asyncio.create_subprocess_exec(
                "adb", "-s", self.adb.device_id, "logcat",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            
            while self.is_running:
                try:
                    line_bytes = await asyncio.wait_for(
                        process.stdout.readline(), 
                        timeout=1.0
                    )
                    
                    if not line_bytes:
                        break
                    
                    line = line_bytes.decode('utf-8', errors='ignore').strip()
                    if line:
                        await self._process_logcat_line(line)
                        
                except asyncio.TimeoutError:
                    continue
                except Exception as e:
                    self.logger.error(f"Logcat processing error: {e}")
                    await asyncio.sleep(1)
            
            process.terminate()
            await process.wait()
            
        except Exception as e:
            self.logger.error(f"Logcat monitor error: {e}")
    
    async def _process_logcat_line(self, line: str):
        """Process a single logcat line"""
        # Add to buffer
        self.logcat_buffer.append({
            "timestamp": time.time(),
            "line": line
        })
        
        # Maintain buffer size
        if len(self.logcat_buffer) > self.max_buffer_size:
            self.logcat_buffer = self.logcat_buffer[-self.max_buffer_size:]
        
        # Detect errors
        try:
            errors = self.error_detector.analyze_logcat_line(line)
            for error in errors:
                await self._handle_detected_error(error)
        except Exception as e:
            self.logger.error(f"Error analysis failed: {e}")
    
    async def _handle_detected_error(self, error: DetectedError):
        """Handle a detected error"""
        self.stats.total_errors_detected += 1
        self.stats.last_error_time = error.timestamp
        
        # Add to detector history
        self.error_detector.add_detected_error(error)
        
        self.logger.warning(f"Detected {error.severity.value} error: {error.error_type}")
        
        # Decide if auto-fix should be triggered
        if (self.config["enable_auto_fix"] and 
            self.error_detector.should_trigger_autofix(error)):
            await self._attempt_auto_fix(error)
    
    async def _attempt_auto_fix(self, error: DetectedError):
        """Attempt to auto-fix an error"""
        # Check rate limiting
        if not self._can_attempt_fix():
            self.logger.info("Fix rate limit reached, skipping auto-fix")
            return
        
        self.stats.total_fixes_attempted += 1
        self.stats.last_fix_time = time.time()
        
        try:
            loop = asyncio.get_event_loop()
            fix_action = await loop.run_in_executor(
                self.executor, 
                self.auto_fixer.apply_fix, 
                error
            )
            
            if fix_action:
                if fix_action.result == FixResult.SUCCESS:
                    self.stats.successful_fixes += 1
                    self.logger.info(f"Successfully fixed {error.error_type}")
                else:
                    self.stats.failed_fixes += 1
                    self.logger.warning(f"Fix failed for {error.error_type}: {fix_action.message}")
            
        except Exception as e:
            self.stats.failed_fixes += 1
            self.logger.error(f"Auto-fix exception: {e}")
    
    def _can_attempt_fix(self) -> bool:
        """Check if we can attempt another fix (rate limiting)"""
        max_fixes = self.config["max_fix_attempts_per_hour"]
        current_time = time.time()
        one_hour_ago = current_time - 3600
        
        # Count recent fixes
        recent_fixes = [
            action for action in self.auto_fixer.fix_history
            if action.timestamp >= one_hour_ago
        ]
        
        return len(recent_fixes) < max_fixes
    
    async def _health_check_loop(self):
        """Periodic health checks"""
        self.logger.info("Starting health check loop")
        
        while self.is_running:
            try:
                await self._perform_health_check()
                await asyncio.sleep(self.config["health_check_interval"])
            except Exception as e:
                self.logger.error(f"Health check error: {e}")
                await asyncio.sleep(5)
    
    async def _perform_health_check(self):
        """Perform comprehensive health check"""
        loop = asyncio.get_event_loop()
        
        try:
            # Check ADB connection
            if not await loop.run_in_executor(self.executor, self.adb.is_connected):
                self.logger.warning("ADB connection lost, attempting reconnection")
                if not await loop.run_in_executor(self.executor, self.adb.connect):
                    self.logger.error("Failed to reconnect ADB")
                    return
            
            # Check app state
            app_running = await loop.run_in_executor(self.executor, self.adb.is_app_running)
            current_activity = await loop.run_in_executor(self.executor, self.adb.get_current_activity)
            
            # Detect app state issues
            app_errors = self.error_detector.analyze_app_state(app_running, current_activity)
            for error in app_errors:
                await self._handle_detected_error(error)
            
            # Check memory usage
            memory_info = await loop.run_in_executor(self.executor, self.adb.get_app_memory_usage)
            if memory_info:
                memory_errors = self.error_detector.analyze_memory_usage(memory_info)
                for error in memory_errors:
                    await self._handle_detected_error(error)
            
            # Check UI state
            ui_dump = await loop.run_in_executor(self.executor, self.adb.get_screen_dump)
            if ui_dump:
                ui_errors = self.error_detector.analyze_ui_dump(ui_dump)
                for error in ui_errors:
                    await self._handle_detected_error(error)
            
        except Exception as e:
            self.logger.error(f"Health check failed: {e}")
    
    async def _maintenance_loop(self):
        """Periodic maintenance tasks"""
        self.logger.info("Starting maintenance loop")
        
        while self.is_running:
            try:
                await asyncio.sleep(self.config["maintenance_interval"])
                if self.is_running:
                    await self._perform_maintenance()
            except Exception as e:
                self.logger.error(f"Maintenance error: {e}")
    
    async def _perform_maintenance(self):
        """Perform routine maintenance"""
        self.logger.info("Performing routine maintenance")
        
        try:
            loop = asyncio.get_event_loop()
            
            # Run auto-fixer maintenance
            await loop.run_in_executor(
                self.executor, 
                self.auto_fixer.schedule_maintenance
            )
            
            # Generate status report
            await self._generate_report()
            
            # Clean up old log entries
            await self._cleanup_logs()
            
        except Exception as e:
            self.logger.error(f"Maintenance failed: {e}")
    
    async def _generate_report(self):
        """Generate monitoring report"""
        try:
            report = {
                "timestamp": datetime.now().isoformat(),
                "uptime_seconds": time.time() - self.stats.start_time,
                "statistics": asdict(self.stats),
                "error_summary": self.error_detector.get_error_summary(),
                "fix_statistics": self.auto_fixer.get_fix_statistics(),
                "device_info": await asyncio.get_event_loop().run_in_executor(
                    self.executor, self.adb.get_device_info
                )
            }
            
            # Convert device_info to dict if it exists
            if report["device_info"]:
                report["device_info"] = asdict(report["device_info"])
            
            # Save report
            with open(self.config["report_file"], 'w') as f:
                json.dump(report, f, indent=2)
            
            self.logger.info(f"Generated monitoring report: {self.config['report_file']}")
            
        except Exception as e:
            self.logger.error(f"Report generation failed: {e}")
    
    async def _cleanup_logs(self):
        """Clean up old log entries"""
        try:
            # Clean error detector history (keep last 500)
            if len(self.error_detector.error_history) > 500:
                self.error_detector.error_history = self.error_detector.error_history[-500:]
            
            # Clean auto-fixer history (keep last 200)
            if len(self.auto_fixer.fix_history) > 200:
                self.auto_fixer.fix_history = self.auto_fixer.fix_history[-200:]
            
            # Clean logcat buffer
            if len(self.logcat_buffer) > self.max_buffer_size:
                self.logcat_buffer = self.logcat_buffer[-self.max_buffer_size:]
            
        except Exception as e:
            self.logger.error(f"Log cleanup failed: {e}")
    
    async def _cleanup(self):
        """Cleanup resources"""
        self.logger.info("Cleaning up monitor resources")
        
        try:
            # Close executor
            self.executor.shutdown(wait=True)
            
            # Generate final report
            await self._generate_report()
            
            # Disconnect ADB
            loop = asyncio.get_event_loop()
            await loop.run_in_executor(self.executor, self.adb.disconnect)
            
            self.stats.current_status = "stopped"
            self.logger.info("Monitor cleanup complete")
            
        except Exception as e:
            self.logger.error(f"Cleanup error: {e}")
    
    def get_status(self) -> Dict[str, Any]:
        """Get current monitoring status"""
        return {
            "is_running": self.is_running,
            "stats": asdict(self.stats),
            "uptime": time.time() - self.stats.start_time,
            "recent_errors": len(self.error_detector.get_recent_errors(5)),
            "config": self.config
        }

async def main():
    """Main entry point"""
    monitor = AppMonitor()
    
    try:
        await monitor.start()
    except KeyboardInterrupt:
        print("Received interrupt, shutting down...")
    finally:
        await monitor.stop()

if __name__ == "__main__":
    asyncio.run(main())