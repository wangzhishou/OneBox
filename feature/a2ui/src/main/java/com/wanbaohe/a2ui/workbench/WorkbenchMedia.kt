package com.wanbaohe.a2ui.workbench

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.wanbaohe.a2ui.official.A2uiHostMedia
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import javax.inject.Inject

internal class WorkbenchMedia @Inject constructor() {
    val host = A2uiHostMedia(
        image = { url, description, scale, modifier, error ->
            if (url == IMAGE) {
                Image(
                    painterResource(R.drawable.a2wb_sample_image), description,
                    modifier.height(120.dp), contentScale = scale,
                )
            } else BlockedMedia(url, modifier, error)
        },
        video = { url, modifier, error ->
            if (url == CLIP) OfflineVideo(modifier, error) else BlockedMedia(url, modifier, error)
        },
        audio = { url, description, modifier, error ->
            if (url == CLIP) OfflineAudio(description, modifier, error) else BlockedMedia(url, modifier, error)
        },
    )

    companion object {
        const val IMAGE = "https://a2ui.invalid/samples/image"
        const val CLIP = "https://a2ui.invalid/samples/clip"
    }
}

@Composable
private fun BlockedMedia(url: String, modifier: Modifier, onError: (Throwable?) -> Unit) {
    val message = stringResource(R.string.a2wb_media_blocked)
    LaunchedEffect(url) { onError(IllegalArgumentException(message)) }
    Text(message, modifier)
}

@Composable
private fun OfflineVideo(modifier: Modifier, onError: (Throwable?) -> Unit) {
    AndroidView(
        modifier = modifier.height(160.dp),
        factory = { context ->
            VideoView(context).apply {
                contentDescription = context.getString(R.string.a2wb_play_video)
                setOnErrorListener { _, code, _ ->
                    onError(IllegalStateException("VideoView: $code"))
                    true
                }
                setOnClickListener { if (isPlaying) pause() else start() }
                setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.a2wb_sample_clip}"))
            }
        },
        onRelease = { it.stopPlayback() },
    )
}

@Composable
private fun OfflineAudio(description: String?, modifier: Modifier, onError: (Throwable?) -> Unit) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val audio = MediaPlayer.create(context, R.raw.a2wb_sample_clip)
        player = audio
        audio?.setOnCompletionListener { playing = false }
        audio?.setOnErrorListener { _, code, _ ->
            playing = false
            onError(IllegalStateException("MediaPlayer: $code"))
            true
        }
        if (audio == null) onError(null)
        onDispose { audio?.release() }
    }
    GlassTextButton(
        modifier = modifier,
        enabled = player != null,
        onClick = {
            val result = runCatching {
                if (playing) player?.pause() else player?.start()
            }
            if (result.isSuccess) playing = !playing else onError(result.exceptionOrNull())
        },
    ) {
        Text(if (playing) stringResource(R.string.a2wb_pause) else description ?: stringResource(R.string.a2wb_play))
    }
}
