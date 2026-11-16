package com.example.studmate.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.studmate.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "studmate_prefs")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
)

class SessionStore(private val context: Context) {
    private object Keys {
        val userId = stringPreferencesKey("current_user_id")
        val organizerMode = booleanPreferencesKey("organizer_mode")
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val radarLocal = booleanPreferencesKey("radar_local")
    }

    val currentUserId: Flow<String?> = context.dataStore.data.map { it[Keys.userId] }
    val organizerMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.organizerMode] ?: false }
    val radarLocal: Flow<Boolean> = context.dataStore.data.map { it[Keys.radarLocal] ?: false }
    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.themeMode]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.dynamicColor] ?: false,
        )
    }

    suspend fun currentUserIdNow(): String? = currentUserId.first()

    suspend fun setCurrentUser(id: String?) = context.dataStore.edit {
        if (id == null) {
            it.remove(Keys.userId)
            it[Keys.organizerMode] = false
        } else it[Keys.userId] = id
    }

    suspend fun setOrganizerMode(enabled: Boolean) = context.dataStore.edit { it[Keys.organizerMode] = enabled }
    suspend fun setRadarLocal(local: Boolean) = context.dataStore.edit { it[Keys.radarLocal] = local }
    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.themeMode] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.dynamicColor] = enabled }
}
