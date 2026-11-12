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

package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router

import androidx.compose.runtime.Composable
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.compress.CompressPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf.ImagesToPdfScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.merge.MergePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.page_numbers.PageNumbersPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_preview.PdfPreviewScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_to_images.PdfToImagesScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.protect.ProtectPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rearrange.RearrangePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.remove_pages.RemovePagesPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rotate.RotatePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic.PdfRouterComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.signature.SignaturePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.split.SplitPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.unlock.UnlockPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.watermark.WatermarkPdfToolContent

@Composable
fun PdfRouterScreen(component: PdfRouterComponent) {
    when (val child = component.child) {
        is PdfRouterComponent.PdfChild.Picker -> PdfPickerLandingScreen(
            onPick = { type -> component.onNavigate(Screen.PdfTools(type)) },
            onGoBack = component.onGoBack,
            recentDocs = component.recentDocs,
            recentPdfs = component.recentPdfs,
            onRecordAccess = component::recordPdfAccess,
        )

        is PdfRouterComponent.PdfChild.Preview -> PdfPreviewScreen(child.component)
        is PdfRouterComponent.PdfChild.PdfToImages -> PdfToImagesScreen(child.component)
        is PdfRouterComponent.PdfChild.ImagesToPdf -> ImagesToPdfScreen(child.component)
        is PdfRouterComponent.PdfChild.Merge -> MergePdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Split -> SplitPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Compress -> CompressPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Rotate -> RotatePdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.RemovePages -> RemovePagesPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Rearrange -> RearrangePdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Protect -> ProtectPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Unlock -> UnlockPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.PageNumbers -> PageNumbersPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Watermark -> WatermarkPdfToolContent(child.component)
        is PdfRouterComponent.PdfChild.Signature -> SignaturePdfToolContent(child.component)
    }
}
