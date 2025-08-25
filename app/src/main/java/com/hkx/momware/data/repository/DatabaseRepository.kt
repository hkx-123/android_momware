package com.hkx.momware.data.repository

import com.hkx.momware.data.db.PlaylistEntity
import com.hkx.momware.data.db.PlaylistEntityDao
import kotlinx.coroutines.flow.Flow


/**
 * Repository that provides insert, update, delete, and retrieve of [PlaylistEntity] from the database.
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
     * Retrieve all playlist entities
     */
    fun getAllPlaylistItems(): List<PlaylistEntity> = playlistEntityDao.getAllPlaylistItems()

    /**
     * Retrieve all playlist entities as Flow
     */
    fun getAllPlaylistItemsStream(): Flow<List<PlaylistEntity>> = playlistEntityDao.getAllPlaylistItemsStream()

}
