package me.calebjones.spacelaunchnow.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import me.calebjones.spacelaunchnow.shared.R

/**
 * Android implementation for opening external links.
 */
actual object ExternalLinkHandler {
    
    private lateinit var context: Context
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    actual fun openEmail(recipient: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body.isNotEmpty()) {
                putExtra(Intent.EXTRA_TEXT, body)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.feedback_chooser_title)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
    
    actual fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
