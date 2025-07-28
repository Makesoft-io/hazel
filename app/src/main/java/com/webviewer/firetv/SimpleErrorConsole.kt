package com.webviewer.firetv

import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*

class SimpleErrorConsole(private val context: Context) {
    
    data class ErrorEntry(
        val message: String,
        val source: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun getFormattedTimestamp(): String {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }
    }
    
    private val errors = mutableListOf<ErrorEntry>()
    private val maxErrors = 50 // Limit to prevent memory issues
    
    fun addError(message: String, source: String = "unknown") {
        val error = ErrorEntry(message, source)
        errors.add(0, error) // Add to beginning
        
        // Limit list size
        if (errors.size > maxErrors) {
            errors.removeAt(errors.size - 1)
        }
        
        android.util.Log.d("SimpleErrorConsole", "JavaScript Error: $message at $source")
    }
    
    fun clearErrors() {
        errors.clear()
        android.util.Log.d("SimpleErrorConsole", "All errors cleared")
    }
    
    fun getErrorCount(): Int = errors.size
    
    fun showErrorConsole() {
        try {
            val message = if (errors.isEmpty()) {
                "🟢 No JavaScript errors detected\n\nThis console captures JavaScript errors from your web application in real-time."
            } else {
                buildErrorMessage()
            }
            
            AlertDialog.Builder(context)
                .setTitle("JavaScript Console (${errors.size} errors)")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .setNeutralButton("Clear All") { _, _ ->
                    clearErrors()
                }
                .setCancelable(true)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("SimpleErrorConsole", "Error showing console", e)
        }
    }
    
    private fun buildErrorMessage(): String {
        val builder = StringBuilder()
        builder.append("🔴 Recent JavaScript Errors:\n\n")
        
        errors.take(10).forEach { error -> // Show max 10 errors
            builder.append("⚠️ ${error.getFormattedTimestamp()}\n")
            builder.append("${error.message}\n")
            if (error.source != "unknown") {
                builder.append("📍 ${error.source}\n")
            }
            builder.append("\n")
        }
        
        if (errors.size > 10) {
            builder.append("... and ${errors.size - 10} more errors")
        }
        
        return builder.toString()
    }
}