package com.hkx.momware.ui.state

import com.hkx.momware.data.db.PlaylistEntity

data class DatabaseState(val dbItemList: List<PlaylistEntity> = listOf())
