package com.hkx.momware.ui.state

import com.hkx.momware.data.db.PlaylistEntity

data class PlaylistState(
    val currentPlaylist: List<PlaylistEntity> = emptyList()
) {
    val currentPlaylistVideoIds: List<String> by lazy { currentPlaylist.map { it.videoId } }
}
