package com.android.launcher.foldable

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages display detection for foldable devices (Moto Fold)
 * Automatically detects which screen is in use: outer (cover) or inner (foldable)
 */
class FoldableDisplayManager(private val context: Context) {
    
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    
    private val _currentDisplayType = MutableStateFlow<DisplayType>(DisplayType.UNKNOWN)
    val currentDisplayType: StateFlow<DisplayType> = _currentDisplayType
    
    private val _isFolded = MutableStateFlow<Boolean?>(null)
    val isFolded: StateFlow<Boolean?> = _isFolded
    
    enum class DisplayType {
        OUTER,      // Cover screen (when folded or closed)
        INNER,      // Main foldable screen (when open)
        UNKNOWN
    }
    
    /**
     * Initialize display detection based on device capabilities
     */
    fun initialize() {
        detectDisplayType()
        detectFoldState()
    }
    
    /**
     * Detect which screen is currently active
     */
    fun detectDisplayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displays = displayManager.displays
            
            when {
                displays.size >= 2 -> {
                    // Device has multiple displays (foldable)
                    val primaryDisplay = displays[0]
                    
                    // Check display size to determine if outer or inner screen
                    val isSmallScreen = primaryDisplay.width < 1000 || primaryDisplay.height < 1000
                    
                    _currentDisplayType.value = if (isSmallScreen) {
                        DisplayType.OUTER
                    } else {
                        DisplayType.INNER
                    }
                }
                displays.size == 1 -> {
                    // Single display device - treat as inner screen
                    _currentDisplayType.value = DisplayType.INNER
                }
                else -> {
                    _currentDisplayType.value = DisplayType.UNKNOWN
                }
            }
        }
    }
    
    /**
     * Detect fold state using device configuration
     */
    private fun detectFoldState() {
        try {
            val foldStateClass = Class.forName("android.os.FoldState")
            val getStateMethod = foldStateClass.getMethod("getState")
            
            // State: 0 = folded, 1 = unfolded
            val state = getStateMethod.invoke(null) as Int
            _isFolded.value = (state == 0)
        } catch (e: Exception) {
            // Fallback: assume unfolded
            _isFolded.value = false
        }
    }
    
    /**
     * Check if device has foldable support
     */
    fun hasFoldableDisplay(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displays = displayManager.displays
            return displays.size >= 2
        }
        return false
    }
    
    /**
     * Get display metrics for current active screen
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getActiveDisplayMetrics(): DisplayMetrics {
        val displays = displayManager.displays
        
        return if (displays.isNotEmpty()) {
            val display = displays[0]
            DisplayMetrics(
                width = display.width,
                height = display.height,
                density = display.density,
                refreshRate = display.refreshRate
            )
        } else {
            DisplayMetrics(0, 0, 1f, 60f)
        }
    }
    
    data class DisplayMetrics(
        val width: Int,
        val height: Int,
        val density: Float,
        val refreshRate: Float
    )
}
