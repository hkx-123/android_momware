package com.hkx.momware.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


private const val TAG = "UserPreferencesRepo"

/**
 * Repository that provides access to [DataStore].
 */
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val PATH_TO_FILE = stringPreferencesKey("path_to_file")
    }

    val pathToFile: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[PATH_TO_FILE]
        }

    suspend fun savePathToFile(pathToFile: String) {
        dataStore.edit { preferences ->
            preferences[PATH_TO_FILE] = pathToFile
        }
    }

}
