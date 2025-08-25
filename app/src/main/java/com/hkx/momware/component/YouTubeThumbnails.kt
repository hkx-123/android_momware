package com.hkx.momware.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private const val TAG = "YouTubeThumbnails"

const val THUMBNAIL_URL = "https://img.youtube.com/vi/%videoId%/hqdefault.jpg"

@Composable
fun YouTubeThumbnails(
    appViewModel: AppViewModel,
    onClick: (videoId: String) -> Unit
) {
    val playlistState by appViewModel.playlistState.collectAsStateWithLifecycle()
    Log.d( TAG, "playlistState.currentPlaylistVideoIds is ${playlistState.currentPlaylistVideoIds}" )

    LazyRow(
        Modifier.testTag("PlaylistRow"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        items(playlistState.currentPlaylist) { video ->
            YouTubeThumbnail(
                videoId = video.videoId,
                videoName = video.title,
                onVideoClick = { selectedId ->
                    Log.d(TAG, "Open Video Id ${selectedId}")
                    onClick(video.videoId)
                }
            )
        }
    }

}


@Composable
fun YouTubeThumbnail(
    videoId: String,
    videoName: String,
    onVideoClick: (String) -> Unit
) {
    Log.d(TAG, "Render video id: ${videoId}")
    val thumbnailUrl = THUMBNAIL_URL.replace("%videoId%", videoId)

    Column(modifier = Modifier.width(200.dp)) {
        Text( videoName, minLines = 2, maxLines = 2, fontWeight = FontWeight.Bold )

        Box(
            modifier = Modifier
                .width(200.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black)
                .clickable { onVideoClick(videoId) }
        ) {

            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "YouTube Thumbnail for video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .padding(8.dp)
            )
        }
    }
}
