package com.hkx.momware.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var dataStore: DataStore<Preferences>
    private val testContext: Context = ApplicationProvider.getApplicationContext()

    private val testCoroutineDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testCoroutineDispatcher + Job())

    @Before
    fun setup() {
        // Create an in-memory DataStore
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testContext.preferencesDataStoreFile("test_preferences") },
        )

        userPreferencesRepository = UserPreferencesRepository(dataStore)

        // Set the main dispatcher for coroutines testing
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun savePathToFile() = testScope.runTest {
        val testPath = "test/path/to/file"
        userPreferencesRepository.savePathToFile(testPath)
        val savedPath = userPreferencesRepository.pathToFile.first()

        assertEquals(testPath, savedPath)
    }

    @Test
    fun pathToFileReturnsNull() = testScope.runTest {
        val savedPath = userPreferencesRepository.pathToFile.first()
        assertEquals(null, savedPath)
    }

    @Test
    fun pathToFileHandlesIOExceptionAndReturnsNull() = testScope.runTest {
        // Create a DataStore that throws an IOException when reading
        val failingDataStore = FailingDataStore()
        val failingRepository = UserPreferencesRepository(failingDataStore)

        val savedPath = failingRepository.pathToFile.first()
        assertEquals(null, savedPath)
    }

    // Mock DataStore that throws IOException for testing error handling
    class FailingDataStore : DataStore<Preferences> {
        override val data : kotlinx.coroutines.flow.Flow<Preferences> = kotlinx.coroutines.flow.flow {
            throw IOException("Simulated IO Exception")
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            throw IOException("Simulated IO Exception")
        }
    }

}