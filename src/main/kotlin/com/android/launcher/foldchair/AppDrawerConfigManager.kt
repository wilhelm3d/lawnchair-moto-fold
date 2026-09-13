package com.android.launcher.foldchair

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages app drawer configuration for Foldchair
 * Handles separate drawer settings for outer and inner screens
 */
class AppDrawerConfigManager(
    private val context: Context,
    private val displayManager: FoldableDisplayManager,
    private val preferencesManager: FoldablePreferencesManager
) {
    
    data class DrawerConfig(
        val columns: Int,
        val rows: Int,
        val iconSize: Float,
        val enableAnimation: Boolean,
        val showSearch: Boolean,
        val animationDuration: Long,
        val activeScreen: FoldableDisplayManager.DisplayType
    )
    
    /**
     * Get current drawer configuration based on active screen
     */
    fun getCurrentDrawerConfig(): DrawerConfig {
        val displayType = displayManager.currentDisplayType.value
        
        val scope = when (displayType) {
            FoldableDisplayManager.DisplayType.OUTER -> FoldablePreferencesManager.PreferenceScope.Outer
            FoldableDisplayManager.DisplayType.INNER -> FoldablePreferencesManager.PreferenceScope.Inner
            FoldableDisplayManager.DisplayType.UNKNOWN -> FoldablePreferencesManager.PreferenceScope.Global
        }
        
        return DrawerConfig(
            columns = preferencesManager.getInt("drawer_columns", 5, scope),
            rows = preferencesManager.getInt("drawer_rows", 5, scope),
            iconSize = preferencesManager.getFloat("drawer_icon_size", 1.0f, scope),
            enableAnimation = preferencesManager.getBoolean("drawer_animation", true, scope),
            showSearch = preferencesManager.getBoolean("drawer_search", true, scope),
            animationDuration = if (preferencesManager.getBoolean("drawer_animation", true, scope)) 300L else 0L,
            activeScreen = displayType
        )
    }
    
    /**
     * Save drawer configuration for specific screen
     */
    fun saveDrawerConfig(config: DrawerConfig, scope: FoldablePreferencesManager.PreferenceScope) {
        preferencesManager.putInt("drawer_columns", config.columns, scope)
        preferencesManager.putInt("drawer_rows", config.rows, scope)
        preferencesManager.putFloat("drawer_icon_size", config.iconSize, scope)
        preferencesManager.putBoolean("drawer_animation", config.enableAnimation, scope)
        preferencesManager.putBoolean("drawer_search", config.showSearch, scope)
    }
    
    /**
     * Get default drawer configuration for outer screen (compact)
     */
    fun getDefaultOuterDrawerConfig(): DrawerConfig {
        return DrawerConfig(
            columns = 3,
            rows = 4,
            iconSize = 0.9f,
            enableAnimation = true,
            showSearch = false,
            animationDuration = 300L,
            activeScreen = FoldableDisplayManager.DisplayType.OUTER
        )
    }
    
    /**
     * Get default drawer configuration for inner screen (spacious)
     */
    fun getDefaultInnerDrawerConfig(): DrawerConfig {
        return DrawerConfig(
            columns = 5,
            rows = 5,
            iconSize = 1.0f,
            enableAnimation = true,
            showSearch = true,
            animationDuration = 300L,
            activeScreen = FoldableDisplayManager.DisplayType.INNER
        )
    }
    
    /**
     * Initialize default drawer settings if not present
     */
    fun initializeDefaultDrawerSettings() {
        if (displayManager.hasFoldableDisplay()) {
            // Setup outer screen drawer defaults
            val outerConfig = getDefaultOuterDrawerConfig()
            if (preferencesManager.getInt("drawer_columns", 0, FoldablePreferencesManager.PreferenceScope.Outer) == 0) {
                saveDrawerConfig(outerConfig, FoldablePreferencesManager.PreferenceScope.Outer)
            }
            
            // Setup inner screen drawer defaults
            val innerConfig = getDefaultInnerDrawerConfig()
            if (preferencesManager.getInt("drawer_columns", 0, FoldablePreferencesManager.PreferenceScope.Inner) == 0) {
                saveDrawerConfig(innerConfig, FoldablePreferencesManager.PreferenceScope.Inner)
            }
        }
    }
}
