package com.webviewer.firetv

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ProfilesAdapter(
    private val onProfileClick: (ServerProfile) -> Unit,
    private val onProfileEdit: (ServerProfile) -> Unit,
    private val onProfileDelete: (ServerProfile) -> Unit
) : RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder>() {
    
    private var profiles: List<ServerProfile> = emptyList()
    private var activeProfileId: String? = null
    
    fun setProfiles(profiles: List<ServerProfile>, activeProfileId: String?) {
        this.profiles = profiles
        this.activeProfileId = activeProfileId
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }
    
    override fun getItemCount(): Int = profiles.size
    
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.profileCard)
        private val nameText: TextView = itemView.findViewById(R.id.profileName)
        private val urlText: TextView = itemView.findViewById(R.id.profileUrl)
        private val lastUsedText: TextView = itemView.findViewById(R.id.profileLastUsed)
        private val activeIndicator: View = itemView.findViewById(R.id.activeIndicator)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        
        fun bind(profile: ServerProfile) {
            nameText.text = profile.name
            urlText.text = profile.getUrl()
            
            // Show last used time
            if (profile.lastUsedAt > 0) {
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    profile.lastUsedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
                lastUsedText.text = itemView.context.getString(R.string.last_used, relativeTime)
                lastUsedText.visibility = View.VISIBLE
            } else {
                lastUsedText.text = itemView.context.getString(R.string.never_used)
                lastUsedText.visibility = View.VISIBLE
            }
            
            // Show active indicator
            activeIndicator.visibility = if (profile.id == activeProfileId) View.VISIBLE else View.GONE
            
            // Set click listeners
            cardView.setOnClickListener {
                onProfileClick(profile)
            }
            
            editButton.setOnClickListener {
                onProfileEdit(profile)
            }
            
            deleteButton.setOnClickListener {
                onProfileDelete(profile)
            }
            
            // Set focus change listeners for TV navigation
            cardView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .setDuration(200)
                        .start()
                } else {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()
                }
            }
        }
    }
}