package com.android.launcher.foldchair

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.window.FoldingFeature
import androidx.annotation.RequiresApi
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

/**
 * Manages display detection for foldable devices (Moto Fold)
 * Uses Android 17 (API 35) Window Manager API for modern foldable support
 */
class FoldableDisplayManager(private val context: Context) {
    
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
    
    private val _currentDisplayType = MutableStateFlow<DisplayType>(DisplayType.UNKNOWN)
    val currentDisplayType: StateFlow<DisplayType> = _currentDisplayType
    
    private val _isFolded = MutableStateFlow<Boolean?>(null)
    val isFolded: StateFlow<Boolean?> = _isFolded
    
    private val _windowWidthSizeClass = MutableStateFlow<WindowWidthSizeClass?>(null)
    val windowWidthSizeClass: StateFlow<WindowWidthSizeClass?> = _windowWidthSizeClass
    
    private val _windowHeightSizeClass = MutableStateFlow<WindowHeightSizeClass?>(null)
    val windowHeightSizeClass: StateFlow<WindowHeightSizeClass?> = _windowHeightSizeClass
    
    enum class DisplayType {
        OUTER,      // Cover screen (when folded or closed)
        INNER,      // Main foldable screen (when open)
        UNKNOWN
    }
    
    /**
     * Initialize display detection using modern Android 17 APIs
     */
    suspend fun initialize() {
        detectDisplayTypeModern()
        detectFoldStateModern()
        detectWindowSizeClasses()
    }
    
    /**
     * Detect which screen is currently active using Android 17 Window Manager API
     */
    private suspend fun detectDisplayTypeModern() {
        try {
            windowInfoTracker.windowLayoutInfo(context).collect { layoutInfo ->
                val hasFoldingFeature = layoutInfo.displayFeatures.any { feature ->
                    feature is FoldingFeature
                }
                
                if (hasFoldingFeature) {
                    val foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                    
                    if (foldingFeature != null) {
                        _currentDisplayType.value = when (foldingFeature.orientation) {
                            FoldingFeature.Orientation.HORIZONTAL -> {
                                // Horizontal fold - screen width matters
                                if (context.resources.displayMetrics.widthPixels < 1000) {
                                    DisplayType.OUTER
                                } else {
                                    DisplayType.INNER
                                }
                            }
                            FoldingFeature.Orientation.VERTICAL -> {
                                // Vertical fold - screen height matters
                                if (context.resources.displayMetrics.heightPixels < 1000) {
                                    DisplayType.OUTER
                                } else {
                                    DisplayType.INNER
                                }
                            }
                            else -> DisplayType.INNER
                        }
                    }
                } else {
                    _currentDisplayType.value = DisplayType.INNER
                }
            }
        } catch (e: Exception) {
            _currentDisplayType.value = DisplayType.INNER
        }
    }
    
    /**
     * Detect fold state using modern Android 17 API
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun detectFoldStateModern() {
        try {
            windowInfoTracker.windowLayoutInfo(context).collect { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
                
                if (foldingFeature != null) {
                    _isFolded.value = foldingFeature.state == FoldingFeature.State.HALF_OPENED
                } else {
                    _isFolded.value = false
                }
            }
        } catch (e: Exception) {
            _isFolded.value = false
        }
    }
    
    /**
     * Detect window size classes (COMPACT, MEDIUM, EXPANDED)
     */
    private suspend fun detectWindowSizeClasses() {
        try {
            windowInfoTracker.windowLayoutInfo(context).collect { layoutInfo ->
                val bounds = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
                    ?.bounds
                
                // Width size class
                val displayWidth = context.resources.displayMetrics.widthPixels
                _windowWidthSizeClass.value = when {
                    displayWidth < 600 -> WindowWidthSizeClass.COMPACT
                    displayWidth < 840 -> WindowWidthSizeClass.MEDIUM
                    else -> WindowWidthSizeClass.EXPANDED
                }
                
                // Height size class
                val displayHeight = context.resources.displayMetrics.heightPixels
                _windowHeightSizeClass.value = when {
                    displayHeight < 480 -> WindowHeightSizeClass.COMPACT
                    displayHeight < 900 -> WindowHeightSizeClass.MEDIUM
                    else -> WindowHeightSizeClass.EXPANDED
                }
            }
        } catch (e: Exception) {
            // Fallback to default
            _windowWidthSizeClass.value = WindowWidthSizeClass.EXPANDED
            _windowHeightSizeClass.value = WindowHeightSizeClass.EXPANDED
        }
    }
    
    /**
     * Check if device has foldable support
     */
    fun hasFoldableDisplay(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displays = displayManager.displays
            displays.size >= 2
        } else {
            false
        }
    }
    
    /**
     * Get display metrics for current active screen
     */
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
    
    /**
     * Get current folding feature state
     */
    suspend fun getFoldingFeature(): FoldingFeature? {
        return try {
            var feature: FoldingFeature? = null
            windowInfoTracker.windowLayoutInfo(context).collect { layoutInfo ->
                feature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
            }
            feature
        } catch (e: Exception) {
            null
        }
    }
    
    data class DisplayMetrics(
        val width: Int,
        val height: Int,
        val density: Float,
        val refreshRate: Float
    )
}
