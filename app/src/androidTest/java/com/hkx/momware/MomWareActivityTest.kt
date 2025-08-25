package com.hkx.momware

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MomWareActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    /**
     * Test for the basic elements on Home screen
     */
    @Test
    fun whenStartApp_thenOnHomeScreenWithAllBasicElements() {

        // Check if NavigationBarItem 'Home' exists and is selected
        composeTestRule
            .onNode(hasText("起始页")).assertExists()
            .assertIsSelected()

        // Check if AndroidView for YT Player exists
        composeTestRule
            .onNode(hasTestTag("AndroidYTView"))
            .assertExists()

        // Check if Playlist LazyRow exists
        composeTestRule
            .onNode(hasTestTag("PlaylistRow"))
            .assertExists()

        // Check if 'Start video' Button exists and is clickable
        // (It can be extended to all UI interaction buttons using semantics tag to group them, but that seems a bit too restrictive to me)
        composeTestRule
            .onNode(hasContentDescription("Start video"))
            .assertExists()
            .assert(hasClickAction())

    }

    /**
     * Test for the basic elements on Playlist screen
     */
    @Test
    fun givenStartedApp_whenSwitchToPlaylistScreen_thenOnPlaylistScreenWithAllBasicElements() {

        // Check if NavigationBarItem 'Playlist' exists and is selected  (I trust on Compose, that 'Home' is deselected then)
        composeTestRule
            .onNode(hasText("播放列表")).assertExists()
            .performClick()
            .assertIsSelected()

        // Check if AndroidView for YT Player exists
        composeTestRule
            .onNode(hasTestTag("AndroidYTView"))
            .assertDoesNotExist()

        // Check if 'There is no video' Text is shown
        composeTestRule
            .onNode(hasText("没有视频"))
            .assertExists()

        // Check if 'Add playlist file' Button exists and is clickable
        composeTestRule
            .onNode(hasContentDescription("Add playlist file", substring = true))
            .assertExists()
            .assert(hasClickAction())

    }

}
