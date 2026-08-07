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

import com.arkivanov.decompose.ComponentContext
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
import com.shifenmiao.common.components.category.ReorderableComponent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.feature.document.component.DocConvertTaskListComponent
import com.shifenmiao.feature.document.component.OcrTaskListComponent
import com.shifenmiao.lifetime.component.LifeTimeAddCountdownComponent
import com.shifenmiao.lifetime.component.LifeTimeAddEventComponent
import com.shifenmiao.lifetime.component.LifeTimeAddMilestoneComponent
import com.shifenmiao.lifetime.component.LifeTimeComponent
import com.shifenmiao.lifetime.component.LifeTimeCountdownDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeMilestoneDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeSettingsComponent
import com.shifenmiao.lifetime.component.LifeTimeWelcomeComponent
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.marktodo.screenLogic.MarkTodoRouterComponent
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.online.component.PreviewHtmlComponent
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.webview.WebViewComponent
import com.shifenmiao.webview.browser.BrowserComponent
import com.t8rin.imagetoolbox.collage_maker.presentation.screenLogic.CollageMakerComponent
import com.t8rin.imagetoolbox.color_tools.presentation.screenLogic.ColorToolsComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
import com.t8rin.imagetoolbox.feature.markup_layers.presentation.screenLogic.MarkupLayersComponent
import com.t8rin.imagetoolbox.feature.mesh_gradients.presentation.screenLogic.MeshGradientsComponent
import com.t8rin.imagetoolbox.feature.palette_tools.presentation.screenLogic.PaletteToolsComponent
import com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router.screenLogic.PdfRouterComponent
import com.t8rin.imagetoolbox.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent
import com.t8rin.imagetoolbox.feature.resize_convert.presentation.screenLogic.ResizeAndConvertComponent
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ApngTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.AsciiArt
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.AudioCoverExtractor
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Base64Tools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ChecksumTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Cipher
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.CollageMaker
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ColorTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Compare
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Crop
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.DeleteExif
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.DocumentScanner
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Draw
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.EditExif
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Filter
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.FormatConversion
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.GifTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.GradientMaker
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ImageCutter
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ImagePreview
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ImageSplitting
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ImageStacking
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ImageStitching
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.LibrariesInfo
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.LibraryDetails
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.LimitResize
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.LoadNetImage
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.MarkupLayers
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.MeshGradients
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.NoiseGeneration
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.PaletteTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.PdfTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.PickColorFromImage
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ResizeAndConvert
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ScanCode
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.ScanQrCode
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.SingleEdit
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.SvgMaker
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.UnitConverter
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.WallpapersExport
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Watermarking
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.WebpTools
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.WeightResize
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild.Zip
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanQrCodeComponent
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic.ScanCodeComponent
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
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
import com.wanbaohe.altitude.component.AltitudeComponent
import com.wanbaohe.app.component.ActivityLogComponent
import com.wanbaohe.app.component.FavoriteComponent
import com.wanbaohe.blog.logic.BlogComponent
import com.wanbaohe.blog.logic.CreateFeedbackComponent
import com.wanbaohe.bookkeeping.router.screenLogic.BookkeepingRouterComponent
import com.wanbaohe.habittracker.router.screenLogic.HabitTrackerRouterComponent
import com.wanbaohe.calendar.router.screenLogic.CalendarRouterComponent
import com.wanbaohe.camera.watermark.presentation.screenLogic.CameraWatermarkComponent
import com.wanbaohe.cloud.storage.screenLogic.CloudStorageComponent
import com.wanbaohe.compass.component.CompassComponent
import com.wanbaohe.measurement.component.MeasurementComponent
import com.wanbaohe.deadpixeltest.component.DeadPixelTestComponent
import com.wanbaohe.decisionwheel.component.DecisionWheelComponent
import com.wanbaohe.passwordvault.router.screenLogic.PasswordVaultRouterComponent
import com.wanbaohe.a2ui.viewModel.A2uiComponent
import com.wanbaohe.diceroller.component.DiceRollerComponent
import com.wanbaohe.file.browser.screenLogic.FileBrowserComponent
import com.wanbaohe.file_transfer.screenLogic.FileTransferComponent
import com.wanbaohe.game2048.component.Game2048Component
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.idphoto.presentation.screenLogic.IdPhotoComponent
import com.wanbaohe.imageviewer.screenLogic.ImageViewerComponent
import com.wanbaohe.markdown.edit.component.MarkdownEditorComponent
import com.wanbaohe.profile.viewmodel.PayComponent
import com.wanbaohe.schedule.component.ScheduleComponent
import com.wanbaohe.setting.router.SettingsRoute
import com.wanbaohe.setting.router.screenLogic.SettingRouterComponent
import com.wanbaohe.speedtest.component.SpeedTestComponent
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.unitconverter.component.UnitConverterComponent
import javax.inject.Inject
import javax.inject.Provider

class ChildProvider @Inject constructor(

    internal val homeFactories: HomeNavigationFactories,
    internal val aiFactories: Provider<AINavigationFactories>,
    internal val imageFactories: Provider<ImageNavigationFactories>,
    internal val lifeFactories: Provider<LifeNavigationFactories>,
    internal val demoScreenProvider: DemoScreenProvider,

) {
    init {
        com.t8rin.imagetoolbox.core.domain.performance.StartupTrace.mark("ChildProvider.ctor.end")
    }

    fun RootComponent.createChild(
        config: Screen,
        componentContext: ComponentContext,
        appComponent: AppComponent,
        loginComponent: LoginComponent
    ): NavigationChild = when (config) {

        Screen.DataSync -> NavigationChild.DataSync(
            lifeFactories.get().dataSyncComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        Screen.ColorTools -> ColorTools(
            imageFactories.get().colorToolsComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        Screen.EasterEgg -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.EasterEgg,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        Screen.DisplaySettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.DisplaySettings,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = appComponent,
            )
        )

        is Screen.ApngTools -> ApngTools(
            imageFactories.get().apngToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Cipher -> Cipher(
            imageFactories.get().cipherComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.CollageMaker -> CollageMaker(
            imageFactories.get().collageMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Compare -> Compare(
            imageFactories.get().compareComponentFactory(
                componentContext = componentContext,
                initialComparableUris = config.uris
                    ?.takeIf { it.size == 2 }
                    ?.let { it[0] to it[1] },
                onGoBack = ::navigateBack
            )
        )

        is Screen.Crop -> Crop(
            imageFactories.get().cropComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.DeleteExif -> DeleteExif(
            imageFactories.get().deleteExifComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        Screen.DocumentScanner -> DocumentScanner(
            imageFactories.get().documentScannerComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Draw -> Draw(
            imageFactories.get().drawComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Filter -> Filter(
            imageFactories.get().filtersComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.FormatConversion -> FormatConversion(
            imageFactories.get().formatConversionComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.PaletteTools -> PaletteTools(
            imageFactories.get().paletteToolsComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.GifTools -> GifTools(
            imageFactories.get().gifToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.GradientMaker -> GradientMaker(
            imageFactories.get().gradientMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImagePreview -> ImagePreview(
            imageFactories.get().imagePreviewComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageSplitting -> ImageSplitting(
            imageFactories.get().imageSplittingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageStacking -> ImageStacking(
            imageFactories.get().imageStackingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageStitching -> ImageStitching(
            imageFactories.get().imageStitchingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.LimitResize -> LimitResize(
            imageFactories.get().limitResizeComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.LoadNetImage -> LoadNetImage(
            imageFactories.get().loadNetImageComponentFactory(
                componentContext = componentContext,
                initialUrl = config.url,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )


        Screen.NoiseGeneration -> NoiseGeneration(
            imageFactories.get().noiseGenerationComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.PdfTools -> PdfTools(
            imageFactories.get().pdfRouterComponentFactory(
                componentContext = componentContext,
                type = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.XiangqiRouter -> NavigationChild.Xiangqi(
            lifeFactories.get().xiangqiRouterComponentFactory(
                componentContext = componentContext,
                type = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.PickColorFromImage -> PickColorFromImage(
            imageFactories.get().pickColorFromImageComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.ResizeAndConvert -> ResizeAndConvert(
            imageFactories.get().resizeAndConvertComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        Screen.ScanCode -> ScanCode(
            imageFactories.get().scanCodeComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ScanQrCode -> ScanQrCode(
            imageFactories.get().scanQrCodeComponentFactory(
                componentContext = componentContext,
                initialQrCodeContent = config.qrCodeContent,
                uriToAnalyze = config.uriToAnalyze,
                onGoBack = ::navigateBack
            )
        )

        is Screen.SingleEdit -> SingleEdit(
            imageFactories.get().singleEditComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onNavigate = ::navigateTo,
                onGoBack = ::navigateBack
            )
        )

        is Screen.SvgMaker -> SvgMaker(
            imageFactories.get().svgMakerComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Watermarking -> Watermarking(
            imageFactories.get().watermarkingComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.WebpTools -> WebpTools(
            imageFactories.get().webpToolsComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.WeightResize -> WeightResize(
            imageFactories.get().weightResizeComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Zip -> Zip(
            imageFactories.get().zipComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack
            )
        )

        Screen.LibrariesInfo -> LibrariesInfo(
            imageFactories.get().librariesInfoComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.MarkupLayers -> MarkupLayers(
            imageFactories.get().markupLayersComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Base64Tools -> Base64Tools(
            imageFactories.get().base64ToolsComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ChecksumTools -> ChecksumTools(
            imageFactories.get().checksumToolsComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )

        is Screen.MeshGradients -> MeshGradients(
            imageFactories.get().meshGradientsComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.EditExif -> EditExif(
            imageFactories.get().editExifComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.ImageCutter -> ImageCutter(
            imageFactories.get().imageCutterComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.AudioCoverExtractor -> AudioCoverExtractor(
            imageFactories.get().audioCoverExtractorComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.LibraryDetails -> LibraryDetails(
            imageFactories.get().libraryDetailsComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                libraryName = config.name,
                libraryDescription = config.htmlDescription
            )
        )

        is Screen.WallpapersExport -> WallpapersExport(
            imageFactories.get().wallpapersExportComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.AsciiArt -> AsciiArt(
            imageFactories.get().asciiArtComponentFactory(
                componentContext = componentContext,
                initialUri = config.uri,
                onGoBack = ::navigateBack
            )
        )


        is Screen.NewApp -> NavigationChild.NewApp(
            appComponent = appComponent,
            itemListComponent = homeFactories.itemListComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
            ),
            playgroundComponent = homeFactories.playgroundComponentFactory(
                componentContext = componentContext,
            ),
            initialTab = config.initialTab,
        )

        is Screen.Online -> NavigationChild.Online(
            activityLogComponent = homeFactories.activityLogComponentFactory(
                componentContext = componentContext
            ),
            favoriteComponent = homeFactories.favoriteComponentFactory(
                componentContext = componentContext
            ),
            appComponent = appComponent
        )

        is Screen.Profile -> NavigationChild.Profile(
            settingsComponent = homeFactories.settingsComponentFactory(
                componentContext = componentContext,
                onNavigate = ::navigateToNew,
                isUpdateAvailable = isUpdateAvailable,
                onGoBack = ::navigateBack,
                initialSearchQuery = ""
            ),
            appComponent = appComponent,
            loginComponent = loginComponent
        )

        is Screen.Marquee -> NavigationChild.Marquee(
            appComponent = appComponent,
            component = homeFactories.marqueeComponentFactory(
                componentContext = componentContext
            )
        )

        is Screen.Calendar -> NavigationChild.Calendar(
            calendarRouterComponent = lifeFactories.get().calendarRouterComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo
            )
        )

        is Screen.Schedule -> NavigationChild.Schedule(
            scheduleComponent = lifeFactories.get().scheduleComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                linkedTaskId = config.linkedTaskId,
                focusDateMillis = config.focusDateMillis,
            )
        )

        is Screen.Feedback -> NavigationChild.Feedback(
            blogComponent = homeFactories.blogComponentFactory(
                componentContext = componentContext,
                screenParams = null
            ),
            appComponent = appComponent
        )

        is Screen.WebView -> NavigationChild.WebView(
            webViewComponent = homeFactories.webViewComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                webViewParams = config.webViewParams
            )
        )

        is Screen.MarkdownRenderWebView -> NavigationChild.MarkdownRenderWebView(
            appComponent = appComponent,
            webViewComponent = homeFactories.webViewComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                webViewParams = null
            ),
            initialUri = config.initialUri
        )

        is Screen.OpenFilePicker -> NavigationChild.OpenFilePicker(
            appComponent = appComponent,
            screen = config
        )

        is Screen.MiniProgram -> NavigationChild.MiniProgram(
            appComponent = appComponent,
            dataItem = config.dataItem
        )

        is Screen.Registration -> NavigationChild.Registration(
            appComponent = appComponent,
            loginComponent = loginComponent,
            onLoginSuccess = config.onLoginSuccess
        )

        is Screen.FullScreenSubtitles -> NavigationChild.FullScreenSubtitlesChild(
            appComponent = appComponent,
            component = homeFactories.marqueeComponentFactory(
                componentContext = componentContext
            )
        )

        is Screen.Login -> NavigationChild.Login(
            appComponent = appComponent,
            loginComponent = loginComponent,
            onLoginSuccess = config.onLoginSuccess
        )

        is Screen.AgentDetail -> NavigationChild.AgentDetail(
            appComponent = appComponent,
            agentDetailComponent = homeFactories.agentDetailComponentFactory(
                componentContext = componentContext,
                aiChatObject = config.chatObject
            )
        )

        is Screen.AgentScreen -> NavigationChild.Agent(
            appComponent = appComponent,
            agentComponent = homeFactories.agentComponentFactory(
                componentContext = componentContext,
                agent = config.agent,
                conversation = Conversation(
                    title = config.agent.title.orEmpty(),
                    prompt = config.agent.prompt.orEmpty(),
                    entryType = AIConversationEntryType.AGENT,
                    entryRefId = config.agent.id.toString(),
                )
            ),
            isPreview = config.isPreview,
        )

        is Screen.AiChatScreen -> NavigationChild.AiChatScreen(
            appComponent = appComponent,
            aiChatComponent = homeFactories.aiChatComponentFactory(
                componentContext = componentContext,
                conversation = config.conversation,
                interactionOwnerId = "screen_ai_chat_${componentContext.hashCode()}",
                ownsInteractiveRuntimeLifecycle = true
            )
        )

        is Screen.AIDuelChatScreen -> NavigationChild.AIDuelChatScreen(
            appComponent = appComponent,
            duelChatComponent = aiFactories.get().aiDuelChatComponentFactory(
                componentContext = componentContext,
                conversation = config.conversation
            )
        )

        is Screen.AITabChatScreen -> NavigationChild.AITabChatScreen(
            appComponent = appComponent,
            // 复用全局单例：流式输出在 Tab 切换期间不中断
            aiChatComponent = this.globalAIChatComponent
        )

        is Screen.AIHistoryCenter -> NavigationChild.AIHistoryCenter(
            appComponent = appComponent,
            component = aiFactories.get().aiHistoryCenterComponentFactory(
                componentContext = componentContext,
                initialFilter = config.initialFilter
            )
        )

        is Screen.Demo -> demoScreenProvider.createChild(
            appComponent = appComponent,
            componentContext = componentContext
        ) ?: throw IllegalArgumentException("Demo screen is not available in release builds")

        is Screen.AboutAIModel -> NavigationChild.AboutAIModel(
            appComponent = appComponent
        )

        is Screen.UserInfo -> NavigationChild.UserInfo(
            appComponent = appComponent,
            loginComponent = loginComponent
        )

        is Screen.BuyCoffee -> NavigationChild.BuyCoffee(
            appComponent = appComponent,
            loginComponent = loginComponent,
            payComponent = homeFactories.payComponentFactory(
                componentContext = componentContext
            )
        )

        is Screen.VipLevel -> NavigationChild.VipLevel(
            appComponent = appComponent
        )

        is Screen.AboutUs -> NavigationChild.AboutUs(
            appComponent = appComponent,
            showContactUs = config.showContactUs
        )

        is Screen.Community -> NavigationChild.Community(
            appComponent = appComponent
        )

        is Screen.AIGCImage -> NavigationChild.AIGCImage(
            aiFactories.get().aiImageComponentFactory(
                componentContext = componentContext,
            ),
            appComponent = appComponent,
        )

        is Screen.Search -> NavigationChild.Search(
            searchComponent = homeFactories.searchComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
            ),
            appComponent = appComponent
        )


        is Screen.CreateFeedback -> NavigationChild.CreateFeedback(
            createFeedbackComponent = homeFactories.feedbackComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                blogType = config.blogType
            ),
            appComponent = appComponent
        )

        is Screen.BlogDetail -> NavigationChild.BlogDetail(
            blogComponent = homeFactories.blogComponentFactory(
                componentContext = componentContext,
                screenParams = config.screenParams
            ),
            appComponent = appComponent
        )

        is Screen.CreateNote -> NavigationChild.CreateNote(
            createNoteComponent = homeFactories.createNoteComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                draftId = config.draftId,
                onResult = config.onResult
            ),
            appComponent = appComponent
        )

        is Screen.CreateHtml -> NavigationChild.CreateHtml(
            createHtmlComponent = homeFactories.createHtmlComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                draftId = config.draftId,
                onResult = config.onResult
            ),
            appComponent = appComponent
        )

        is Screen.EditPromptItem -> NavigationChild.EditPromptItem(
            editPromptComponent = homeFactories.editPromptComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                draftId = config.draftId,
                onResult = config.onResult
            ),
            appComponent = appComponent
        )

        is Screen.NoteItem -> NavigationChild.NoteItem(
            noteItemComponent = homeFactories.noteItemComponentFactory(
                componentContext = componentContext,
                itemEntityParams = config.itemEntityParams,
                onResult = config.onResult
            ),
            appComponent = appComponent
        )

        is Screen.PreviewHtml -> NavigationChild.PreviewHtml(
            previewHtmlComponent = homeFactories.previewHtmlComponentFactory(
                componentContext = componentContext,
                itemEntityParams = config.itemEntityParams,
                localUri = config.localUri,
                onResult = config.onResult
            ),
            appComponent = appComponent
        )

        is Screen.DecisionWheelScreen -> NavigationChild.DecisionWheel(
            decisionWheelComponent = homeFactories.decisionWheelComponentFactory(
                componentContext = componentContext,
            ),
            appComponent = appComponent
        )

        is Screen.FileTransfer -> NavigationChild.FileTransfer(
            fileTransferComponent = homeFactories.fileTransferComponentFactory(
                componentContext = componentContext,
            ),
            appComponent = appComponent
        )

        is Screen.Reorderable -> NavigationChild.Reorderable(
            reorderableComponent = homeFactories.reorderableComponentFactory(
                componentContext = componentContext,
                type = config.type
            ),
            appComponent = appComponent
        )

        is Screen.FileBrowser -> NavigationChild.FileBrowser(
            fileBrowserComponent = homeFactories.fileBrowserComponentFactory(
                componentContext = componentContext,
                initialUri = config.initialUri,
                onNavigate = ::navigateTo,
            ),
            appComponent = appComponent
        )

        is Screen.CloudStorage -> NavigationChild.CloudStorage(
            cloudStorageComponent = lifeFactories.get().cloudStorageComponentFactory(
                componentContext = componentContext,
            ),
            appComponent = appComponent
        )

        is Screen.MarkTodoRouter -> NavigationChild.MarkTodoRouter(
            markTodoRouterComponent = lifeFactories.get().markTodoRouterComponentFactory(
                componentContext = componentContext,
                type = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.PasswordVault -> NavigationChild.PasswordVault(
            passwordVaultRouterComponent = lifeFactories.get().passwordVaultRouterComponentFactory(
                componentContext = componentContext,
                initialType = config.type,
                onGoBack = ::navigateBack
            )
        )

        is Screen.A2UI -> NavigationChild.A2UI(
            a2uiComponent = lifeFactories.get().a2uiComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Bookkeeping -> NavigationChild.Bookkeeping(
            lifeFactories.get().bookkeepingRouterComponentFactory(
                componentContext = componentContext,
                type = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.HabitTracker -> NavigationChild.HabitTracker(
            lifeFactories.get().habitTrackerRouterComponentFactory(
                componentContext = componentContext,
                type = config.type,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        is Screen.LifeTime -> NavigationChild.LifeTime(
            lifeTimeComponent = lifeFactories.get().lifeTimeComponentFactory(
                componentContext = componentContext,
                onNavigate = { screen -> appComponent.onNavigate(screen) },
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeSettings -> NavigationChild.LifeTimeSettings(
            lifeTimeSettingsComponent = lifeFactories.get().lifeTimeSettingsComponentFactory(
                componentContext = componentContext,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeAddEvent -> NavigationChild.LifeTimeAddEvent(
            lifeTimeAddEventComponent = lifeFactories.get().lifeTimeAddEventComponentFactory(
                componentContext = componentContext,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeAddMilestone -> NavigationChild.LifeTimeAddMilestone(
            lifeTimeAddMilestoneComponent = lifeFactories.get().lifeTimeAddMilestoneComponentFactory(
                componentContext = componentContext,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeMilestoneDetail -> NavigationChild.LifeTimeMilestoneDetail(
            lifeTimeMilestoneDetailComponent = lifeFactories.get().lifeTimeMilestoneDetailComponentFactory(
                componentContext = componentContext,
                milestoneId = config.milestoneId,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeAddCountdown -> NavigationChild.LifeTimeAddCountdown(
            lifeTimeAddCountdownComponent = lifeFactories.get().lifeTimeAddCountdownComponentFactory(
                componentContext = componentContext,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeCountdownDetail -> NavigationChild.LifeTimeCountdownDetail(
            lifeTimeCountdownDetailComponent = lifeFactories.get().lifeTimeCountdownDetailComponentFactory(
                componentContext = componentContext,
                countdownId = config.countdownId,
                onGoBack = { appComponent.onGoBack() }
            )
        )

        is Screen.LifeTimeWelcome -> NavigationChild.LifeTimeWelcome(
            lifeTimeWelcomeComponent = lifeFactories.get().lifeTimeWelcomeComponentFactory(
                componentContext = componentContext,
                onStartSetup = { appComponent.onNavigate(Screen.LifeTimeSettings) }
            )
        )

        is Screen.CameraWatermark -> NavigationChild.CameraWatermark(
            cameraWatermarkComponent = lifeFactories.get().cameraWatermarkComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
            ),
            appComponent = appComponent
        )

        is Screen.IdPhoto -> NavigationChild.IdPhoto(
            idPhotoComponent = lifeFactories.get().idPhotoComponentFactory(
                componentContext = componentContext,
                initialUris = config.uris,
            ),
            appComponent = appComponent
        )

        is Screen.ImageViewer -> NavigationChild.ImageViewer(
            imageViewerComponent = imageFactories.get().imageViewerComponent(
                componentContext = componentContext,
                imageViewerInfo = config.imageViewerInfo,
                onGoBack = ::navigateBack,
            )
        )

        is Screen.MarkdownEditor -> NavigationChild.MarkdownEditor(
            markdownEditorComponent = lifeFactories.get().markdownEditorComponentFactory(
                componentContext = componentContext,
                initialUri = config.initialUri,
                editDraftId = config.editDraftId,
                editTitle = config.editTitle,
                onGoBack = ::navigateBack
            )
        )

        is Screen.OcrDocument -> NavigationChild.OcrDocument(
            ocrTaskListComponent = imageFactories.get().ocrTaskListComponent(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.DocConvertDocument -> NavigationChild.DocConvertDocument(
            docConvertTaskListComponent = imageFactories.get().docConvertTaskListComponent(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Altitude -> NavigationChild.Altitude(
            altitudeComponent = lifeFactories.get().altitudeComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.SpeedTest -> NavigationChild.SpeedTest(
            speedTestComponent = lifeFactories.get().speedTestComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.UnitConverter -> UnitConverter(
            unitConverterComponent = lifeFactories.get().unitConverterComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                initialTab = config.initialTab,
                initialCategory = config.initialCategory,
            )
        )

        is Screen.Compass -> NavigationChild.Compass(
            compassComponent = lifeFactories.get().compassComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.MeasurementTools -> NavigationChild.MeasurementTools(
            measurementComponent = lifeFactories.get().measurementComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.DeadPixelTest -> NavigationChild.DeadPixelTest(
            component = lifeFactories.get().deadPixelTestComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Minesweeper -> NavigationChild.Minesweeper(
            component = lifeFactories.get().minesweeperComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.LoanCalculator -> NavigationChild.LoanCalculator(
            appComponent = appComponent
        )

        is Screen.DiceRoller -> NavigationChild.DiceRoller(
            diceRollerComponent = lifeFactories.get().diceRollerComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Game2048 -> NavigationChild.Game2048(
            game2048Component = lifeFactories.get().game2048ComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.Teleprompter -> NavigationChild.Teleprompter(
            teleprompterComponent = lifeFactories.get().teleprompterComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                type = config.type,
            )
        )

        is Screen.CreateAIAgent -> NavigationChild.CreateAIAgent(
            createAIAgentComponent = aiFactories.get().createAIAgentComponentFactory(
                componentContext,
                config.editDraftId,
                ::navigateBack,
                ::navigateTo,
                ::navigateReplacingCurrent,
            )
        )

        is Screen.CreateAIChatPrompt -> NavigationChild.CreateAIChatPrompt(
            createAIPromptComponent = aiFactories.get().createAIPromptComponentFactory(
                componentContext = componentContext,
                initialDraftId = config.draftId,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                onNavigateReplacingCurrent = ::navigateReplacingCurrent,
            )
        )

        is Screen.AgentJsonEditor -> NavigationChild.AgentJsonEditor(
            agentJsonEditorComponent = aiFactories.get().agentJsonEditorComponentFactory(
                componentContext = componentContext,
                editDraftId = config.editDraftId,
                editTitle = config.editTitle,
                onGoBack = ::navigateBack
            )
        )

        is Screen.CodeEditor -> NavigationChild.CodeEditor(
            plainTextCodeEditorComponent = lifeFactories.get().plainTextCodeEditorComponentFactory(
                componentContext = componentContext,
                initialUri = config.initialUri,
                editDraftId = config.editDraftId,
                editTitle = config.editTitle,
                onGoBack = ::navigateBack
            )
        )

        Screen.ThemeSettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.ThemeSettings,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        Screen.AIFeatureSettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.AIFeatureSettings,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        is Screen.AISettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = config.type.toSettingsRoute(),
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        Screen.SystemPromptManagement -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.SystemPromptManagement,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        Screen.TTSSettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = com.wanbaohe.setting.router.SettingsRoute.TTSSettings,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        Screen.AuthCodeSettings -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = com.wanbaohe.setting.router.SettingsRoute.AuthCodeSettings,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )

        is Screen.SystemPromptDetail -> NavigationChild.SettingsRouter(
            lifeFactories.get().settingRouterComponentFactory(
                componentContext = componentContext,
                route = SettingsRoute.SystemPromptDetail(promptId = config.promptId),
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
                appComponent = null,
            )
        )


        is Screen.Survive30s -> NavigationChild.Survive30s(
            lifeFactories.get().survive30sComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.WebBrowser -> NavigationChild.WebBrowser(
            lifeFactories.get().browserComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                initialUrl = config.url
            )
        )

        Screen.TokenUsage -> NavigationChild.TokenUsage(
            aiFactories.get().tokenUsageComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack
            )
        )

        is Screen.AIStreamAnswer -> NavigationChild.AIStreamAnswer(
            appComponent = appComponent,
            component = aiFactories.get().aiStreamAnswerComponentFactory(
                componentContext = componentContext,
                screen = config,
            )
        )

        is Screen.BlessingWall -> NavigationChild.BlessingWall(
            lifeFactories.get().blessingWallComponentFactory(
                componentContext = componentContext,
                date = config.date,
                initialType = config.initialType,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        Screen.BlessingWallRecord -> NavigationChild.BlessingWallRecord(
            lifeFactories.get().blessingRecordComponentFactory(
                componentContext = componentContext,
                onGoBack = ::navigateBack,
                onNavigate = ::navigateTo,
            )
        )

        else -> throw IllegalArgumentException("Unknown screen: $config")
    }
}

private fun Screen.AISettings.Type?.toSettingsRoute(): SettingsRoute = when (this) {
    null -> SettingsRoute.AIEngineList
    is Screen.AISettings.Type.EngineDetail -> SettingsRoute.AIEngineDetail(
        engineName = engineName,
        requestProtocol = requestProtocol,
    )
    Screen.AISettings.Type.WorkingModel -> SettingsRoute.AIWorkingModel
    Screen.AISettings.Type.AddEngine -> SettingsRoute.AIAddEngine
}
