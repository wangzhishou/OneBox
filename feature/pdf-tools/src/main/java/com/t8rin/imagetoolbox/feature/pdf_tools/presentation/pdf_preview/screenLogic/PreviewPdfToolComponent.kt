/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.preview.screenLogic

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.isFromAppFileProvider
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.takePersistablePermission
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.utils.filename
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common.BasePdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File

class PreviewPdfToolComponent @AssistedInject internal constructor(
    @Assisted val initialUri: Uri?,
    @Assisted componentContext: ComponentContext,
    @Assisted onGoBack: () -> Unit,
    @Assisted onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    pdfManager: PdfManager,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
    dispatchersHolder: DispatchersHolder
) : BasePdfToolComponent(
    onGoBack = onGoBack,
    onNavigate = onNavigate,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    pdfManager = pdfManager
) {
    override val _haveChanges: MutableState<Boolean> = mutableStateOf(false)
    override val haveChanges: Boolean by _haveChanges

    private val _uri: MutableState<Uri?> = mutableStateOf(null)
    val uri by _uri

    private fun normalizePreviewUri(uri: Uri): Uri {
        val normalizedUri = uri.takePersistablePermission()
        val shouldCopyToAppStorage =
            normalizedUri.scheme == "content" &&
                !normalizedUri.isFromAppFileProvider() &&
                normalizedUri.authority == "com.android.providers.downloads.documents"

        if (!shouldCopyToAppStorage) return normalizedUri

        return runCatching {
            val previewDir = File(context.cacheDir, "pdf-preview").apply {
                mkdirs()
            }
            val sourceName = normalizedUri.filename().orEmpty().ifBlank { "preview.pdf" }
            val targetName = sourceName.takeIf { it.endsWith(".pdf", ignoreCase = true) }
                ?: "$sourceName.pdf"
            val targetFile = File(previewDir, "${System.currentTimeMillis()}_$targetName")

            context.contentResolver.openInputStream(normalizedUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return normalizedUri

            FileProvider.getUriForFile(
                context,
                context.getString(R.string.file_provider),
                targetFile
            )
        }.onFailure {
        }.getOrElse { normalizedUri }
    }

    private fun updatePreviewUri(uri: Uri) {
        componentScope.launch {
            _uri.value = normalizePreviewUri(uri)
        }
    }

    init {
        if (initialUri != null) {
            checkPdf(
                uri = initialUri,
                onDecrypted = ::updatePreviewUri,
                onSuccess = ::updatePreviewUri
            )
        }
    }

    fun setUri(uri: Uri?) {
        registerChangesCleared()
        _uri.update { null }
        if (uri == null) return
        checkPdf(
            uri = uri,
            onDecrypted = ::updatePreviewUri,
            onSuccess = ::updatePreviewUri
        )
    }

    override fun saveTo(
        uri: Uri
    ) = Unit

    override fun performSharing(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        prepareForSharing(
            onSuccess = {
                shareProvider.shareUris(it.map(Uri::toString))
                registerSave()
                onSuccess()
            },
            onFailure = onFailure
        )
    }

    override fun prepareForSharing(
        onSuccess: suspend (List<Uri>) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        doSharing(
            action = {
                shareProvider.cacheData(
                    writeData = { writeable ->
                        fileController.transferBytes(
                            fromUri = _uri.value.toString(),
                            to = writeable
                        )
                    },
                    filename = _uri.value?.filename() ?: createTargetFilename()
                )?.let {
                    onSuccess(listOf(it.toUri()))
                    registerSave()
                }
            },
            onFailure = onFailure
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            initialUri: Uri?,
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PreviewPdfToolComponent
    }
}