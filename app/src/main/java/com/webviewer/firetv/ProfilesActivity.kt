package com.webviewer.firetv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfilesActivity : AppCompatActivity() {
    
    private lateinit var profileManager: ProfileManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var addButton: View
    private lateinit var adapter: ProfilesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)
        
        profileManager = ProfileManager(this)
        
        initializeViews()
        loadProfiles()
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.profilesRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        addButton = findViewById(R.id.addProfileButton)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ProfilesAdapter(
            onProfileClick = { profile ->
                selectProfile(profile)
            },
            onProfileEdit = { profile ->
                editProfile(profile)
            },
            onProfileDelete = { profile ->
                confirmDeleteProfile(profile)
            }
        )
        
        recyclerView.adapter = adapter
        
        addButton.setOnClickListener {
            createNewProfile()
        }
    }
    
    private fun loadProfiles() {
        val profiles = profileManager.getProfilesSortedByLastUsed()
        val activeProfile = profileManager.getActiveProfile()
        
        adapter.setProfiles(profiles, activeProfile?.id)
        
        if (profiles.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
    
    private fun selectProfile(profile: ServerProfile) {
        profileManager.setActiveProfile(profile)
        Toast.makeText(this, "Profile '${profile.name}' activated", Toast.LENGTH_SHORT).show()
        
        // Return to main activity
        val intent = Intent()
        intent.putExtra("profile_changed", true)
        setResult(RESULT_OK, intent)
        finish()
    }
    
    private fun createNewProfile() {
        val intent = Intent(this, ProfileEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_PROFILE)
    }
    
    private fun editProfile(profile: ServerProfile) {
        val intent = Intent(this, ProfileEditActivity::class.java)
        intent.putExtra("profile_id", profile.id)
        startActivityForResult(intent, REQUEST_EDIT_PROFILE)
    }
    
    private fun confirmDeleteProfile(profile: ServerProfile) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_profile)
            .setMessage(R.string.confirm_delete_profile)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteProfile(profile)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun deleteProfile(profile: ServerProfile) {
        profileManager.deleteProfile(profile.id)
        Toast.makeText(this, R.string.profile_deleted, Toast.LENGTH_SHORT).show()
        loadProfiles()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CREATE_PROFILE, REQUEST_EDIT_PROFILE -> {
                    loadProfiles()
                }
            }
        }
    }
    
    companion object {
        private const val REQUEST_CREATE_PROFILE = 1001
        private const val REQUEST_EDIT_PROFILE = 1002
    }
}