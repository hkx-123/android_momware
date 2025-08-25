package com.hkx.momware.screen

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hkx.momware.component.AppViewModel
import com.hkx.momware.component.YouTubeThumbnails
import com.hkx.momware.component.YoutubeIFrameVideoPlayer
import com.hkx.momware.ui.theme.BgColorHomeScreen

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgColorHomeScreen)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val webView = WebView(LocalContext.current).apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {

                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    Log.d(TAG, "${message.message()} -- From line " +
                            "${message.lineNumber()} of ${message.sourceId()}")
                    return true
                }
            }
        }

        // YouTube Player in IFrame using official YouTube Web API
        YoutubeIFrameVideoPlayer(webView, appViewModel)

        // Playlist with YouTube Video Thumbnails
        YouTubeThumbnails(appViewModel, onClick = { videoId ->
            webView.loadUrl("javascript:nextVideo('${videoId}');")
        })

    }

}