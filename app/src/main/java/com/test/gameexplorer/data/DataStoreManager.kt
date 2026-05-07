package com.test.gameexplorer.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { _ ->
        listOf(
            object : DataMigration<Preferences> {
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    val map = currentData.asMap()
                    val key = map.keys.find { it.name == "selected_genres" }
                    return map[key] is String
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    val mutablePrefs = currentData.toMutablePreferences()
                    val stringKey = stringPreferencesKey("selected_genres")
                    val stringValue = currentData[stringKey]
                    if (stringValue != null) {
                        val set = stringValue.split(",").filter { it.isNotBlank() }.toSet()
                        mutablePrefs[stringSetPreferencesKey("selected_genres")] = set
                    }
                    return mutablePrefs
                }

                override suspend fun cleanUp() {}
            }
        )
    }
)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SELECTED_GENRES = stringSetPreferencesKey("selected_genres")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val selectedGenres: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_GENRES] ?: emptySet()
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    suspend fun saveSelectedGenres(genres: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_GENRES] = genres
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
