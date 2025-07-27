package com.webviewer.firetv

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

data class PortOption(
    val port: String,
    val description: String,
    val useCase: String
) {
    override fun toString(): String = "$port - $description"
}

class PortSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val portSpinner: Spinner
    private val useCaseText: TextView
    
    private var onPortSelectedListener: ((String) -> Unit)? = null
    
    private val commonPorts = listOf(
        PortOption("3000", "Node.js Dev Server", "React, Next.js, Express development"),
        PortOption("8080", "HTTP Proxy/Dev", "Webpack dev server, general HTTP"),
        PortOption("5000", "Flask Dev Server", "Python Flask applications"),
        PortOption("8000", "Python HTTP Server", "Django, SimpleHTTPServer"),
        PortOption("4000", "Jekyll Server", "Static site generators"),
        PortOption("3001", "Alternative Dev", "Secondary development server"),
        PortOption("9000", "PHP/Apache", "PHP development, PHPStorm"),
        PortOption("8081", "Alternative HTTP", "Secondary HTTP server"),
        PortOption("5173", "Vite Dev Server", "Modern build tool (Vite)"),
        PortOption("4200", "Angular CLI", "Angular development server"),
        PortOption("8888", "Jupyter Notebook", "Data science/Python notebooks"),
        PortOption("3333", "Gatsby Dev", "Gatsby development server"),
        PortOption("Custom", "Enter Custom Port", "Specify your own port number")
    )

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_port_selector, this, true)
        
        portSpinner = findViewById(R.id.portSpinner)
        useCaseText = findViewById(R.id.useCaseText)
        
        setupSpinner()
    }
    
    private fun setupSpinner() {
        val adapter = object : ArrayAdapter<PortOption>(
            context,
            R.layout.spinner_port_item,
            commonPorts
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val portOption = getItem(position)
                
                // Style for TV (larger text, better contrast)
                (view as TextView).apply {
                    textSize = 20f
                    setPadding(24, 16, 24, 16)
                    setTextColor(context.getColor(R.color.text_primary))
                    setBackgroundColor(context.getColor(R.color.surface))
                }
                
                return view
            }
        }
        
        adapter.setDropDownViewResource(R.layout.spinner_port_dropdown_item)
        portSpinner.adapter = adapter
        
        portSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPort = commonPorts[position]
                useCaseText.text = selectedPort.useCase
                onPortSelectedListener?.invoke(selectedPort.port)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Set default selection to Node.js (3000)
        portSpinner.setSelection(0)
    }
    
    fun setSelectedPort(port: String) {
        val index = commonPorts.indexOfFirst { it.port == port }
        if (index >= 0) {
            portSpinner.setSelection(index)
        } else {
            // If port not found, select "Custom" and update the custom port
            portSpinner.setSelection(commonPorts.size - 1)
        }
    }
    
    fun getSelectedPort(): String {
        val selectedIndex = portSpinner.selectedItemPosition
        return if (selectedIndex >= 0) {
            commonPorts[selectedIndex].port
        } else {
            "3000"
        }
    }
    
    fun setOnPortSelectedListener(listener: (String) -> Unit) {
        onPortSelectedListener = listener
    }
    
    fun requestFocusOnSpinner(): Boolean {
        return portSpinner.requestFocus()
    }
}