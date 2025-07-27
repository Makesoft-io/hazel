package com.webviewer.firetv

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class IPAddressInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val field1: EditText
    private val field2: EditText
    private val field3: EditText
    private val field4: EditText
    private val dot1: TextView
    private val dot2: TextView
    private val dot3: TextView
    
    private val fields: List<EditText>
    private val dots: List<TextView>
    
    private var onIpChangedListener: ((String) -> Unit)? = null
    private var onValidationChangedListener: ((Boolean) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_ip_address_input, this, true)
        
        // Initialize views
        field1 = findViewById(R.id.ipField1)
        field2 = findViewById(R.id.ipField2)
        field3 = findViewById(R.id.ipField3)
        field4 = findViewById(R.id.ipField4)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        
        fields = listOf(field1, field2, field3, field4)
        dots = listOf(dot1, dot2, dot3)
        
        setupFields()
        setupFocusNavigation()
    }
    
    private fun setupFields() {
        fields.forEachIndexed { index, field ->
            field.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(InputFilter.LengthFilter(3))
                
                // Custom text watcher for each field
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    
                    override fun afterTextChanged(s: Editable?) {
                        val text = s.toString()
                        
                        // Validate the field value (0-255)
                        val isValid = validateField(text)
                        updateFieldAppearance(field, isValid)
                        
                        // Auto-jump to next field when complete
                        if (text.length == 3 || (text.toIntOrNull() ?: 0) > 25) {
                            if (index < fields.size - 1) {
                                fields[index + 1].requestFocus()
                            }
                        }
                        
                        // Notify listeners
                        notifyIpChanged()
                        notifyValidationChanged()
                    }
                })
                
                // Custom key listener for better navigation
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (selectionStart == 0 && index > 0) {
                                    fields[index - 1].requestFocus()
                                    fields[index - 1].setSelection(fields[index - 1].text.length)
                                    true
                                } else false
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (selectionStart == text.length && index < fields.size - 1) {
                                    fields[index + 1].requestFocus()
                                    fields[index + 1].setSelection(0)
                                    true
                                } else false
                            }
                            KeyEvent.KEYCODE_DEL -> {
                                if (text.isEmpty() && index > 0) {
                                    fields[index - 1].requestFocus()
                                    fields[index - 1].setSelection(fields[index - 1].text.length)
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                }
                
                // Focus change listeners for animations
                setOnFocusChangeListener { _, hasFocus ->
                    animateFieldFocus(field, hasFocus)
                    if (hasFocus) {
                        animateDots(true)
                    }
                }
            }
        }
    }
    
    private fun setupFocusNavigation() {
        // Set up next focus IDs for D-pad navigation
        field1.nextFocusRightId = field2.id
        field2.nextFocusLeftId = field1.id
        field2.nextFocusRightId = field3.id
        field3.nextFocusLeftId = field2.id
        field3.nextFocusRightId = field4.id
        field4.nextFocusLeftId = field3.id
    }
    
    private fun validateField(text: String): Boolean {
        if (text.isEmpty()) return true // Allow empty for partial input
        val value = text.toIntOrNull() ?: return false
        return value in 0..255
    }
    
    private fun updateFieldAppearance(field: EditText, isValid: Boolean) {
        val backgroundRes = when {
            !isValid -> R.drawable.ip_field_error
            field.hasFocus() -> R.drawable.ip_field_focused
            field.text.isNotEmpty() -> R.drawable.ip_field_success
            else -> R.drawable.ip_field_normal
        }
        field.setBackgroundResource(backgroundRes)
    }
    
    private fun animateFieldFocus(field: EditText, hasFocus: Boolean) {
        val scaleX = if (hasFocus) 1.05f else 1.0f
        val scaleY = if (hasFocus) 1.05f else 1.0f
        
        field.animate()
            .scaleX(scaleX)
            .scaleY(scaleY)
            .setDuration(200)
            .start()
    }
    
    private fun animateDots(highlight: Boolean) {
        val color = if (highlight) {
            ContextCompat.getColor(context, R.color.accent)
        } else {
            ContextCompat.getColor(context, R.color.ip_dot_color)
        }
        
        dots.forEach { dot ->
            dot.animate()
                .alpha(if (highlight) 1.0f else 0.7f)
                .setDuration(200)
                .start()
            dot.setTextColor(color)
        }
    }
    
    private fun notifyIpChanged() {
        val ip = getIpAddress()
        onIpChangedListener?.invoke(ip)
    }
    
    private fun notifyValidationChanged() {
        val isValid = isValidIpAddress()
        onValidationChangedListener?.invoke(isValid)
    }
    
    fun getIpAddress(): String {
        return fields.joinToString(".") { it.text.toString().ifEmpty { "0" } }
    }
    
    fun setIpAddress(ip: String) {
        val parts = ip.split(".")
        if (parts.size == 4) {
            fields.forEachIndexed { index, field ->
                field.setText(parts[index])
            }
        }
    }
    
    fun isValidIpAddress(): Boolean {
        return fields.all { field ->
            val text = field.text.toString()
            text.isNotEmpty() && validateField(text)
        }
    }
    
    fun clearFields() {
        fields.forEach { it.text.clear() }
        field1.requestFocus()
    }
    
    fun setOnIpChangedListener(listener: (String) -> Unit) {
        onIpChangedListener = listener
    }
    
    fun setOnValidationChangedListener(listener: (Boolean) -> Unit) {
        onValidationChangedListener = listener
    }
    
    fun requestFocusOnFirstField(): Boolean {
        return field1.requestFocus()
    }
    
    fun getIPAddress(): String {
        return "${field1.text}.${field2.text}.${field3.text}.${field4.text}"
    }
    
    fun setIPAddress(ip: String) {
        val parts = ip.split(".")
        if (parts.size == 4) {
            field1.setText(parts[0])
            field2.setText(parts[1])
            field3.setText(parts[2])
            field4.setText(parts[3])
        }
    }
    
    fun setDefaultLocalNetworkIP() {
        field1.setText("192")
        field2.setText("168")
        field3.setText("4")
        field4.setText("")
        // Focus on the last field for user to complete
        field4.requestFocus()
    }
}