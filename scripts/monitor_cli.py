#!/usr/bin/env python3
"""
Command Line Interface for Fire TV Monitoring System
Provides easy control and status reporting
"""

import asyncio
import argparse
import json
import sys
import os
from typing import Dict, Any

# Add current directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from monitor import AppMonitor
from adb_manager import ADBManager

class MonitorCLI:
    """Command line interface for monitoring system"""
    
    def __init__(self):
        self.config_path = "monitor_config.json"
    
    async def run_monitor(self, args):
        """Run the monitoring daemon"""
        print("Starting Fire TV App Monitor...")
        print("Press Ctrl+C to stop")
        
        monitor = AppMonitor(self.config_path)
        await monitor.start()
    
    async def check_status(self, args):
        """Check current status of app and device"""
        try:
            # Load config
            with open(self.config_path, 'r') as f:
                config = json.load(f)
            
            adb = ADBManager(config["device_ip"], config["device_port"])
            
            print("🔍 Fire TV App Status Check")
            print("=" * 40)
            
            # Check ADB connection
            print(f"📱 Connecting to {config['device_ip']}:{config['device_port']}...")
            if adb.connect():
                print("✅ ADB connection successful")
            else:
                print("❌ ADB connection failed")
                return
            
            # Get device info
            device_info = adb.get_device_info()
            if device_info:
                print(f"📋 Device: {device_info.model}")
                print(f"🤖 Android: {device_info.android_version} (API {device_info.api_level})")
            
            # Check app status
            app_installed = adb.is_app_installed()
            app_running = adb.is_app_running()
            current_activity = adb.get_current_activity()
            
            print(f"📦 App installed: {'✅ Yes' if app_installed else '❌ No'}")
            print(f"🏃 App running: {'✅ Yes' if app_running else '❌ No'}")
            
            if current_activity:
                print(f"🎯 Current activity: {current_activity}")
            
            # Memory usage
            memory_info = adb.get_app_memory_usage()
            if memory_info and app_running:
                total_mb = memory_info.get("total_pss_kb", 0) / 1024
                print(f"🧠 Memory usage: {total_mb:.1f} MB")
            
            # Check if monitoring is active
            if os.path.exists("monitor.log"):
                print("📊 Monitor log found")
            
            if os.path.exists("monitor_report.json"):
                with open("monitor_report.json", 'r') as f:
                    report = json.load(f)
                    print(f"📈 Last report: {report.get('timestamp', 'Unknown')}")
            
        except Exception as e:
            print(f"❌ Status check failed: {e}")
    
    async def test_connection(self, args):
        """Test ADB connection to device"""
        try:
            # Load config
            with open(self.config_path, 'r') as f:
                config = json.load(f)
            
            adb = ADBManager(config["device_ip"], config["device_port"])
            
            print("🔌 Testing ADB Connection")
            print("=" * 30)
            
            # Test basic connection
            print(f"Connecting to {config['device_ip']}:{config['device_port']}...")
            if adb.connect():
                print("✅ Connection successful")
            else:
                print("❌ Connection failed")
                return
            
            # Test device responsiveness
            print("Testing device responsiveness...")
            devices = adb.get_connected_devices()
            device_found = False
            for device in devices:
                if device.device_id == adb.device_id:
                    print(f"✅ Device found: {device.state}")
                    device_found = True
                    break
            
            if not device_found:
                print("❌ Device not found in device list")
                return
            
            # Test command execution
            print("Testing command execution...")
            result = adb.execute_command("echo 'test'")
            if result and "test" in result:
                print("✅ Command execution successful")
            else:
                print("❌ Command execution failed")
            
            print("\n🎉 Connection test completed successfully!")
            
        except Exception as e:
            print(f"❌ Connection test failed: {e}")
    
    async def show_config(self, args):
        """Show current configuration"""
        try:
            with open(self.config_path, 'r') as f:
                config = json.load(f)
            
            print("⚙️  Monitor Configuration")
            print("=" * 30)
            print(json.dumps(config, indent=2))
            
        except Exception as e:
            print(f"❌ Could not load config: {e}")
    
    async def show_logs(self, args):
        """Show recent log entries"""
        try:
            if not os.path.exists("monitor.log"):
                print("❌ Monitor log file not found")
                return
            
            lines = args.lines if hasattr(args, 'lines') else 20
            
            print(f"📝 Last {lines} log entries")
            print("=" * 40)
            
            with open("monitor.log", 'r') as f:
                log_lines = f.readlines()
                for line in log_lines[-lines:]:
                    print(line.rstrip())
            
        except Exception as e:
            print(f"❌ Could not read logs: {e}")
    
    async def show_report(self, args):
        """Show monitoring report"""
        try:
            if not os.path.exists("monitor_report.json"):
                print("❌ Monitor report not found")
                print("💡 Run the monitor first to generate a report")
                return
            
            with open("monitor_report.json", 'r') as f:
                report = json.load(f)
            
            print("📊 Monitoring Report")
            print("=" * 25)
            
            # Basic stats
            stats = report.get("statistics", {})
            print(f"⏱️  Uptime: {report.get('uptime_seconds', 0):.0f} seconds")
            print(f"🚨 Total errors: {stats.get('total_errors_detected', 0)}")
            print(f"🔧 Fix attempts: {stats.get('total_fixes_attempted', 0)}")
            print(f"✅ Successful fixes: {stats.get('successful_fixes', 0)}")
            print(f"❌ Failed fixes: {stats.get('failed_fixes', 0)}")
            
            # Success rate
            total_fixes = stats.get('total_fixes_attempted', 0)
            if total_fixes > 0:
                success_rate = (stats.get('successful_fixes', 0) / total_fixes) * 100
                print(f"📈 Fix success rate: {success_rate:.1f}%")
            
            # Error summary
            error_summary = report.get("error_summary", {})
            if error_summary.get("recent_errors", 0) > 0:
                print(f"\n🔍 Recent Error Types:")
                error_types = error_summary.get("error_types", {})
                for error_type, count in error_types.items():
                    print(f"  • {error_type}: {count}")
            
            print(f"\n📅 Report generated: {report.get('timestamp', 'Unknown')}")
            
        except Exception as e:
            print(f"❌ Could not read report: {e}")
    
    def main(self):
        """Main CLI entry point"""
        parser = argparse.ArgumentParser(
            description="Fire TV App Monitoring System",
            formatter_class=argparse.RawDescriptionHelpFormatter,
            epilog="""
Examples:
  %(prog)s start                    # Start monitoring daemon
  %(prog)s status                   # Check app and device status
  %(prog)s test                     # Test ADB connection
  %(prog)s config                   # Show configuration
  %(prog)s logs --lines 50          # Show last 50 log entries
  %(prog)s report                   # Show monitoring report
            """
        )
        
        subparsers = parser.add_subparsers(dest="command", help="Available commands")
        
        # Start command
        start_parser = subparsers.add_parser("start", help="Start monitoring daemon")
        start_parser.set_defaults(func=self.run_monitor)
        
        # Status command
        status_parser = subparsers.add_parser("status", help="Check status")
        status_parser.set_defaults(func=self.check_status)
        
        # Test command
        test_parser = subparsers.add_parser("test", help="Test ADB connection")
        test_parser.set_defaults(func=self.test_connection)
        
        # Config command
        config_parser = subparsers.add_parser("config", help="Show configuration")
        config_parser.set_defaults(func=self.show_config)
        
        # Logs command
        logs_parser = subparsers.add_parser("logs", help="Show log entries")
        logs_parser.add_argument("--lines", type=int, default=20, 
                                help="Number of lines to show (default: 20)")
        logs_parser.set_defaults(func=self.show_logs)
        
        # Report command
        report_parser = subparsers.add_parser("report", help="Show monitoring report")
        report_parser.set_defaults(func=self.show_report)
        
        args = parser.parse_args()
        
        if not args.command:
            parser.print_help()
            return
        
        # Run the selected command
        try:
            asyncio.run(args.func(args))
        except KeyboardInterrupt:
            print("\n👋 Interrupted by user")
        except Exception as e:
            print(f"❌ Command failed: {e}")
            sys.exit(1)

if __name__ == "__main__":
    cli = MonitorCLI()
    cli.main()