package com.hkx.momware.component

import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.FormatListNumbered
import androidx.compose.material.icons.twotone.PauseCircle
import androidx.compose.material.icons.twotone.PlayCircle
import androidx.compose.material.icons.twotone.SkipNext
import androidx.compose.material.icons.twotone.SkipPrevious
import androidx.compose.material.icons.twotone.Timer3
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun YoutubeIFrameVideoPlayer(webView: WebView, appViewModel: AppViewModel) {

    val playlistState by appViewModel.playlistState.collectAsStateWithLifecycle()

    val htmlData = getHTMLData(appViewModel.videoIdToPlay)

    Column(Modifier.fillMaxSize()) {

        Row(modifier = Modifier
            .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .testTag("AndroidYTView")
                    .weight(0.8f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                factory = { webView }
            ) { view ->
                view.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    htmlData,
                    "text/html",
                    "UTF-8",
                    null
                )
            }

            Column(modifier = Modifier.weight(0.1f).padding(horizontal = 0.dp)) {

                IconButton(onClick = {
                    webView.loadUrl("javascript:playVideo();")
                }) {
                    Icon(
                        Icons.TwoTone.PlayCircle,
                        contentDescription = "Start video",
                        modifier = Modifier.size(60.dp)
                    )
                }

                IconButton(onClick = {
                    webView.loadUrl("javascript:pauseVideo();")
                }) {
                    Icon(
                        Icons.TwoTone.PauseCircle,
                        contentDescription = "Pause video",
                        modifier = Modifier.size(60.dp)
                    )
                }

                IconButton(onClick = {
                    webView.loadUrl("javascript:nextVideo();")
                }) {
                    Icon(
                        Icons.TwoTone.SkipNext,
                        contentDescription = "Next video",
                        modifier = Modifier.size(60.dp)
                    )
                }

                IconButton(onClick = {
                    webView.loadUrl("javascript:previousVideo();")
                }) {
                    Icon(
                        Icons.TwoTone.SkipPrevious,
                        contentDescription = "Previous video",
                        modifier = Modifier.size(60.dp)
                    )
                }

                IconButton(onClick = {
                    webView.loadUrl("javascript:seekBy(-3);")
                }) {
                    Icon(
                        Icons.TwoTone.Timer3,
                        contentDescription = "Back 3s",
                        modifier = Modifier.size(60.dp)
                    )
                }

                IconButton(onClick = {
                    webView.loadUrl("javascript:loadPlaylist([${playlistState.currentPlaylistVideoIds.map { "'$it'" }.joinToString(",")}]);")
                }) {
                    Icon(
                        Icons.TwoTone.FormatListNumbered,
                        contentDescription = "Start playing playlist",
                        modifier = Modifier.size(60.dp)
                    )
                }

            }
        }

    }

}


fun getHTMLData(videoId: String): String {
    return """
        <html>
            <head>
                <style>
                    .videoWrapper {
                        width: 100%;
                    }
                    
                    h1 {
                        color: blue;
                    }
                    p {
                        color: red;
                    }
                </style>
            </head>
            <body style="margin:0px;padding:0px;">
            
                <div id="outputText" style="font:14px;">Test</div>
                
                <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->
                <div id="player" class="videoWrapper"></div>
               
                <script>
                    var UNSTARTED = "UNSTARTED";
                    var ENDED = "ENDED";
                    var PLAYING = "PLAYING";
                    var PAUSED = "PAUSED";
                    var BUFFERING = "BUFFERING";
                    var CUED = "CUED";
    
                    var player;
                    var timerId;
                    
                    var currentScale = 1;
                    var defaultWidth = 640;
                    var defaultHeight = 390;
                    
                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            height: '390',
                            width: '640',
                            videoId: '$videoId',
                            playerVars: {
                                'playsinline': 1,
                                controls: 1,
                                cc_load_policy: 0,
                                fs: 0, // fullscreen button
                                iv_load_policy: 3,
                                loop: 0 // 0 == deactivate loop
                                
                                
                            },
                            events: {
                                'onReady': onPlayerReady,
                                'onStateChange': function(event) {
                                    sendPlayerStateChange(event.data);
                                },
                            }
                        });
                    }
                    
                    function onPlayerReady(event) {
                        //player.playVideo();     // If you want this to work, you have to set up autostart in WebView
                        
                        // Player is ready
                        setOutputMessage(event.data)
                    }
                    function setOutputMessage(text) {
                        var outputTextField = document.getElementById('outputText');
                        outputTextField.style.fontSize = "50px";
                        outputTextField.innerHTML = text;
                    }
                    function sendPlayerStateChange(playerState) {
                    
                      switch (playerState) {
                        case YT.PlayerState.UNSTARTED:
                          setOutputMessage(UNSTARTED);
                          return;
                
                        case YT.PlayerState.ENDED:
                          setOutputMessage(ENDED);
                          return;
                
                        case YT.PlayerState.PLAYING:
                          setOutputMessage(PLAYING);
                          return;
                
                        case YT.PlayerState.PAUSED:
                          setOutputMessage(PAUSED);
                          return;
                
                        case YT.PlayerState.BUFFERING:
                          setOutputMessage(BUFFERING);
                          return;
                
                        case YT.PlayerState.CUED:
                          setOutputMessage(CUED);
                          return;
                      }
                    }
                    
                    function seekTo(time) {
                        player.seekTo(time, true);
                    }
                    function seekBy(time) {
                        player.seekTo(player.getCurrentTime() + time, true);
                    }
                    function playVideo() {
                        player.playVideo();
                    }
                    function pauseVideo() {
                        player.pauseVideo();
                    }
                    function nextVideo(videoId) {
                        if (typeof videoId === 'string' || videoId instanceof String) {
                            player.loadVideoById(videoId)
                            console.debug("next video started: " + videoId)
                        } else {
                            console.debug("no next video, try next video of playlist (if given)")
                            player.nextVideo()
                        }
                    }
                    function previousVideo() {
                        console.debug("previous video")
                        player.previousVideo()
                    }
                    function setLoop(loop) {
                        player.setLoop(loop);
                    }
                    function loadPlaylist(playlist) {
                        player.loadPlaylist(playlist)
                    }
                    function zoomIn() {
                        console.debug('currentScale: ' + currentScale);
                        if (currentScale < 5.0) {
                            currentScale += 0.2;
                        }
                        player.setSize(defaultWidth * currentScale, defaultHeight  * currentScale);
                    }
                    function zoomOut() {
                        console.debug('currentScale: ' + currentScale);
                        if (currentScale > 0.4) {
                            currentScale -= 0.2;
                        }
                        player.setSize(defaultWidth * currentScale, defaultHeight  * currentScale);
                    }
                </script>
                
                <script src="https://www.youtube.com/iframe_api"></script>
                
            </body>
        </html>
    """.trimIndent()
}
