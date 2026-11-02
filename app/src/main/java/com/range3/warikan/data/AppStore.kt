package com.range3.warikan.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.range3.warikan.domain.Bill
import com.range3.warikan.domain.Preset
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 端末に保存するアプリの状態。前回の入力とグループのプリセット。 */
@Serializable
data class SavedState(
    val bill: Bill,
    val presets: List<Preset>,
)

interface AppStore {
    suspend fun load(): SavedState?
    suspend fun save(state: SavedState)
}

private val Context.dataStore by preferencesDataStore(name = "warikan")

class DataStoreAppStore(context: Context) : AppStore {
    private val context = context.applicationContext
    private val key = stringPreferencesKey("state")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): SavedState? {
        val text = context.dataStore.data.first()[key] ?: return null
        // 形式が変わって読めなくなった場合は初期状態から始める
        return runCatching { json.decodeFromString<SavedState>(text) }.getOrNull()
    }

    override suspend fun save(state: SavedState) {
        context.dataStore.edit { it[key] = json.encodeToString(state) }
    }
}
