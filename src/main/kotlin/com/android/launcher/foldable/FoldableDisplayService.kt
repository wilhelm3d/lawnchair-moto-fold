package com.android.launcher.foldable

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Background service that monitors fold state continuously
 * Ensures launcher settings are applied even when app is backgrounded
 */
class FoldableDisplayService : Service() {
    
    private lateinit var displayManager: FoldableDisplayManager
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        displayManager = FoldableDisplayManager(this)
        displayManager.initialize()
        
        startFoldStateMonitoring()
    }
    
    /**
     * Start continuous monitoring of fold state
     */
    private fun startFoldStateMonitoring() {
        serviceScope.launch {
            displayManager.isFolded.collect { isFolded ->
                if (isFolded != null) {
                    Log.d("FoldableDisplayService", "Fold state: ${if (isFolded) "FOLDED" else "OPEN"}")
                    // Notify launcher to update settings
                    notifyFoldStateChanged(isFolded)
                }
            }
        }
    }
    
    /**
     * Notify launcher of fold state change
     */
    private fun notifyFoldStateChanged(isFolded: Boolean) {
        val intent = Intent(ACTION_FOLD_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_FOLDED, isFolded)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    companion object {
        const val ACTION_FOLD_STATE_CHANGED = "com.android.launcher.foldable.FOLD_STATE_CHANGED"
        const val EXTRA_IS_FOLDED = "is_folded"
    }
}
