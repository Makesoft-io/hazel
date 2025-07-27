package com.webviewer.firetv

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ControlsGuideActivity : AppCompatActivity() {
    
    private lateinit var scrollView: ScrollView
    private lateinit var contentTextView: TextView
    private val scrollAmount = 200 // Amount to scroll with each D-pad press
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controls_guide)
        
        scrollView = findViewById(R.id.scrollView)
        contentTextView = findViewById(R.id.controlsContent)
        
        // Set the controls manual content
        contentTextView.text = getControlsManualContent()
        
        // Request focus for the scroll view to enable D-pad navigation
        scrollView.requestFocus()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                scrollView.smoothScrollBy(0, -scrollAmount)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                scrollView.smoothScrollBy(0, scrollAmount)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun getControlsManualContent(): String {
        return """
FIRE TV REMOTE CONTROLS

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

MAIN SCREEN CONTROLS

Navigation (D-pad)
• ↑ Up: Navigate up / Focus toolbar
• ↓ Down: Navigate down / Leave toolbar
• ← Left: Navigate left / Previous button
• → Right: Navigate right / Next button
• SELECT: Click/select / Activate button

System Controls
• MENU: Open app settings
• BACK: Browser back or exit
• HOME: Exit to Fire TV home

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

BROWSER TOOLBAR

Press ↑ Up to access toolbar buttons:
1. Back - Previous page
2. Forward - Next page
3. Refresh - Reload page
4. Home - Return to server URL
5. Settings - Open app settings

Navigation wraps around (← from Back goes to Settings)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SETTINGS SCREEN

• ↑/↓: Move between fields
• SELECT on field: Show keyboard
• SELECT on button: Save or Cancel
• BACK: Exit without saving

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

WEB CONTENT

Remote buttons translate to keyboard:
• D-pad → Arrow keys
• SELECT → Enter key

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TROUBLESHOOTING

Remote Not Working?
• Check batteries and pairing
• Try other Fire TV apps
• Restart the app

Can't Navigate Web Content?
• Web page must support keyboard
• Try refreshing the page
• Check for JavaScript errors

Focus Lost?
• Press ↑ Up then ↓ Down
• Use D-pad to restore focus

Settings Not Saving?
• Use Save button (not BACK)
• Check IP format (192.168.x.x)
• Port must be 1-65535

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

QUICK REFERENCE

Open Settings: MENU
Navigate: D-pad (↑↓←→)
Select: CENTER/SELECT
Go Back: BACK
Refresh: ↑ then → to Refresh
Home: ↑ then → to Home

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Use ↑ Up and ↓ Down to scroll this guide
Press BACK to return to settings
        """.trimIndent()
    }
}