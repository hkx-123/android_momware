package com.hkx.momware

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hkx.momware.component.AppViewModel
import com.hkx.momware.component.NavItem
import com.hkx.momware.screen.HomeScreen
import com.hkx.momware.screen.PlaylistScreen
import com.hkx.momware.ui.theme.MomWareTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MomWareTheme {
                val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)

                InitComposable(appViewModel)

                MomWareAppContent(appViewModel)

            }
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    onSwitchTab: (index: Int) -> Unit,
    selectedIndex: Int
) {
    when(selectedIndex){
        0-> HomeScreen(modifier, appViewModel)
        1-> PlaylistScreen(modifier, appViewModel, onSwitchTab)
    }
}

@Composable
fun InitComposable(appViewModel: AppViewModel) {
    val pathToFile = appViewModel.pathToFile.collectAsStateWithLifecycle()

    // Init playlist if pathToFile is set
    pathToFile.value?.also { it ->
        LaunchedEffect(it) {
            launch(Dispatchers.IO) {
                appViewModel.initPlaylist(it)
            }
        }
    }

    val dbState by appViewModel.dbState.collectAsStateWithLifecycle()

    // Set current Playlist consisting of bookmarked entries
    appViewModel.updateCurrentPlaylist(dbState.dbItemList.filter { it.bookmarked }.sortedByDescending { it.id })
}


@Composable
fun MomWareAppContent(appViewModel: AppViewModel) {
    Log.d( TAG, "newPlaylistEntities is ${appViewModel.newPlaylistEntities}" )

    val newPlaylistEntitiesSize = appViewModel.newPlaylistEntities.size

    val navItemList = listOf(
        NavItem("起始页", Icons.Default.Home, 0),
        NavItem("播放列表", Icons.Default.AddToPhotos, newPlaylistEntitiesSize),
    )

    var selectedIndex by remember { mutableIntStateOf(0) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(Modifier) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected =  selectedIndex == index ,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            BadgedBox(badge = {
                                if(navItem.badgeCount>0)
                                    Badge { Text(text = navItem.badgeCount.toString()) }
                            }) {
                                Icon(imageVector = navItem.icon, contentDescription = "Badge Icon")
                            }

                        },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        val changeTabFunction = { index : Int -> selectedIndex = index }

        ContentScreen(modifier = Modifier.padding(innerPadding), appViewModel, changeTabFunction, selectedIndex)
    }
}


@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MomWareTheme {
        val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)

        MomWareAppContent(appViewModel)
    }
}