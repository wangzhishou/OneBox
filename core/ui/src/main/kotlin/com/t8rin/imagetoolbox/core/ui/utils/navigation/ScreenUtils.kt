/*
 * ImageToolbox is Screen.an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this Screen.file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is Screen.distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this Screen.program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person4
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import com.shifenmiao.core.constants.UrlConstants.DEEP_LINKS_PREFIX
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.webview.WebViewParams
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ChatPlus
import com.t8rin.imagetoolbox.core.resources.icons.ImageToText
import com.t8rin.imagetoolbox.core.resources.icons.MiniEditLarge
import com.t8rin.imagetoolbox.core.resources.icons.RobotHappy
import com.t8rin.imagetoolbox.core.resources.icons.Toolbox
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAltitude
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApngTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAsciiArt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioCoverExtractor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBase64Tools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlessingWall
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlog
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookkeeping
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraWatermark
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChecksumTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCipher
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCollageMaker
import com.t8rin.imagetoolbox.core.resources.icons.line.LineColorTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompass
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeadPixelTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDecisionWheel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteExif
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDemo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDiceRoller
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDocumentScanner
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDraw
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEditExif
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileBrowser
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileTransfer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatConversion
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGame2048
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGifTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGradientMaker
import com.t8rin.imagetoolbox.core.resources.icons.line.LineIdPhoto
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageCutting
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImagePreview
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageSplitting
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageStacking
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageStitch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImageViewer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLifetime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLimitsResize
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLoadNetImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLoanCalculator
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkdownEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkupLayers
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarquee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMeasurement
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinesweeper
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNoiseGeneration
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOcrDocument
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOnline
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePasswordVault
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdfTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePickColor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineProfile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineResizeConvert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScanQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSchedule
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSurvive30s
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSvgMaker
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTeleprompter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnitConverter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWallpapersExport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWatermarking
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWebpTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWeightResize
import com.t8rin.imagetoolbox.core.resources.icons.line.LineXiangqi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZip
import com.t8rin.imagetoolbox.core.resources.icons.EditAlt
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.Lightbulb
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiDuelChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScan
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AIGCImage
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AITabChatScreen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AboutAIModel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AboutUs
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AgentDetail
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AgentScreen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AiChatScreen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ApngTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.App
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AsciiArt
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.AudioCoverExtractor
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Base64Tools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.BuyCoffee
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Calendar
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ChecksumTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Cipher
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.CollageMaker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ColorTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Compare
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Crop
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.DeleteExif
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Demo
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.DocumentScanner
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Draw
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.EasterEgg
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.EditExif
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Feedback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Filter
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.FormatConversion
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.FullScreenSubtitles
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.GifTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.GradientMaker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ImageCutter
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ImagePreview
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ImageSplitting
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ImageStacking
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ImageStitching
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.LibrariesInfo
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.LibraryDetails
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.LimitResize
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.LoadNetImage
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Login
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Main
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.MarkupLayers
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Marquee
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.MeshGradients
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.MiniProgram
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.NewApp
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.NoiseGeneration
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Online
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.PaletteTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.PdfTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.PickColorFromImage
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Profile
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.RecognizeText
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Registration
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ResizeAndConvert
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ScanCode
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.ScanQrCode
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Search
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Settings
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.SingleEdit
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.SvgMaker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.UserInfo
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.WallpapersExport
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Watermarking
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.WebView
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.WebpTools
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.WeightResize
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen.Zip
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import android.net.Uri as AndroidUri
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInsights
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCoffee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsNotFixed
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LineModelTraining
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkspacePremium

internal fun Screen.isBetaFeature(): Boolean = when (this) {
    else -> false
}

internal fun Screen.simpleName(): String = when (this) {
    is ApngTools -> "APNG_Tools"
    is Cipher -> "Cipher"
    is Compare -> "Compare"
    is Crop -> "Crop"
    is DeleteExif -> "Delete_Exif"
    is Draw -> "Draw"
    is EasterEgg -> "Easter_Egg"
    is Filter -> "Filter"
    is PaletteTools -> "PaletteTools"
    is GifTools -> "GIF_Tools"
    is GradientMaker -> "Gradient_Maker"
    is ImagePreview -> "Image_Preview"
    is ImageStitching -> "Image_Stitching"
    is LimitResize -> "Limit_Resize"
    is LoadNetImage -> "Load_Net_Image"
    is PdfTools -> "PDF_Tools"
    is PickColorFromImage -> "Pick_Color_From_Image"
    is ResizeAndConvert -> "Resize_And_Convert"
    is WeightResize -> "Resize_By_Bytes"
    is Settings -> "Settings"
    is SingleEdit -> "Single_Edit"
    is Watermarking -> "Watermarking"
    is Zip -> "Zip"
    is App -> "App"
    is NewApp -> "NewApp"
    is Calendar -> "App"
    is Feedback -> "Feedback"
    is Marquee -> "Marquee"
    is Online -> "Online"
    is Profile -> "Profile"
    is WebView -> "WebView"
    is Login -> "Login"
    is Registration -> "Registration"
    is SvgMaker -> "Svg"
    is FormatConversion -> "Convert"
    is FullScreenSubtitles -> "FullScreenSubtitles"
    is MiniProgram -> "MiniProgram"
    is BuyCoffee -> "BuyCoffee"
    is UserInfo -> "UserInfo"
    is Demo -> "DemoScreen"
    is DocumentScanner -> "Document_Scanner"
    is ScanCode -> "Scan_Code"
    is ScanQrCode -> "QR_Code"
    is ImageStacking -> "Image_Stacking"
    is ImageSplitting -> "Image_Splitting"
    is ColorTools -> "Color_Tools"
    is WebpTools -> "WEBP_Tools"
    is NoiseGeneration -> "Noise_Generation"
    is CollageMaker -> "Collage_Maker"
    is LibrariesInfo -> "Libraries_Info"
    is MarkupLayers -> "Markup_Layers"
    is Base64Tools -> "Base64_Tools"
    is ChecksumTools -> "Checksum_Tools"
    is MeshGradients -> "Mesh_Gradients"
    is EditExif -> "Edit_EXIF"
    is ImageCutter -> "Image_Cutting"
    is AudioCoverExtractor -> "Audio_Cover_Extractor"
    is LibraryDetails -> "Library_Details"
    is WallpapersExport -> "Wallpapers_Export"
    is AsciiArt -> "Ascii_Art"
    is AiChatScreen -> "AiChatScreen"
    is Screen.AIDuelChatScreen -> "AIDuelChatScreen"
    is AboutUs -> "AboutUs"
    is AgentScreen -> "Agent"
    is AgentDetail -> "AgentDetail"
    is AboutAIModel -> "AboutAIModel"
    is AIGCImage -> "AIGCImage"
    is Search -> "Search"
    is Screen.EditPrompt -> "EditPrompt"
    is Screen.CreateFeedback -> "CreateFeedback"
    is Screen.BlogDetail -> "BlogDetail"
    is Screen.FileExplorer -> "FileExplorer"
    is AITabChatScreen -> "AIChatContent"
    is Screen.NoteItem -> "NoteItem"
    is Screen.DecisionWheelScreen -> "DecisionWheelScreen"
    is Screen.FileTransfer -> "FileTransfer"
    is Screen.FileBrowser -> "FileBrowser"
    is Screen.MarkTodoRouter -> "MarkTodoRouter"
    is Screen.Schedule -> "Schedule"
    is Screen.CameraWatermark -> "CameraWatermark"
    is Screen.LifeTime -> "LifeTime"
    is Screen.IdPhoto -> "IdPhoto"
    is Screen.ImageViewer -> "ImageViewer"
    is Screen.MarkdownEditor -> "MarkdownEditor"
    is Screen.CreateNote -> "CreateNote"
    is Screen.CreateHtml -> "CreateHtml"
    is Screen.PreviewHtml -> "PreviewHtml"
    is Screen.EditPromptItem -> "EditPromptItem"
    is Screen.OcrDocument -> "OcrDocument"
    is Screen.DocConvertDocument -> "DocConvertDocument"
    is Screen.Altitude -> "Altitude"
    is Screen.SpeedTest -> "SpeedTest"
    is Screen.DshClient -> "DshClient"
    is Screen.UnitConverter -> "UnitConverter"
    is Screen.Compass -> "Compass"
    is Screen.DeadPixelTest -> "DeadPixelTest"
    is Screen.Bookkeeping -> "Bookkeeping"
    is Screen.HabitTracker -> "HabitTracker"
    is Screen.DiceRoller -> "DiceRoller"
    is Screen.Teleprompter -> "Teleprompter"
    is Screen.CreateAIAgent -> "CreateAIAgent"
    is Screen.CreateAIChatPrompt -> "CreateAIChatPrompt"
    is Screen.Minesweeper -> "Minesweeper"
    is Screen.Survive30s -> "Survive30s"
    is Screen.DisplaySettings -> "DisplaySettings"
    is Screen.ThemeSettings -> "ThemeSettings"
    is Screen.AIFeatureSettings -> "AIFeatureSettings"
    is Screen.AuthCodeSettings -> "AuthCodeSettings"
    is Screen.AISettings -> "AISettings"
    is Screen.SystemPromptManagement -> "SystemPromptManagement"
    is Screen.OpenFilePicker -> "OpenFilePicker"
    is Screen.TokenUsage -> "TokenUsage"
    is Screen.VipLevel -> "VipLevel"
    is Screen.PasswordVault -> "PasswordVault"
    is Screen.A2UI -> "A2UI"
    is Screen.BlessingWall -> "BlessingWall"
    is Screen.BlessingWallRecord -> "BlessingWallRecord"
    is Screen.Poem -> "Poem"
    is Screen.PoemSearch -> "PoemSearch"
    is Screen.PoemHistory -> "PoemHistory"
    Main -> ""
    else -> ""
}

internal fun Screen.icon(): ImageVector? = when (this) {
    is EasterEgg,
    is Main,
    is Settings,
    is LibrariesInfo,
    is MeshGradients,
    is LibraryDetails -> null

    is SingleEdit -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit
    is ApngTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApngTools
    is Cipher -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCipher
    is Compare -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompare
    is Crop -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCrop
    is DeleteExif -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteExif
    is Draw -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDraw
    is Filter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilters
    is PaletteTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePaletteTools
    is GifTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGifTools
    is GradientMaker -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGradientMaker
    is ImagePreview -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview
    is ImageStitching -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageStitch
    is LimitResize -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLimitsResize
    is LoadNetImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLoadNetImage
    is PdfTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdfTools
    is PickColorFromImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePickColor
    is ResizeAndConvert -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineResizeConvert
    is WeightResize -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWeightResize
    is Watermarking -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWatermarking
    is Zip -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZip
    is SvgMaker -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSvgMaker
    is FormatConversion -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatConversion
    is DocumentScanner -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDocumentScanner
    is ScanCode -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScan
    is ScanQrCode -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScanQrCode
    is ImageStacking -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageStacking
    is ImageSplitting -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageSplitting
    is ColorTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineColorTools
    is WebpTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWebpTools
    is NoiseGeneration -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNoiseGeneration
    is CollageMaker -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCollageMaker
    is MarkupLayers -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkupLayers
    is Base64Tools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBase64Tools
    is ChecksumTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChecksumTools
    is EditExif -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEditExif
    is ImageCutter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageCutting
    is AudioCoverExtractor -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioCoverExtractor
    is WallpapersExport -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWallpapersExport
    is AsciiArt -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAsciiArt
    is App -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp
    is NewApp -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp
    is Calendar -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar
    is Feedback -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog
    is Marquee -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarquee
    is Online -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOnline
    is Profile -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile
    is WebView -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language
    is MiniProgram -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
    is BuyCoffee -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCoffee
    is UserInfo -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile
    is Demo -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDemo
    is AiChatScreen -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat
    is Screen.AIDuelChatScreen -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiDuelChat
    is AboutUs -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo
    is AgentScreen -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.RobotHappy
    is AgentDetail -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.RobotHappy
    is AboutAIModel -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineModelTraining
    is AIGCImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiImage
    is Search -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearch
    is Screen.EditPrompt -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.EditAlt
    is Screen.CreateFeedback -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.EditAlt
    is Screen.BlogDetail -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog
    is Screen.FileExplorer -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileBrowser
    is AITabChatScreen -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat
    is Screen.NoteItem -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit
    is Screen.DecisionWheelScreen -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDecisionWheel
    is Screen.FileTransfer -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileTransfer
    is Screen.FileBrowser -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileBrowser
    is Screen.MarkTodoRouter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchedule
    is Screen.Schedule -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchedule
    is Screen.CameraWatermark -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraWatermark
    is Screen.LifeTime -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLifetime
    is Screen.IdPhoto -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineIdPhoto
    is Screen.ImageViewer -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageViewer
    is Screen.MarkdownEditor -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit
    is Screen.CreateNote -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog
    is Screen.CreateHtml -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor
    is Screen.PreviewHtml -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility
    is Screen.EditPromptItem -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.EditAlt
    is Screen.OcrDocument -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOcrDocument
    is Screen.DocConvertDocument -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatConversion
    is Screen.Bookkeeping -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookkeeping
    is Screen.HabitTracker -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline
    is Screen.Teleprompter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter
    is Screen.CreateAIAgent -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    is Screen.CreateAIChatPrompt -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ChatPlus
    is Screen.TokenUsage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights
    is Screen.VipLevel -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkspacePremium
    is Screen.AIHistoryCenter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory
    is Screen.Altitude -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAltitude
    is Screen.SpeedTest -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest
    is Screen.DshClient -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRobot
    is Screen.UnitConverter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnitConverter
    is Screen.LoanCalculator -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLoanCalculator
    is Screen.DiceRoller -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDiceRoller
    is Screen.Game2048 -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGame2048
    is Screen.XiangqiRouter -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineXiangqi
    is Screen.Survive30s -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSurvive30s
    is Screen.WebBrowser -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language
    is Screen.CodeEditor -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor
    is Screen.AIFeatureSettings -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.RobotHappy
    is Screen.AuthCodeSettings -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock
    is Screen.CloudStorage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage
    is Screen.Compass -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompass
    is Screen.DeadPixelTest -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeadPixelTest
    is Screen.Minesweeper -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinesweeper
    is Screen.Community -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGroup
    is Screen.MeasurementTools -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeasurement
    is Screen.Prompt -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb
    is Screen.DataSync -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileTransfer
    is Screen.PasswordVault -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePasswordVault
    is Screen.A2UI -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic
    is Screen.BlessingWall -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlessingWall
    is Screen.BlessingWallRecord -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlessingWall
    // TODO(poem): 占位图标,待定制 LinePoem 后替换
    is Screen.Poem -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook
    is Screen.PoemSearch -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook
    is Screen.PoemHistory -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook
    else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGpsNotFixed
}

internal fun Screen.twoToneIcon(): ImageVector? = when (this) {
    is EasterEgg,
    is Main,
    is Settings,
    is LibrariesInfo,
    is MeshGradients,
    is LibraryDetails -> null

    is RecognizeText -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageToText
    else -> icon()
}

internal object UriSerializer : KSerializer<AndroidUri> {
    override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun deserialize(
        decoder: Decoder
    ): AndroidUri = decoder.decodeString().toUri()

    override fun serialize(
        encoder: Encoder,
        value: AndroidUri
    ) = encoder.encodeString(value.toString())
}

internal typealias Uri = @Serializable(UriSerializer::class) AndroidUri

internal interface ScreenConstants {

    fun valueOf(screenUrl: String?): Screen

    val typedEntries: List<ScreenGroup>

    val entries: List<Screen>

    /**
     * app screen tab menu
     */
    val tabEntries: List<Screen>

    /**
     * app screen start
     */
    val startEntries: List<Screen>

    val FEATURES_COUNT: Int
}

internal object ScreenConstantsImpl : ScreenConstants {

    /**
     * Default portrait top-level tab menu.
     */
    override val tabEntries by lazy {
        listOf(
            NewApp(), AITabChatScreen(), Online(), Profile()
        )
    }

    /**
     * Default portrait start-entry choices.
     */
    override val startEntries by lazy {
        listOf(
            NewApp(), AITabChatScreen(), Online()
        )
    }

    override fun valueOf(screenUrl: String?): Screen {
        if (screenUrl == null || !screenUrl.startsWith(DEEP_LINKS_PREFIX)) {
            return NewApp()
        }
        try {
            // Decode the URL first
            val decodedUrl = Uri.decode(screenUrl)
            // Check if the decoded URL starts with the screen prefix
            if (!decodedUrl.startsWith(DEEP_LINKS_PREFIX)) {
                return NewApp()
            }
            // Extract the path (screen name) after the prefix
            val path = decodedUrl.substringAfter(DEEP_LINKS_PREFIX).substringBefore("?")
            // Extract query parameters if they exist
            val queryParams = if (decodedUrl.contains("?")) {
                val queryString = decodedUrl.substringAfter("?")
                queryString.split("&").associate {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) {
                        parts[0] to parts[1]
                    } else {
                        parts[0] to ""
                    }
                }
            } else {
                emptyMap()
            }
            // Convert query parameters to ScreenParams
            val title = URLDecoder.decode(queryParams["title"] ?: "", StandardCharsets.UTF_8.toString())
            val description = URLDecoder.decode(queryParams["description"] ?: "", StandardCharsets.UTF_8.toString())
            val screenParams = ScreenParams(
                id = queryParams["id"]?.toIntOrNull() ?: queryParams["ida"]?.toIntOrNull() ?: -1,
                title = title,
                description = description,
                isScreen = queryParams["isScreen"]?.toIntOrNull() == 1
            )
            when (path) {
                "BlogDetail" -> return Screen.BlogDetail(
                    screenParams = screenParams
                )
                "MiniProgram" -> {
                    return MiniProgram(
                        dataItem = DataItem(
                            id = screenParams.id,
                            title = screenParams.title,
                            url = screenParams.url
                        )
                    )
                }
                "WebView" -> {
                    return WebView(
                        webViewParams = WebViewParams(
                            title = screenParams.title,
                            url = screenParams.url
                        )
                    )
                }
                else -> {
                    return entries.find {
                        it.simpleName().equals(path, ignoreCase = true)
                    } ?: NewApp()
                }
            }
        } catch (_: Exception) {
            // Return default screen in case of parsing error
            return NewApp()
        }

    }

    override val typedEntries by lazy {
        listOf(
            ScreenGroup(
                entries = listOf(
                    Screen.FileTransfer(),
                    Screen.DecisionWheelScreen(),
                    AITabChatScreen(),
                    AiChatScreen(),
                    ScanCode,
                    ScanQrCode(),
                    ColorTools,
                    Marquee(),
                    PdfTools(),
                    DocumentScanner,
                    GifTools(),
                    ApngTools(),
                    Cipher(),
                    ChecksumTools(),
                    Zip(),
                    AsciiArt(),
                    Calendar(),
                    WebpTools(),
                    Screen.CreateNote(),
                    Screen.CreateHtml(),
                    AudioCoverExtractor()
                ),
                title = R.string.edit,
                selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.MiniEditLarge,
                baseIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.MiniEditLarge
            ),
            ScreenGroup(
                entries = listOf(
                    SingleEdit(),
                    ResizeAndConvert(),
                    FormatConversion(),
                    Crop(),
                    ImageCutter(),
                    WeightResize(),
                    LimitResize(),
                    EditExif(),
                    DeleteExif(),
                ),
                title = R.string.edit,
                selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.MiniEditLarge,
                baseIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.MiniEditLarge
            ),
            ScreenGroup(
                entries = listOf(
                    Filter(),
                    Draw(),
                    MarkupLayers(),
                    CollageMaker(),
                    ImageStitching(),
                    ImageStacking(),
                    ImageSplitting(),
                    Watermarking(),
                    GradientMaker(),
                    NoiseGeneration,
                    Screen.FileBrowser(),
                    Screen.CameraWatermark(),
                    Screen.IdPhoto()
                ),
                title = R.string.create,
                selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                baseIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic
            ),
            ScreenGroup(
                entries = listOf(
                    PickColorFromImage(),
                    Compare(),
                    ImagePreview(),
                    WallpapersExport,
                    Base64Tools(),
                    SvgMaker(),
                    PaletteTools(),
                    LoadNetImage(),
                    Screen.MarkTodoRouter(),
                    Screen.Schedule(),
                    Screen.Bookkeeping(),
                    Screen.HabitTracker(),
                    Screen.LifeTime,
                    Screen.MarkdownEditor(),
                    Screen.OcrDocument(),
                    Screen.DocConvertDocument(),
                    Screen.AIDuelChatScreen(),
                    Screen.Teleprompter(),
                    RecognizeText(),
                    AIGCImage(),
                    Screen.Prompt(),
                    Screen.AIHistoryCenter(),
                    Screen.FileExplorer(),
                    Screen.Altitude,
                    Screen.SpeedTest,
                    Screen.DshClient,
                    Screen.UnitConverter(),
                    Screen.LoanCalculator,
                    Screen.DiceRoller,
                    Screen.Game2048,
                    Screen.Survive30s,
                    Screen.WebBrowser(),
                    Screen.CodeEditor(),
                    Screen.CloudStorage(),
                    Screen.Compass,
                    Screen.DeadPixelTest,
                    Screen.Minesweeper,
                    Screen.Community,
                    Screen.MeasurementTools,
                    Screen.XiangqiRouter(),
                    Screen.PasswordVault(),
                    Screen.BlessingWall(),
                    Screen.Poem(),
                ),
                title = R.string.tools,
                selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Toolbox,
                baseIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Toolbox
            )
        )
    }

    override val entries by lazy {
        typedEntries.flatMap { it.entries }.sortedBy { it.id }
    }

    override val FEATURES_COUNT = 79
}
