package com.hkx.momware.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PlaylistDatabaseTest {

    private lateinit var playlistEntityDao: PlaylistEntityDao
    private lateinit var db: PlaylistDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(  // creates in-memory DB
            context,
            PlaylistDatabase::class.java
        ).build()
        playlistEntityDao = db.playlistEntityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun createSomeEntity(videoId: String) : PlaylistEntity {
        return PlaylistEntity(
            title = "Coldplay - The Scientist",
            videoId = videoId,
            link = "https://anyUrl/watch?v=any11Chars.",
            initializedSuccessful = true,
            flags = null
        )
    }

    @Test
    fun writePlaylistEntityAndReadInList() =
        runTest {
            val targetVideoId = "any11Chars."
            val testEntity = createSomeEntity(targetVideoId)
            playlistEntityDao.insert(testEntity)

            val allEntities = playlistEntityDao.getAllPlaylistItems().first()

            assertEquals(targetVideoId, allEntities.single().videoId)
        }

    @Test
    fun updatePlaylistEntity() =
        runTest {
            val initialVideoId = "any11Chars."
            val testEntity = createSomeEntity(initialVideoId)
            playlistEntityDao.insert(testEntity)

            val updateVideoId = "someVideoId"
            playlistEntityDao.update(playlistEntityDao.getPlaylistItemByVideoId(initialVideoId).first()
                .copy(videoId = updateVideoId))

            assertNotNull("Updated entity could not be found", playlistEntityDao.getPlaylistItemByVideoId(updateVideoId).first())
        }

    @Test
    fun deletePlaylistEntity() =
        runTest {
            val targetVideoId = "any11Chars."
            val testEntity = createSomeEntity(targetVideoId)
            playlistEntityDao.insert(testEntity)

            playlistEntityDao.delete(playlistEntityDao.getPlaylistItemByVideoId(targetVideoId).first())

            assertNull("Deleted entity still exists", playlistEntityDao.getPlaylistItemByVideoId(targetVideoId).first())
        }

}
