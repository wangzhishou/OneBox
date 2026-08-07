package com.wanbaohe.visual.automation.injector

import android.app.Activity
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 在 App 内部注入触摸事件，模拟用户点击、滑动、长按。
 * 事件通过 DecorView 分发，目标 View 会自然响应（涟漪、点击回调等）。
 */
object TouchInjector {

    /**
     * 单点点击指定屏幕坐标。
     * @param x 屏幕绝对坐标 X
     * @param y 屏幕绝对坐标 Y
     */
    suspend fun performClick(activity: Activity, x: Float, y: Float) {
        withContext(Dispatchers.Main) {
            val decorView = activity.window.decorView
            val downTime = SystemClock.uptimeMillis()

            val downEvent = MotionEvent.obtain(
                downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0
            )
            decorView.dispatchTouchEvent(downEvent)

            delay(100)

            val upEvent = MotionEvent.obtain(
                downTime, downTime + 100,
                MotionEvent.ACTION_UP, x, y, 0
            )
            decorView.dispatchTouchEvent(upEvent)

            downEvent.recycle()
            upEvent.recycle()
        }
    }

    /**
     * 长按指定屏幕坐标。
     * @param durationMs 长按持续时间
     */
    suspend fun performLongPress(activity: Activity, x: Float, y: Float, durationMs: Long = 800) {
        withContext(Dispatchers.Main) {
            val decorView = activity.window.decorView
            val downTime = SystemClock.uptimeMillis()

            val downEvent = MotionEvent.obtain(
                downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0
            )
            decorView.dispatchTouchEvent(downEvent)

            delay(durationMs)

            val upEvent = MotionEvent.obtain(
                downTime, downTime + durationMs,
                MotionEvent.ACTION_UP, x, y, 0
            )
            decorView.dispatchTouchEvent(upEvent)

            downEvent.recycle()
            upEvent.recycle()
        }
    }

    /**
     * 滑动操作。
     */
    suspend fun performSwipe(
        activity: Activity,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        durationMs: Long = 300
    ) {
        withContext(Dispatchers.Main) {
            val decorView = activity.window.decorView
            val downTime = SystemClock.uptimeMillis()
            val steps = (durationMs / 16).toInt().coerceAtLeast(10)

            // ACTION_DOWN
            val downEvent = MotionEvent.obtain(
                downTime, downTime,
                MotionEvent.ACTION_DOWN, fromX, fromY, 0
            )
            decorView.dispatchTouchEvent(downEvent)

            // MOVE 插值
            for (i in 1..steps) {
                val progress = i / steps.toFloat()
                val currentX = fromX + (toX - fromX) * progress
                val currentY = fromY + (toY - fromY) * progress
                val moveEvent = MotionEvent.obtain(
                    downTime, downTime + (i * 16),
                    MotionEvent.ACTION_MOVE, currentX, currentY, 0
                )
                decorView.dispatchTouchEvent(moveEvent)
                moveEvent.recycle()
                delay(16)
            }

            // ACTION_UP
            val upEvent = MotionEvent.obtain(
                downTime, downTime + durationMs,
                MotionEvent.ACTION_UP, toX, toY, 0
            )
            decorView.dispatchTouchEvent(upEvent)

            downEvent.recycle()
            upEvent.recycle()
        }
    }

    /**
     * 根据屏幕坐标查找目标 View（调试用）。
     */
    fun findViewAt(activity: Activity, x: Float, y: Float): View? {
        return findViewAtRecursive(activity.window.decorView, x.toInt(), y.toInt())
    }

    private fun findViewAtRecursive(view: View, x: Int, y: Int): View? {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right = left + view.width
        val bottom = top + view.height

        if (x in left..right && y in top..bottom) {
            if (view is ViewGroup) {
                for (i in view.childCount - 1 downTo 0) {
                    val child = view.getChildAt(i)
                    val found = findViewAtRecursive(child, x, y)
                    if (found != null) return found
                }
            }
            return view
        }
        return null
    }
}
