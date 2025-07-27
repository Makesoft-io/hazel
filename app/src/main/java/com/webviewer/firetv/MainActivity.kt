package com.webviewer.firetv

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private lateinit var welcomeContainer: ConstraintLayout
    private lateinit var statusMessage: TextView
    private lateinit var retryButton: View
    private lateinit var settingsButton: View
    private lateinit var loadingOverlay: ConstraintLayout
    private lateinit var browserToolbar: BrowserToolbarView
    private lateinit var webViewCard: View
    private lateinit var preferences: SharedPreferences
    private lateinit var profileManager: ProfileManager
    
    private var serverUrl: String? = null
    private var currentProfile: ServerProfile? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        profileManager = ProfileManager(this)
        
        initializeViews()
        setupWebView()
        
        checkAndLoadServer()
    }
    
    private fun initializeViews() {
        webView = findViewById(R.id.webView)
        welcomeContainer = findViewById(R.id.welcomeContainer)
        statusMessage = findViewById(R.id.statusMessage)
        retryButton = findViewById(R.id.retryButton)
        settingsButton = findViewById(R.id.settingsButton)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        browserToolbar = findViewById(R.id.browserToolbar)
        webViewCard = findViewById(R.id.webViewCard)
        
        retryButton.setOnClickListener {
            checkAndLoadServer()
        }
        
        settingsButton.setOnClickListener {
            openSettings()
        }
        
        // Setup browser toolbar callbacks
        browserToolbar.onSettingsClicked = {
            openSettings()
        }
        
        browserToolbar.onProfilesClicked = {
            openProfiles()
        }
        
        browserToolbar.onHomeClicked = {
            checkAndLoadServer()
        }
        
        browserToolbar.onRefreshClicked = {
            if (serverUrl != null) {
                android.util.Log.d("MainActivity", "Refresh button clicked - reloading: $serverUrl")
                webView.reload()
                Toast.makeText(this, "Refreshing page...", Toast.LENGTH_SHORT).show()
            } else {
                android.util.Log.d("MainActivity", "Refresh clicked but no server URL set")
                Toast.makeText(this, "No page to refresh", Toast.LENGTH_SHORT).show()
            }
        }
        
        browserToolbar.onStopLoadingClicked = {
            android.util.Log.d("MainActivity", "Stop loading button clicked")
            webView.stopLoading()
            Toast.makeText(this, "Stopped loading", Toast.LENGTH_SHORT).show()
        }
        
        browserToolbar.onBackClicked = {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
        
        browserToolbar.onForwardClicked = {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading()
                browserToolbar.setLoadingState(true)
                url?.let { browserToolbar.updateUrl(it) }
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
                showWebView()
                browserToolbar.setLoadingState(false)
                browserToolbar.updateNavigationState(webView.canGoBack(), webView.canGoForward())
            }
            
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showError("Failed to load page: ${error?.description}")
                }
            }
            
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    showError("HTTP Error: ${errorResponse?.statusCode}")
                }
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                // Progress will be handled by the browser toolbar
                browserToolbar.updateProgress(newProgress)
            }
        }
    }
    
    private fun checkAndLoadServer() {
        currentProfile = profileManager.getActiveProfile()
        
        if (currentProfile == null) {
            // Check for legacy settings or show error
            if (!profileManager.hasProfiles()) {
                showError("No server profiles configured. Please create a profile to get started.")
            } else {
                showError("No active profile selected. Please select a profile to connect.")
            }
            return
        }
        
        serverUrl = currentProfile!!.getUrl()
        browserToolbar.updateCurrentProfile(currentProfile!!)
        loadUrl(serverUrl!!)
    }
    
    private fun loadUrl(url: String) {
        showLoading()
        webView.loadUrl(url)
    }
    
    private fun showLoading() {
        loadingOverlay.visibility = View.VISIBLE
        welcomeContainer.visibility = View.GONE
        webViewCard.visibility = View.GONE
        browserToolbar.visibility = View.GONE
    }
    
    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }
    
    private fun showWebView() {
        webViewCard.visibility = View.VISIBLE
        browserToolbar.visibility = View.VISIBLE
        welcomeContainer.visibility = View.GONE
        webView.requestFocus()
    }
    
    private fun showError(message: String) {
        hideLoading()
        welcomeContainer.visibility = View.VISIBLE
        webViewCard.visibility = View.GONE
        browserToolbar.visibility = View.GONE
        statusMessage.text = message
        retryButton.visibility = View.VISIBLE
        retryButton.requestFocus()
    }
    
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
    
    private fun openProfiles() {
        val intent = Intent(this, ProfilesActivity::class.java)
        startActivityForResult(intent, REQUEST_PROFILES)
    }
    
    private fun safeOpenSettings() {
        try {
            android.util.Log.d("MainActivity", "Opening settings activity...")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error opening settings: ${e.message}", e)
            Toast.makeText(this, "Settings unavailable", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Debug logging to see what keys are being pressed
        if (event.action == KeyEvent.ACTION_DOWN) {
            android.util.Log.d("MainActivity", "Key pressed: ${event.keyCode} (${KeyEvent.keyCodeToString(event.keyCode)})")
            
            // Handle menu button at dispatch level with Fire TV 50S450F-CA specific codes
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU,          // Standard menu
                KeyEvent.KEYCODE_BUTTON_MODE,   // Mode button  
                KeyEvent.KEYCODE_SETTINGS,      // Settings button
                82,   // Common Fire TV menu button
                229,  // Alternative Fire TV menu button
                139,  // KEYCODE_MENU_LONG (Fire TV specific)
                168,  // KEYCODE_HOME_LONG (sometimes used for menu)
                227,  // KEYCODE_NAVIGATE_NEXT (Fire TV remote)
                228,  // KEYCODE_NAVIGATE_PREVIOUS (Fire TV remote)
                174,  // KEYCODE_OPTIONS (Fire TV options button)
                1001, // Fire TV custom code 1  
                1002, // Fire TV custom code 2
                1003 -> { // Fire TV custom code 3
                    safeOpenSettings()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU, 
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_SETTINGS,
            82, 229, 139, 168, 174, 227, 228, 1001, 1002, 1003 -> {
                safeOpenSettings()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (webView.canGoBack() && webViewCard.visibility == View.VISIBLE) {
                    webView.goBack()
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (webViewCard.visibility == View.VISIBLE) {
                    simulateKeyPress("ArrowLeft")
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (webViewCard.visibility == View.VISIBLE) {
                    simulateKeyPress("ArrowRight")
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (webViewCard.visibility == View.VISIBLE) {
                    simulateKeyPress("ArrowUp")
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (webViewCard.visibility == View.VISIBLE) {
                    simulateKeyPress("ArrowDown")
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (webViewCard.visibility == View.VISIBLE) {
                    simulateKeyPress("Enter")
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun simulateKeyPress(key: String) {
        webView.evaluateJavascript("""
            var event = new KeyboardEvent('keydown', {
                key: '$key',
                bubbles: true,
                cancelable: true
            });
            document.activeElement.dispatchEvent(event);
        """.trimIndent(), null)
    }
    
    override fun onResume() {
        super.onResume()
        if (serverUrl == null) {
            checkAndLoadServer()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_PROFILES && resultCode == RESULT_OK) {
            // Profile was changed, reload the server
            checkAndLoadServer()
        }
    }
    
    companion object {
        private const val REQUEST_PROFILES = 1001
    }
}