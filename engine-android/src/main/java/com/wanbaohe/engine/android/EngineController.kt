package com.wanbaohe.engine.android

import android.app.Activity
import android.view.Surface
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

object EngineCommands {
    const val POINTER_DOWN = 1
    const val POINTER_MOVE = 2
    const val POINTER_UP = 3
    const val PAUSE = 4
    const val RESUME = 5
    const val RESET = 6
    const val GAME_SPECIFIC = 256
}

class EngineSnapshot(
    val sequence: Long,
    val eventSequence: Int,
    val eventStrength: Float,
    val integers: IntArray,
    val floats: FloatArray,
)

class EngineController(gameId: String) : Closeable {
    private var handle = EngineNative.nativeCreate(gameId)
    private val snapshotBuffer = ByteBuffer.allocateDirect(SNAPSHOT_BYTES).order(ByteOrder.LITTLE_ENDIAN)
    private val commandBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)

    init {
        require(handle != 0L) { "Unknown game: $gameId" }
    }

    fun dispatch(type: Int, payload: ByteArray? = null) {
        if (handle != 0L) EngineNative.nativeDispatchCommand(handle, type, payload)
    }

    fun dispatchPointer(type: Int, normalizedX: Float, normalizedY: Float) {
        val payload = commandBuffer.apply {
            clear()
            putFloat(normalizedX)
            putFloat(normalizedY)
        }
        dispatch(type, payload.array())
    }

    fun setPaused(paused: Boolean) {
        if (handle != 0L) EngineNative.nativeSetPaused(handle, paused)
    }

    fun surfaceCreated(surface: Surface, activity: Activity?) {
        if (handle != 0L) EngineNative.nativeSurfaceCreated(handle, surface, activity)
    }

    fun surfaceChanged(width: Int, height: Int) {
        if (handle != 0L) EngineNative.nativeSurfaceChanged(handle, width, height)
    }

    fun surfaceDestroyed() {
        if (handle != 0L) EngineNative.nativeSurfaceDestroyed(handle)
    }

    fun readSnapshot(): EngineSnapshot? {
        val currentHandle = handle
        if (currentHandle == 0L || !EngineNative.nativeReadSnapshot(currentHandle, snapshotBuffer)) {
            return null
        }
        return EngineSnapshot(
            sequence = snapshotBuffer.getLong(0),
            eventSequence = snapshotBuffer.getInt(8),
            eventStrength = snapshotBuffer.getFloat(12),
            integers = IntArray(16) { index -> snapshotBuffer.getInt(16 + index * 4) },
            floats = FloatArray(16) { index -> snapshotBuffer.getFloat(80 + index * 4) },
        )
    }

    override fun close() {
        if (handle == 0L) return
        EngineNative.nativeSurfaceDestroyed(handle)
        EngineNative.nativeDestroy(handle)
        handle = 0L
    }

    private companion object {
        const val SNAPSHOT_BYTES = 144

        init {
            System.loadLibrary("onebox_engine")
        }
    }
}
