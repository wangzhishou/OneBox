/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
