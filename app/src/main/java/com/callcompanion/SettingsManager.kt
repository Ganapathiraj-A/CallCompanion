package com.callcompanion

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("call_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_SHARE_TEXT = "share_text"
        const val KEY_VIDEO_LINK = "video_link"
        
        const val DEFAULT_SHARE_TEXT = "Thank you for the call! Check out Sri Bagavath: https://sribagavath.org"
        const val DEFAULT_VIDEO_LINK = "https://youtube.com/sribagavath"
    }

    fun getShareText(): String {
        return prefs.getString(KEY_SHARE_TEXT, DEFAULT_SHARE_TEXT) ?: DEFAULT_SHARE_TEXT
    }

    fun setShareText(text: String) {
        prefs.edit().putString(KEY_SHARE_TEXT, text).apply()
    }

    fun getVideoLink(): String {
        return prefs.getString(KEY_VIDEO_LINK, DEFAULT_VIDEO_LINK) ?: DEFAULT_VIDEO_LINK
    }

    fun setVideoLink(link: String) {
        prefs.edit().putString(KEY_VIDEO_LINK, link).apply()
    }
}
