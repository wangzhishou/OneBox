package com.wanbaohe.file_transfer.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * 二维码生成工具类
 */
object QRCodeGenerator {

    /**
     * 生成二维码Bitmap
     * @param content 二维码内容
     * @param size 二维码尺寸(像素)
     * @param foregroundColor 前景色(黑色部分)
     * @param backgroundColor 背景色(白色部分)
     * @return 生成的二维码Bitmap，失败返回null
     */
    fun generateQRCode(
        content: String,
        size: Int = 512,
        foregroundColor: Int = 0xFF000000.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt()
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) {
                        foregroundColor
                    } else {
                        backgroundColor
                    }
                }
            }

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

