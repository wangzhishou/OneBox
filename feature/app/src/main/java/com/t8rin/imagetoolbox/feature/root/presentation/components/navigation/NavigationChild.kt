/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.component.AIDuelChatComponent
import com.shifenmiao.ai.component.AIHistoryCenterComponent
import com.shifenmiao.ai.component.AgentComponent
import com.shifenmiao.ai.component.AgentDetailComponent
import com.shifenmiao.ai.component.AgentJsonEditorComponent
import com.shifenmiao.ai.component.CreateAIAgentComponent
import com.shifenmiao.ai.component.CreateAIPromptComponent
import com.shifenmiao.ai.component.TokenUsageComponent
import com.shifenmiao.ai.image.controllers.AIImageComponent
import com.shifenmiao.ai.image.screen.AIGCImageScreen
import com.shifenmiao.ai.screen.AIChatScreen
import com.shifenmiao.ai.screen.AIHistoryCenterScreen
import com.shifenmiao.ai.screen.AgentDetailScreen
import com.shifenmiao.ai.screen.AgentScreen
import com.shifenmiao.ai.screen.AgentJsonEditorScreen
import com.shifenmiao.ai.screen.CreateAIAgentScreen
import com.shifenmiao.ai.screen.CreateAIPromptScreen
import com.shifenmiao.ai.screen.TokenUsageScreen
import com.shifenmiao.common.components.category.ReorderableComponent
import com.shifenmiao.common.components.category.ReorderableScreen
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.feature.document.component.DocConvertTaskListComponent
import com.shifenmiao.feature.document.component.OcrTaskListComponent
import com.shifenmiao.feature.document.screen.DocConvertTaskListScreen
import com.shifenmiao.feature.document.screen.OcrTaskListScreen
import com.shifenmiao.lifetime.component.LifeTimeAddCountdownComponent
import com.shifenmiao.lifetime.component.LifeTimeAddEventComponent
import com.shifenmiao.lifetime.component.LifeTimeAddMilestoneComponent
import com.shifenmiao.lifetime.component.LifeTimeComponent
import com.shifenmiao.lifetime.component.LifeTimeCountdownDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeMilestoneDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeSettingsComponent
import com.shifenmiao.lifetime.component.LifeTimeWelcomeComponent
import com.shifenmiao.lifetime.screen.LifeTimeAddCountdownScreen
import com.shifenmiao.lifetime.screen.LifeTimeAddEventScreen
import com.shifenmiao.lifetime.screen.LifeTimeAddMilestoneScreen
import com.shifenmiao.lifetime.screen.LifeTimeCountdownDetailScreen
import com.shifenmiao.lifetime.screen.LifeTimeMilestoneDetailScreen
import com.shifenmiao.lifetime.screen.LifeTimeScreen
import com.shifenmiao.lifetime.screen.LifeTimeSettingsScreen
import com.shifenmiao.lifetime.screen.LifeTimeWelcomeScreen
import com.shifenmiao.login.LoginScreen
import com.shifenmiao.login.RegistrationScreen
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.marktodo.screen.MarkTodoRouterScreen
import com.shifenmiao.marktodo.screenLogic.MarkTodoRouterComponent
import com.shifenmiao.marquee.screen.FullScreenSubtitles
import com.wanbaohe.passwordvault.router.PasswordVaultRouterScreen
import com.wanbaohe.passwordvault.router.screenLogic.PasswordVaultRouterComponent
import com.wanbaohe.a2ui.A2uiScreen
import com.wanbaohe.a2ui.viewModel.A2uiComponent
import com.wanbaohe.blessingwall.component.BlessingRecordComponent
import com.wanbaohe.blessingwall.component.BlessingWallComponent
import com.wanbaohe.poem.component.PoemComponent
import com.wanbaohe.poem.component.PoemSearchComponent
import com.wanbaohe.poem.screen.PoemScreen
import com.wanbaohe.poem.screen.PoemSearchScreen
import com.wanbaohe.blessingwall.screen.BlessingRecordScreen
import com.wanbaohe.blessingwall.screen.BlessingWallScreen
import com.shifenmiao.marquee.screen.MarqueeScreen
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.online.component.PlaygroundComponent
import com.shifenmiao.online.component.PreviewHtmlComponent
import com.shifenmiao.online.screen.CreateHtmlScreen
import com.shifenmiao.online.screen.CreateNoteScreen
import com.shifenmiao.online.screen.EditPromptScreen
import com.shifenmiao.online.screen.MiniProgramScreen
import com.shifenmiao.online.screen.NoteItemScreen
import com.shifenmiao.online.screen.PreviewHtmlScreen
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.search.screen.SearchScreen
import com.shifenmiao.webview.BaseWebViewScreen
import com.shifenmiao.webview.MarkdownRenderWebViewScreen
import com.shifenmiao.webview.WebViewComponent
import com.t8rin.imagetoolbox.collage_maker.presentation.CollageMakerContent
import com.t8rin.imagetoolbox.collage_maker.presentation.screenLogic.CollageMakerComponent
import com.t8rin.imagetoolbox.color_tools.presentation.ColorToolsContent
import com.t8rin.imagetoolbox.color_tools.presentation.screenLogic.ColorToolsComponent
import com.t8rin.imagetoolbox.feature.apng_tools.presentation.ApngToolsContent
import com.t8rin.imagetoolbox.feature.apng_tools.presentation.screenLogic.ApngToolsComponent
import com.t8rin.imagetoolbox.feature.ascii_art.presentation.AsciiArtContent
import com.t8rin.imagetoolbox.feature.ascii_art.presentation.screenLogic.AsciiArtComponent
import com.t8rin.imagetoolbox.feature.audio_cover_extractor.ui.AudioCoverExtractorContent
import com.t8rin.imagetoolbox.feature.audio_cover_extractor.ui.screenLogic.AudioCoverExtractorComponent
import com.t8rin.imagetoolbox.feature.base64_tools.presentation.Base64ToolsContent
import com.t8rin.imagetoolbox.feature.base64_tools.presentation.screenLogic.Base64ToolsComponent
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.ChecksumToolsContent
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.screenLogic.ChecksumToolsComponent
import com.t8rin.imagetoolbox.feature.cipher.presentation.CipherContent
import com.t8rin.imagetoolbox.feature.cipher.presentation.screenLogic.CipherComponent
import com.t8rin.imagetoolbox.feature.compare.presentation.CompareContent
import com.t8rin.imagetoolbox.feature.compare.presentation.screenLogic.CompareComponent
import com.t8rin.imagetoolbox.feature.crop.presentation.CropContent
import com.t8rin.imagetoolbox.feature.crop.presentation.screenLogic.CropComponent
import com.t8rin.imagetoolbox.feature.delete_exif.presentation.DeleteExifContent
import com.t8rin.imagetoolbox.feature.delete_exif.presentation.screenLogic.DeleteExifComponent
import com.t8rin.imagetoolbox.feature.document_scanner.presentation.DocumentScannerContent
import com.t8rin.imagetoolbox.feature.document_scanner.presentation.screenLogic.DocumentScannerComponent
import com.t8rin.imagetoolbox.feature.draw.presentation.DrawContent
import com.t8rin.imagetoolbox.feature.draw.presentation.screenLogic.DrawComponent
import com.t8rin.imagetoolbox.feature.edit_exif.presentation.EditExifContent
import com.t8rin.imagetoolbox.feature.edit_exif.presentation.screenLogic.EditExifComponent
import com.t8rin.imagetoolbox.feature.filters.presentation.FiltersContent
import com.t8rin.imagetoolbox.feature.filters.presentation.screenLogic.FiltersComponent
import com.t8rin.imagetoolbox.feature.format_conversion.presentation.FormatConversionContent
import com.t8rin.imagetoolbox.feature.format_conversion.presentation.screenLogic.FormatConversionComponent
import com.t8rin.imagetoolbox.feature.gif_tools.presentation.GifToolsContent
import com.t8rin.imagetoolbox.feature.gif_tools.presentation.screenLogic.GifToolsComponent
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.GradientMakerContent
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent
import com.t8rin.imagetoolbox.feature.image_preview.presentation.ImagePreviewContent
import com.t8rin.imagetoolbox.feature.image_preview.presentation.screenLogic.ImagePreviewComponent
import com.t8rin.imagetoolbox.feature.image_stacking.presentation.ImageStackingContent
import com.t8rin.imagetoolbox.feature.image_stacking.presentation.screenLogic.ImageStackingComponent
import com.t8rin.imagetoolbox.feature.image_stitch.presentation.ImageStitchingContent
import com.t8rin.imagetoolbox.feature.image_stitch.presentation.screenLogic.ImageStitchingComponent
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.LibrariesInfoContent
import com.t8rin.imagetoolbox.feature.libraries_info.presentation.screenLogic.LibrariesInfoComponent
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.LimitsResizeContent
import com.t8rin.imagetoolbox.feature.limits_resize.presentation.screenLogic.LimitsResizeComponent
import com.t8rin.imagetoolbox.feature.load_net_image.presentation.LoadNetImageContent
import com.t8rin.imagetoolbox.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent
import com.wanbaohe.markuplayers.presentation.MarkupLayersContent
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.textcard.presentation.TextCardContent
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.t8rin.imagetoolbox.feature.mesh_gradients.presentation.MeshGradientsContent
import com.t8rin.imagetoolbox.feature.mesh_gradients.presentation.screenLogic.MeshGradientsComponent
import com.t8rin.imagetoolbox.feature.palette_tools.presentation.PaletteToolsContent
import com.t8rin.imagetoolbox.feature.palette_tools.presentation.screenLogic.PaletteToolsComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.PdfRouterScreen
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic.PdfRouterComponent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.PickColorFromImageContent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import com.t8rin.imagetoolbox.feature.resize_convert.presentation.ResizeAndConvertContent
import com.t8rin.imagetoolbox.feature.resize_convert.presentation.screenLogic.ResizeAndConvertComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.ScanCodeContent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.ScanQrCodeContent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanCodeComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanQrCodeComponent
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.t8rin.imagetoolbox.feature.single_edit.presentation.SingleEditContent
import com.t8rin.imagetoolbox.feature.single_edit.presentation.screenLogic.SingleEditComponent
import com.t8rin.imagetoolbox.feature.svg_maker.presentation.SvgMakerContent
import com.t8rin.imagetoolbox.feature.svg_maker.presentation.screenLogic.SvgMakerComponent
import com.t8rin.imagetoolbox.feature.wallpapers_export.presentation.WallpapersExportContent
import com.t8rin.imagetoolbox.feature.wallpapers_export.presentation.screenLogic.WallpapersExportComponent
import com.t8rin.imagetoolbox.feature.watermarking.presentation.WatermarkingContent
import com.t8rin.imagetoolbox.feature.watermarking.presentation.screenLogic.WatermarkingComponent
import com.t8rin.imagetoolbox.feature.webp_tools.presentation.WebpToolsContent
import com.t8rin.imagetoolbox.feature.webp_tools.presentation.screenLogic.WebpToolsComponent
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.WeightResizeContent
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.screenLogic.WeightResizeComponent
import com.t8rin.imagetoolbox.feature.zip.presentation.ZipContent
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipComponent
import com.t8rin.imagetoolbox.image_cutting.presentation.ImageCutterContent
import com.t8rin.imagetoolbox.image_cutting.presentation.screenLogic.ImageCutterComponent
import com.t8rin.imagetoolbox.image_splitting.presentation.ImageSplitterContent
import com.t8rin.imagetoolbox.image_splitting.presentation.screenLogic.ImageSplitterComponent
import com.t8rin.imagetoolbox.library_details.presentation.LibraryDetailsContent
import com.t8rin.imagetoolbox.library_details.presentation.screenLogic.LibraryDetailsComponent
import com.t8rin.imagetoolbox.noise_generation.presentation.NoiseGenerationContent
import com.t8rin.imagetoolbox.noise_generation.presentation.screenLogic.NoiseGenerationComponent
import com.wanbaohe.altitude.component.AltitudeComponent
import com.wanbaohe.altitude.screen.AltitudeScreen
import com.wanbaohe.app.app_screen.FavoriteScreen
import com.wanbaohe.app.app_screen.NewAppScreen
import com.wanbaohe.app.component.ActivityLogComponent
import com.wanbaohe.app.component.FavoriteComponent
import com.wanbaohe.blog.logic.BlogComponent
import com.wanbaohe.blog.logic.CreateFeedbackComponent
import com.wanbaohe.blog.screen.BlogDetailScreen
import com.wanbaohe.blog.screen.CreateFeedbackScreen
import com.wanbaohe.blog.screen.FeedbackScreen
import com.wanbaohe.bookkeeping.router.BookkeepingRouterScreen
import com.wanbaohe.bookkeeping.router.screenLogic.BookkeepingRouterComponent
import com.wanbaohe.habittracker.router.HabitTrackerRouterScreen
import com.wanbaohe.habittracker.router.screenLogic.HabitTrackerRouterComponent
import com.wanbaohe.calendar.router.CalendarRouterScreen
import com.wanbaohe.calendar.router.screenLogic.CalendarRouterComponent
import com.wanbaohe.camera.watermark.presentation.screen.CameraWatermarkScreen
import com.wanbaohe.camera.watermark.presentation.screenLogic.CameraWatermarkComponent
import com.wanbaohe.compass.component.CompassComponent
import com.wanbaohe.compass.screen.CompassScreen
import com.wanbaohe.cloud.storage.screen.CloudStorageScreen
import com.wanbaohe.measurement.component.MeasurementComponent
import com.wanbaohe.measurement.screen.MeasurementScreen
import com.wanbaohe.cloud.storage.screenLogic.CloudStorageComponent
import com.wanbaohe.deadpixeltest.component.DeadPixelTestComponent
import com.wanbaohe.deadpixeltest.screen.DeadPixelTestScreen
import com.wanbaohe.decisionwheel.component.DecisionWheelComponent
import com.wanbaohe.decisionwheel.screen.DecisionWheelScreen
import com.wanbaohe.diceroller.component.DiceRollerComponent
import com.wanbaohe.diceroller.screen.DiceRollerScreen
import com.wanbaohe.iching.component.IChingDivinationComponent
import com.wanbaohe.iching.component.IChingHistoryComponent
import com.wanbaohe.iching.screen.IChingDivinationScreen
import com.wanbaohe.iching.screen.IChingHistoryScreen
import com.wanbaohe.file.browser.screen.FileBrowserScreen
import com.wanbaohe.file.browser.screenLogic.FileBrowserComponent
import com.wanbaohe.file_transfer.screen.FileTransferScreen
import com.wanbaohe.file_transfer.screenLogic.FileTransferComponent
import com.wanbaohe.game2048.component.Game2048Component
import com.wanbaohe.game2048.screen.Game2048Screen
import com.wanbaohe.xiangqi.router.XiangqiRouterScreen
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.shifenmiao.webview.browser.BrowserComponent
import com.shifenmiao.webview.browser.BrowserScreen
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.survive30s.screen.Survive30sScreen
import com.wanbaohe.idphoto.presentation.screen.IdPhotoScreen
import com.wanbaohe.idphoto.presentation.screenLogic.IdPhotoComponent
import com.wanbaohe.imageviewer.components.ImageViewerScreen
import com.wanbaohe.imageviewer.screenLogic.ImageViewerComponent
import com.wanbaohe.loancalculator.LoanCalculatorScreen
import com.wanbaohe.markdown.edit.component.MarkdownEditorComponent
import com.wanbaohe.markdown.edit.screen.MarkdownEditorScreen
import com.wanbaohe.profile.ProfileScreen
import com.wanbaohe.profile.screen.AboutModelScreen
import com.wanbaohe.profile.screen.AboutUsScreen
import com.wanbaohe.profile.screen.BuyCoffeeScreen
import com.wanbaohe.profile.screen.CommunityScreen
import com.wanbaohe.profile.screen.UserInfoScreen
import com.wanbaohe.profile.screen.VipLevelScreen
import com.wanbaohe.profile.viewmodel.PayComponent
import com.wanbaohe.schedule.component.ScheduleComponent
import com.wanbaohe.schedule.screen.ScheduleScreen
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.screen.DshRootScreen
import com.wanbaohe.speedtest.component.SpeedTestComponent
import com.wanbaohe.speedtest.screen.SpeedTestScreen
import com.wanbaohe.setting.router.SettingRouterScreen
import com.wanbaohe.setting.router.screenLogic.SettingRouterComponent
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.teleprompter.screen.TeleprompterScreen
import com.wanbaohe.unitconverter.component.UnitConverterComponent
import com.wanbaohe.unitconverter.screen.UnitConverterScreen


sealed interface NavigationChild {

    @Composable
    fun Content()


    class ApngTools(private val component: ApngToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = ApngToolsContent(component)
    }

    class Cipher(private val component: CipherComponent) : NavigationChild {
        @Composable
        override fun Content() = CipherContent(component)
    }

    class CollageMaker(private val component: CollageMakerComponent) : NavigationChild {
        @Composable
        override fun Content() = CollageMakerContent(component)
    }

    class ColorTools(private val component: ColorToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = ColorToolsContent(component)
    }

    class Compare(private val component: CompareComponent) : NavigationChild {
        @Composable
        override fun Content() = CompareContent(component)
    }

    class Crop(private val component: CropComponent) : NavigationChild {
        @Composable
        override fun Content() = CropContent(component)
    }

    class DeleteExif(private val component: DeleteExifComponent) : NavigationChild {
        @Composable
        override fun Content() = DeleteExifContent(component)
    }

    class DocumentScanner(private val component: DocumentScannerComponent) : NavigationChild {
        @Composable
        override fun Content() = DocumentScannerContent(component)
    }

    class Draw(private val component: DrawComponent) : NavigationChild {
        @Composable
        override fun Content() = DrawContent(component)
    }

    class Filter(private val component: FiltersComponent) : NavigationChild {
        @Composable
        override fun Content() = FiltersContent(component)
    }

    class FormatConversion(private val component: FormatConversionComponent) : NavigationChild {
        @Composable
        override fun Content() = FormatConversionContent(component)
    }

    class PaletteTools(private val component: PaletteToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = PaletteToolsContent(component)
    }

    class GifTools(private val component: GifToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = GifToolsContent(component)
    }

    class GradientMaker(private val component: GradientMakerComponent) : NavigationChild {
        @Composable
        override fun Content() = GradientMakerContent(component)
    }

    class ImagePreview(private val component: ImagePreviewComponent) : NavigationChild {
        @Composable
        override fun Content() = ImagePreviewContent(component)
    }

    class ImageSplitting(private val component: ImageSplitterComponent) : NavigationChild {
        @Composable
        override fun Content() = ImageSplitterContent(component)
    }

    class ImageStacking(private val component: ImageStackingComponent) : NavigationChild {
        @Composable
        override fun Content() = ImageStackingContent(component)
    }

    class ImageStitching(private val component: ImageStitchingComponent) : NavigationChild {
        @Composable
        override fun Content() = ImageStitchingContent(component)
    }

    class LimitResize(private val component: LimitsResizeComponent) : NavigationChild {
        @Composable
        override fun Content() = LimitsResizeContent(component)
    }

    class LoadNetImage(private val component: LoadNetImageComponent) : NavigationChild {
        @Composable
        override fun Content() = LoadNetImageContent(component)
    }

    class NoiseGeneration(private val component: NoiseGenerationComponent) : NavigationChild {
        @Composable
        override fun Content() = NoiseGenerationContent(component)
    }

    class PdfTools(private val component: PdfRouterComponent) : NavigationChild {
        @Composable
        override fun Content() = PdfRouterScreen(component)
    }

    class Xiangqi(private val component: XiangqiRouterComponent) : NavigationChild {
        @Composable
        override fun Content() = XiangqiRouterScreen(component)
    }

    class PickColorFromImage(private val component: PickColorFromImageComponent) : NavigationChild {
        @Composable
        override fun Content() = PickColorFromImageContent(component)
    }

    class ResizeAndConvert(private val component: ResizeAndConvertComponent) : NavigationChild {
        @Composable
        override fun Content() = ResizeAndConvertContent(component)
    }

    class ScanCode(private val component: ScanCodeComponent) : NavigationChild {
        @Composable
        override fun Content() = ScanCodeContent(component)
    }

    class ScanQrCode(private val component: ScanQrCodeComponent) : NavigationChild {
        @Composable
        override fun Content() = ScanQrCodeContent(component)
    }

    class SingleEdit(private val component: SingleEditComponent) : NavigationChild {
        @Composable
        override fun Content() = SingleEditContent(component)
    }

    class SvgMaker(private val component: SvgMakerComponent) : NavigationChild {
        @Composable
        override fun Content() = SvgMakerContent(component)
    }

    class Watermarking(private val component: WatermarkingComponent) : NavigationChild {
        @Composable
        override fun Content() = WatermarkingContent(component)
    }

    class WebpTools(private val component: WebpToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = WebpToolsContent(component)
    }

    class WeightResize(private val component: WeightResizeComponent) : NavigationChild {
        @Composable
        override fun Content() = WeightResizeContent(component)
    }

    class Zip(private val component: ZipComponent) : NavigationChild {
        @Composable
        override fun Content() = ZipContent(component)
    }

    class LibrariesInfo(private val component: LibrariesInfoComponent) : NavigationChild {
        @Composable
        override fun Content() = LibrariesInfoContent(component)
    }

    class MarkupLayers(private val component: MarkupLayersComponent) : NavigationChild {
        @Composable
        override fun Content() = MarkupLayersContent(component)
    }

    class TextCard(private val component: TextCardComponent) : NavigationChild {
        @Composable
        override fun Content() = TextCardContent(component)
    }

    class Base64Tools(private val component: Base64ToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = Base64ToolsContent(component)
    }

    class ChecksumTools(private val component: ChecksumToolsComponent) : NavigationChild {
        @Composable
        override fun Content() = ChecksumToolsContent(component)
    }

    class MeshGradients(private val component: MeshGradientsComponent) : NavigationChild {
        @Composable
        override fun Content() = MeshGradientsContent(component)
    }

    class EditExif(private val component: EditExifComponent) : NavigationChild {
        @Composable
        override fun Content() = EditExifContent(component)
    }

    class ImageCutter(private val component: ImageCutterComponent) : NavigationChild {
        @Composable
        override fun Content() = ImageCutterContent(component)
    }

    class AudioCoverExtractor(
        private val component: AudioCoverExtractorComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = AudioCoverExtractorContent(component)
    }

    class LibraryDetails(private val component: LibraryDetailsComponent) : NavigationChild {
        @Composable
        override fun Content() = LibraryDetailsContent(component)
    }

    class WallpapersExport(private val component: WallpapersExportComponent) : NavigationChild {
        @Composable
        override fun Content() = WallpapersExportContent(component)
    }

    class DataSync(private val component: com.shifenmiao.common.export.DataSyncComponent) : NavigationChild {
        @Composable
        override fun Content() = com.shifenmiao.common.export.DataSyncScreen(component)
    }

    class AsciiArt(private val component: AsciiArtComponent) : NavigationChild {
        @Composable
        override fun Content() = AsciiArtContent(component)
    }


    class NewApp(
        private val appComponent: AppComponent,
        private val itemListComponent: ItemListComponent,
        private val playgroundComponent: PlaygroundComponent,
        private val initialTab: HomeTabKey? = null,
    ) : NavigationChild {
        @Composable
        override fun Content() = NewAppScreen(
            appComponent = appComponent,
            itemListComponent = itemListComponent,
            playgroundComponent = playgroundComponent,
            initialTab = initialTab,
        )
    }

    class Online(
        private val activityLogComponent: ActivityLogComponent,
        private val appComponent: AppComponent,
        private val favoriteComponent: FavoriteComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = FavoriteScreen(
            favoriteComponent = favoriteComponent,
            activityLogComponent = activityLogComponent,
            appComponent = appComponent,
        )
    }

    class Profile(
        private val settingsComponent: SettingsComponent,
        private val appComponent: AppComponent,
        private val loginComponent: LoginComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = ProfileScreen(
            settingsComponent = settingsComponent,
            appComponent = appComponent,
            loginComponent = loginComponent
        )
    }


    class Marquee(
        private val appComponent: AppComponent,
        private val component: MarqueeComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = MarqueeScreen(
            onGoBack = appComponent.onGoBack,
            component = component
        )
    }


    class Calendar(
        private val calendarRouterComponent: CalendarRouterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = CalendarRouterScreen(
            component = calendarRouterComponent
        )
    }

    class Schedule(
        private val scheduleComponent: ScheduleComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = ScheduleScreen(
            component = scheduleComponent
        )
    }

    class Feedback(
        private val appComponent: AppComponent,
        private val blogComponent: BlogComponent,
    ) : NavigationChild {
        @Composable
        override fun Content() = FeedbackScreen(
            blogComponent = blogComponent,
            appComponent = appComponent
        )
    }

    class WebView(
        private val webViewComponent: WebViewComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = BaseWebViewScreen(
            webViewComponent = webViewComponent
        )
    }

    class MarkdownRenderWebView(
        private val appComponent: AppComponent,
        private val webViewComponent: WebViewComponent,
        private val initialUri: Uri?
    ) : NavigationChild {
        @Composable
        override fun Content() = MarkdownRenderWebViewScreen(
            onGoBack = appComponent.onGoBack,
            webViewComponent = webViewComponent,
            initialUri = initialUri
        )
    }

    class OpenFilePicker(
        private val appComponent: AppComponent,
        private val screen: Screen.OpenFilePicker
    ) : NavigationChild {
        @Composable
        override fun Content() = OpenFilePickerActionScreen(
            screen = screen,
            appComponent = appComponent
        )
    }

    class MiniProgram(
        private val appComponent: AppComponent,
        private val dataItem: DataItem
    ) : NavigationChild {
        @Composable
        override fun Content() = MiniProgramScreen(
            dataItem = dataItem,
            onGoBack = appComponent.onGoBack,
            appComponent = appComponent
        )
    }

    class Registration(
        private val appComponent: AppComponent,
        private val loginComponent: LoginComponent,
        private val onLoginSuccess: () -> Unit
    ) : NavigationChild {
        @Composable
        override fun Content() = RegistrationScreen(
            loginComponent = loginComponent,
            onLoginSuccess = onLoginSuccess,
            onGoBack = appComponent.onGoBack
        )
    }

    class Login(
        private val appComponent: AppComponent,
        private val loginComponent: LoginComponent,
        private val onLoginSuccess: () -> Unit
    ) : NavigationChild {
        @Composable
        override fun Content() = LoginScreen(
            loginComponent = loginComponent,
            onLoginSuccess = onLoginSuccess,
            onGoBack = appComponent.onGoBack,
            appComponent = appComponent
        )
    }

    class FullScreenSubtitlesChild(
        private val appComponent: AppComponent,
        private val component: MarqueeComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = FullScreenSubtitles(
            onGoBack = appComponent.onGoBack,
            component = component
        )
    }

    class AgentDetail(
        private val appComponent: AppComponent,
        private val agentDetailComponent: AgentDetailComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = AgentDetailScreen(
            agentDetailComponent = agentDetailComponent,
            appComponent = appComponent
        )
    }

    class Agent(
        private val appComponent: AppComponent,
        private val agentComponent: AgentComponent,
        private val isPreview: Boolean,
    ) : NavigationChild {
        @Composable
        override fun Content() = AgentScreen(
            agentComponent = agentComponent,
            appComponent = appComponent,
            isPreview = isPreview,
        )
    }

    class AiChatScreen(
        private val appComponent: AppComponent,
        private val aiChatComponent: AIChatComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = AIChatScreen(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent
        )
    }

    class AIDuelChatScreen(
        private val appComponent: AppComponent,
        private val duelChatComponent: AIDuelChatComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = com.shifenmiao.ai.screen.AIDuelChatScreen(
            appComponent = appComponent,
            duelChatComponent = duelChatComponent
        )
    }

    class AITabChatScreen(
        private val appComponent: AppComponent,
        private val aiChatComponent: AIChatComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = com.shifenmiao.ai.screen.AITabChatScreen(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent
        )
    }

    class AIHistoryCenter(
        private val appComponent: AppComponent,
        private val component: AIHistoryCenterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = AIHistoryCenterScreen(
            appComponent = appComponent,
            component = component
        )
    }

    class AboutAIModel(
        private val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = AboutModelScreen(
            onGoBack = appComponent.onGoBack,
            appComponent = appComponent
        )
    }

    class UserInfo(
        private val appComponent: AppComponent,
        private val loginComponent: LoginComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = UserInfoScreen(
            loginComponent = loginComponent,
            onGoBack = appComponent.onGoBack,
            onNavigateToVipLevel = { appComponent.onNavigate(Screen.VipLevel()) }
        )
    }

    class BuyCoffee(
        private val appComponent: AppComponent,
        private val loginComponent: LoginComponent,
        private val payComponent: PayComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = BuyCoffeeScreen(
            appComponent = appComponent,
            loginComponent = loginComponent,
            payComponent = payComponent,
            onGoBack = appComponent.onGoBack,
            onNavigateToVipLevel = { appComponent.onNavigate(Screen.VipLevel()) }
        )
    }

    class VipLevel(
        private val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = VipLevelScreen(
            onGoBack = appComponent.onGoBack
        )
    }

    class AboutUs(
        private val appComponent: AppComponent,
        private val showContactUs: Boolean = false
    ) : NavigationChild {
        @Composable
        override fun Content() = AboutUsScreen(
            onGoBack = appComponent.onGoBack,
            showContactUs = showContactUs,
            appComponent = appComponent
        )
    }

    class Community(
        private val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = CommunityScreen(
            onGoBack = appComponent.onGoBack,
            appComponent = appComponent
        )
    }

    class LoanCalculator(
        private val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() = LoanCalculatorScreen(
            onGoBack = appComponent.onGoBack
        )
    }

    class AIGCImage(
        private val aiImageComponent: AIImageComponent,
        private val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            AIGCImageScreen(
                appComponent = appComponent,
                aiImageComponent = aiImageComponent
            )
        }
    }

    class Search(
        private val appComponent: AppComponent,
        private val searchComponent: SearchComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            SearchScreen(
                appComponent = appComponent,
                searchComponent = searchComponent
            )
        }
    }

    class BlogDetail(
        private val appComponent: AppComponent,
        private val blogComponent: BlogComponent,
    ) : NavigationChild {
        @Composable
        override fun Content() = BlogDetailScreen(
            blogComponent = blogComponent,
            appComponent = appComponent
        )
    }

    class CreateFeedback(
        private val appComponent: AppComponent,
        private val createFeedbackComponent: CreateFeedbackComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CreateFeedbackScreen(
                appComponent = appComponent,
                createFeedbackComponent = createFeedbackComponent
            )
        }
    }

    class CreateNote(
        val createNoteComponent: CreateNoteComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CreateNoteScreen(
                appComponent = appComponent,
                createNoteComponent = createNoteComponent
            )
        }
    }

    class CreateHtml(
        val createHtmlComponent: CreateHtmlComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CreateHtmlScreen(
                appComponent = appComponent,
                createHtmlComponent = createHtmlComponent
            )
        }
    }

    class NoteItem(
        val noteItemComponent: NoteItemComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            NoteItemScreen(
                appComponent = appComponent,
                noteItemComponent = noteItemComponent
            )
        }
    }

    class PreviewHtml(
        val previewHtmlComponent: PreviewHtmlComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            PreviewHtmlScreen(
                appComponent = appComponent,
                previewHtmlComponent = previewHtmlComponent
            )
        }
    }

    class DecisionWheel(
        val decisionWheelComponent: DecisionWheelComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            DecisionWheelScreen(
                decisionWheelComponent = decisionWheelComponent,
                appComponent = appComponent
            )
        }
    }

    class FileTransfer(
        val fileTransferComponent: FileTransferComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            FileTransferScreen(
                transferComponent = fileTransferComponent,
                appComponent = appComponent
            )
        }
    }

    class Reorderable(
        val reorderableComponent: ReorderableComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            ReorderableScreen(
                reorderableComponent = reorderableComponent,
                onGoBack = appComponent.onGoBack
            )
        }
    }

    class FileBrowser(
        val fileBrowserComponent: FileBrowserComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            FileBrowserScreen(
                fileBrowserComponent = fileBrowserComponent,
                onGoBack = appComponent.onGoBack
            )
        }
    }

    class CloudStorage(
        val cloudStorageComponent: CloudStorageComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CloudStorageScreen(
                component = cloudStorageComponent,
                onGoBack = appComponent.onGoBack
            )
        }
    }

    class MarkTodoRouter(
        val markTodoRouterComponent: MarkTodoRouterComponent,
    ) : NavigationChild {
        @Composable
        override fun Content() {
            MarkTodoRouterScreen(
                component = markTodoRouterComponent,
                onGoBack = markTodoRouterComponent.onGoBack
            )
        }
    }

    class PasswordVault(
        val passwordVaultRouterComponent: PasswordVaultRouterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            PasswordVaultRouterScreen(
                component = passwordVaultRouterComponent
            )
        }
    }

    class A2UI(
        val a2uiComponent: A2uiComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            A2uiScreen(component = a2uiComponent)
        }
    }

    class BlessingWall(
        private val component: BlessingWallComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            BlessingWallScreen(component = component)
        }
    }

    class BlessingWallRecord(
        private val component: BlessingRecordComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            BlessingRecordScreen(component = component)
        }
    }

    class Poem(
        private val component: PoemComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            PoemScreen(component = component)
        }
    }

    class PoemSearch(
        private val component: PoemSearchComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            PoemSearchScreen(component = component)
        }
    }

    class Bookkeeping(
        private val component: BookkeepingRouterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            BookkeepingRouterScreen(component = component)
        }
    }

    class HabitTracker(
        private val component: HabitTrackerRouterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            HabitTrackerRouterScreen(component = component)
        }
    }

    class LifeTime(
        val lifeTimeComponent: LifeTimeComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeScreen(component = lifeTimeComponent)
        }
    }

    class LifeTimeSettings(
        val lifeTimeSettingsComponent: LifeTimeSettingsComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeSettingsScreen(component = lifeTimeSettingsComponent)
        }
    }

    class LifeTimeAddEvent(
        val lifeTimeAddEventComponent: LifeTimeAddEventComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeAddEventScreen(component = lifeTimeAddEventComponent)
        }
    }

    class LifeTimeAddMilestone(
        val lifeTimeAddMilestoneComponent: LifeTimeAddMilestoneComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeAddMilestoneScreen(component = lifeTimeAddMilestoneComponent)
        }
    }

    class LifeTimeMilestoneDetail(
        val lifeTimeMilestoneDetailComponent: LifeTimeMilestoneDetailComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeMilestoneDetailScreen(component = lifeTimeMilestoneDetailComponent)
        }
    }

    class LifeTimeAddCountdown(
        val lifeTimeAddCountdownComponent: LifeTimeAddCountdownComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeAddCountdownScreen(component = lifeTimeAddCountdownComponent)
        }
    }

    class LifeTimeCountdownDetail(
        val lifeTimeCountdownDetailComponent: LifeTimeCountdownDetailComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeCountdownDetailScreen(component = lifeTimeCountdownDetailComponent)
        }
    }

    class LifeTimeWelcome(
        val lifeTimeWelcomeComponent: LifeTimeWelcomeComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            LifeTimeWelcomeScreen(component = lifeTimeWelcomeComponent)
        }
    }

    class CameraWatermark(
        val cameraWatermarkComponent: CameraWatermarkComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CameraWatermarkScreen(
                component = cameraWatermarkComponent,
                appComponent = appComponent
            )
        }
    }

    class IdPhoto(
        val idPhotoComponent: IdPhotoComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            IdPhotoScreen(
                component = idPhotoComponent,
                appComponent = appComponent
            )
        }
    }

    class ImageViewer(
        val imageViewerComponent: ImageViewerComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            ImageViewerScreen(
                imageViewerComponent = imageViewerComponent
            )
        }
    }

    class MarkdownEditor(
        val markdownEditorComponent: MarkdownEditorComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            MarkdownEditorScreen(
                component = markdownEditorComponent
            )
        }
    }

    class OcrDocument(
        val ocrTaskListComponent: OcrTaskListComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            OcrTaskListScreen(
                component = ocrTaskListComponent,
                onBack = ocrTaskListComponent.onGoBack
            )
        }
    }

    class DocConvertDocument(
        val docConvertTaskListComponent: DocConvertTaskListComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            DocConvertTaskListScreen(
                component = docConvertTaskListComponent,
                onBack = docConvertTaskListComponent.onGoBack
            )
        }
    }

    class Altitude(
        val altitudeComponent: AltitudeComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            AltitudeScreen(component = altitudeComponent)
        }
    }

    class SpeedTest(
        val speedTestComponent: SpeedTestComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            SpeedTestScreen(component = speedTestComponent)
        }
    }

    class DshClient(
        val dshRootComponent: DshRootComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            DshRootScreen(component = dshRootComponent)
        }
    }

    class UnitConverter(
        val unitConverterComponent: UnitConverterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            UnitConverterScreen(component = unitConverterComponent)
        }
    }

    class Compass(
        val compassComponent: CompassComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CompassScreen(component = compassComponent)
        }
    }

    class MeasurementTools(
        val measurementComponent: MeasurementComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            MeasurementScreen(component = measurementComponent)
        }
    }

    class DeadPixelTest(
        val component: DeadPixelTestComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            DeadPixelTestScreen(component = component)
        }
    }

    class Minesweeper(
        val component: com.wanbaohe.minesweeper.component.MinesweeperComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            com.wanbaohe.minesweeper.screen.MinesweeperScreen(component = component)
        }
    }

    class DiceRoller(
        val diceRollerComponent: DiceRollerComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            DiceRollerScreen(component = diceRollerComponent)
        }
    }

    class IChingDivination(
        val component: IChingDivinationComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            IChingDivinationScreen(component = component)
        }
    }

    class IChingHistory(
        val component: IChingHistoryComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            IChingHistoryScreen(component = component)
        }
    }

    class Game2048(
        val game2048Component: Game2048Component
    ) : NavigationChild {
        @Composable
        override fun Content() {
            Game2048Screen(component = game2048Component)
        }
    }

    class Teleprompter(
        val teleprompterComponent: TeleprompterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            TeleprompterScreen(component = teleprompterComponent)
        }
    }

    class CreateAIAgent(
        val createAIAgentComponent: CreateAIAgentComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CreateAIAgentScreen(component = createAIAgentComponent)
        }
    }

    class CreateAIChatPrompt(
        val createAIPromptComponent: CreateAIPromptComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            CreateAIPromptScreen(component = createAIPromptComponent)
        }
    }

    class AgentJsonEditor(
        val agentJsonEditorComponent: AgentJsonEditorComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            AgentJsonEditorScreen(component = agentJsonEditorComponent)
        }
    }

    class CodeEditor(
        val plainTextCodeEditorComponent: com.wanbaohe.code.editor.component.CodeEditorComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            com.wanbaohe.code.editor.screen.CodeEditorScreen(component = plainTextCodeEditorComponent)
        }
    }

    class EditPromptItem(
        val editPromptComponent: EditPromptComponent,
        val appComponent: AppComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            EditPromptScreen(
                editPromptComponent = editPromptComponent,
                appComponent = appComponent
            )
        }
    }

    class SettingsRouter(
        private val component: SettingRouterComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            SettingRouterScreen(component = component)
        }
    }

    class Survive30s(
        val component: Survive30sComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            Survive30sScreen(component = component)
        }
    }

    class WebBrowser(
        val component: BrowserComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            BrowserScreen(
                component = component,
                onGoBack = component.onGoBack
            )
        }
    }

    class TokenUsage(
        val component: TokenUsageComponent
    ) : NavigationChild {
        @Composable
        override fun Content() {
            TokenUsageScreen(component = component)
        }
    }

    class AIStreamAnswer(
        private val appComponent: AppComponent,
        private val component: com.shifenmiao.ai.component.AIStreamAnswerComponent,
    ) : NavigationChild {
        @Composable
        override fun Content() {
            com.shifenmiao.ai.screen.AIStreamAnswerScreen(
                appComponent = appComponent,
                component = component,
            )
        }
    }
}
