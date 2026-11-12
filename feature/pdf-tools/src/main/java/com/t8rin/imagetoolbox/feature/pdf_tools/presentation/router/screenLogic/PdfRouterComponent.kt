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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.shifenmiao.common.recent.RecentAccessRepository
import com.shifenmiao.database.activity.repository.ActivityLogRepository
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.model.activity.ActivityLogEntry
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.t8rin.imagetoolbox.core.utils.filename
import com.t8rin.imagetoolbox.core.utils.path
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.compress.screenLogic.CompressPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf.screenLogic.ImagesToPdfComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.merge.screenLogic.MergePdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers.screenLogic.PageNumbersPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_to_images.screenLogic.ExtractPagesPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.preview.screenLogic.PreviewPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.protect.screenLogic.ProtectPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rearrange.screenLogic.RearrangePdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.remove_pages.screenLogic.RemovePagesPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rotate.screenLogic.RotatePdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature.screenLogic.SignaturePdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.split.screenLogic.SplitPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.unlock.screenLogic.UnlockPdfToolComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.watermark.screenLogic.WatermarkPdfToolComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PdfRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.PdfTools.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val pdfPreviewFactory: PreviewPdfToolComponent.Factory,
    private val pdfToImagesFactory: ExtractPagesPdfToolComponent.Factory,
    private val imagesToPdfFactory: ImagesToPdfComponent.Factory,
    private val mergePdfFactory: MergePdfToolComponent.Factory,
    private val splitPdfFactory: SplitPdfToolComponent.Factory,
    private val compressPdfFactory: CompressPdfToolComponent.Factory,
    private val rotatePdfFactory: RotatePdfToolComponent.Factory,
    private val removePagesPdfFactory: RemovePagesPdfToolComponent.Factory,
    private val rearrangePdfFactory: RearrangePdfToolComponent.Factory,
    private val protectPdfFactory: ProtectPdfToolComponent.Factory,
    private val unlockPdfFactory: UnlockPdfToolComponent.Factory,
    private val pageNumbersPdfFactory: PageNumbersPdfToolComponent.Factory,
    private val watermarkPdfFactory: WatermarkPdfToolComponent.Factory,
    private val signaturePdfFactory: SignaturePdfToolComponent.Factory,
    private val activityLogRepository: ActivityLogRepository,
    private val recentAccessRepository: RecentAccessRepository,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _recentDocs: MutableState<List<ActivityLogEntry>> = mutableStateOf(emptyList())
    val recentDocs: List<ActivityLogEntry> by _recentDocs

    private val _recentPdfs: MutableState<List<RecentAccessEntity>> = mutableStateOf(emptyList())
    val recentPdfs: List<RecentAccessEntity> by _recentPdfs

    init {
        loadRecentDocs()
        observeRecentPdfs()
    }

    private fun observeRecentPdfs() {
        componentScope.launch {
            recentAccessRepository
                .observeByType(RecentAccessRepository.TYPE_FILE, limit = 100)
                .collect { entries ->
                    _recentPdfs.update {
                        entries.filter { entity ->
                            entity.displayName.lowercase().endsWith(".pdf")
                        }
                    }
                }
        }
    }

    fun recordPdfAccess(uri: Uri) {
        componentScope.launch(ioDispatcher) {
            val fileName = uri.filename().orEmpty().ifBlank { "PDF" }
            recentAccessRepository.recordAccess(
                uri = uri.toString(),
                displayName = fileName,
                accessType = RecentAccessRepository.TYPE_FILE,
                pathHint = uri.path(),
            )
        }
    }

    private fun loadRecentDocs() {
        componentScope.launch {
            val docs = activityLogRepository.getRecentByScreenRoute(
                routePrefix = SCREEN_ROUTE_PREFIX,
                limit = 20
            )
            _recentDocs.update { docs }
        }
    }

    val child: PdfChild = when (type) {
        is Screen.PdfTools.Type.Preview -> PdfChild.Preview(
            pdfPreviewFactory(
                componentContext = componentContext.childContext("pdf_preview"),
                initialUri = type.pdfUri,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.PdfToImages -> PdfChild.PdfToImages(
            pdfToImagesFactory(
                componentContext = componentContext.childContext("pdf_to_images"),
                initialUri = type.pdfUri,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.ImagesToPdf -> PdfChild.ImagesToPdf(
            imagesToPdfFactory(
                componentContext.childContext("images_to_pdf"),
                type.imageUris,
                onGoBack,
                onNavigate,
            )
        )

        is Screen.PdfTools.Type.Merge -> PdfChild.Merge(
            mergePdfFactory(
                initialUris = type.pdfUris,
                componentContext = componentContext.childContext("merge_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Split -> PdfChild.Split(
            splitPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("split_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Compress -> PdfChild.Compress(
            compressPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("compress_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Rotate -> PdfChild.Rotate(
            rotatePdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("rotate_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.RemovePages -> PdfChild.RemovePages(
            removePagesPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("remove_pages_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Rearrange -> PdfChild.Rearrange(
            rearrangePdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("rearrange_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Protect -> PdfChild.Protect(
            protectPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("protect_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Unlock -> PdfChild.Unlock(
            unlockPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("unlock_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.PageNumbers -> PdfChild.PageNumbers(
            pageNumbersPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("page_numbers_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Watermark -> PdfChild.Watermark(
            watermarkPdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("watermark_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.PdfTools.Type.Signature -> PdfChild.Signature(
            signaturePdfFactory(
                initialUri = type.pdfUri,
                componentContext = componentContext.childContext("signature_pdf"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        null -> PdfChild.Picker
    }

    sealed interface PdfChild {
        data object Picker : PdfChild
        class Preview(val component: PreviewPdfToolComponent) : PdfChild
        class PdfToImages(val component: ExtractPagesPdfToolComponent) : PdfChild
        class ImagesToPdf(val component: ImagesToPdfComponent) : PdfChild
        class Merge(val component: MergePdfToolComponent) : PdfChild
        class Split(val component: SplitPdfToolComponent) : PdfChild
        class Compress(val component: CompressPdfToolComponent) : PdfChild
        class Rotate(val component: RotatePdfToolComponent) : PdfChild
        class RemovePages(val component: RemovePagesPdfToolComponent) : PdfChild
        class Rearrange(val component: RearrangePdfToolComponent) : PdfChild
        class Protect(val component: ProtectPdfToolComponent) : PdfChild
        class Unlock(val component: UnlockPdfToolComponent) : PdfChild
        class PageNumbers(val component: PageNumbersPdfToolComponent) : PdfChild
        class Watermark(val component: WatermarkPdfToolComponent) : PdfChild
        class Signature(val component: SignaturePdfToolComponent) : PdfChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.PdfTools.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PdfRouterComponent
    }

    companion object {
        private const val SCREEN_ROUTE_PREFIX = "pdf"
    }
}
