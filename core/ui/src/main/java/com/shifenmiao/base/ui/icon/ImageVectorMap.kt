package com.shifenmiao.base.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.base.ui.icon.IconRegistry.appIcons
import com.shifenmiao.base.ui.icon.IconRegistry.materialIcons
import com.shifenmiao.base.ui.icon.IconRegistry.resolve
import com.shifenmiao.core.icons.Box
import com.shifenmiao.core.icons.QQ
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.ChatPlus
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.DshWhale
import com.t8rin.imagetoolbox.core.resources.icons.EditAlt
import com.t8rin.imagetoolbox.core.resources.icons.ImageText
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.Lightbulb
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.RobotHappy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiDuelChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiImage
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEditExif
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileBrowser
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileTransfer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatConversion
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGame2048
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGifTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGradientMaker
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkdownEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkupLayers
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickyNote
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePoem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineProfile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineResizeConvert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScanQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSchedule
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSurvive30s
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSvgMaker
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTeleprompter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnitConverter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnlock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWallpapersExport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWatermarking
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWebpTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWeightResize
import com.t8rin.imagetoolbox.core.resources.icons.line.LineXiangqi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZip
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookmark
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBuild
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExtension
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTexture
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.SelectAll
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronLeft
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDateRange
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmail
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatPaint
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGridOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMovie
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSend
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShuffle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapHoriz
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUpload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomOut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrCode
import com.t8rin.imagetoolbox.core.resources.icons.BrokenImageAlt
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.FolderOpened
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountBalance
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountWallet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArchive
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAttachFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBorderColor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCoffee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContacts
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDashboard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEnergyLeaf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInbox
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLogout
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMessage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineModelTraining
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePhone
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRepeat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShield
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipNext
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipPrevious
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTravelExplore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVerifiedUser
import com.t8rin.imagetoolbox.core.resources.icons.Block
import com.t8rin.imagetoolbox.core.resources.icons.Interface
import com.t8rin.imagetoolbox.core.resources.icons.Toolbox
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBadge
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAlarm
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBluetooth
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBurstMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCategory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContactPhone
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCreditCard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDataObject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDesignServices
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNotifications
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePermContactCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReceipt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestaurant
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSchool
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScreenshot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShoppingCart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSort
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStyle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineThumbDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineThumbUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineToken
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTransform
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTv
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnfoldLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnfoldMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVerified
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVideocam
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVideocamOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewCompact
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifiOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkspaces
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDumbbell
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFruit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMeditation
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMoon
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePiggybank
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePill
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRunning
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSunrise
import com.t8rin.imagetoolbox.core.resources.icons.line.LineToothbrush
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWriting

/**
 * 图标注册表
 *
 * 分两个 Map 注册图标：[appIcons]（应用自定义）和 [materialIcons]（Material 标准）。
 * 添加新图标只需放入对应 Map，分类自动生效，无需手动同步。
 *
 * lambda 工厂模式：Map 初始化仅分配 lambda 对象（~16 bytes/个），
 * ImageVector 仅在首次 [resolve] 时才被创建。
 */
object IconRegistry {

    // ── 应用自定义图标 ─────────────────────────────────────────
    private val appIcons: Map<String, () -> ImageVector> = mapOf(
        "QQ" to { QQ },
        "Box" to { Box },
        "SingleEdit" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit },
        "ApngTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApngTools },
        "Cipher" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCipher },
        "Compare" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompare },
        "Crop" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCrop },
        "DeleteExif" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteExif },
        "Draw" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDraw },
        "Filter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilters },
        "GeneratePalette" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePaletteTools },
        "GifTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGifTools },
        "GradientMaker" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGradientMaker },
        "ImagePreview" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview },
        "ImageStitching" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageStitch },
        "LimitResize" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLimitsResize },
        "LoadNetImage" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLoadNetImage },
        "PdfTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdfTools },
        "PickColorFromImage" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePickColor },
        "RecognizeText" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageText },
        "ResizeAndConvert" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineResizeConvert },
        "WeightResize" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWeightResize },
        "Watermarking" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWatermarking },
        "Zip" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZip },
        "SvgMaker" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSvgMaker },
        "FormatConversion" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatConversion },
        "DocumentScanner" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDocumentScanner },
        "ScanQrCode" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScanQrCode },
        "ImageStacking" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageStacking },
        "ImageSplitting" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageSplitting },
        "ColorTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineColorTools },
        "WebpTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWebpTools },
        "NoiseGeneration" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNoiseGeneration },
        "CollageMaker" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCollageMaker },
        "MarkupLayers" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkupLayers },
        "TextCard" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStickyNote },
        "Base64Tools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBase64Tools },
        "ChecksumTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChecksumTools },
        "EditExif" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEditExif },
        "ImageCutter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageCutting },
        "AudioCoverExtractor" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAudioCoverExtractor },
        "WallpapersExport" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWallpapersExport },
        "AsciiArt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAsciiArt },
        "App" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp },
        "NewApp" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp },
        "Calendar" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar },
        "Bookkeeping" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookkeeping },
        "DshClient" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.DshWhale },
        "Feedback" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog },
        "Marquee" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarquee },
        "Online" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOnline },
        "Profile" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile },
        "Webview" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language },
        "MiniProgram" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles },
        "BuyCoffee" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCoffee },
        "UserInfo" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile },
        "Demo" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDemo },
        "AiChatScreen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat },
        "AIDuelChatScreen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiDuelChat },
        "AiDuelChat" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiDuelChat },
        "AboutUs" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo },
        "AgentScreen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.RobotHappy },
        "AgentDetail" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.RobotHappy },
        "AboutAIModel" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineModelTraining },
        "AIGCImage" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiImage },
        "Search" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearch },
        "Prompt" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb },
        "EditPrompt" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.EditAlt },
        "CreateFeedback" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.EditAlt },
        "BlogDetail" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog },
        "FileExplorer" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileBrowser },
        "AITabChatScreen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat },
        "CreateItem" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add },
        "NoteItem" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit },
        "DecisionWheelScreen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDecisionWheel },
        "FileTransfer" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileTransfer },
        "Settings" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings },
        "FileBrowser" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileBrowser },
        "CameraWatermark" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraWatermark },
        "LifeTime" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLifetime },
        "IdPhoto" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineIdPhoto },
        "MarkTodo" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchedule },
        "ImageViewer" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImageViewer },
        "MarkdownEditor" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkdownEdit },
        "CreateNote" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlog },
        "CreateHtml" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor },
        "OcrDocument" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOcrDocument },
        "DocConvertDocument" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatConversion },
        "AIHistoryCenter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory },
        "Altitude" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAltitude },
        "SpeedTest" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest },
        "UnitConverter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnitConverter },
        "LoanCalculator" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLoanCalculator },
        "DiceRoller" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDiceRoller },
        "Game2048" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGame2048 },
        "Survive30s" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSurvive30s },
        "WebBrowser" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language },
        "CodeEditor" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor },
        "CloudStorage" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage },
        "Compass" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompass },
        "DeadPixelTest" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeadPixelTest },
        "Minesweeper" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinesweeper },
        "Community" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGroup },
        "MeasurementTools" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeasurement },
        "DataSync" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileTransfer },
        "Teleprompter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter },
        "XiangqiRouter" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineXiangqi },
        "LifeTime" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLifetime },
        "CreateAIAgent" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent },
        "CreateAIChatPrompt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ChatPlus },
        "PasswordVault" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePasswordVault },
        "BlessingWall" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBlessingWall },
        "Poem" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePoem },
        // ── 习惯打卡定制图标(键与 HabitIcons 存库 iconKey 的 PascalCase 形式对应)──
        "Waterdrop" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWaterDrop },
        "Sunrise" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSunrise },
        "Running" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRunning },
        "Book" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook },
        "Meditation" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeditation },
        "Moon" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMoon },
        "Toothbrush" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineToothbrush },
        "Pill" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePill },
        "Fruit" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFruit },
        "Writing" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWriting },
        "Piggybank" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePiggybank },
        "Dumbbell" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDumbbell },
    )

    // ── Material 标准图标 ──────────────────────────────────────
    private val materialIcons: Map<String, () -> ImageVector> = mapOf(
        "Home" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp },
        "Widgets" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles },
        "AccountBox" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBadge },
        "AccountCircle" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile },
        "AddCircle" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddCircleOutline },
        "Android" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent },
        "Archive" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArchive },
        "Bookmark" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookmark },
        "BorderColor" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBorderColor },
        "BrokenImage" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.BrokenImageAlt },
        "Build" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBuild },
        "BurstMode" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBurstMode },
        "CalendarToday" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar },
        "CalendarMonth" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar },
        "Camera" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraAlt },
        "CameraAlt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraAlt },
        "Category" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCategory },
        "Check" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check },
        "CheckCircle" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle },
        "Close" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close },
        "Cloud" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage },
        "Code" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor },
        "ContentCopy" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy },
        "CopyAll" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy },
        "Create" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit },
        "CreditCard" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCreditCard },
        "Crop" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCrop },
        "DataObject" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDataObject },
        "DateRange" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDateRange },
        "Delete" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete },
        "Description" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription },
        "DesignServices" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDesignServices },
        "Download" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload },
        "Email" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmail },
        "EnergySavingsLeaf" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEnergyLeaf },
        "ErrorOutline" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError },
        "Event" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar },
        "Extension" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExtension },
        "Favorite" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite },
        "FavoriteBorder" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite },
        "FileOpen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileOpen },
        "FilterAlt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilterAlt },
        "FilterList" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilterAlt },
        "Folder" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder },
        "FolderOpen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.FolderOpened },
        "FormatPaint" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatPaint },
        "GetApp" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload },
        "Grid3x3" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGridOn },
        "GridOn" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGridOn },
        "Help" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp },
        "HelpOutline" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp },
        "History" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory },
        "Label" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel },
        "Landscape" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage },
        "Layers" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkupLayers },
        "Link" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink },
        "List" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList },
        "Lock" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock },
        "LockOpen" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnlock },
        "Logout" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLogout },
        "Menu" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawer },
        "MoreVert" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore },
        "Notifications" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNotifications },
        "Palette" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme },
        "ColorLens" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme },
        "PermMedia" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview },
        "Photo" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage },
        "PhotoAlbum" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview },
        "PhotoLibrary" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview },
        "PlayArrow" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle },
        "PlayCircleOutline" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle },
        "QrCode2" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrCode },
        "Refresh" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh },
        "Remove" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus },
        "Save" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave },
        "SaveAlt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave },
        "Schedule" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory },
        "Screenshot" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScreenshot },
        "Security" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSecurity },
        "SelectAll" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.SelectAll },
        "Share" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare },
        "Shield" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShield },
        "Sort" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSort },
        "Star" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar },
        "StarBorder" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar },
        "Storage" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStorage },
        "Style" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStyle },
        "TextFields" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText },
        "Texture" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTexture },
        "Timer" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTimer },
        "Transform" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTransform },
        "Tune" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune },
        "Upload" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUpload },
        "Verified" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVerified },
        "ViewCompact" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewCompact },
        "ViewList" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList },
        "ViewModule" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures },
        "Visibility" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility },
        "VisibilityOff" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff },
        "Wallpaper" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWallpapersExport },
        "Warning" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning },
        "Workspaces" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkspaces },
        "ZoomIn" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZoomIn },
        "ZoomOut" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineZoomOut },
        "Flag" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag },
        "Dashboard" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDashboard },
        "Work" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Toolbox },
        "ShoppingCart" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShoppingCart },
        "Movie" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMovie },
        "Inbox" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInbox },
        "Translate" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language },
        "TravelExplore" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTravelExplore },
        "Psychology" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology },
        // Navigation
        "ArrowBack" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack },
        "ArrowForward" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos },
        "ArrowDropDown" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown },
        "ArrowDropUp" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropUp },
        "ChevronLeft" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronLeft },
        "ChevronRight" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight },
        "OpenInNew" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew },
        "SwapHoriz" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapHoriz },
        "SwapVert" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert },
        "UnfoldLess" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnfoldLess },
        "UnfoldMore" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnfoldMore },
        // Communication
        "Call" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePhone },
        "Chat" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat },
        "ContactPhone" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContactPhone },
        "Contacts" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContacts },
        "MessageIcon" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMessage },
        "SendIcon" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSend },
        // Content
        "AttachFile" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAttachFile },
        "Block" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Block },
        "ContentCut" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentCut },
        "ContentPaste" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentPaste },
        "Report" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReport },
        // Action / Status
        "AccessTime" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime },
        "AccountBalance" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccountBalance },
        "AccountBalanceWallet" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccountWallet },
        "Alarm" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAlarm },
        "Cached" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSync },
        "Receipt" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReceipt },
        "TipsAndUpdates" to { com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb },
        "Token" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineToken },
        "UpdateIcon" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSync },
        "VerifiedUser" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVerifiedUser },
        // Social
        "School" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchool },
        "ThumbDown" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineThumbDown },
        "ThumbUp" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineThumbUp },
        // Media / AV
        "Repeat" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRepeat },
        "Shuffle" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShuffle },
        "SkipNext" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipNext },
        "SkipPrevious" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipPrevious },
        "Stop" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStop },
        "Videocam" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVideocam },
        "VideocamOff" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVideocamOff },
        // Places
        "LocationOn" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocationOn },
        "Restaurant" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRestaurant },
        // Device / Hardware
        "Bluetooth" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBluetooth },
        "CloudDownload" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDownload },
        "CloudUpload" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload },
        "Computer" to { com.t8rin.imagetoolbox.core.resources.Icons.Filled.Interface },
        "Tv" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTv },
        "Wifi" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifi },
        "WifiOff" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifiOff },
        "PermContactCalendar" to { com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePermContactCalendar },
    )

    // ── 合并后的全量 Map（appIcons 优先，key 冲突时 app 覆盖 material）──
    private val all: Map<String, () -> ImageVector> = materialIcons + appIcons

    /** 按 key 获取 ImageVector，首次调用 lambda 后 Material Icons 内部自动缓存 */
    fun resolve(key: String): ImageVector? = all[key]?.invoke()

    /** 判断 key 是否存在 */
    fun contains(key: String): Boolean = key in all

    /** 应用自定义图标 key 列表（按字母排序） */
    val appKeys: List<String> by lazy { appIcons.keys.sorted() }

    /** Material 标准图标 key 列表（按字母排序） */
    val materialKeys: List<String> by lazy { materialIcons.keys.sorted() }

    /** 全部图标 key（按字母排序） */
    val allKeys: List<String> by lazy { all.keys.sorted() }
}

