package com.webviewer.firetv

import java.util.UUID

/**
 * Represents a saved server configuration profile
 */
data class ServerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val ipAddress: String,
    val port: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Returns the full server URL
     */
    fun getUrl(): String {
        return "http://$ipAddress:$port"
    }
    
    /**
     * Returns a display name with server details
     */
    fun getDisplayName(): String {
        return "$name ($ipAddress:$port)"
    }
    
    /**
     * Updates the last used timestamp
     */
    fun withUpdatedLastUsed(): ServerProfile {
        return copy(lastUsedAt = System.currentTimeMillis())
    }
    
    companion object {
        /**
         * Creates a default profile
         */
        fun createDefault(): ServerProfile {
            return ServerProfile(
                name = "Local Development",
                ipAddress = "192.168.1.100",
                port = "3000",
                isDefault = true
            )
        }
        
        /**
         * Common development server presets
         */
        fun getPresets(): List<ServerProfile> {
            return listOf(
                ServerProfile(name = "React Dev Server", ipAddress = "", port = "3000"),
                ServerProfile(name = "Vue Dev Server", ipAddress = "", port = "8080"),
                ServerProfile(name = "Angular Dev Server", ipAddress = "", port = "4200"),
                ServerProfile(name = "Next.js Server", ipAddress = "", port = "3000"),
                ServerProfile(name = "Python Server", ipAddress = "", port = "8000"),
                ServerProfile(name = "Rails Server", ipAddress = "", port = "3000"),
                ServerProfile(name = "Express Server", ipAddress = "", port = "5000"),
                ServerProfile(name = "Vite Server", ipAddress = "", port = "5173")
            )
        }
    }
}