package com.wanbaohe.engine.android

internal object EngineNative {
    external fun nativeCreate(gameId: String): Long
    external fun nativeDestroy(handle: Long)
    external fun nativeSurfaceCreated(handle: Long, surface: android.view.Surface, activity: android.app.Activity?)
    external fun nativeSurfaceChanged(handle: Long, width: Int, height: Int)
    external fun nativeSurfaceDestroyed(handle: Long)
    external fun nativeDispatchCommand(handle: Long, type: Int, payload: ByteArray?)
    external fun nativeReadSnapshot(handle: Long, buffer: java.nio.ByteBuffer): Boolean
    external fun nativeSetPaused(handle: Long, paused: Boolean)
}
