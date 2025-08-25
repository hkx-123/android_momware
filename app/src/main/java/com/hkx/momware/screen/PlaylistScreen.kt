package com.hkx.momware.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hkx.momware.component.AppViewModel
import com.hkx.momware.component.THUMBNAIL_URL
import com.hkx.momware.data.db.PlaylistEntity

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory),
    onSwitchTab: (index: Int) -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isInSelectionMode by remember { mutableStateOf(false) }
    val selectedItemsToDelete = remember { mutableStateListOf<String>() }
    val resetSelectionMode = {
        isInSelectionMode = false
        selectedItemsToDelete.clear()
    }
    val dbState by appViewModel.dbState.collectAsStateWithLifecycle()

    // File Picker for YouTube Links File
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri ->
        uri?.run { context.contentResolver.takePersistableUriPermission(this,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            appViewModel.setFilePathIntoPreference(uri.toString())
        }
    }

    val filteredPlaylistItems : List<PlaylistEntity> = dbState.dbItemList.sortedByDescending { it.id }

    // Overwrite BackHandler
    BackHandler(
        enabled = isInSelectionMode,
    ) {
        resetSelectionMode()
    }

    LaunchedEffect(
        key1 = isInSelectionMode,
        key2 = selectedItemsToDelete.size,
    ) {
        if (isInSelectionMode && selectedItemsToDelete.isEmpty()) {
            isInSelectionMode = false
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {

            if (isInSelectionMode) {
                SelectionModeTopAppBar(
                    selectedItems = selectedItemsToDelete,
                    resetSelectionMode = resetSelectionMode,
                    deleteSelectedItems = { selectedItems ->
                        filteredPlaylistItems.filter { it.videoId in selectedItems }.forEach(appViewModel::deletePlaylistEntity)
                    }
                )
            } else {
                PlaylistTopAppBar(
                    title = "视频",
                    canNavigateBack = false,
                    scrollBehavior = scrollBehavior
                )
            }

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add playlist file Icon"
                )
            }
        },
    ) { innerPadding ->

        if (filteredPlaylistItems.isEmpty()) {
            Text(
                text = "没有视频",  // no videos
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            VideoList(
                modifier = Modifier.padding(innerPadding),
                appViewModel,
                itemList = filteredPlaylistItems,
                onItemClick = { index, item ->
                    appViewModel.videoIdToPlay = item.videoId

                    onSwitchTab(0)
                },
                isInSelectionMode,
                selectedItemsToDelete,
                { selectionMode -> isInSelectionMode = selectionMode }
            )
        }

    }

}



/**
 * Top app bar of PlaylistScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectionModeTopAppBar(
    selectedItems: SnapshotStateList<String>,
    resetSelectionMode: () -> Unit,
    deleteSelectedItems: (SnapshotStateList<String>) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "选择 ${selectedItems.size} 个",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = resetSelectionMode,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            var isDropDownVisible by remember { mutableStateOf(false) }
            Box(modifier = Modifier,)
            {
                IconButton(
                    onClick = { isDropDownVisible = true },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                DropdownMenu(
                    expanded = isDropDownVisible,
                    onDismissRequest = { isDropDownVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text( text = "删除" ) },
                        onClick = {
                            isDropDownVisible = false
                            deleteSelectedItems(selectedItems)
                        },
                        leadingIcon = {
                            Icon( Icons.Outlined.Delete, contentDescription = null )
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoList(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory),
    itemList: List<PlaylistEntity>,
    onItemClick: (Int, PlaylistEntity) -> Unit,
    isInSelectionMode: Boolean,
    selectedItemsToDelete: SnapshotStateList<String>,
    onLongPressOnVideo: (Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(items = itemList) { index, item ->

            val isSelected = selectedItemsToDelete.contains(item.videoId)
            val cardColor: Color = if (appViewModel.newPlaylistEntities.map { it.videoId }.contains(item.videoId))
                Color(0xFFFCA9A9) else Color.Unspecified

            VideoCard(item = item, CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier
                    .padding(8.dp)
                    .combinedClickable(
                        onClick = {
                            if (isInSelectionMode) {
                                if (isSelected) {
                                    selectedItemsToDelete.remove(item.videoId)
                                } else {
                                    selectedItemsToDelete.add(item.videoId)
                                }
                            } else {
                                onItemClick(index, item)
                            }
                        },
                        onLongClick = {
                            if (isInSelectionMode) {
                                if (isSelected) {
                                    selectedItemsToDelete.remove(item.videoId)
                                } else {
                                    selectedItemsToDelete.add(item.videoId)
                                }
                            } else {
                                onLongPressOnVideo(true)
                                selectedItemsToDelete.add(item.videoId)
                            }
                        },
                    ),
                onBookmarkCheckBoxChanged = { playlistItem ->
                    appViewModel.updatePlaylistEntity(playlistItem.copy(bookmarked = !playlistItem.bookmarked))
                },
                isInSelectionMode,
                isSelected
            )

        }
    }

}


@Composable
private fun VideoCard(
    item: PlaylistEntity,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
    onBookmarkCheckBoxChanged: (PlaylistEntity) -> Unit,
    isInSelectionMode: Boolean,
    isSelected: Boolean
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val thumbnailUrl = THUMBNAIL_URL.replace("%videoId%", item.videoId)
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "YouTube Thumbnail for video",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Row(modifier.fillMaxWidth()) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f)
                )

                Spacer(modifier = Modifier.weight(0.1f))

                if (isInSelectionMode) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    Checkbox(checked = item.bookmarked, onCheckedChange = { onBookmarkCheckBoxChanged(item) },
                        modifier = Modifier
                            .scale(2f)
                            .weight(0.2f),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Green,
                            checkmarkColor = Color.Black,
                        )
                    )
                }

            }

            Text(
                text = "ID: ${item.id}, 位: ${item.position}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
