package com.android.launcher.foldchair

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Modern DataStore-based preferences manager for Android 17
 * Replaces SharedPreferences with improved type safety and async support
 */
class FoldablePreferencesManager(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foldchair_prefs")
    }
    
    private val dataStore = context.dataStore
    
    sealed class PreferenceScope {
        object Outer : PreferenceScope()
        object Inner : PreferenceScope()
        object Global : PreferenceScope()
    }
    
    /**
     * Get preference key with screen scope prefix
     */
    private fun getScopedKey(key: String, scope: PreferenceScope): String {
        return when (scope) {
            PreferenceScope.Outer -> "outer_screen_$key"
            PreferenceScope.Inner -> "inner_screen_$key"
            PreferenceScope.Global -> key
        }
    }
    
    // String Preferences (with Flow for reactive updates)
    fun getString(key: String, defaultValue: String, scope: PreferenceScope = PreferenceScope.Global): Flow<String> {
        val prefKey = stringPreferencesKey(getScopedKey(key, scope))
        return dataStore.data.map { prefs -> prefs[prefKey] ?: defaultValue }
    }
    
    suspend fun putString(key: String, value: String, scope: PreferenceScope = PreferenceScope.Global) {
        val prefKey = stringPreferencesKey(getScopedKey(key, scope))
        dataStore.edit { prefs -> prefs[prefKey] = value }
    }
    
    // Int Preferences
    fun getInt(key: String, defaultValue: Int, scope: PreferenceScope = PreferenceScope.Global): Flow<Int> {
        val prefKey = intPreferencesKey(getScopedKey(key, scope))
        return dataStore.data.map { prefs -> prefs[prefKey] ?: defaultValue }
    }
    
    suspend fun putInt(key: String, value: Int, scope: PreferenceScope = PreferenceScope.Global) {
        val prefKey = intPreferencesKey(getScopedKey(key, scope))
        dataStore.edit { prefs -> prefs[prefKey] = value }
    }
    
    // Boolean Preferences
    fun getBoolean(key: String, defaultValue: Boolean, scope: PreferenceScope = PreferenceScope.Global): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(getScopedKey(key, scope))
        return dataStore.data.map { prefs -> prefs[prefKey] ?: defaultValue }
    }
    
    suspend fun putBoolean(key: String, value: Boolean, scope: PreferenceScope = PreferenceScope.Global) {
        val prefKey = booleanPreferencesKey(getScopedKey(key, scope))
        dataStore.edit { prefs -> prefs[prefKey] = value }
    }
    
    // Float Preferences
    fun getFloat(key: String, defaultValue: Float, scope: PreferenceScope = PreferenceScope.Global): Flow<Float> {
        val prefKey = floatPreferencesKey(getScopedKey(key, scope))
        return dataStore.data.map { prefs -> prefs[prefKey] ?: defaultValue }
    }
    
    suspend fun putFloat(key: String, value: Float, scope: PreferenceScope = PreferenceScope.Global) {
        val prefKey = floatPreferencesKey(getScopedKey(key, scope))
        dataStore.edit { prefs -> prefs[prefKey] = value }
    }
    
    // Launcher Grid Settings
    fun getGridColumns(scope: PreferenceScope = PreferenceScope.Global): Flow<Int> {
        return getInt("grid_columns", 5, scope)
    }
    
    suspend fun putGridColumns(columns: Int, scope: PreferenceScope = PreferenceScope.Global) {
        putInt("grid_columns", columns, scope)
    }
    
    fun getGridRows(scope: PreferenceScope = PreferenceScope.Global): Flow<Int> {
        return getInt("grid_rows", 5, scope)
    }
    
    suspend fun putGridRows(rows: Int, scope: PreferenceScope = PreferenceScope.Global) {
        putInt("grid_rows", rows, scope)
    }
    
    // Icon Size Settings
    fun getIconSize(scope: PreferenceScope = PreferenceScope.Global): Flow<Float> {
        return getFloat("icon_size", 1.0f, scope)
    }
    
    suspend fun putIconSize(size: Float, scope: PreferenceScope = PreferenceScope.Global) {
        putFloat("icon_size", size, scope)
    }
    
    // Show/Hide Dock
    fun isShowDock(scope: PreferenceScope = PreferenceScope.Global): Flow<Boolean> {
        return getBoolean("show_dock", true, scope)
    }
    
    suspend fun putShowDock(show: Boolean, scope: PreferenceScope = PreferenceScope.Global) {
        putBoolean("show_dock", show, scope)
    }
    
    // Theme Settings
    fun getTheme(scope: PreferenceScope = PreferenceScope.Global): Flow<String> {
        return getString("theme", "default", scope)
    }
    
    suspend fun putTheme(theme: String, scope: PreferenceScope = PreferenceScope.Global) {
        putString("theme", theme, scope)
    }
    
    // Wallpaper Settings
    fun getWallpaper(scope: PreferenceScope = PreferenceScope.Global): Flow<String> {
        return getString("wallpaper", "", scope)
    }
    
    suspend fun putWallpaper(wallpaper: String, scope: PreferenceScope = PreferenceScope.Global) {
        putString("wallpaper", wallpaper, scope)
    }
    
    // Copy settings from one scope to another
    suspend fun copySettings(fromScope: PreferenceScope, toScope: PreferenceScope) {
        dataStore.edit { prefs ->
            val prefix = when (fromScope) {
                PreferenceScope.Outer -> "outer_screen_"
                PreferenceScope.Inner -> "inner_screen_"
                PreferenceScope.Global -> ""
            }
            
            val toPrefix = when (toScope) {
                PreferenceScope.Outer -> "outer_screen_"
                PreferenceScope.Inner -> "inner_screen_"
                PreferenceScope.Global -> ""
            }
            
            prefs.asMap().forEach { (key, value) ->
                if (prefix.isEmpty() || key.name.startsWith(prefix)) {
                    val cleanKey = key.name.replace(prefix, "")
                    val newKeyName = toPrefix + cleanKey
                    
                    // Copy value to new key
                    when (value) {
                        is String -> prefs[stringPreferencesKey(newKeyName)] = value
                        is Int -> prefs[intPreferencesKey(newKeyName)] = value
                        is Boolean -> prefs[booleanPreferencesKey(newKeyName)] = value
                        is Float -> prefs[floatPreferencesKey(newKeyName)] = value
                    }
                }
            }
        }
    }
    
    // Clear all settings
    suspend fun clearAll() {
        dataStore.edit { prefs -> prefs.clear() }
    }
}
