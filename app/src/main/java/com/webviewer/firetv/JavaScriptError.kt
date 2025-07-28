package com.webviewer.firetv

import java.text.SimpleDateFormat
import java.util.*

data class JavaScriptError(
    val message: String,
    val source: String,
    val lineNumber: Int,
    val columnNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val type: ErrorType = ErrorType.ERROR
) {
    enum class ErrorType {
        ERROR,
        WARNING,
        LOG,
        INFO
    }
    
    fun getFormattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    fun getFormattedSource(): String {
        return if (lineNumber > 0 && columnNumber > 0) {
            "${source}:${lineNumber}:${columnNumber}"
        } else if (lineNumber > 0) {
            "${source}:${lineNumber}"
        } else {
            source
        }
    }
}