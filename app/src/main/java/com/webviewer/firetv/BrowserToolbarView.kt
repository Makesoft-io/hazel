package com.webviewer.firetv

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class BrowserToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val backButton: ImageButton
    private val forwardButton: ImageButton
    private val refreshButton: ImageButton
    private val autoRefreshButton: ImageButton
    private val errorConsoleButton: ImageButton
    private val homeButton: ImageButton
    private val profilesButton: ImageButton
    private val urlDisplay: TextView
    private val connectionStatus: View
    private val progressBar: ProgressBar
    
    private var canGoBack = false
    private var canGoForward = false
    private var isLoading = false
    private var autoRefreshEnabled = false
    private var autoRefreshHandler: Handler? = null
    private var autoRefreshRunnable: Runnable? = null
    
    // Auto-refresh interval in milliseconds (default: 3 seconds)
    private var autoRefreshInterval = 3000L
    
    // Callback interfaces
    var onBackClicked: (() -> Unit)? = null
    var onForwardClicked: (() -> Unit)? = null
    var onRefreshClicked: (() -> Unit)? = null
    var onStopLoadingClicked: (() -> Unit)? = null
    var onAutoRefreshToggled: ((Boolean) -> Unit)? = null
    var onErrorConsoleClicked: (() -> Unit)? = null
    var onHomeClicked: (() -> Unit)? = null
    var onProfilesClicked: (() -> Unit)? = null

    init {
        try {
            // Inflate the custom layout
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.view_browser_toolbar, this, true)
            
            // Initialize views
            backButton = findViewById(R.id.backButton)
            forwardButton = findViewById(R.id.forwardButton)
            refreshButton = findViewById(R.id.refreshButton)
            autoRefreshButton = findViewById(R.id.autoRefreshButton)
            errorConsoleButton = findViewById(R.id.errorConsoleButton)
            homeButton = findViewById(R.id.homeButton)
            profilesButton = findViewById(R.id.profilesButton)
            urlDisplay = findViewById(R.id.urlDisplay)
            connectionStatus = findViewById(R.id.connectionStatus)
            progressBar = findViewById(R.id.toolbarProgressBar)
            
            setupButtons()
            setupFocusNavigation()
            
            // Set initial states
            updateButtonStates()
        } catch (e: Exception) {
            android.util.Log.e("BrowserToolbar", "Error initializing BrowserToolbarView", e)
            throw e
        }
    }
    
    private fun setupButtons() {
        backButton.setOnClickListener {
            if (canGoBack) {
                animateButtonPress(backButton)
                onBackClicked?.invoke()
            }
        }
        
        forwardButton.setOnClickListener {
            if (canGoForward) {
                animateButtonPress(forwardButton)
                onForwardClicked?.invoke()
            }
        }
        
        refreshButton.setOnClickListener {
            animateButtonPress(refreshButton)
            if (isLoading) {
                // Stop loading functionality
                onStopLoadingClicked?.invoke()
            } else {
                animateRefreshButton()
                onRefreshClicked?.invoke()
            }
        }
        
        autoRefreshButton.setOnClickListener {
            animateButtonPress(autoRefreshButton)
            toggleAutoRefresh()
        }
        
        errorConsoleButton.setOnClickListener {
            android.util.Log.d("BrowserToolbar", "Error console button clicked")
            animateButtonPress(errorConsoleButton)
            onErrorConsoleClicked?.invoke()
        }
        
        homeButton.setOnClickListener {
            animateButtonPress(homeButton)
            onHomeClicked?.invoke()
        }
        
        profilesButton.setOnClickListener {
            android.util.Log.d("BrowserToolbar", "Profiles button clicked")
            animateButtonPress(profilesButton)
            onProfilesClicked?.invoke()
        }
        
        // Add focus change listeners for smooth animations and debugging
        val buttons = listOf(backButton, forwardButton, refreshButton, autoRefreshButton, errorConsoleButton, homeButton, profilesButton)
        buttons.forEach { button ->
            button.setOnFocusChangeListener { _, hasFocus ->
                animateButtonFocus(button, hasFocus)
                
                // Debug logging to track focus changes
                if (hasFocus) {
                    val buttonName = when (button) {
                        backButton -> "back"
                        forwardButton -> "forward" 
                        refreshButton -> "refresh"
                        autoRefreshButton -> "auto-refresh"
                        errorConsoleButton -> "error-console"
                        homeButton -> "home"
                        profilesButton -> "profiles"
                        else -> "unknown"
                    }
                    android.util.Log.d("BrowserToolbar", "Focus gained by: $buttonName button")
                }
            }
        }
    }
    
    private fun setupFocusNavigation() {
        // XML attributes should handle the main navigation, this is just backup
        // and additional configuration
        
        val allButtons = listOf(backButton, forwardButton, refreshButton, autoRefreshButton, errorConsoleButton, homeButton, profilesButton)
        
        // Ensure all buttons are properly focusable
        allButtons.forEach { button ->
            button.isFocusable = true
            button.isFocusableInTouchMode = false
        }
        
        // Add key event handling as extra backup for navigation
        allButtons.forEach { button ->
            button.setOnKeyListener { view, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    android.util.Log.d("BrowserToolbar", "Key $keyCode pressed on ${getButtonName(view)}")
                    
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            val nextButton = when (view) {
                                backButton -> profilesButton
                                forwardButton -> backButton
                                refreshButton -> forwardButton
                                autoRefreshButton -> refreshButton
                                errorConsoleButton -> autoRefreshButton
                                homeButton -> errorConsoleButton
                                profilesButton -> homeButton
                                else -> null
                            }
                            nextButton?.requestFocus()
                            return@setOnKeyListener true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            val nextButton = when (view) {
                                backButton -> forwardButton
                                forwardButton -> refreshButton
                                refreshButton -> autoRefreshButton
                                autoRefreshButton -> errorConsoleButton
                                errorConsoleButton -> homeButton
                                homeButton -> profilesButton
                                profilesButton -> backButton
                                else -> null
                            }
                            nextButton?.requestFocus()
                            return@setOnKeyListener true
                        }
                    }
                }
                false
            }
        }
        
        android.util.Log.d("BrowserToolbar", "Enhanced focus navigation setup complete")
    }
    
    private fun toggleAutoRefresh() {
        autoRefreshEnabled = !autoRefreshEnabled
        autoRefreshButton.isSelected = autoRefreshEnabled
        
        if (autoRefreshEnabled) {
            startAutoRefresh()
            android.util.Log.d("BrowserToolbar", "Auto-refresh enabled with ${autoRefreshInterval}ms interval")
        } else {
            stopAutoRefresh()
            android.util.Log.d("BrowserToolbar", "Auto-refresh disabled")
        }
        
        // Notify callback
        onAutoRefreshToggled?.invoke(autoRefreshEnabled)
        
        // Animate button state change
        animateButtonPress(autoRefreshButton)
    }
    
    private fun startAutoRefresh() {
        stopAutoRefresh() // Clear any existing timer
        
        autoRefreshHandler = Handler(Looper.getMainLooper())
        autoRefreshRunnable = object : Runnable {
            override fun run() {
                if (autoRefreshEnabled && !isLoading) {
                    android.util.Log.d("BrowserToolbar", "Auto-refresh triggered")
                    onRefreshClicked?.invoke()
                }
                // Schedule next refresh
                autoRefreshHandler?.postDelayed(this, autoRefreshInterval)
            }
        }
        
        // Start the auto-refresh cycle
        autoRefreshHandler?.postDelayed(autoRefreshRunnable!!, autoRefreshInterval)
    }
    
    private fun stopAutoRefresh() {
        autoRefreshRunnable?.let { runnable ->
            autoRefreshHandler?.removeCallbacks(runnable)
        }
        autoRefreshRunnable = null
        autoRefreshHandler = null
    }
    
    fun setAutoRefreshInterval(intervalMs: Long) {
        autoRefreshInterval = intervalMs
        if (autoRefreshEnabled) {
            // Restart with new interval
            startAutoRefresh()
        }
    }
    
    fun isAutoRefreshEnabled(): Boolean = autoRefreshEnabled
    
    private fun getButtonName(view: View): String {
        return when (view) {
            backButton -> "back"
            forwardButton -> "forward"
            refreshButton -> "refresh"
            autoRefreshButton -> "auto-refresh"
            errorConsoleButton -> "error-console"
            homeButton -> "home"
            profilesButton -> "profiles"
            else -> "unknown"
        }
    }
    
    private fun animateButtonPress(button: View) {
        button.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                button.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    private fun animateButtonFocus(button: View, hasFocus: Boolean) {
        val scale = if (hasFocus) 1.1f else 1.0f
        val alpha = if (hasFocus) 1.0f else if (isButtonEnabled(button)) 0.8f else 0.4f
        
        button.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .setDuration(200)
            .start()
    }
    
    private fun animateRefreshButton() {
        refreshButton.animate()
            .rotation(refreshButton.rotation + 360f)
            .setDuration(500)
            .start()
    }
    
    private fun isButtonEnabled(button: View): Boolean {
        return when (button) {
            backButton -> canGoBack
            forwardButton -> canGoForward
            else -> true
        }
    }
    
    fun updateUrl(url: String) {
        urlDisplay.text = url
        
        // Animate URL change
        urlDisplay.animate()
            .alpha(0.7f)
            .setDuration(150)
            .withEndAction {
                urlDisplay.animate()
                    .alpha(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        this.canGoBack = canGoBack
        this.canGoForward = canGoForward
        updateButtonStates()
    }
    
    private fun updateButtonStates() {
        // Update back button
        backButton.alpha = if (canGoBack) 1.0f else 0.4f
        backButton.isClickable = canGoBack
        
        // Update forward button
        forwardButton.alpha = if (canGoForward) 1.0f else 0.4f
        forwardButton.isClickable = canGoForward
    }
    
    fun setLoadingState(loading: Boolean) {
        isLoading = loading
        
        if (loading) {
            progressBar.visibility = View.VISIBLE
            progressBar.alpha = 0f
            progressBar.animate().alpha(1f).setDuration(300).start()
            
            // Update refresh button to show stop icon (if you have one)
            // refreshButton.setImageResource(R.drawable.ic_stop)
        } else {
            progressBar.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    progressBar.visibility = View.GONE
                }
                .start()
            
            // Update refresh button back to refresh icon
            // refreshButton.setImageResource(R.drawable.ic_refresh)
        }
    }
    
    fun updateProgress(progress: Int) {
        progressBar.progress = progress
    }
    
    
    fun setConnectionStatus(connected: Boolean) {
        // Show green dot regardless of connection status (user preference)
        val colorRes = R.color.success
        val color = context.getColor(colorRes)
        
        connectionStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        
        // Animate status change
        connectionStatus.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                connectionStatus.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
    
    fun getFirstFocusableView(): View = backButton
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up auto-refresh to prevent memory leaks
        stopAutoRefresh()
    }
    
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    val currentFocus = findFocus()
                    if (currentFocus != null && isToolbarButton(currentFocus)) {
                        val nextButton = getNextButton(currentFocus, event.keyCode)
                        if (nextButton != null) {
                            android.util.Log.d("BrowserToolbar", "Forcing navigation from ${getButtonName(currentFocus)} to ${getButtonName(nextButton)}")
                            nextButton.requestFocus()
                            return true
                        }
                    }
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    // When coming from below (like WebView), focus on refresh button
                    val currentFocus = findFocus()
                    if (currentFocus == null || !isToolbarButton(currentFocus)) {
                        android.util.Log.d("BrowserToolbar", "UP pressed - focusing refresh button")
                        refreshButton.requestFocus()
                        return true
                    }
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    // When leaving toolbar, return focus to WebView or parent
                    val currentFocus = findFocus()
                    if (currentFocus != null && isToolbarButton(currentFocus)) {
                        android.util.Log.d("BrowserToolbar", "DOWN pressed - leaving toolbar")
                        // Let parent handle this to focus WebView
                        return false
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
    
    private fun isToolbarButton(view: View): Boolean {
        return view == backButton || view == forwardButton || view == refreshButton || 
               view == autoRefreshButton || view == errorConsoleButton || view == homeButton || view == profilesButton
    }
    
    private fun getNextButton(currentButton: View, keyCode: Int): View? {
        val buttons = listOf(backButton, forwardButton, refreshButton, autoRefreshButton, errorConsoleButton, homeButton, profilesButton)
        val currentIndex = buttons.indexOf(currentButton)
        
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                val prevIndex = if (currentIndex <= 0) buttons.size - 1 else currentIndex - 1
                buttons[prevIndex]
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                val nextIndex = if (currentIndex >= buttons.size - 1) 0 else currentIndex + 1
                buttons[nextIndex]
            }
            else -> null
        }
    }
    
    fun updateCurrentProfile(profile: ServerProfile) {
        // Update the URL display to show the profile name and URL
        urlDisplay.text = "${profile.name} - ${profile.getUrl()}"
    }
}