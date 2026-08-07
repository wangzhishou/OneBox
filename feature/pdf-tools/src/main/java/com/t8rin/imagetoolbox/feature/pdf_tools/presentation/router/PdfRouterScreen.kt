package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router

import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.compress.CompressPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.images_to_pdf.ImagesToPdfScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.merge.MergePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_preview.PdfPreviewScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.pdf_to_images.PdfToImagesScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rearrange.RearrangePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.remove_pages.RemovePagesPdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.rotate.RotatePdfToolContent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic.PdfRouterComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.split.SplitPdfToolContent

@Composable
fun PdfRouterScreen(component: PdfRouterComponent) {
    when (val child = component.child) {
        is PdfRouterComponent.PdfChild.Picker -> PdfPickerLandingScreen(
            onPick = { type -> component.onNavigate(Screen.PdfTools(type)) },
            onGoBack = component.onGoBack,
            recentDocs = component.recentDocs,
            recentPdfs = component.recentPdfs,
            onRecordAccess = component::recordPdfAccess,
            searchQuery = component.searchQuery,
            onSearchQueryChange = component::onSearchQueryChange,
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
    }
}
