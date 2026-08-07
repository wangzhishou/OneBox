package com.shifenmiao.marquee.screen

import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Applies an immersive, edge-to-edge full-screen mode and keeps the screen on.
 *
 * - Hides status/navigation bars.
 * - Re-applies hiding on START/RESUME to handle OEM quirks.
 */
@Composable
internal fun ImmersiveFullscreenEffect(
    window: Window?,
    keepScreenOn: Boolean = true,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    fun apply() {
        if(window == null) return
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    DisposableEffect(window, lifecycleOwner, keepScreenOn) {
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        apply()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                apply()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (keepScreenOn) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            // Restore default behavior for the rest of the app
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, true) }
            window?.let { WindowInsetsControllerCompat(it, window.decorView) }
                ?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Some devices only respect hide after the first frame.
    LaunchedEffect(window) {
        apply()
    }
}
