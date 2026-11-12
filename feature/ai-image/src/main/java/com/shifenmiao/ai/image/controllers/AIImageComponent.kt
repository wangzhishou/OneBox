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

package com.shifenmiao.ai.image.controllers

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.image.components.model.BackgroundBehavior
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.state.update
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class AIImageComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val imageGenerationManager: ImageGenerationManager,
    defaultDispatchersHolder: DispatchersHolder,
    private val fileController: FileController,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : BaseComponent(defaultDispatchersHolder, componentContext) {

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _uri = mutableStateOf(Uri.EMPTY)

    private val _imageFormat = mutableStateOf(ImageFormat.Default)
    val imageFormat by _imageFormat

    private val _backgroundBehavior: MutableState<BackgroundBehavior> =
        mutableStateOf(BackgroundBehavior.None)
    val backgroundBehavior: BackgroundBehavior by _backgroundBehavior

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _saveExif: MutableState<Boolean> = mutableStateOf(false)
    val saveExif: Boolean by _saveExif

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    suspend fun generateImage(
        prompt: String,
        inputImages: List<String> = emptyList(),
    ): Result<ImageGenerationResult> = imageGenerationManager.generate(
        ImageGenerationRequest(
            prompt = prompt,
            inputImages = inputImages,
        )
    )

    private suspend fun getDrawingBitmap(): Bitmap? = withContext(defaultDispatcher) {
        null //TODO
    }

    fun shareBitmap(onComplete: () -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            getDrawingBitmap()?.let {
                shareProvider.shareImage(
                    image = it,
                    imageInfo = ImageInfo(
                        imageFormat = imageFormat,
                        width = it.width,
                        height = it.height
                    ),
                    onComplete = onComplete
                )
            }
            _isSaving.value = false
        }
    }

    fun cacheCurrentImage(onComplete: (Uri) -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            getDrawingBitmap()?.let { image ->
                shareProvider.cacheImage(
                    image = image,
                    imageInfo = ImageInfo(
                        imageFormat = imageFormat,
                        width = image.width,
                        height = image.height
                    )
                )?.let { uri ->
                    onComplete(uri.toUri())
                }
            }
            _isSaving.value = false
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): AIImageComponent
    }

}