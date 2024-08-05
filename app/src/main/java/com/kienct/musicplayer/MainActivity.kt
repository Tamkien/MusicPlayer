package com.kienct.musicplayer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.kienct.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null
    private var playbackState = PlaybackState()
    private var isPlayingUrl by mutableStateOf(false)
    private var isPlayingFile by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaybackScreen(
                        modifier = Modifier.padding(innerPadding),
                        isPlayingUrl = isPlayingUrl,
                        isPlayingFile = isPlayingFile,
                        onPlayPauseUrlClick = {
                            isPlayingUrl = !isPlayingUrl
                            if (isPlayingUrl) {
                                isPlayingFile = false
                                playWithUrl()
                            } else pausePlaying()
                        },
                        onPlayPauseFileClick = {
                            isPlayingFile = !isPlayingFile
                            if (isPlayingFile) {
                                isPlayingUrl = false
                                playWithFile()
                            } else pausePlaying()
                        }
                    )
                }
            }
        }
    }

    private fun playWithUrl() {
        val url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val musicPlayer = MusicPlayer(playbackState)
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }
        musicPlayer.setPlaybackStrategy(UrlPlaybackStrategy()).play(exoPlayer!!, url)
    }

    @OptIn(UnstableApi::class)
    private fun playWithFile() {
        val musicPlayer = MusicPlayer(playbackState)
        val rawResourceDataSource = RawResourceDataSource(this)
        rawResourceDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.music)))
        val uri = rawResourceDataSource.uri ?: return
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }
        musicPlayer.setPlaybackStrategy(FilePlaybackStrategy()).play(exoPlayer!!, uri)
    }

    private fun pausePlaying() {
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    interface PlaybackStrategy {
        fun play(exoPlayer: ExoPlayer, playbackState: PlaybackState, source: Any)
    }

    class UrlPlaybackStrategy : PlaybackStrategy {
        override fun play(exoPlayer: ExoPlayer, playbackState: PlaybackState, source: Any) {
            val mediaItem = MediaItem.fromUri(source.toString())
            if (playbackState.currentMediaItem != mediaItem) {
                playbackState.currentMediaItem = mediaItem
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
    }

    class FilePlaybackStrategy : PlaybackStrategy {
        @OptIn(UnstableApi::class)
        override fun play(exoPlayer: ExoPlayer, playbackState: PlaybackState, source: Any) {
            val uri = source as Uri
            val mediaItem = MediaItem.fromUri(uri)
            if (playbackState.currentMediaItem != mediaItem) {
                playbackState.currentMediaItem = mediaItem
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
            exoPlayer.play()
        }
    }

    class MusicPlayer(private val playbackState: PlaybackState) {
        private var playbackStrategy: PlaybackStrategy? = null

        fun setPlaybackStrategy(playbackStrategy: PlaybackStrategy): MusicPlayer {
            this.playbackStrategy = playbackStrategy
            return this
        }

        fun play(exoPlayer: ExoPlayer, source: Any) {
            playbackStrategy?.play(exoPlayer, playbackState, source)
        }
    }

    class PlaybackState {
        var currentMediaItem: MediaItem? = null
    }
}


@Composable
fun PlaybackScreen(
    modifier: Modifier = Modifier,
    isPlayingUrl: Boolean,
    isPlayingFile: Boolean,
    onPlayPauseUrlClick: () -> Unit,
    onPlayPauseFileClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onPlayPauseUrlClick) {
            Text(
                text = if (isPlayingUrl) "Pause" else "Play with URL",
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
        Button(onClick = onPlayPauseFileClick) {
            Text(
                text = if (isPlayingFile) "Pause" else "Play with File",
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MusicPlayerTheme {
        PlaybackScreen(
            isPlayingUrl = false,
            isPlayingFile = false,
            onPlayPauseUrlClick = {},
            onPlayPauseFileClick = {}
        )
    }
}