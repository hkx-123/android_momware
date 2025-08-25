package com.hkx.momware.component

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hkx.momware.AppContainerInstance
import com.hkx.momware.api.YouTubeVideoInfosApi
import com.hkx.momware.data.db.PlaylistEntity
import com.hkx.momware.data.repository.DatabaseRepository
import com.hkx.momware.data.repository.UserPreferencesRepository
import com.hkx.momware.ui.state.DatabaseState
import com.hkx.momware.ui.state.PlaylistState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.collections.forEach
import kotlin.math.min

private const val TAG = "AppViewModel"

class AppViewModel(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val dataStoreRepository: UserPreferencesRepository
) : ViewModel() {

    private val _newPlaylistEntities: SnapshotStateList<PlaylistEntity> = mutableStateListOf()
    val newPlaylistEntities: List<PlaylistEntity> = _newPlaylistEntities

    var videoIdToPlay: String = "_k-5U7IeK8g"

    val pathToFile = dataStoreRepository.pathToFile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    val dbState : StateFlow<DatabaseState> = databaseRepository.getAllPlaylistItemsStream()
        .map { DatabaseState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DatabaseState()
        )

    private val _playlistState = MutableStateFlow(PlaylistState())
    val playlistState: StateFlow<PlaylistState> = _playlistState


    fun updateCurrentPlaylist(entities: List<PlaylistEntity>) {
        _playlistState.update {
            it.copy(currentPlaylist = entities)
        }
    }

    fun initPlaylist(pathToFile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileEntries = loadItemsFromFile(pathToFile)
            saveNewItemsIntoDB(fileEntries)
            updateFailedItemsInDB()
        }
    }


    fun readFile(currentContext: Context, filePath: String?) : List<String> {
        synchronized(this) {
            val fileData: String? = filePath?.run { readFileFromSDCard(currentContext, filePath) }
            val fileEntries: MutableList<String> = mutableListOf()
            fileData?.also { data -> fileEntries.addAll(data.lines()) }

            return fileEntries
        }
    }

    fun readFileFromSDCard(context: Context, filePath: String): String {
        val filePathUri = Uri.parse(filePath)
        val fileContent = context.contentResolver.openInputStream(filePathUri)?.bufferedReader().use { it?.readText() } ?: ""

        Log.d( TAG, "Content of File is: ${fileContent.substring(0, min(100, fileContent.length))}")

        return fileContent
    }


    fun loadItemsFromFile(pathToFile: String): List<String> {
        Log.d( TAG, "pathToFile: ${pathToFile}")

        try {
            val readLines = readFile(context, pathToFile).filter {
                it.isNotEmpty() && it.startsWith("https://www.youtube.com/watch?v=")
            }.also { Log.d( TAG, "content of read file: ${it}") }

            return readLines
        } catch (e : Exception) {
            Log.e( TAG, "Error reading data of ${pathToFile}: ", e)
        }

        // else add a default video
        return listOf("https://www.youtube.com/watch?v=bv_cEeDlop0")
    }

    private fun saveNewItemsIntoDB(fileEntries: List<String>) {
        val allDbItems = databaseRepository.getAllPlaylistItems()
        val linksOfDBItems = allDbItems.map { it.link }

        // get new items from file as entities
        var filteredFileEntries = fileEntries.filter { !linksOfDBItems.contains(it) }.map { createPlaylistEntityFromUrl(it) }
        _newPlaylistEntities.addAll(filteredFileEntries)

        // store new items to DB
        _newPlaylistEntities.forEach { savePlaylistEntity(it) }
    }

    suspend fun updateFailedItemsInDB() {
        val allDbItems = databaseRepository.getAllPlaylistItems()
        val failedPlaylistEntities = allDbItems.filter { !it.initializedSuccessful }

        failedPlaylistEntities.forEach { playlistEntity ->
            val videoTitle = getVideoTitleFromVideoId(playlistEntity.videoId)
            if (!videoTitle.startsWith("Error: ")) {
                databaseRepository.updatePlaylistItem(playlistEntity.copy(title = videoTitle, initializedSuccessful  = true))
            }
        }
    }



    // for the sake of simplicity, I omitted parsing between PlaylistEntity <-> PlaylistUiData
    /**
     * Save the playlist entity in the [DatabaseRepository]'s data source
     */
    fun savePlaylistEntity(newPlaylistEntity : PlaylistEntity) {
        viewModelScope.launch {
            databaseRepository.insertPlaylistItem(newPlaylistEntity)
        }
    }

    /**
     * Update the playlist entity in the [DatabaseRepository]'s data source
     */
    fun updatePlaylistEntity(updatedPlaylistEntity : PlaylistEntity) {
        viewModelScope.launch {
            databaseRepository.updatePlaylistItem(updatedPlaylistEntity)
        }
    }

    /**
     * Update the playlist entity in the [DatabaseRepository]'s data source
     */
    fun deletePlaylistEntity(playlistEntity : PlaylistEntity) {
        viewModelScope.launch {
            databaseRepository.deletePlaylistItem(playlistEntity)
        }
    }


    /**
     * Save the new path to the [UserPreferencesRepository]'s DataStore
     */
    fun setFilePathIntoPreference(newValue: String) {
        viewModelScope.launch {
            dataStoreRepository.savePathToFile(newValue)
        }
    }


    /**
     * Factory for [AppViewModel]
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                momWareApplication()
                val containerDatabaseRepository = momWareApplication().container.databaseRepository
                val containerDataStoreRepository = momWareApplication().container.userPreferencesRepository

                AppViewModel(
                    momWareApplication(),
                    databaseRepository = containerDatabaseRepository,
                    dataStoreRepository = containerDataStoreRepository
                )
            }
        }
    }
}


fun createPlaylistEntityFromUrl(url : String): PlaylistEntity {
    val videoId = url.substringAfter("watch?v=")    // gets the id from URL
    val videoTitle = getVideoTitleFromVideoId(videoId)
    return PlaylistEntity(title = videoTitle, videoId = videoId, link = url,
        initializedSuccessful = !videoTitle.startsWith("Error: "), flags = null)
}

fun getVideoTitleFromVideoId(url : String): String {
    try {
        return runBlocking { YouTubeVideoInfosApi.getVideoInfo(url) }.title
    } catch (e: Exception) {
        Log.e( TAG, "Error retrieving title from YouTube API for ${url}", e)
        return "Error: ${e.message}"
    }
}


/**
 * Extension function to queries for [Application] object and returns an instance of
 * [AppContainerInstance].
 */
fun CreationExtras.momWareApplication(): AppContainerInstance =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AppContainerInstance)
