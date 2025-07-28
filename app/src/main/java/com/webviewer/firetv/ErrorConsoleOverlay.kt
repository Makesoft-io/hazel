package com.webviewer.firetv

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ErrorConsoleOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val errorRecyclerView: RecyclerView
    private val errorCount: TextView
    private val emptyErrorsText: TextView
    private val clearErrorsButton: ImageButton
    private val closeConsoleButton: ImageButton
    
    private val adapter = ErrorConsoleAdapter()
    
    var onCloseRequested: (() -> Unit)? = null
    
    init {
        try {
            android.util.Log.d("ErrorConsole", "Initializing ErrorConsoleOverlay")
            
            // Inflate the overlay layout
            val inflater = LayoutInflater.from(context)
            inflater.inflate(R.layout.error_console_overlay, this, true)
            
            // Initialize views
            errorRecyclerView = findViewById(R.id.errorRecyclerView)
            errorCount = findViewById(R.id.errorCount)
            emptyErrorsText = findViewById(R.id.emptyErrorsText)
            clearErrorsButton = findViewById(R.id.clearErrorsButton)
            closeConsoleButton = findViewById(R.id.closeConsoleButton)
            
            setupRecyclerView()
            setupButtons()
            updateEmptyState()
            
            android.util.Log.d("ErrorConsole", "ErrorConsoleOverlay initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("ErrorConsole", "Error initializing ErrorConsoleOverlay", e)
            throw e
        }
    }
    
    private fun setupRecyclerView() {
        errorRecyclerView.layoutManager = LinearLayoutManager(context)
        errorRecyclerView.adapter = adapter
        
        // Enable focus navigation for TV
        errorRecyclerView.isFocusable = false
        errorRecyclerView.descendantFocusability = FOCUS_AFTER_DESCENDANTS
    }
    
    private fun setupButtons() {
        clearErrorsButton.setOnClickListener {
            clearErrors()
        }
        
        closeConsoleButton.setOnClickListener {
            hide()
            onCloseRequested?.invoke()
        }
        
        // Focus animations for TV
        val buttons = listOf(clearErrorsButton, closeConsoleButton)
        buttons.forEach { button ->
            button.setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) 1.1f else 1.0f
                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(200)
                    .start()
            }
        }
    }
    
    fun addError(error: JavaScriptError) {
        adapter.addError(error)
        updateErrorCount()
        updateEmptyState()
        
        // Auto-scroll to top for latest error
        errorRecyclerView.scrollToPosition(0)
        
        android.util.Log.d("ErrorConsole", "JavaScript error: ${error.message} at ${error.getFormattedSource()}")
    }
    
    fun clearErrors() {
        adapter.clearErrors()
        updateErrorCount()
        updateEmptyState()
        android.util.Log.d("ErrorConsole", "All JavaScript errors cleared")
    }
    
    private fun updateErrorCount() {
        val count = adapter.getErrorCount()
        errorCount.text = count.toString()
        errorCount.visibility = if (count > 0) View.VISIBLE else View.GONE
    }
    
    private fun updateEmptyState() {
        val isEmpty = adapter.getErrorCount() == 0
        emptyErrorsText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        errorRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    fun show() {
        try {
            android.util.Log.d("ErrorConsole", "Showing error console overlay")
            visibility = View.VISIBLE
            alpha = 1f // Start visible, no animation for now
            
            // Focus the close button by default for TV navigation
            closeConsoleButton.requestFocus()
            
            android.util.Log.d("ErrorConsole", "Error console overlay shown successfully")
        } catch (e: Exception) {
            android.util.Log.e("ErrorConsole", "Error showing overlay", e)
            visibility = View.GONE
        }
    }
    
    fun hide() {
        try {
            android.util.Log.d("ErrorConsole", "Hiding error console overlay")
            visibility = View.GONE
            android.util.Log.d("ErrorConsole", "Error console overlay hidden successfully")
        } catch (e: Exception) {
            android.util.Log.e("ErrorConsole", "Error hiding overlay", e)
        }
    }
    
    fun isShowing(): Boolean {
        return visibility == View.VISIBLE
    }
    
    fun getFirstFocusableView(): View = closeConsoleButton
}