package com.hkx.momware.data.repository

import com.hkx.momware.data.db.PlaylistEntity
import com.hkx.momware.data.db.PlaylistEntityDao
import kotlinx.coroutines.flow.Flow


/**
 * Repository that provides insert, update, delete, and retrieve of [PlaylistEntity] from a given data source.
 */
class DatabaseRepository(
    private val playlistEntityDao: PlaylistEntityDao
) {

    /**
     * Insert playlist entity
     */
    suspend fun insertPlaylistItem(playlistEntity: PlaylistEntity) = playlistEntityDao.insert(playlistEntity)

    /**
     * Update playlist entity
     */
    suspend fun updatePlaylistItem(playlistEntity: PlaylistEntity) = playlistEntityDao.update(playlistEntity)

    /**
     * Delete playlist entity
     */
    suspend fun deletePlaylistItem(playlistEntity: PlaylistEntity) = playlistEntityDao.delete(playlistEntity)

    /**
     * Retrieve a playlist entity that matches with the [videoId].
     */
    fun getPlaylistItemByVideoIdStream(videoId: String): Flow<PlaylistEntity?> = playlistEntityDao.getPlaylistItemByVideoId(videoId)

    /**
     * Retrieve all playlist entities
     */
    fun getAllPlaylistItemsStream(): Flow<List<PlaylistEntity>> = playlistEntityDao.getAllPlaylistItems()

}
