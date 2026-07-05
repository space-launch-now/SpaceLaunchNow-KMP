package me.calebjones.spacelaunchnow.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

/** Application context for share intents fired from common code. Set in MainApplication.onCreate. */
@SuppressLint("StaticFieldLeak")
object ShareContextHolder {
    var appContext: Context? = null
}

actual fun sharePlainText(text: String, subject: String) {
    val context = ShareContextHolder.appContext ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, subject).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
