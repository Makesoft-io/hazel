package com.webviewer.firetv

import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileEditActivity : AppCompatActivity() {
    
    private lateinit var profileManager: ProfileManager
    private lateinit var nameEditText: EditText
    private lateinit var ipEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var saveButton: TextView
    private lateinit var cancelButton: TextView
    private lateinit var titleText: TextView
    
    private var existingProfile: ServerProfile? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        
        profileManager = ProfileManager(this)
        
        initializeViews()
        loadExistingProfile()
        setupFocusFlow()
    }
    
    private fun initializeViews() {
        nameEditText = findViewById(R.id.profileNameEditText)
        ipEditText = findViewById(R.id.ipEditText)
        portEditText = findViewById(R.id.portEditText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        titleText = findViewById(R.id.titleText)
        
        // Apply IP input formatting for better Fire TV experience
        IPInputFormatter.applyTo(ipEditText)
        
        saveButton.setOnClickListener {
            saveProfile()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadExistingProfile() {
        val profileId = intent.getStringExtra("profile_id")
        if (profileId != null) {
            existingProfile = profileManager.getAllProfiles().find { it.id == profileId }
            existingProfile?.let { profile ->
                titleText.text = getString(R.string.edit_profile)
                nameEditText.setText(profile.name)
                ipEditText.setText(profile.ipAddress)
                portEditText.setText(profile.port)
            }
        } else {
            titleText.text = getString(R.string.new_profile)
            portEditText.setText(getString(R.string.default_port))
        }
    }
    
    private fun setupFocusFlow() {
        // Set up D-pad navigation between fields
        nameEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                ipEditText.requestFocus()
                true
            } else {
                false
            }
        }
        
        ipEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        portEditText.requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        nameEditText.requestFocus()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        
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
        
        // Focus flow for buttons
        saveButton.nextFocusRightId = R.id.cancelButton
        cancelButton.nextFocusLeftId = R.id.saveButton
    }
    
    private fun saveProfile() {
        val name = nameEditText.text.toString().trim()
        val ip = ipEditText.text.toString().trim()
        val port = portEditText.text.toString().trim()
        
        // Validate inputs
        if (name.isEmpty()) {
            nameEditText.error = "Profile name is required"
            nameEditText.requestFocus()
            return
        }
        
        if (!isValidIpAddress(ip)) {
            ipEditText.error = getString(R.string.error_invalid_ip)
            ipEditText.requestFocus()
            return
        }
        
        if (!isValidPort(port)) {
            portEditText.error = getString(R.string.error_invalid_port)
            portEditText.requestFocus()
            return
        }
        
        // Save or update profile
        val profile = if (existingProfile != null) {
            existingProfile!!.copy(
                name = name,
                ipAddress = ip,
                port = port
            )
        } else {
            ServerProfile(
                name = name,
                ipAddress = ip,
                port = port
            )
        }
        
        profileManager.saveProfile(profile)
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
        
        setResult(RESULT_OK)
        finish()
    }
    
    private fun isValidIpAddress(ip: String): Boolean {
        if (ip.isEmpty()) return false
        
        // Allow localhost
        if (ip == "localhost" || ip == "127.0.0.1") return true
        
        // Check standard IPv4 format
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            try {
                val num = part.toInt()
                num in 0..255
            } catch (e: NumberFormatException) {
                false
            }
        }
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
}