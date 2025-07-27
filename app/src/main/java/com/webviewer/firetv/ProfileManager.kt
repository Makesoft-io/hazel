package com.webviewer.firetv

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages server profiles storage and retrieval
 */
class ProfileManager(private val context: Context) {
    
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    
    companion object {
        private const val KEY_PROFILES = "server_profiles"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_LEGACY_IP = "server_ip"
        private const val KEY_LEGACY_PORT = "server_port"
    }
    
    /**
     * Get all saved profiles
     */
    fun getAllProfiles(): List<ServerProfile> {
        val profilesJson = preferences.getString(KEY_PROFILES, null)
        
        return if (profilesJson != null) {
            try {
                val type = object : TypeToken<List<ServerProfile>>() {}.type
                gson.fromJson(profilesJson, type)
            } catch (e: Exception) {
                // If parsing fails, return empty list
                emptyList()
            }
        } else {
            // Check for legacy settings and migrate if found
            migrateLegacySettings()
        }
    }
    
    /**
     * Save a new profile or update existing one
     */
    fun saveProfile(profile: ServerProfile) {
        val profiles = getAllProfiles().toMutableList()
        
        // Remove existing profile with same ID if updating
        profiles.removeAll { it.id == profile.id }
        
        // Add the new/updated profile
        profiles.add(profile)
        
        // Save to preferences
        saveProfiles(profiles)
    }
    
    /**
     * Delete a profile
     */
    fun deleteProfile(profileId: String) {
        val profiles = getAllProfiles().toMutableList()
        profiles.removeAll { it.id == profileId }
        
        // If we deleted the active profile, clear it
        if (getActiveProfileId() == profileId) {
            setActiveProfileId(null)
        }
        
        saveProfiles(profiles)
    }
    
    /**
     * Get the currently active profile
     */
    fun getActiveProfile(): ServerProfile? {
        val activeId = getActiveProfileId() ?: return null
        return getAllProfiles().find { it.id == activeId }
    }
    
    /**
     * Set the active profile
     */
    fun setActiveProfile(profile: ServerProfile) {
        setActiveProfileId(profile.id)
        
        // Update last used timestamp
        saveProfile(profile.withUpdatedLastUsed())
    }
    
    /**
     * Get the active profile ID
     */
    private fun getActiveProfileId(): String? {
        return preferences.getString(KEY_ACTIVE_PROFILE_ID, null)
    }
    
    /**
     * Set the active profile ID
     */
    private fun setActiveProfileId(profileId: String?) {
        preferences.edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()
    }
    
    /**
     * Save profiles list to preferences
     */
    private fun saveProfiles(profiles: List<ServerProfile>) {
        val json = gson.toJson(profiles)
        preferences.edit().putString(KEY_PROFILES, json).apply()
    }
    
    /**
     * Migrate legacy IP/port settings to profile system
     */
    private fun migrateLegacySettings(): List<ServerProfile> {
        val legacyIp = preferences.getString(KEY_LEGACY_IP, null)
        val legacyPort = preferences.getString(KEY_LEGACY_PORT, "3000")
        
        return if (!legacyIp.isNullOrEmpty()) {
            // Create a profile from legacy settings
            val legacyProfile = ServerProfile(
                name = "Migrated Settings",
                ipAddress = legacyIp,
                port = legacyPort ?: "3000",
                isDefault = true
            )
            
            // Save the migrated profile
            saveProfiles(listOf(legacyProfile))
            setActiveProfileId(legacyProfile.id)
            
            // Clear legacy settings
            preferences.edit()
                .remove(KEY_LEGACY_IP)
                .remove(KEY_LEGACY_PORT)
                .apply()
            
            listOf(legacyProfile)
        } else {
            // No legacy settings, return empty list
            emptyList()
        }
    }
    
    /**
     * Check if any profiles exist
     */
    fun hasProfiles(): Boolean {
        return getAllProfiles().isNotEmpty()
    }
    
    /**
     * Get profiles sorted by last used time
     */
    fun getProfilesSortedByLastUsed(): List<ServerProfile> {
        return getAllProfiles().sortedByDescending { it.lastUsedAt }
    }
    
    /**
     * Create and save a profile from current input
     */
    fun createProfileFromInput(name: String, ipAddress: String, port: String): ServerProfile {
        val profile = ServerProfile(
            name = name,
            ipAddress = ipAddress,
            port = port
        )
        saveProfile(profile)
        return profile
    }
}