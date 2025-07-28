#!/usr/bin/env python3
"""
Auto-Fix Engine for Fire TV App Monitoring
Implements automated fixes for detected errors
"""

import logging
import time
import json
import subprocess
from typing import Dict, List, Optional, Any, Callable
from dataclasses import dataclass
from enum import Enum
from adb_manager import ADBManager
from error_detector import DetectedError, ErrorSeverity

class FixResult(Enum):
    SUCCESS = "success"
    FAILED = "failed"
    PARTIAL = "partial"
    SKIPPED = "skipped"

@dataclass
class FixAction:
    """Represents a fix action result"""
    action_name: str
    result: FixResult
    message: str
    details: Dict[str, Any]
    timestamp: float
    duration: float

class AutoFixer:
    """Automated error fixing engine"""
    
    def __init__(self, adb_manager: ADBManager):
        self.adb = adb_manager
        self.logger = logging.getLogger(__name__)
        self.fix_strategies = self._initialize_fix_strategies()
        self.fix_history: List[FixAction] = []
        self.max_fix_attempts = 3
        self.fix_cooldown = 60  # seconds between fix attempts for same error type
        self.last_fix_times: Dict[str, float] = {}
        
    def _initialize_fix_strategies(self) -> Dict[str, Callable]:
        """Initialize fix strategies for different error types"""
        return {
            "app_crash": self._fix_app_crash,
            "anr": self._fix_anr,
            "out_of_memory": self._fix_memory_issue,
            "network_error": self._fix_network_error,
            "webview_error": self._fix_webview_error,
            "profile_error": self._fix_profile_error,
            "preferences_error": self._fix_preferences_error,
            "focus_error": self._fix_focus_error,
            "lifecycle_error": self._fix_lifecycle_error,
            "app_not_running": self._fix_app_not_running,
            "high_memory_usage": self._fix_high_memory_usage,
            "potential_memory_leak": self._fix_memory_leak,
            "missing_ui_element": self._fix_missing_ui_element,
            "no_focused_element": self._fix_no_focus,
            "unexpected_activity": self._fix_unexpected_activity
        }
    
    def can_attempt_fix(self, error_type: str) -> bool:
        """Check if we can attempt a fix (not in cooldown)"""
        last_fix_time = self.last_fix_times.get(error_type, 0)
        return time.time() - last_fix_time >= self.fix_cooldown
    
    def apply_fix(self, error: DetectedError) -> Optional[FixAction]:
        """Apply appropriate fix for the detected error"""
        if not self.can_attempt_fix(error.error_type):
            self.logger.info(f"Fix for {error.error_type} is in cooldown")
            return None
        
        fix_strategy = self.fix_strategies.get(error.error_type)
        if not fix_strategy:
            self.logger.warning(f"No fix strategy for error type: {error.error_type}")
            return None
        
        self.logger.info(f"Attempting to fix {error.error_type}")
        start_time = time.time()
        
        try:
            result = fix_strategy(error)
            duration = time.time() - start_time
            
            fix_action = FixAction(
                action_name=f"fix_{error.error_type}",
                result=result,
                message=f"Fix attempt for {error.error_type}",
                details={"original_error": error.to_dict()},
                timestamp=start_time,
                duration=duration
            )
            
            self.fix_history.append(fix_action)
            self.last_fix_times[error.error_type] = start_time
            
            self.logger.info(f"Fix result for {error.error_type}: {result.value}")
            return fix_action
            
        except Exception as e:
            duration = time.time() - start_time
            self.logger.error(f"Fix failed for {error.error_type}: {e}")
            
            fix_action = FixAction(
                action_name=f"fix_{error.error_type}",
                result=FixResult.FAILED,
                message=f"Fix failed: {str(e)}",
                details={"error": str(e), "original_error": error.to_dict()},
                timestamp=start_time,
                duration=duration
            )
            
            self.fix_history.append(fix_action)
            return fix_action
    
    def _fix_app_crash(self, error: DetectedError) -> FixResult:
        """Fix app crash by restarting the app"""
        self.logger.info("Fixing app crash by restarting app")
        
        # Force stop the app
        if not self.adb.force_stop_app():
            return FixResult.FAILED
        
        # Wait a moment
        time.sleep(2)
        
        # Start the app again
        if self.adb.start_app():
            time.sleep(5)  # Wait for app to start
            if self.adb.is_app_running():
                return FixResult.SUCCESS
        
        return FixResult.FAILED
    
    def _fix_anr(self, error: DetectedError) -> FixResult:
        """Fix ANR by force stopping and restarting"""
        self.logger.info("Fixing ANR by force restarting app")
        
        # Send back key to try to dismiss ANR dialog
        self.adb.send_key_event(4)  # KEYCODE_BACK
        time.sleep(1)
        
        # Force stop the app
        if not self.adb.force_stop_app():
            return FixResult.FAILED
        
        time.sleep(3)
        
        # Restart app
        if self.adb.start_app():
            time.sleep(5)
            if self.adb.is_app_running():
                return FixResult.SUCCESS
        
        return FixResult.FAILED
    
    def _fix_memory_issue(self, error: DetectedError) -> FixResult:
        """Fix memory issues by clearing cache and restarting"""
        self.logger.info("Fixing memory issue by clearing cache")
        
        # Force stop app
        self.adb.force_stop_app()
        time.sleep(2)
        
        # Clear app cache (not data to preserve profiles)
        try:
            # Clear cache only
            result = self.adb.execute_command(f"pm trim-caches 1000M")
            time.sleep(2)
            
            # Restart app
            if self.adb.start_app():
                time.sleep(5)
                return FixResult.SUCCESS
        except Exception as e:
            self.logger.error(f"Failed to clear cache: {e}")
        
        return FixResult.FAILED
    
    def _fix_network_error(self, error: DetectedError) -> FixResult:
        """Fix network errors by refreshing connection"""
        self.logger.info("Fixing network error by refreshing WebView")
        
        # Send refresh key combination (if app is running)
        if self.adb.is_app_running():
            # Navigate to refresh button and press it
            # Up arrow to toolbar, then right arrows to refresh button
            self.adb.send_key_event(19)  # KEYCODE_DPAD_UP (to toolbar)
            time.sleep(0.5)
            self.adb.send_key_event(22)  # KEYCODE_DPAD_RIGHT (to forward)
            time.sleep(0.5)
            self.adb.send_key_event(22)  # KEYCODE_DPAD_RIGHT (to refresh)
            time.sleep(0.5)
            self.adb.send_key_event(23)  # KEYCODE_DPAD_CENTER (press refresh)
            
            return FixResult.SUCCESS
        
        return FixResult.FAILED
    
    def _fix_webview_error(self, error: DetectedError) -> FixResult:
        """Fix WebView errors by refreshing or restarting"""
        self.logger.info("Fixing WebView error")
        
        # Try refresh first
        refresh_result = self._fix_network_error(error)
        if refresh_result == FixResult.SUCCESS:
            return FixResult.SUCCESS
        
        # If refresh doesn't work, restart app
        return self._fix_app_crash(error)
    
    def _fix_profile_error(self, error: DetectedError) -> FixResult:
        """Fix profile-related errors"""
        self.logger.info("Fixing profile error by navigating to profiles")
        
        if self.adb.is_app_running():
            # Navigate to profiles button and open profiles
            self.adb.send_key_event(19)  # KEYCODE_DPAD_UP (to toolbar)
            time.sleep(0.5)
            # Navigate to profiles button (last button)
            for _ in range(5):  # Navigate to end
                self.adb.send_key_event(22)  # KEYCODE_DPAD_RIGHT
                time.sleep(0.3)
            self.adb.send_key_event(23)  # KEYCODE_DPAD_CENTER (open profiles)
            
            return FixResult.PARTIAL
        
        return FixResult.FAILED
    
    def _fix_preferences_error(self, error: DetectedError) -> FixResult:
        """Fix SharedPreferences errors by clearing app data"""
        self.logger.info("Fixing preferences error by clearing app data")
        
        # This will clear all data including profiles - use carefully
        self.adb.force_stop_app()
        time.sleep(2)
        
        if self.adb.clear_app_data():
            time.sleep(3)
            if self.adb.start_app():
                time.sleep(5)
                return FixResult.SUCCESS
        
        return FixResult.FAILED
    
    def _fix_focus_error(self, error: DetectedError) -> FixResult:
        """Fix focus issues by sending navigation keys"""
        self.logger.info("Fixing focus error by resetting navigation")
        
        if self.adb.is_app_running():
            # Send back key to reset focus
            self.adb.send_key_event(4)  # KEYCODE_BACK
            time.sleep(1)
            # Send center key to establish focus
            self.adb.send_key_event(23)  # KEYCODE_DPAD_CENTER
            
            return FixResult.PARTIAL
        
        return FixResult.FAILED
    
    def _fix_lifecycle_error(self, error: DetectedError) -> FixResult:
        """Fix activity lifecycle errors by restarting app"""
        self.logger.info("Fixing lifecycle error by restarting app")
        return self._fix_app_crash(error)
    
    def _fix_app_not_running(self, error: DetectedError) -> FixResult:
        """Fix app not running by starting it"""
        self.logger.info("Fixing app not running by starting app")
        
        if self.adb.start_app():
            time.sleep(5)
            if self.adb.is_app_running():
                return FixResult.SUCCESS
        
        return FixResult.FAILED
    
    def _fix_high_memory_usage(self, error: DetectedError) -> FixResult:
        """Fix high memory usage by garbage collection and optimization"""
        self.logger.info("Fixing high memory usage")
        
        # Force garbage collection
        if self.adb.is_app_running():
            try:
                # Send system-level GC trigger
                self.adb.execute_command("am broadcast -a android.intent.action.TRIM_MEMORY")
                time.sleep(2)
                return FixResult.PARTIAL
            except Exception:
                pass
        
        # If still high, restart app
        return self._fix_memory_issue(error)
    
    def _fix_memory_leak(self, error: DetectedError) -> FixResult:
        """Fix potential memory leak by restarting app"""
        self.logger.info("Fixing potential memory leak by restarting app")
        return self._fix_memory_issue(error)
    
    def _fix_missing_ui_element(self, error: DetectedError) -> FixResult:
        """Fix missing UI elements with state-aware recovery"""
        missing_element = error.details.get("missing_element", "unknown")
        app_state = error.details.get("app_state", "unknown")
        
        self.logger.info(f"Fixing missing UI element: {missing_element} in state: {app_state}")
        
        if not self.adb.is_app_running():
            self.logger.warning("App not running, cannot fix missing UI element")
            return FixResult.FAILED
        
        # State-aware recovery strategies
        if app_state == "settings" and missing_element in ["profilesButton", "webView", "browserToolbar"]:
            # We're in settings - these elements should be missing, this is normal
            self.logger.info("Missing browser elements in settings state is expected")
            return FixResult.SUCCESS
        
        elif app_state in ["loading", "error_welcome"] and missing_element in ["profilesButton", "webView", "browserToolbar"]:
            # Elements are hidden during loading/error - this is normal
            self.logger.info(f"Missing browser elements in {app_state} state is expected")
            return FixResult.SUCCESS
        
        elif app_state == "browsing" and missing_element in ["profilesButton", "webView", "browserToolbar"]:
            # Elements should be visible in browsing state - try to recover
            self.logger.warning(f"Browser elements missing in browsing state - attempting recovery")
            return self._recover_to_main_activity()
        
        elif app_state == "unknown":
            # Unknown state - try gentle recovery
            self.logger.info("Unknown app state - attempting gentle recovery")
            return self._gentle_recovery()
        
        else:
            # Default fallback
            self.logger.info("Using default recovery strategy")
            return self._gentle_recovery()
    
    def _recover_to_main_activity(self) -> FixResult:
        """Navigate back to main activity when UI elements are genuinely missing"""
        try:
            # Press back button to potentially exit menus/dialogs
            self.adb.send_key_event(4)  # KEYCODE_BACK
            time.sleep(0.5)
            
            # Press home button to go to main activity
            self.adb.send_key_event(3)  # KEYCODE_HOME (app home, not system home)
            time.sleep(1)
            
            # Try to focus on main content
            self.adb.send_key_event(23)  # KEYCODE_DPAD_CENTER
            time.sleep(0.5)
            
            return FixResult.PARTIAL
            
        except Exception as e:
            self.logger.error(f"Failed to recover to main activity: {e}")
            return FixResult.FAILED
    
    def _gentle_recovery(self) -> FixResult:
        """Gentle recovery method for ambiguous situations"""
        try:
            # Send a simple navigation to refresh state
            self.adb.send_key_event(20)  # KEYCODE_DPAD_DOWN
            time.sleep(0.3)
            self.adb.send_key_event(19)  # KEYCODE_DPAD_UP
            time.sleep(0.3)
            
            return FixResult.PARTIAL
            
        except Exception as e:
            self.logger.error(f"Gentle recovery failed: {e}")
            return FixResult.FAILED
    
    def _fix_no_focus(self, error: DetectedError) -> FixResult:
        """Fix no focused element by establishing focus"""
        self.logger.info("Fixing no focused element")
        
        if self.adb.is_app_running():
            # Send directional keys to establish focus
            self.adb.send_key_event(20)  # KEYCODE_DPAD_DOWN
            time.sleep(0.5)
            self.adb.send_key_event(19)  # KEYCODE_DPAD_UP
            time.sleep(0.5)
            self.adb.send_key_event(23)  # KEYCODE_DPAD_CENTER
            
            return FixResult.PARTIAL
        
        return FixResult.FAILED
    
    def _fix_unexpected_activity(self, error: DetectedError) -> FixResult:
        """Fix unexpected activity by navigating back to main"""
        self.logger.info("Fixing unexpected activity by navigating to main")
        
        # Send back key multiple times to get to main activity
        for _ in range(3):
            self.adb.send_key_event(4)  # KEYCODE_BACK
            time.sleep(1)
        
        return FixResult.PARTIAL
    
    def get_fix_statistics(self) -> Dict[str, Any]:
        """Get statistics about fix attempts"""
        if not self.fix_history:
            return {"total_fixes": 0}
        
        stats = {
            "total_fixes": len(self.fix_history),
            "success_rate": 0,
            "fix_types": {},
            "recent_fixes": 0
        }
        
        # Calculate success rate
        successful_fixes = [f for f in self.fix_history if f.result == FixResult.SUCCESS]
        stats["success_rate"] = len(successful_fixes) / len(self.fix_history) * 100
        
        # Count fix types
        for fix in self.fix_history:
            action_name = fix.action_name
            if action_name not in stats["fix_types"]:
                stats["fix_types"][action_name] = {"total": 0, "successful": 0}
            
            stats["fix_types"][action_name]["total"] += 1
            if fix.result == FixResult.SUCCESS:
                stats["fix_types"][action_name]["successful"] += 1
        
        # Recent fixes (last hour)
        recent_time = time.time() - 3600
        stats["recent_fixes"] = len([f for f in self.fix_history if f.timestamp >= recent_time])
        
        return stats
    
    def emergency_recovery(self) -> bool:
        """Emergency recovery procedure for severe issues"""
        self.logger.warning("Initiating emergency recovery procedure")
        
        try:
            # 1. Force stop app
            self.adb.force_stop_app()
            time.sleep(3)
            
            # 2. Clear app cache (not data)
            self.adb.execute_command("pm trim-caches 500M")
            time.sleep(2)
            
            # 3. Ensure ADB connection
            if not self.adb.ensure_connection():
                return False
            
            # 4. Restart app
            if not self.adb.start_app():
                return False
            
            # 5. Wait and verify
            time.sleep(10)
            if self.adb.is_app_running():
                self.logger.info("Emergency recovery successful")
                return True
            
            return False
            
        except Exception as e:
            self.logger.error(f"Emergency recovery failed: {e}")
            return False
    
    def schedule_maintenance(self) -> bool:
        """Perform routine maintenance tasks"""
        self.logger.info("Performing scheduled maintenance")
        
        try:
            # Clear system cache
            self.adb.execute_command("pm trim-caches 200M")
            
            # Force garbage collection
            self.adb.execute_command("am broadcast -a android.intent.action.TRIM_MEMORY")
            
            return True
            
        except Exception as e:
            self.logger.error(f"Maintenance failed: {e}")
            return False