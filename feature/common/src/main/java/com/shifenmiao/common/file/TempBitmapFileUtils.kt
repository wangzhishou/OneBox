package com.shifenmiao.common.file

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object TempBitmapFileUtils {

    /**
     * 保存位图到临时文件并返回 URI 字符串
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): String {
        val tempFile = File(context.cacheDir, "temp_webview_${System.currentTimeMillis()}.png")
        try {
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            return tempFile.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

