package com.android.launcher.foldable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives system configuration change broadcasts
 * Triggers display detection when device is folded/unfolded
 */
class ConfigurationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_CONFIGURATION_CHANGED -> {
                Log.d("ConfigurationReceiver", "Configuration changed - detecting display")
                // Trigger display detection
                if (context != null) {
                    val displayManager = FoldableDisplayManager(context)
                    displayManager.detectDisplayType()
                    displayManager.detectFoldState()
                }
            }
            Intent.ACTION_DEVICE_STORAGE_LOW -> {
                Log.w("ConfigurationReceiver", "Low storage space")
            }
        }
    }
}
