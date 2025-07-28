package com.webviewer.firetv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ErrorConsoleAdapter : RecyclerView.Adapter<ErrorConsoleAdapter.ErrorViewHolder>() {
    
    private val errors = mutableListOf<JavaScriptError>()
    
    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorTypeIcon: ImageView = itemView.findViewById(R.id.errorTypeIcon)
        val errorMessage: TextView = itemView.findViewById(R.id.errorMessage)
        val errorSource: TextView = itemView.findViewById(R.id.errorSource)
        val errorTimestamp: TextView = itemView.findViewById(R.id.errorTimestamp)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_error_entry, parent, false)
        return ErrorViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
        val error = errors[position]
        
        holder.errorMessage.text = error.message
        holder.errorSource.text = error.getFormattedSource()
        holder.errorTimestamp.text = error.getFormattedTimestamp()
        
        // Set icon based on error type
        val iconRes = when (error.type) {
            JavaScriptError.ErrorType.ERROR -> R.drawable.ic_error_outline
            JavaScriptError.ErrorType.WARNING -> R.drawable.ic_warning_outline
            JavaScriptError.ErrorType.LOG -> R.drawable.ic_info_outline
            JavaScriptError.ErrorType.INFO -> R.drawable.ic_info_outline
        }
        holder.errorTypeIcon.setImageResource(iconRes)
        
        // Set icon tint based on error type
        val colorRes = when (error.type) {
            JavaScriptError.ErrorType.ERROR -> R.color.error
            JavaScriptError.ErrorType.WARNING -> R.color.warning
            JavaScriptError.ErrorType.LOG -> R.color.info
            JavaScriptError.ErrorType.INFO -> R.color.info
        }
        holder.errorTypeIcon.setColorFilter(holder.itemView.context.getColor(colorRes))
        
        // Add focus animation for TV navigation
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            val scale = if (hasFocus) 1.05f else 1.0f
            view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(200)
                .start()
        }
    }
    
    override fun getItemCount(): Int = errors.size
    
    fun addError(error: JavaScriptError) {
        errors.add(0, error) // Add to top
        notifyItemInserted(0)
        
        // Limit to last 100 errors to prevent memory issues
        if (errors.size > 100) {
            errors.removeAt(errors.size - 1)
            notifyItemRemoved(errors.size)
        }
    }
    
    fun clearErrors() {
        val count = errors.size
        errors.clear()
        notifyItemRangeRemoved(0, count)
    }
    
    fun getErrorCount(): Int = errors.size
}