package com.webviewer.firetv

import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var ipEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var saveButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var preferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_simple)
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        initializeViews()
        loadCurrentSettings()
        setupFocusFlow()
    }
    
    private fun initializeViews() {
        ipEditText = findViewById(R.id.ipEditText)
        portEditText = findViewById(R.id.portEditText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        
        setupFields()
        
        saveButton.setOnClickListener {
            saveSettings()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupFields() {
        // Setup IP field with Fire TV navigation
        ipEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        portEditText.requestFocus()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        
        // Setup port field with Fire TV navigation
        portEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        saveButton.requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        ipEditText.requestFocus()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }
    
    private fun loadCurrentSettings() {
        val currentIp = preferences.getString("server_ip", "")
        val currentPort = preferences.getString("server_port", "3000")
        
        if (!currentIp.isNullOrEmpty()) {
            ipEditText.setText(currentIp)
        } else {
            // Set default 192.168.4.xxx pattern
            ipEditText.setText("192.168.4.")
            ipEditText.setSelection(ipEditText.text.length) // Cursor at end
        }
        
        portEditText.setText(currentPort)
        
        // Focus on IP field if empty
        if (currentIp.isNullOrEmpty()) {
            ipEditText.requestFocus()
        }
    }
    
    private fun setupFocusFlow() {
        ipEditText.nextFocusDownId = R.id.portEditText
        portEditText.nextFocusUpId = R.id.ipEditText
        portEditText.nextFocusDownId = R.id.saveButton
        saveButton.nextFocusUpId = R.id.portEditText
        saveButton.nextFocusRightId = R.id.cancelButton
        cancelButton.nextFocusLeftId = R.id.saveButton
        cancelButton.nextFocusUpId = R.id.portEditText
    }
    
    private fun saveSettings() {
        val ip = ipEditText.text.toString().trim()
        val port = portEditText.text.toString().trim()
        
        if (!isValidIpAddress(ip)) {
            showError("Invalid IP address. Please check the format.")
            ipEditText.requestFocus()
            return
        }
        
        if (!isValidPort(port)) {
            showError("Invalid port number. Please enter a port between 1-65535.")
            portEditText.requestFocus()
            return
        }
        
        preferences.edit()
            .putString("server_ip", ip)
            .putString("server_port", port)
            .apply()
        
        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun isValidIpAddress(ip: String): Boolean {
        if (ip.isEmpty()) return false
        
        // Split IP into parts
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        // Check each part is valid (0-255)
        for (part in parts) {
            try {
                val num = part.toInt()
                if (num < 0 || num > 255) return false
            } catch (e: NumberFormatException) {
                return false
            }
        }
        
        return true
    }
    
    private fun isValidPort(port: String): Boolean {
        if (port.isEmpty()) return false
        
        return try {
            val portNumber = port.toInt()
            portNumber in 1..65535
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}