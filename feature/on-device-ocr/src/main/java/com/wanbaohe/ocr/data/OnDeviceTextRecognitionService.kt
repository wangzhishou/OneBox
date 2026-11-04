package com.wanbaohe.ocr.data

import android.net.Uri
import com.shifenmiao.interfaces.ocr.TextRecognitionService
import com.wanbaohe.ocr.domain.toPlainText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OnDeviceTextRecognitionService @Inject constructor(
    private val modelStore: OcrModelStore,
    private val decoder: OcrBitmapDecoder,
    private val engine: PpOcrV6Engine,
) : TextRecognitionService {
    override fun isModelReady(): Boolean = modelStore.isReady()

    override suspend fun prepareModel(onProgress: (Float?) -> Unit): Result<Unit> {
        return try {
            modelStore.ensureDownloaded(onProgress)
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun recognizeText(uri: Uri): Result<String> = withContext(Dispatchers.Default) {
        try {
            val bitmap = decoder.decode(uri)
            try {
                val text = engine.recognize(bitmap).toPlainText()
                if (text.isBlank()) {
                    Result.failure(IllegalStateException("No text recognized"))
                } else {
                    Result.success(text)
                }
            } finally {
                bitmap.recycle()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
