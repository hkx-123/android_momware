package com.hkx.momware.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single playlist entry in the database.
 */
@Entity(tableName = "playlist")
data class PlaylistEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val videoId: String,
    val link: String,
    val position: Int = 0,
    val bookmarked: Boolean = false,
    val declined: Boolean = false,
    val initializedSuccessful: Boolean = false,
    val flags: String?

)
