package com.wanbaohe.engine.android.surface

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.EngineSnapshot

interface GameRenderView {
    val androidView: View
    fun dispatchHaptics(eventSequence: Int, strength: Float)
    fun release()
}

@Composable
fun GameSurface(
    controller: EngineController,
    modifier: Modifier = Modifier,
    onSnapshot: (EngineSnapshot) -> Unit,
    translucent: Boolean = false,
) {
    val context = LocalContext.current
    val currentOnSnapshot by rememberUpdatedState(onSnapshot)
    val view: GameRenderView = remember(controller, translucent) {
        if (translucent) {
            GameTextureView(context).apply { attach(controller) }
        } else {
            GameSurfaceView(context).apply { attach(controller) }
        }
    }
    AndroidView(
        factory = { view.androidView },
        modifier = modifier,
    )
    LaunchedEffect(view) {
        var lastSequence = 0L
        var lastHapticSequence = 0
        while (true) {
            withFrameNanos { }
            val snapshot = view.let { controller.readSnapshot() } ?: continue
            if (snapshot.sequence != lastSequence) {
                lastSequence = snapshot.sequence
                currentOnSnapshot(snapshot)
            }
            if (snapshot.eventSequence != lastHapticSequence) {
                lastHapticSequence = snapshot.eventSequence
                view.dispatchHaptics(snapshot.eventSequence, snapshot.eventStrength)
            }
        }
    }
    DisposableEffect(view) {
        onDispose(view::release)
    }
}
