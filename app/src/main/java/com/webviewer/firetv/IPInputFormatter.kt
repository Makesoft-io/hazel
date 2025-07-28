package com.webviewer.firetv

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * IP Address Input Formatter for Fire TV
 * 
 * Automatically adds dots after every 3 digits for better Fire TV remote experience.
 * Example: User types "192168001100" → Automatically becomes "192.168.001.100"
 * 
 * Usage:
 * val ipFormatter = IPInputFormatter()
 * editText.addTextChangedListener(ipFormatter)
 */
class IPInputFormatter : TextWatcher {
    
    private var isUpdating = false
    private var editText: EditText? = null
    
    fun attachTo(editText: EditText): IPInputFormatter {
        this.editText = editText
        return this
    }
    
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not used in this implementation
    }
    
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Not used in this implementation
    }
    
    override fun afterTextChanged(s: Editable?) {
        if (isUpdating || s == null) return
        
        val originalText = s.toString()
        val digitsOnly = originalText.replace(Regex("[^0-9]"), "")
        
        // Don't format if empty
        if (digitsOnly.isEmpty()) return
        
        val formatted = formatIPAddress(digitsOnly)
        
        // Only update if formatting actually changed something
        if (formatted != originalText) {
            isUpdating = true
            
            try {
                // Get cursor position before change
                val cursorPos = editText?.selectionStart ?: formatted.length
                
                // Replace text with formatted version
                s.replace(0, s.length, formatted)
                
                // Calculate and set new cursor position
                val newCursorPosition = calculateNewCursorPosition(digitsOnly, formatted, cursorPos)
                
                // Restore cursor position in the EditText
                editText?.setSelection(newCursorPosition.coerceIn(0, formatted.length))
            } catch (e: IndexOutOfBoundsException) {
                // Handle span issues by just setting the text directly
                editText?.removeTextChangedListener(this)
                editText?.setText(formatted)
                editText?.setSelection(formatted.length)
                editText?.addTextChangedListener(this)
            } finally {
                isUpdating = false
            }
        }
    }
    
    private fun formatIPAddress(digitsOnly: String): String {
        val result = StringBuilder()
        var digitCount = 0
        
        for (digit in digitsOnly) {
            // Add dot before adding digit if we've added 3 digits in current octet
            if (digitCount > 0 && digitCount % 3 == 0 && result.isNotEmpty()) {
                result.append('.')
            }
            
            result.append(digit)
            digitCount++
            
            // Stop at 12 digits (4 octets of 3 digits each)
            if (digitCount >= 12) break
        }
        
        return result.toString()
    }
    
    private fun calculateNewCursorPosition(digitsOnly: String, formatted: String, oldCursor: Int): Int {
        // Place cursor at the end after formatting
        return formatted.length
    }
    
    private fun getEditTextFromEditable(editable: Editable): EditText? {
        // This is a workaround to get EditText reference from Editable
        // In practice, the EditText will handle cursor positioning
        return null
    }
    
    companion object {
        /**
         * Convenience method to apply IP formatting to an EditText
         */
        fun applyTo(editText: EditText): IPInputFormatter {
            val formatter = IPInputFormatter().attachTo(editText)
            
            // Set input type to show numeric keyboard on Fire TV
            editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            
            // Limit to reasonable IP length (15 chars: xxx.xxx.xxx.xxx)
            editText.filters = arrayOf(android.text.InputFilter.LengthFilter(15))
            
            // Apply formatter after setting up the field
            editText.addTextChangedListener(formatter)
            
            return formatter
        }
        
        /**
         * Validate if a string is a valid IP address format
         */
        fun isValidIP(ip: String): Boolean {
            val parts = ip.split(".")
            if (parts.size != 4) return false
            
            return parts.all { part ->
                val num = part.toIntOrNull()
                num != null && num in 0..255
            }
        }
        
        /**
         * Clean up IP address string (remove extra dots, fix formatting)
         */
        fun cleanupIP(ip: String): String {
            val digitsOnly = ip.replace(Regex("[^0-9]"), "")
            return formatIPAddressStatic(digitsOnly)
        }
        
        private fun formatIPAddressStatic(digitsOnly: String): String {
            val result = StringBuilder()
            var digitCount = 0
            
            for (digit in digitsOnly) {
                if (digitCount > 0 && digitCount % 3 == 0 && result.isNotEmpty()) {
                    result.append('.')
                }
                
                result.append(digit)
                digitCount++
                
                if (digitCount >= 12) break
            }
            
            return result.toString()
        }
    }
}