package com.shifenmiao.webview.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.shifenmiao.common.file.TempBitmapFileUtils
import com.t8rin.logger.makeLog
import kotlin.math.sqrt

/**
 * WebView 导出工具类
 */
object WebViewExportUtils {

    private const val MAX_CAPTURE_BITMAP_PIXELS = 24_000_000

    private const val CAPTURE_NOT_READY = "WebView 尚未完成渲染，请稍候再试"

    /**
     * 导出 WebView 内容为 PDF（指定URI和文件名）
     */
    fun exportToPdf(
        webView: WebView,
        context: Context,
        fileName: String? = null,
        onComplete: (Boolean, Uri?) -> Unit = { _, _ ->}
    ) {
        try {
            val jobName = fileName ?: "WebPage_${System.currentTimeMillis()}"
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
            Toast.makeText(context, "正在准备PDF导出...", Toast.LENGTH_SHORT).show()
            onComplete(true, null)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF导出失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            onComplete(false, null)
        }
    }

    /**
     * 导出WebView为PNG
     */
    fun exportToPng(
        webView: WebView,
        context: Context,
        uri: Uri,
        fileName: String,
        onComplete: (Boolean, Uri?) -> Unit = { _, _ ->}
    ) {
        try {
            val bitmap = captureWebViewToBitmap(webView)
            saveImageToUri(context, uri, fileName, bitmap, onComplete)
            bitmap.recycle()
        } catch (e: Exception) {
            e.makeLog("WebViewExportUtils")
            onComplete(false, null)
        }
    }

    /**
     * 捕获 WebView 为位图。
     *
     * 使用 [WebView.capturePicture] 获取完整页面内容，避免 [WebView.draw] 在部分机型上
     * 只渲染可视区域的问题。
     *
     * 必须在主线程调用。当 WebView 尚未完成渲染（progress<100 / 宽高未测量）时，
     * 抛 [IllegalStateException]，由上层决定重试或提示用户。
     */
    @Suppress("DEPRECATION")
    fun captureWebViewToBitmap(webView: WebView): Bitmap {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "captureWebViewToBitmap must be called on the main thread"
        }

        val viewWidth = webView.width
        val viewHeight = webView.height
        if (viewWidth <= 0 || viewHeight <= 0) {
            throw IllegalStateException(CAPTURE_NOT_READY)
        }
        if (webView.progress < 100) {
            throw IllegalStateException(CAPTURE_NOT_READY)
        }

        val picture = webView.capturePicture()
        val picWidth = picture.width.coerceAtLeast(viewWidth)
        val picHeight = picture.height.coerceAtLeast(viewHeight)

        if (picWidth.toLong() * picHeight.toLong() > MAX_CAPTURE_BITMAP_PIXELS) {
            return captureVisibleWebViewToBitmap(webView)
        }

        return try {
            val bitmap = Bitmap.createBitmap(
                picWidth,
                picHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            picture.draw(canvas)

            if (bitmap.isEntirelyTransparent()) {
                bitmap.recycle()
                captureVisibleWebViewToBitmap(webView)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.makeLog("WebViewExportUtils")
            captureVisibleWebViewToBitmap(webView)
        }
    }

    /**
     * 使用已废弃但稳定的 [WebView.capturePicture] 截取长图。
     * 当 draw(canvas) 全透明或 OOM 时作为兜底方案。
     */
    @Suppress("DEPRECATION")
    fun captureWebViewPicture(webView: WebView): Bitmap {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "captureWebViewPicture must be called on the main thread"
        }
        val picture = webView.capturePicture()
        val width = picture.width.coerceAtLeast(webView.width)
        val height = picture.height.coerceAtLeast(webView.height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        picture.draw(canvas)
        return bitmap
    }

    /**
     * 截取 WebView 完整内容为 Bitmap。
     *
     * 先尝试 [WebView.capturePicture] 快速获取完整页面；在部分机型/WebView 版本中，
     * capturePicture 仍只返回可视区域（高宽等于 view 尺寸），此时会回退到“按完整滚动
     * 范围重新 measure/layout 后再 draw”的方式，确保截取长页面。
     *
     * 当总像素超过 [maxPixels] 时，会等比缩放而不是裁剪，避免 OOM。
     *
     * @param webView 目标 WebView
     * @param maxPixels 输出 Bitmap 最大像素数
     */
    @Suppress("DEPRECATION")
    fun captureWebViewFullPageBitmap(
        webView: WebView,
        maxPixels: Int = MAX_CAPTURE_BITMAP_PIXELS
    ): Bitmap {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "captureWebViewFullPageBitmap must be called on the main thread"
        }

        val viewWidth = webView.width.coerceAtLeast(1)
        val viewHeight = webView.height.coerceAtLeast(1)
        if (viewWidth <= 0 || viewHeight <= 0) {
            throw IllegalStateException(CAPTURE_NOT_READY)
        }
        if (webView.progress < 100) {
            throw IllegalStateException(CAPTURE_NOT_READY)
        }

        // 1. 先尝试 capturePicture（高效且通常已包含完整页面）
        try {
            val picture = webView.capturePicture()
            val picWidth = picture.width.coerceAtLeast(viewWidth)
            val picHeight = picture.height.coerceAtLeast(viewHeight)

            if (picWidth > viewWidth || picHeight > viewHeight) {
                val (finalWidth, finalHeight) = computeScaledSize(picWidth, picHeight, maxPixels)
                val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                if (finalWidth != picWidth || finalHeight != picHeight) {
                    canvas.scale(
                        finalWidth.toFloat() / picWidth.toFloat(),
                        finalHeight.toFloat() / picHeight.toFloat()
                    )
                }
                picture.draw(canvas)

                if (!bitmap.isEntirelyTransparent()) {
                    return bitmap
                }
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.makeLog("WebViewExportUtils")
        }

        // 2. capturePicture 只拿到可视区域时，强制按完整内容高度重新 layout 再 draw
        return captureWebViewByFullLayout(webView, maxPixels)
    }

    /**
     * 按完整滚动范围强制 WebView 重新 measure/layout 后绘制到 Bitmap。
     *
     * 这是 capturePicture 失效机型上的兜底方案：把 WebView 当成一个高度等于完整内容
     * 高度的普通 View 进行绘制，因此 canvas 能覆盖整个页面，而不是当前可视窗口。
     */
    @Suppress("DEPRECATION")
    private fun captureWebViewByFullLayout(
        webView: WebView,
        maxPixels: Int
    ): Bitmap {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "captureWebViewByFullLayout must be called on the main thread"
        }

        val originalLayerType = webView.layerType
        val originalScrollX = webView.scrollX
        val originalScrollY = webView.scrollY

        val viewWidth = webView.width.coerceAtLeast(1)
        val viewHeight = webView.height.coerceAtLeast(1)
        // contentHeight 返回的是 CSS 像素高度，乘以当前缩放比例得到物理像素高度
        val contentWidth = viewWidth
        val contentHeight = (webView.contentHeight * webView.scale).toInt().coerceAtLeast(viewHeight)

        val (finalWidth, finalHeight) = computeScaledSize(contentWidth, contentHeight, maxPixels)

        return try {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.scrollTo(0, 0)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(contentHeight, View.MeasureSpec.EXACTLY)
            webView.measure(widthSpec, heightSpec)
            webView.layout(0, 0, contentWidth, contentHeight)

            val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (finalWidth != contentWidth || finalHeight != contentHeight) {
                canvas.scale(
                    finalWidth.toFloat() / contentWidth.toFloat(),
                    finalHeight.toFloat() / contentHeight.toFloat()
                )
            }
            webView.draw(canvas)

            if (bitmap.isEntirelyTransparent()) {
                bitmap.recycle()
                captureVisibleWebViewToBitmap(webView)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.makeLog("WebViewExportUtils")
            captureVisibleWebViewToBitmap(webView)
        } finally {
            webView.setLayerType(originalLayerType, null)
            webView.scrollTo(originalScrollX, originalScrollY)
            webView.requestLayout()
        }
    }

    private fun computeScaledSize(width: Int, height: Int, maxPixels: Int): Pair<Int, Int> {
        if (width.toLong() * height.toLong() <= maxPixels) {
            return width to height
        }
        val scale = sqrt(maxPixels.toFloat() / (width * height).toFloat())
        return (width * scale).toInt().coerceAtLeast(1) to (height * scale).toInt().coerceAtLeast(1)
    }

    /**
     * 检查 Bitmap 是否所有像素都是完全透明。
     */
    private fun Bitmap.isEntirelyTransparent(): Boolean {
        if (width <= 0 || height <= 0) return true
        // 小图直接遍历；大图取 5 条扫描线采样，兼顾性能与准确性
        return if (width * height <= 25_000) {
            IntArray(width * height).also { pixels ->
                getPixels(pixels, 0, width, 0, 0, width, height)
            }.all { it ushr 24 == 0 }
        } else {
            val sampleRows = 5
            val rowStep = (height / sampleRows).coerceAtLeast(1)
            val buffer = IntArray(width)
            for (row in 0 until height step rowStep) {
                getPixels(buffer, 0, width, 0, row, width, 1)
                if (buffer.any { it ushr 24 != 0 }) return false
            }
            true
        }
    }

    /**
     * 捕获 WebView 当前可见区域为位图。
     *
     * 完整文档超过像素上限或 draw 全透明时的兜底方案，仅捕获当前视口，保证非 0。
     */
    fun captureVisibleWebViewToBitmap(webView: WebView): Bitmap {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "captureVisibleWebViewToBitmap must be called on the main thread"
        }

        val width = webView.width.coerceAtLeast(1)
        val height = webView.height.coerceAtLeast(1)

        val originalLayerType = webView.layerType
        val originalScrollX = webView.scrollX
        val originalScrollY = webView.scrollY

        return try {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.buildLayer()

            val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            canvas.translate(-originalScrollX.toFloat(), -originalScrollY.toFloat())
            webView.draw(canvas)

            if (bitmap.isEntirelyTransparent()) {
                bitmap.recycle()
                return captureWebViewPicture(webView)
            }
            bitmap
        } catch (e: Exception) {
            e.makeLog("WebViewExportUtils")
            captureWebViewPicture(webView)
        } finally {
            webView.setLayerType(originalLayerType, null)
            webView.scrollTo(originalScrollX, originalScrollY)
        }
    }

    /**
     * 保存位图到临时文件并返回URI字符串
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): String {
        return TempBitmapFileUtils.saveBitmapToTempFile(
            context = context,
            bitmap = bitmap
        )
    }

    /**
     * 在 Bitmap 底部叠加一条 "AI 生成内容声明" 横幅 —— 徕卡水印风格。
     *
     * 设计要点:
     * - **背景条**(主题色纯色,扁平化、无渐变/边框/阴影);
     * - **一行文字,加粗,右对齐**(贴齐右边缘);
     * - **紧贴图片最底部**,banner 之外不再留白;
     * - 字号、padding 都是**固定 px**,不再按短边百分比算,方便用户直接调。
     *
     * @param backgroundColor 主题色背景(建议传入 `tertiaryContainer` 或 `secondaryContainer`)
     * @param textColor       主题色文字(建议传入 `onTertiaryContainer` 等对应 on* 色)
     */
    fun drawAiNoticeOnBitmap(
        source: Bitmap,
        noticeText: String,
        backgroundColor: Int,
        textColor: Int,
    ): Bitmap {
        val srcWidth = source.width.coerceAtLeast(1)
        val srcHeight = source.height.coerceAtLeast(1)

        // 固定参数,用户自己调
        val textSize = 64f                  // 文字字号(px)
        val verticalPadding = 20f           // 上下 padding
        val horizontalPadding = 32f         // 左右 padding(右对齐时只影响右边距)
        val bannerHeight = (textSize + verticalPadding * 2).toInt()

        val out = Bitmap.createBitmap(srcWidth, srcHeight + bannerHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        // 1) 原图
        canvas.drawBitmap(source, 0f, 0f, null)
        // 2) 主题色纯色背景(扁平化:无渐变/边框/阴影,直接贴最底部)
        val bgPaint = Paint().apply { color = backgroundColor }
        canvas.drawRect(
            0f, srcHeight.toFloat(),
            srcWidth.toFloat(), (srcHeight + bannerHeight).toFloat(),
            bgPaint
        )
        // 3) 加粗文字,右对齐,贴齐右边缘
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            color = textColor
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val finalText = ellipsizeText(noticeText, textPaint, srcWidth - horizontalPadding)
        val textX = srcWidth - horizontalPadding
        val textY = srcHeight + bannerHeight / 2f -
            (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(finalText, textX, textY, textPaint)
        return out
    }

    private fun ellipsizeText(text: String, paint: Paint, maxWidth: Float): String {
        if (maxWidth <= 0f) return ""
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end <= 0) ellipsis else text.substring(0, end) + ellipsis
    }

    /**
     * 保存位图到文件
     */
    private fun saveImageToUri(
        context: Context,
        folderUri: Uri,
        fileName: String,
        bitmap: Bitmap,
        onComplete: (Boolean, Uri?) -> Unit = { _, _ ->}
    ) {
        val documentFile = DocumentFile.fromTreeUri(context, folderUri)?.createFile("image/png", fileName)

        if (documentFile != null) {
            try {
                context.contentResolver.openOutputStream(documentFile.uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                onComplete(true, documentFile.uri)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, null)
            }
        } else {
            onComplete(false, null)
        }
    }

    /**
     * 保存PDF字节数组到文件
     */
    fun savePdfBytes(
        context: Context,
        folderUri: Uri,
        fileName: String,
        pdfBytes: ByteArray,
        onComplete: (Boolean, Uri?) -> Unit = { _, _ ->
        }
    ) {
        val documentFile = DocumentFile.fromTreeUri(context, folderUri)?.createFile("application/pdf", fileName)

        if (documentFile != null) {
            try {
                context.contentResolver.openOutputStream(documentFile.uri)?.use { outputStream ->
                    outputStream.write(pdfBytes)
                }
                onComplete(true, documentFile.uri)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, null)
            }
        } else {
            onComplete(false, null)
        }
    }
}
