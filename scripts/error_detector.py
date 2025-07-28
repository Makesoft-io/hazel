#!/usr/bin/env python3
"""
Error Detection Engine for Fire TV App Monitoring
Detects various types of errors and issues in real-time
"""

import re
import logging
import time
from typing import Dict, List, Optional, Callable, Any
from dataclasses import dataclass
from enum import Enum
import json

class ErrorSeverity(Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"

@dataclass
class DetectedError:
    """Represents a detected error"""
    error_type: str
    severity: ErrorSeverity
    message: str
    details: Dict[str, Any]
    timestamp: float
    source: str  # logcat, memory, ui, etc.
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            "error_type": self.error_type,
            "severity": self.severity.value,
            "message": self.message,
            "details": self.details,
            "timestamp": self.timestamp,
            "source": self.source
        }

class ErrorPattern:
    """Defines a pattern for detecting specific errors"""
    def __init__(self, name: str, pattern: str, severity: ErrorSeverity, 
                 extract_func: Optional[Callable] = None):
        self.name = name
        self.pattern = re.compile(pattern, re.IGNORECASE | re.MULTILINE)
        self.severity = severity
        self.extract_func = extract_func or self._default_extract
    
    def _default_extract(self, match: re.Match) -> Dict[str, Any]:
        """Default extraction function"""
        return {"matched_text": match.group(0)}
    
    def match(self, text: str) -> Optional[Dict[str, Any]]:
        """Check if pattern matches and extract details"""
        match = self.pattern.search(text)
        if match:
            return self.extract_func(match)
        return None

class ErrorDetector:
    """Main error detection engine"""
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        self.logger = logging.getLogger(__name__)
        self.app_package = "com.webviewer.firetv"
        self.config = config or {}
        self.ui_config = self.config.get("ui_monitoring", {})
        self.error_patterns = self._initialize_patterns()
        self.error_history: List[DetectedError] = []
        self.max_history_size = 1000
        
    def _initialize_patterns(self) -> List[ErrorPattern]:
        """Initialize all error detection patterns"""
        patterns = []
        
        # Application Crashes
        patterns.append(ErrorPattern(
            name="app_crash",
            pattern=rf"FATAL EXCEPTION.*{self.app_package}",
            severity=ErrorSeverity.CRITICAL,
            extract_func=self._extract_crash_info
        ))
        
        # ANR (Application Not Responding)
        patterns.append(ErrorPattern(
            name="anr",
            pattern=rf"ANR in {self.app_package}",
            severity=ErrorSeverity.HIGH,
            extract_func=self._extract_anr_info
        ))
        
        # Out of Memory
        patterns.append(ErrorPattern(
            name="out_of_memory",
            pattern=r"OutOfMemoryError|OOM|Low memory|GC_FOR_ALLOC",
            severity=ErrorSeverity.HIGH
        ))
        
        # Network Errors
        patterns.append(ErrorPattern(
            name="network_error",
            pattern=r"NetworkOnMainThreadException|ConnectException|SocketException|UnknownHostException",
            severity=ErrorSeverity.MEDIUM,
            extract_func=self._extract_network_error
        ))
        
        # WebView Errors
        patterns.append(ErrorPattern(
            name="webview_error",
            pattern=r"WebView.*error|onReceivedError|ERR_|Failed to load|net::ERR_",
            severity=ErrorSeverity.MEDIUM,
            extract_func=self._extract_webview_error
        ))
        
        # Profile Management Errors
        patterns.append(ErrorPattern(
            name="profile_error",
            pattern=r"ProfileManager.*error|Failed to.*profile|Profile.*not found",
            severity=ErrorSeverity.MEDIUM
        ))
        
        # SharedPreferences Errors
        patterns.append(ErrorPattern(
            name="preferences_error",
            pattern=r"SharedPreferences.*error|Failed to save|Gson.*error",
            severity=ErrorSeverity.LOW
        ))
        
        # UI/Focus Errors
        patterns.append(ErrorPattern(
            name="focus_error",
            pattern=r"Focus.*error|IllegalStateException.*focus|Unable to focus",
            severity=ErrorSeverity.LOW
        ))
        
        # Activity Lifecycle Issues
        patterns.append(ErrorPattern(
            name="lifecycle_error",
            pattern=rf"{self.app_package}.*IllegalStateException|Activity.*destroyed|Fragment.*destroyed",
            severity=ErrorSeverity.MEDIUM
        ))
        
        # Permission Errors
        patterns.append(ErrorPattern(
            name="permission_error",
            pattern=r"SecurityException|Permission denied|ACCESS_DENIED",
            severity=ErrorSeverity.MEDIUM
        ))
        
        # Resource Errors
        patterns.append(ErrorPattern(
            name="resource_error",
            pattern=r"ResourceNotFoundException|Unable to find resource|Resources\$NotFoundException",
            severity=ErrorSeverity.LOW
        ))
        
        return patterns
    
    def _extract_crash_info(self, match: re.Match) -> Dict[str, Any]:
        """Extract crash information from logcat"""
        full_text = match.string
        lines = full_text.split('\n')
        crash_lines = []
        
        # Find the start of the crash
        start_idx = 0
        for i, line in enumerate(lines):
            if "FATAL EXCEPTION" in line:
                start_idx = i
                break
        
        # Collect crash lines
        for i in range(start_idx, min(start_idx + 20, len(lines))):
            if lines[i].strip():
                crash_lines.append(lines[i])
        
        return {
            "crash_trace": crash_lines,
            "exception_type": self._extract_exception_type(crash_lines)
        }
    
    def _extract_anr_info(self, match: re.Match) -> Dict[str, Any]:
        """Extract ANR information"""
        text = match.group(0)
        return {
            "anr_text": text,
            "reason": "Application not responding"
        }
    
    def _extract_network_error(self, match: re.Match) -> Dict[str, Any]:
        """Extract network error details"""
        error_text = match.group(0)
        return {
            "network_error": error_text,
            "error_type": "connectivity"
        }
    
    def _extract_webview_error(self, match: re.Match) -> Dict[str, Any]:
        """Extract WebView error details"""
        error_text = match.group(0)
        return {
            "webview_error": error_text,
            "component": "webview"
        }
    
    def _extract_exception_type(self, crash_lines: List[str]) -> Optional[str]:
        """Extract exception type from crash lines"""
        for line in crash_lines:
            if "Exception:" in line or "Error:" in line:
                parts = line.split(":")
                if len(parts) >= 2:
                    return parts[0].split()[-1]  # Get the exception class name
        return None
    
    def analyze_logcat_line(self, line: str) -> List[DetectedError]:
        """Analyze a single logcat line for errors"""
        detected_errors = []
        
        # Only analyze lines from our app or system errors that might affect us
        if self.app_package in line or any(keyword in line.lower() for keyword in 
                                          ["fatal", "error", "exception", "crash", "anr"]):
            
            for pattern in self.error_patterns:
                match_details = pattern.match(line)
                if match_details:
                    error = DetectedError(
                        error_type=pattern.name,
                        severity=pattern.severity,
                        message=f"{pattern.name.replace('_', ' ').title()} detected",
                        details=match_details,
                        timestamp=time.time(),
                        source="logcat"
                    )
                    detected_errors.append(error)
        
        return detected_errors
    
    def analyze_memory_usage(self, memory_info: Dict[str, Any]) -> List[DetectedError]:
        """Analyze memory usage for potential issues"""
        detected_errors = []
        
        if not memory_info:
            return detected_errors
        
        # Check for high memory usage
        total_pss = memory_info.get("total_pss_kb", 0)
        if total_pss > 500000:  # 500MB threshold
            error = DetectedError(
                error_type="high_memory_usage",
                severity=ErrorSeverity.MEDIUM,
                message=f"High memory usage detected: {total_pss}KB",
                details={"memory_usage_kb": total_pss, "threshold_kb": 500000},
                timestamp=time.time(),
                source="memory"
            )
            detected_errors.append(error)
        
        # Check for memory leaks (rapid increase)
        if hasattr(self, '_last_memory_usage'):
            last_usage = self._last_memory_usage
            if total_pss > last_usage * 1.5:  # 50% increase
                error = DetectedError(
                    error_type="potential_memory_leak",
                    severity=ErrorSeverity.HIGH,
                    message=f"Potential memory leak: {last_usage}KB -> {total_pss}KB",
                    details={
                        "previous_usage_kb": last_usage,
                        "current_usage_kb": total_pss,
                        "increase_percentage": ((total_pss - last_usage) / last_usage) * 100
                    },
                    timestamp=time.time(),
                    source="memory"
                )
                detected_errors.append(error)
        
        self._last_memory_usage = total_pss
        return detected_errors
    
    def analyze_ui_dump(self, ui_dump: str) -> List[DetectedError]:
        """Analyze UI dump for navigation and focus issues"""
        detected_errors = []
        
        if not ui_dump:
            return detected_errors
        
        # Detect current app state and activity
        app_state = self._detect_app_state(ui_dump)
        
        # State-aware UI element checking
        expected_elements = self._get_expected_elements_for_state(app_state, ui_dump)
        
        for element in expected_elements:
            if element not in ui_dump:
                error = DetectedError(
                    error_type="missing_ui_element",
                    severity=ErrorSeverity.LOW,
                    message=f"Missing UI element: {element} (state: {app_state})",
                    details={
                        "missing_element": element,
                        "app_state": app_state,
                        "expected_elements": expected_elements
                    },
                    timestamp=time.time(),
                    source="ui"
                )
                detected_errors.append(error)
        
        # Check for focus issues (state-aware)
        focus_config = self.ui_config.get("focus_monitoring", {})
        if focus_config.get("enabled", True):
            ignore_focus_states = focus_config.get("ignore_in_states", [])
            
            if app_state not in ignore_focus_states and "focused=\"true\"" not in ui_dump:
                error = DetectedError(
                    error_type="no_focused_element",
                    severity=ErrorSeverity.LOW,
                    message=f"No focused UI element detected (state: {app_state})",
                    details={"app_state": app_state},
                    timestamp=time.time(),
                    source="ui"
                )
                detected_errors.append(error)
        
        return detected_errors
    
    def _detect_app_state(self, ui_dump: str) -> str:
        """Detect current app state from UI dump"""
        # Check for specific activities and UI patterns
        if "SettingsActivity" in ui_dump or "ProfilesActivity" in ui_dump or "ProfileEditActivity" in ui_dump:
            return "settings"
        
        # Check if we're in loading state
        if "loading" in ui_dump.lower() or "progress" in ui_dump.lower():
            return "loading"
        
        # Check if we're showing error/welcome screen
        if "welcomeContainer" in ui_dump and "visibility=\"gone\"" not in ui_dump:
            return "error_welcome"
        
        # Check if we have webview visible (browsing state)
        if "webViewCard" in ui_dump and "visibility=\"gone\"" not in ui_dump:
            return "browsing"
        
        # Default state when unclear
        return "unknown"
    
    def _get_expected_elements_for_state(self, app_state: str, ui_dump: str) -> List[str]:
        """Get expected UI elements based on current app state"""
        # Check if state-aware checking is enabled
        if not self.ui_config.get("state_aware_checking", True):
            # Fall back to original behavior if disabled
            return ["profilesButton", "webView", "browserToolbar"]
        
        # Use configuration for expected elements
        expected_by_state = self.ui_config.get("expected_elements_by_state", {})
        expected = expected_by_state.get(app_state, [])
        
        # Check if we should ignore missing elements in this state
        ignore_states = self.ui_config.get("ignore_missing_elements_in_states", [])
        if app_state in ignore_states:
            expected = []
        
        # Use strict checking if enabled
        if self.ui_config.get("strict_element_checking", False):
            # In strict mode, be more demanding about expected elements
            if app_state == "browsing":
                expected = ["profilesButton", "webView", "browserToolbar"]
        
        return expected
    
    def analyze_app_state(self, is_running: bool, current_activity: Optional[str]) -> List[DetectedError]:
        """Analyze app state for issues"""
        detected_errors = []
        
        # App should be running
        if not is_running:
            error = DetectedError(
                error_type="app_not_running",
                severity=ErrorSeverity.HIGH,
                message="App is not running when it should be",
                details={"expected_state": "running", "actual_state": "stopped"},
                timestamp=time.time(),
                source="app_state"
            )
            detected_errors.append(error)
        
        # Check if app is in expected activity
        expected_activities = [
            "com.webviewer.firetv.MainActivity",
            "com.webviewer.firetv.SettingsActivity",
            "com.webviewer.firetv.ProfilesActivity",
            "com.webviewer.firetv.ProfileEditActivity"
        ]
        
        if current_activity and not any(activity in current_activity for activity in expected_activities):
            error = DetectedError(
                error_type="unexpected_activity",
                severity=ErrorSeverity.LOW,
                message=f"App in unexpected activity: {current_activity}",
                details={"current_activity": current_activity, "expected_activities": expected_activities},
                timestamp=time.time(),
                source="app_state"
            )
            detected_errors.append(error)
        
        return detected_errors
    
    def add_detected_error(self, error: DetectedError):
        """Add error to history"""
        self.error_history.append(error)
        
        # Maintain history size limit
        if len(self.error_history) > self.max_history_size:
            self.error_history = self.error_history[-self.max_history_size:]
        
        # Log the error
        self.logger.error(f"Detected {error.severity.value} error: {error.error_type} - {error.message}")
    
    def get_recent_errors(self, minutes: int = 5) -> List[DetectedError]:
        """Get errors from the last N minutes"""
        cutoff_time = time.time() - (minutes * 60)
        return [error for error in self.error_history if error.timestamp >= cutoff_time]
    
    def get_error_summary(self) -> Dict[str, Any]:
        """Get summary of detected errors"""
        recent_errors = self.get_recent_errors(60)  # Last hour
        
        summary = {
            "total_errors": len(self.error_history),
            "recent_errors": len(recent_errors),
            "error_types": {},
            "severity_counts": {severity.value: 0 for severity in ErrorSeverity}
        }
        
        for error in recent_errors:
            # Count by type
            if error.error_type not in summary["error_types"]:
                summary["error_types"][error.error_type] = 0
            summary["error_types"][error.error_type] += 1
            
            # Count by severity
            summary["severity_counts"][error.severity.value] += 1
        
        return summary
    
    def should_trigger_autofix(self, error: DetectedError) -> bool:
        """Determine if error should trigger auto-fix"""
        # Critical errors always trigger auto-fix
        if error.severity == ErrorSeverity.CRITICAL:
            return True
        
        # High severity errors trigger auto-fix
        if error.severity == ErrorSeverity.HIGH:
            return True
        
        # Medium severity errors trigger auto-fix if repeated
        if error.severity == ErrorSeverity.MEDIUM:
            recent_similar = [e for e in self.get_recent_errors(10) 
                             if e.error_type == error.error_type]
            return len(recent_similar) >= 3
        
        return False