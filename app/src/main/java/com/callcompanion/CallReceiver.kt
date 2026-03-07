package com.callcompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val prefs = context.getSharedPreferences("call_prefs", Context.MODE_PRIVATE)
            val wasOffHook = prefs.getBoolean("was_off_hook", false)

            Log.d("CallReceiver", "Phone State: $state, wasOffHook: $wasOffHook")

            when (state) {
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call is active or being dialed
                    prefs.edit().putBoolean("was_off_hook", true).apply()
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended
                    if (wasOffHook) {
                        prefs.edit().putBoolean("was_off_hook", false).apply()
                        
                        // Launch the Companion UI
                        val postCallIntent = Intent(context, PostCallActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        context.startActivity(postCallIntent)
                    }
                }
            }
        }
    }
}
