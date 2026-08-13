package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import javax.inject.Inject

import com.shifenmiao.feature.document.component.DocConvertTaskListComponent
import com.shifenmiao.feature.document.component.OcrTaskListComponent
import com.t8rin.imagetoolbox.collage_maker.presentation.screenLogic.CollageMakerComponent
import com.t8rin.imagetoolbox.color_tools.presentation.screenLogic.ColorToolsComponent
import com.t8rin.imagetoolbox.feature.apng_tools.presentation.screenLogic.ApngToolsComponent
import com.t8rin.imagetoolbox.feature.ascii_art.presentation.screenLogic.AsciiArtComponent
import com.t8rin.imagetoolbox.feature.audio_cover_extractor.ui.screenLogic.AudioCoverExtractorComponent
import com.t8rin.imagetoolbox.feature.base64_tools.presentation.screenLogic.Base64ToolsComponent
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.screenLogic.ChecksumToolsComponent
import com.t8rin.imagetoolbox.feature.cipher.presentation.screenLogic.CipherComponent
import com.t8rin.imagetoolbox.feature.compare.presentation.screenLogic.CompareComponent
import com.t8rin.imagetoolbox.feature.crop.presentation.screenLogic.CropComponent
import com.t8rin.imagetoolbox.feature.delete_exif.presentation.screenLogic.DeleteExifComponent
import com.t8rin.imagetoolbox.feature.document_scanner.presentation.screenLogic.DocumentScannerComponent
import com.t8rin.imagetoolbox.feature.draw.presentation.screenLogic.DrawComponent
import com.t8rin.imagetoolbox.feature.edit_exif.presentation.screenLogic.EditExifComponent
import com.t8rin.imagetoolbox.feature.filters.presentation.screenLogic.FiltersComponent
import com.t8rin.imagetoolbox.feature.format_conversion.presentation.screenLogic.FormatConversionComponent
import com.t8rin.imagetoolbox.feature.gif_tools.presentation.screenLogic.GifToolsComponent
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent
import com.t8rin.imagetoolbox.feature.image_preview.presentation.screenLogic.ImagePreviewComponent
import com.t8rin.imagetoolbox.feature.image_stacking.presentation.screenLogic.ImageStackingComponent
import com.t8rin.imagetoolbox.feature.image_stitch.presentation.screenLogic.ImageStitchingComponent
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.screenLogic.LibrariesInfoComponent
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.screenLogic.LimitsResizeComponent
import com.t8rin.imagetoolbox.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.t8rin.imagetoolbox.feature.mesh_gradients.presentation.screenLogic.MeshGradientsComponent
import com.t8rin.imagetoolbox.feature.palette_tools.presentation.screenLogic.PaletteToolsComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic.PdfRouterComponent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import com.t8rin.imagetoolbox.feature.resize_convert.presentation.screenLogic.ResizeAndConvertComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanQrCodeComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanCodeComponent
import com.t8rin.imagetoolbox.feature.single_edit.presentation.screenLogic.SingleEditComponent
import com.t8rin.imagetoolbox.feature.svg_maker.presentation.screenLogic.SvgMakerComponent
import com.t8rin.imagetoolbox.feature.wallpapers_export.presentation.screenLogic.WallpapersExportComponent
import com.t8rin.imagetoolbox.feature.watermarking.presentation.screenLogic.WatermarkingComponent
import com.t8rin.imagetoolbox.feature.webp_tools.presentation.screenLogic.WebpToolsComponent
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.screenLogic.WeightResizeComponent
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipComponent
import com.t8rin.imagetoolbox.image_cutting.presentation.screenLogic.ImageCutterComponent
import com.t8rin.imagetoolbox.image_splitting.presentation.screenLogic.ImageSplitterComponent
import com.t8rin.imagetoolbox.library_details.presentation.screenLogic.LibraryDetailsComponent
import com.t8rin.imagetoolbox.noise_generation.presentation.screenLogic.NoiseGenerationComponent
import com.wanbaohe.imageviewer.screenLogic.ImageViewerComponent

/**
 * 启动期按需解析的工厂集合——Image 组。
 *
 * 由 [ChildProvider] 通过 `Provider<ImageNavigationFactories>` 持有，
 * 仅当用户首次进入本组对应 screen 时才触发 Hilt 解析本类字段，
 * 避免冷启时一次性解析全部 110 个 binding 节点。
 */
class ImageNavigationFactories @Inject constructor(
    val apngToolsComponentFactory: ApngToolsComponent.Factory,
    val asciiArtComponentFactory: AsciiArtComponent.Factory,
    val audioCoverExtractorComponentFactory: AudioCoverExtractorComponent.Factory,
    val base64ToolsComponentFactory: Base64ToolsComponent.Factory,
    val checksumToolsComponentFactory: ChecksumToolsComponent.Factory,
    val cipherComponentFactory: CipherComponent.Factory,
    val collageMakerComponentFactory: CollageMakerComponent.Factory,
    val compareComponentFactory: CompareComponent.Factory,
    val cropComponentFactory: CropComponent.Factory,
    val deleteExifComponentFactory: DeleteExifComponent.Factory,
    val documentScannerComponentFactory: DocumentScannerComponent.Factory,
    val drawComponentFactory: DrawComponent.Factory,
    val editExifComponentFactory: EditExifComponent.Factory,
    val filtersComponentFactory: FiltersComponent.Factory,
    val formatConversionComponentFactory: FormatConversionComponent.Factory,
    val gifToolsComponentFactory: GifToolsComponent.Factory,
    val gradientMakerComponentFactory: GradientMakerComponent.Factory,
    val imagePreviewComponentFactory: ImagePreviewComponent.Factory,
    val imageSplittingComponentFactory: ImageSplitterComponent.Factory,
    val imageStackingComponentFactory: ImageStackingComponent.Factory,
    val imageStitchingComponentFactory: ImageStitchingComponent.Factory,
    val imageCutterComponentFactory: ImageCutterComponent.Factory,
    val limitResizeComponentFactory: LimitsResizeComponent.Factory,
    val loadNetImageComponentFactory: LoadNetImageComponent.Factory,
    val noiseGenerationComponentFactory: NoiseGenerationComponent.Factory,
    val pdfRouterComponentFactory: PdfRouterComponent.Factory,
    val paletteToolsComponentFactory: PaletteToolsComponent.Factory,
    val pickColorFromImageComponentFactory: PickColorFromImageComponent.Factory,
    val resizeAndConvertComponentFactory: ResizeAndConvertComponent.Factory,
    val scanCodeComponentFactory: ScanCodeComponent.Factory,
    val scanQrCodeComponentFactory: ScanQrCodeComponent.Factory,
    val singleEditComponentFactory: SingleEditComponent.Factory,
    val svgMakerComponentFactory: SvgMakerComponent.Factory,
    val wallpapersExportComponentFactory: WallpapersExportComponent.Factory,
    val watermarkingComponentFactory: WatermarkingComponent.Factory,
    val webpToolsComponentFactory: WebpToolsComponent.Factory,
    val weightResizeComponentFactory: WeightResizeComponent.Factory,
    val zipComponentFactory: ZipComponent.Factory,
    val markupLayersComponentFactory: MarkupLayersComponent.Factory,
    val meshGradientsComponentFactory: MeshGradientsComponent.Factory,
    val librariesInfoComponentFactory: LibrariesInfoComponent.Factory,
    val libraryDetailsComponentFactory: LibraryDetailsComponent.Factory,
    val colorToolsComponentFactory: ColorToolsComponent.Factory,
    val imageViewerComponent: ImageViewerComponent.Factory,
    val ocrTaskListComponent: OcrTaskListComponent.Factory,
    val docConvertTaskListComponent: DocConvertTaskListComponent.Factory,
)
