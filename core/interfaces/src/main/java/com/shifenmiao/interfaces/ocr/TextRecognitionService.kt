package com.shifenmiao.interfaces.ocr

import android.net.Uri

interface TextRecognitionService {
    fun isModelReady(): Boolean
    suspend fun prepareModel(onProgress: (Float?) -> Unit): Result<Unit>
    suspend fun recognizeText(uri: Uri): Result<String>
}
