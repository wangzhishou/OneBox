package com.wanbaohe.visual.automation.injector

import android.app.Activity
import android.os.SystemClock
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 在 App 内部注入文字输入。
 * 策略：
 * 1. 先点击目标坐标，尝试聚焦 EditText
 * 2. 如果当前焦点是 EditText，直接 setText / append
 * 3. 如果需要模拟逐字输入效果，逐个字符 delay 输入
 */
object TextInputInjector {

    /**
     * 在指定坐标点击以聚焦输入框，然后输入文字。
     * @param x 输入框所在屏幕坐标（用于点击聚焦）
     * @param y 输入框所在屏幕坐标
     * @param text 要输入的文字
     * @param simulateTyping 是否模拟逐字输入效果（带延迟）
     * @param typingDelayMs 逐字输入间隔
     */
    suspend fun inputText(
        activity: Activity,
        x: Float,
        y: Float,
        text: String,
        simulateTyping: Boolean = false,
        typingDelayMs: Long = 50
    ) {
        withContext(Dispatchers.Main) {
            // 1. 点击坐标尝试聚焦
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

            delay(200)

            // 2. 获取当前焦点的 EditText
            val focusedView = activity.currentFocus
            if (focusedView is EditText) {
                if (simulateTyping) {
                    focusedView.setText("")
                    text.forEach { char ->
                        focusedView.append(char.toString())
                        delay(typingDelayMs)
                    }
                } else {
                    focusedView.setText(text)
                    // 将光标移到末尾
                    focusedView.setSelection(text.length)
                }
            } else {
                // 如果没有聚焦到 EditText，尝试通过 InputMethodManager 输入
                val imm = activity.getSystemService(InputMethodManager::class.java)
                imm?.let {
                    // 无法直接输入到非 EditText，记录日志即可
                }
            }
        }
    }

    /**
     * 直接对指定的 EditText 输入文字（如果已经持有引用）。
     */
    suspend fun inputTextDirectly(
        editText: EditText,
        text: String,
        simulateTyping: Boolean = false,
        typingDelayMs: Long = 50
    ) {
        withContext(Dispatchers.Main) {
            editText.requestFocus()
            if (simulateTyping) {
                editText.setText("")
                text.forEach { char ->
                    editText.append(char.toString())
                    delay(typingDelayMs)
                }
            } else {
                editText.setText(text)
                editText.setSelection(text.length)
            }
        }
    }
}
