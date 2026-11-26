package com.wanbaohe.engine.android.surface

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.wanbaohe.engine.android.EngineCommands
import com.wanbaohe.engine.android.EngineController
import com.wanbaohe.engine.android.haptics.GameHaptics

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var controller: EngineController? = null
    private val haptics = GameHaptics(context)

    init {
        holder.addCallback(this)
        isClickable = true
        isFocusable = true
    }

    fun attach(controller: EngineController) {
        this.controller = controller
    }

    fun dispatchHaptics(eventSequence: Int, strength: Float) {
        haptics.tick(strength)
    }

    fun release() {
        holder.removeCallback(this)
        haptics.cancel()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        controller?.surfaceCreated(holder.surface, context.findActivity())
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        controller?.surfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        controller?.surfaceDestroyed()
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
