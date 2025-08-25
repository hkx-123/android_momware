package com.hkx.momware.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room generates all the necessary code to perform database operations
 */
@Dao
interface PlaylistEntityDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(playlistEntity: PlaylistEntity)

    @Update
    suspend fun update(playlistEntity: PlaylistEntity)

    @Delete
    suspend fun delete(playlistEntity: PlaylistEntity)

    @Query("SELECT * from playlist WHERE videoId = :videoId")
    fun getPlaylistItemByVideoId(videoId: String): Flow<PlaylistEntity>

    @Query("SELECT * from playlist ORDER BY title ASC")
    fun getAllPlaylistItems(): List<PlaylistEntity>

    @Query("SELECT * from playlist ORDER BY title ASC")
    fun getAllPlaylistItemsStream(): Flow<List<PlaylistEntity>>

}
