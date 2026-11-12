/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update

/**
 * Base for the tools that take one PDF, transform it and save the result:
 * encrypt, unlock, page numbers, watermark and signature.
 *
 * Subclasses only describe the transformation in [processPdf]; saving and sharing are
 * shared so that all five tools behave exactly the same way.
 */
abstract class BasePdfProcessComponent(
    initialUri: Uri?,
    onGoBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    componentContext: ComponentContext,
    protected val pdfManager: PdfManager,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
) : BasePdfToolComponent(
    onGoBack = onGoBack,
    onNavigate = onNavigate,
    dispatchersHolder = dispatchersHolder,
    componentContext = componentContext,
    pdfManager = pdfManager,
) {
    override val _haveChanges: MutableState<Boolean> = mutableStateOf(initialUri != null)
    override val haveChanges: Boolean by _haveChanges

    protected val _uri: MutableState<Uri?> = mutableStateOf(initialUri)
    val uri by _uri

    /** Applies the tool parameters to [uri] and returns the processed file uri. */
    protected abstract suspend fun processPdf(uri: String): String

    protected fun setSourceUri(uri: Uri?) {
        if (uri == null) registerChangesCleared() else registerChanges()
        _uri.update { uri }
    }

    override fun saveTo(uri: Uri) {
        doSaving {
            val processed = processPdf(_uri.value.toString())
            fileController.transferBytes(
                fromUri = processed,
                toUri = uri.toString(),
            ).onSuccess(::registerSave)
        }
    }

    override fun performSharing(
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        prepareForSharing(
            onSuccess = {
                shareProvider.shareUris(it.map(Uri::toString))
                registerSave()
                onSuccess()
            },
            onFailure = onFailure,
        )
    }

    override fun prepareForSharing(
        onSuccess: suspend (List<Uri>) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        doSharing(
            action = {
                onSuccess(listOf(processPdf(_uri.value.toString()).toUri()))
                registerSave()
            },
            onFailure = onFailure,
        )
    }
}
