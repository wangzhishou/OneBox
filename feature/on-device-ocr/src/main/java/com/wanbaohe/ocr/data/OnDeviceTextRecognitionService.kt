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
