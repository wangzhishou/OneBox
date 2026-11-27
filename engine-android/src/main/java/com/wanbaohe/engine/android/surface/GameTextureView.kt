package com.wanbaohe.engine.android.surface

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.wanbaohe.engine.android.EngineCommands
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.haptics.GameHaptics

class GameTextureView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener, GameRenderView {
    private var controller: EngineController? = null
    private val haptics = GameHaptics(context)
    private var renderSurface: Surface? = null

    override val androidView: View
        get() = this

    init {
        surfaceTextureListener = this
        isOpaque = false
        isClickable = true
        isFocusable = true
    }

    fun attach(controller: EngineController) {
        this.controller = controller
    }

    override fun dispatchHaptics(eventSequence: Int, strength: Float) {
        haptics.tick(strength)
    }

    override fun release() {
        surfaceTextureListener = null
        destroyRenderSurface()
        haptics.cancel()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        destroyRenderSurface()
        val newSurface = Surface(surface)
        renderSurface = newSurface
        controller?.surfaceCreated(newSurface, context.findActivity())
        controller?.surfaceChanged(width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        controller?.surfaceChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        destroyRenderSurface()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    private fun destroyRenderSurface() {
        if (renderSurface == null) return
        controller?.surfaceDestroyed()
        renderSurface?.release()
        renderSurface = null
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return false
        val type = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> EngineCommands.POINTER_DOWN
            MotionEvent.ACTION_MOVE -> EngineCommands.POINTER_MOVE
            MotionEvent.ACTION_UP -> EngineCommands.POINTER_UP
            else -> return true
        }
        controller?.dispatchPointer(type, event.x / width, event.y / height)
        if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
