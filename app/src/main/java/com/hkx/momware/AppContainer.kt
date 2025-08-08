package com.hkx.momware

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hkx.momware.data.db.PlaylistDatabase
import com.hkx.momware.data.repository.DatabaseRepository
import com.hkx.momware.data.repository.UserPreferencesRepository


interface AppContainerInterface {
    val databaseRepository: DatabaseRepository
    var userPreferencesRepository: UserPreferencesRepository
}


class AppContainerInstance : Application() {

    /**
     * AppContainer instance to obtain dependencies
     */
    lateinit var container: AppContainerInterface

    override fun onCreate() {
        super.onCreate()

        container = AppContainerImpl(this)
    }

}


/**
 * [AppContainerInstance] implementation that provides instances of repositories
 */
class AppContainerImpl(context: Context) : AppContainerInterface {

    companion object {
        private const val MOMWARE_PREFERENCE_NAME = "momware_preferences"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = MOMWARE_PREFERENCE_NAME
        )
    }

    override val databaseRepository: DatabaseRepository by lazy {
        DatabaseRepository(
            PlaylistDatabase.getDatabase(context).playlistEntityDao()
        )
    }

    override var userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepository(context.dataStore)

}
