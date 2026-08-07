package com.wanbaohe.visual.automation.capturer

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import com.shifenmiao.model.automation.ScreenSize
import java.io.ByteArrayOutputStream

/**
 * App 内截图工具。
 * 截取当前 Activity 的 DecorView，不包含系统状态栏和其他应用。
 */
object ScreenshotCapturer {

    /**
     * 截取当前 Activity 屏幕并返回 Bitmap。
     */
    fun capture(activity: Activity): Bitmap {
        val decorView = activity.window.decorView
        val bitmap = Bitmap.createBitmap(
            decorView.width,
            decorView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        decorView.draw(canvas)
        return bitmap
    }

    /**
     * 截取屏幕并编码为 Base64 JPEG，用于传给 AI（多模态模型）。
     * @param quality JPEG 质量 0-100
     */
    fun captureToBase64(activity: Activity, quality: Int = 85): String {
        val bitmap = capture(activity)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        val bytes = output.toByteArray()
        bitmap.recycle()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * 获取适合 AI 输入的图片 Data URI。
     */
    fun captureToDataUri(activity: Activity, quality: Int = 85): String {
        val base64 = captureToBase64(activity, quality)
        return "data:image/jpeg;base64,$base64"
    }

    /**
     * 获取当前屏幕尺寸，用于将 AI 返回的坐标映射到实际像素。
     */
    fun getScreenSize(activity: Activity): ScreenSize {
        val decorView = activity.window.decorView
        return ScreenSize(decorView.width, decorView.height)
    }
}
