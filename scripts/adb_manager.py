#!/usr/bin/env python3
"""
ADB Connection Manager for Fire TV Monitoring
Handles ADB connections, device management, and command execution
"""

import subprocess
import logging
import time
import re
from typing import Optional, List, Dict, Any
from dataclasses import dataclass

@dataclass
class DeviceInfo:
    """Information about a connected device"""
    device_id: str
    state: str
    model: Optional[str] = None
    android_version: Optional[str] = None
    api_level: Optional[int] = None

class ADBManager:
    """Manages ADB connections and commands for Fire TV monitoring"""
    
    def __init__(self, device_ip: str = "192.168.4.94", device_port: int = 5555):
        self.device_ip = device_ip
        self.device_port = device_port
        self.device_id = f"{device_ip}:{device_port}"
        self.app_package = "com.webviewer.firetv"
        self.app_activity = "com.webviewer.firetv.MainActivity"
        self.logger = logging.getLogger(__name__)
        
    def connect(self) -> bool:
        """Connect to the Fire TV device"""
        try:
            result = subprocess.run(
                ["adb", "connect", self.device_id],
                capture_output=True,
                text=True,
                timeout=10
            )
            
            if result.returncode == 0:
                if "connected" in result.stdout.lower():
                    self.logger.info(f"Successfully connected to {self.device_id}")
                    return True
                elif "already connected" in result.stdout.lower():
                    self.logger.info(f"Already connected to {self.device_id}")
                    return True
            
            self.logger.error(f"Failed to connect to {self.device_id}: {result.stderr}")
            return False
            
        except subprocess.TimeoutExpired:
            self.logger.error(f"Connection timeout to {self.device_id}")
            return False
        except Exception as e:
            self.logger.error(f"Connection error: {e}")
            return False
    
    def disconnect(self) -> bool:
        """Disconnect from the Fire TV device"""
        try:
            result = subprocess.run(
                ["adb", "disconnect", self.device_id],
                capture_output=True,
                text=True,
                timeout=5
            )
            self.logger.info(f"Disconnected from {self.device_id}")
            return result.returncode == 0
        except Exception as e:
            self.logger.error(f"Disconnect error: {e}")
            return False
    
    def is_connected(self) -> bool:
        """Check if device is connected and responsive"""
        try:
            devices = self.get_connected_devices()
            for device in devices:
                if device.device_id == self.device_id and device.state == "device":
                    return True
            return False
        except Exception:
            return False
    
    def get_connected_devices(self) -> List[DeviceInfo]:
        """Get list of connected devices"""
        try:
            result = subprocess.run(
                ["adb", "devices"],
                capture_output=True,
                text=True,
                timeout=5
            )
            
            devices = []
            lines = result.stdout.strip().split('\n')[1:]  # Skip header
            
            for line in lines:
                if line.strip():
                    parts = line.split('\t')
                    if len(parts) >= 2:
                        devices.append(DeviceInfo(
                            device_id=parts[0],
                            state=parts[1]
                        ))
            
            return devices
            
        except Exception as e:
            self.logger.error(f"Error getting devices: {e}")
            return []
    
    def execute_command(self, command: str, timeout: int = 30) -> Optional[str]:
        """Execute ADB shell command"""
        try:
            if not self.is_connected():
                if not self.connect():
                    return None
            
            cmd = ["adb", "-s", self.device_id, "shell", command]
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=timeout
            )
            
            if result.returncode == 0:
                return result.stdout
            else:
                self.logger.error(f"Command failed: {command}, Error: {result.stderr}")
                return None
                
        except subprocess.TimeoutExpired:
            self.logger.error(f"Command timeout: {command}")
            return None
        except Exception as e:
            self.logger.error(f"Command execution error: {e}")
            return None
    
    def get_device_info(self) -> Optional[DeviceInfo]:
        """Get detailed device information"""
        try:
            model = self.execute_command("getprop ro.product.model")
            android_version = self.execute_command("getprop ro.build.version.release")
            api_level = self.execute_command("getprop ro.build.version.sdk")
            
            return DeviceInfo(
                device_id=self.device_id,
                state="device",
                model=model.strip() if model else None,
                android_version=android_version.strip() if android_version else None,
                api_level=int(api_level.strip()) if api_level and api_level.strip().isdigit() else None
            )
        except Exception as e:
            self.logger.error(f"Error getting device info: {e}")
            return None
    
    def is_app_installed(self) -> bool:
        """Check if the Fire TV app is installed"""
        try:
            result = self.execute_command(f"pm list packages | grep {self.app_package}")
            return result is not None and self.app_package in result
        except Exception:
            return False
    
    def is_app_running(self) -> bool:
        """Check if the app is currently running"""
        try:
            result = self.execute_command(f"ps | grep {self.app_package}")
            return result is not None and self.app_package in result
        except Exception:
            return False
    
    def start_app(self) -> bool:
        """Start the Fire TV app"""
        try:
            cmd = f"am start -n {self.app_package}/{self.app_activity}"
            result = self.execute_command(cmd)
            return result is not None and "Starting" in result
        except Exception as e:
            self.logger.error(f"Error starting app: {e}")
            return False
    
    def force_stop_app(self) -> bool:
        """Force stop the app"""
        try:
            result = self.execute_command(f"am force-stop {self.app_package}")
            return result is not None
        except Exception as e:
            self.logger.error(f"Error force stopping app: {e}")
            return False
    
    def clear_app_data(self) -> bool:
        """Clear app data and cache"""
        try:
            result = self.execute_command(f"pm clear {self.app_package}")
            return result is not None and "Success" in result
        except Exception as e:
            self.logger.error(f"Error clearing app data: {e}")
            return False
    
    def get_app_memory_usage(self) -> Optional[Dict[str, Any]]:
        """Get app memory usage information"""
        try:
            result = self.execute_command(f"dumpsys meminfo {self.app_package}")
            if not result:
                return None
            
            memory_info = {}
            lines = result.split('\n')
            
            for line in lines:
                if "TOTAL" in line and "PSS" in line:
                    parts = line.split()
                    if len(parts) >= 2:
                        try:
                            memory_info["total_pss_kb"] = int(parts[1])
                        except ValueError:
                            pass
                elif "Native Heap" in line:
                    parts = line.split()
                    if len(parts) >= 4:
                        try:
                            memory_info["native_heap_kb"] = int(parts[3])
                        except ValueError:
                            pass
                elif "Dalvik Heap" in line:
                    parts = line.split()
                    if len(parts) >= 4:
                        try:
                            memory_info["dalvik_heap_kb"] = int(parts[3])
                        except ValueError:
                            pass
            
            return memory_info if memory_info else None
            
        except Exception as e:
            self.logger.error(f"Error getting memory usage: {e}")
            return None
    
    def send_key_event(self, key_code: int) -> bool:
        """Send key event to device"""
        try:
            result = self.execute_command(f"input keyevent {key_code}")
            return result is not None
        except Exception as e:
            self.logger.error(f"Error sending key event {key_code}: {e}")
            return False
    
    def send_tap(self, x: int, y: int) -> bool:
        """Send tap event to device"""
        try:
            result = self.execute_command(f"input tap {x} {y}")
            return result is not None
        except Exception as e:
            self.logger.error(f"Error sending tap {x},{y}: {e}")
            return False
    
    def get_screen_dump(self) -> Optional[str]:
        """Get UI hierarchy dump"""
        try:
            return self.execute_command("uiautomator dump /dev/stdout")
        except Exception as e:
            self.logger.error(f"Error getting screen dump: {e}")
            return None
    
    def get_current_activity(self) -> Optional[str]:
        """Get current activity name"""
        try:
            result = self.execute_command("dumpsys window windows | grep mCurrentFocus")
            if result:
                # Extract activity name from focus info
                match = re.search(r'mCurrentFocus=.*\{.*\s(.+)/(.+)\s', result)
                if match:
                    return f"{match.group(1)}/{match.group(2)}"
            return None
        except Exception as e:
            self.logger.error(f"Error getting current activity: {e}")
            return None
    
    def install_apk(self, apk_path: str) -> bool:
        """Install APK on device"""
        try:
            result = subprocess.run(
                ["adb", "-s", self.device_id, "install", "-r", apk_path],
                capture_output=True,
                text=True,
                timeout=60
            )
            return result.returncode == 0 and "Success" in result.stdout
        except Exception as e:
            self.logger.error(f"Error installing APK: {e}")
            return False
    
    def ensure_connection(self) -> bool:
        """Ensure device is connected, reconnect if necessary"""
        if self.is_connected():
            return True
        
        self.logger.warning("Device not connected, attempting reconnection...")
        return self.connect()