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

package com.t8rin.imagetoolbox.core.ui.utils.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.StreamAnswerCachePolicy
import com.shifenmiao.model.image.ImageViewerInfo
import com.shifenmiao.model.item.ItemEntityParams
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.model.webview.WebViewParams
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Apng
import com.t8rin.imagetoolbox.core.resources.icons.Exif
import com.t8rin.imagetoolbox.core.resources.icons.Gif
import com.t8rin.imagetoolbox.core.resources.icons.ImageToText
import com.t8rin.imagetoolbox.core.resources.icons.Webp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTexture
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImagePreview
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompareArrows
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompress
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilePresent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMergeType
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotate90

@Stable
@Immutable
@Serializable
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@JsonClassDiscriminator("screen_type")
sealed class Screen(
    open val id: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    open val name: String = "",
    val description: String = "",
) {

    val isBetaFeature: Boolean by lazy { isBetaFeature() }
    val simpleName: String by lazy { simpleName() }
    val routeKey: String by lazy { routeKey() }
    val canonicalName: String by lazy { canonicalName() }
    val slug: String by lazy { slug() }

    val icon: ImageVector? by lazy { icon() }

    val twoToneIcon: ImageVector? by lazy { twoToneIcon() }

    @Serializable
    @SerialName("FullScreenSubtitles")
    data object FullScreenSubtitles : Screen(
        id = -6,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("Registration")
    data class Registration(
        @Transient var onLoginSuccess: () -> Unit = {}
    ) : Screen(
        id = -5,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("Login")
    data class Login(
        @Transient var onLoginSuccess: () -> Unit = {}
    ) : Screen(
        id = -4,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("LibraryDetails")
    data class LibraryDetails(
        @SerialName("libraryName")
        override val name: String,
        val htmlDescription: String
    ) : Screen(
        name = name,
        id = -7,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("LibrariesInfo")
    data object LibrariesInfo : Screen(
        id = -8,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("Settings")
    data class Settings(
        val searchQuery: String = ""
    ) : Screen(
        id = -3,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("EasterEgg")
    data object EasterEgg : Screen(
        id = -2,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("Main")
    data object Main : Screen(
        id = -1,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("SingleEdit")
    data class SingleEdit(
        val uri: Uri? = null
    ) : Screen(
        id = 0,
        title = R.string.single_edit,
        subtitle = R.string.single_edit_sub
    )

    @Serializable
    @SerialName("ResizeAndConvert")
    data class ResizeAndConvert(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 1,
        title = R.string.resize_and_convert,
        subtitle = R.string.resize_and_convert_sub
    )

    @Serializable
    @SerialName("WeightResize")
    data class WeightResize(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 2,
        title = R.string.by_bytes_resize,
        subtitle = R.string.by_bytes_resize_sub
    )

    @Serializable
    @SerialName("Crop")
    data class Crop(
        val uri: Uri? = null
    ) : Screen(
        id = 3,
        title = R.string.crop,
        subtitle = R.string.crop_sub
    )

    @Serializable
    @SerialName("Filter")
    data class Filter(
        val type: Type? = null
    ) : Screen(
        id = 4,
        title = R.string.filter,
        subtitle = R.string.filter_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is Masking -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTexture
                    is Basic -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
                }

            @Serializable
            @SerialName("FilterMasking")
            data class Masking(
                val uri: Uri? = null
            ) : Type(
                title = R.string.mask_filter,
                subtitle = R.string.mask_filter_sub
            )

            @Serializable
            @SerialName("FilterBasic")
            data class Basic(
                val uris: List<Uri>? = null
            ) : Type(
                title = R.string.full_filter,
                subtitle = R.string.full_filter_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        Basic(),
                        Masking()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("Draw")
    data class Draw(
        val uri: Uri? = null
    ) : Screen(
        id = 5,
        title = R.string.draw,
        subtitle = R.string.draw_sub
    )

    @Serializable
    @SerialName("Cipher")
    data class Cipher(
        val uri: Uri? = null
    ) : Screen(
        id = 6,
        title = R.string.cipher,
        subtitle = R.string.cipher_sub
    )

    @Serializable
    @SerialName("ImagePreview")
    data class ImagePreview(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 8,
        title = R.string.image_preview,
        subtitle = R.string.image_preview_sub
    )

    @Serializable
    @SerialName("ImageStitching")
    data class ImageStitching(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 9,
        title = R.string.image_stitching,
        subtitle = R.string.image_stitching_sub
    )

    @Serializable
    @SerialName("LoadNetImage")
    data class LoadNetImage(
        val url: String = ""
    ) : Screen(
        id = 10,
        title = R.string.load_image_from_net,
        subtitle = R.string.load_image_from_net_sub
    )

    @Serializable
    @SerialName("PickColorFromImage")
    data class PickColorFromImage(
        val uri: Uri? = null
    ) : Screen(
        id = 11,
        title = R.string.pick_color,
        subtitle = R.string.pick_color_sub
    )

    @Serializable
    @SerialName("PaletteTools")
    data class PaletteTools(
        val uri: Uri? = null
    ) : Screen(
        id = 12,
        title = R.string.palette_tools,
        subtitle = R.string.palette_tools_sub
    )

    @Serializable
    @SerialName("DeleteExif")
    data class DeleteExif(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 13,
        title = R.string.delete_exif,
        subtitle = R.string.delete_exif_sub
    )

    @Serializable
    @SerialName("Compare")
    data class Compare(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 14,
        title = R.string.compare,
        subtitle = R.string.compare_sub
    )

    @Serializable
    @SerialName("LimitResize")
    data class LimitResize(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 15,
        title = R.string.limits_resize,
        subtitle = R.string.limits_resize_sub
    )

    @Serializable
    @SerialName("PdfTools")
    data class PdfTools(
        val type: Type? = null
    ) : Screen(
        id = 16,
        title = R.string.pdf_tools,
        subtitle = R.string.pdf_tools_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is ImagesToPdf -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf
                    is PdfToImages -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview
                    is Preview -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility
                    is Merge -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMergeType
                    is Split -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentCut
                    is Compress -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompress
                    is Rotate -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRotate90
                    is RemovePages -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete
                    is Rearrange -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert
                }

            @Serializable
            @SerialName("PdfToolsPreview")
            data class Preview(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.preview_pdf,
                subtitle = R.string.preview_pdf_sub
            )

            @Serializable
            @SerialName("PdfToImages")
            data class PdfToImages(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.pdf_to_images,
                subtitle = R.string.pdf_to_images_sub
            )

            @Serializable
            @SerialName("ImagesToPdf")
            data class ImagesToPdf(
                val imageUris: List<Uri>? = null
            ) : Type(
                title = R.string.images_to_pdf,
                subtitle = R.string.images_to_pdf_sub
            )

            @Serializable
            @SerialName("MergePdf")
            data class Merge(
                val pdfUris: List<Uri>? = null
            ) : Type(
                title = R.string.merge_pdf,
                subtitle = R.string.merge_pdf_sub
            )

            @Serializable
            @SerialName("SplitPdf")
            data class Split(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.split_pdf,
                subtitle = R.string.split_pdf_sub
            )

            @Serializable
            @SerialName("CompressPdf")
            data class Compress(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.compress_pdf,
                subtitle = R.string.compress_pdf_sub
            )

            @Serializable
            @SerialName("RotatePdf")
            data class Rotate(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.rotate_pdf,
                subtitle = R.string.rotate_pdf_sub
            )

            @Serializable
            @SerialName("RemovePagesPdf")
            data class RemovePages(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.remove_pages_pdf,
                subtitle = R.string.remove_pages_pdf_sub
            )

            @Serializable
            @SerialName("RearrangePdf")
            data class Rearrange(
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.rearrange_pdf,
                subtitle = R.string.rearrange_pdf_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        Preview(),
                        PdfToImages(),
                        ImagesToPdf(),
                        Merge(),
                        Split(),
                        Compress(),
                        Rotate(),
                        RemovePages(),
                        Rearrange()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("RecognizeText")
    data class RecognizeText(
        val type: Type? = null
    ) : Screen(
        id = 17,
        title = R.string.recognize_text,
        subtitle = R.string.recognize_text_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is Extraction -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ImageToText
                    is WriteToFile -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilePresent
                    is WriteToMetadata -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Exif
                }

            @Serializable
            @SerialName("RecognizeTextExtraction")
            data class Extraction(
                val uri: Uri? = null
            ) : Type(
                title = R.string.recognize_text,
                subtitle = R.string.recognize_text_sub
            )

            @Serializable
            @SerialName("WriteToFile")
            data class WriteToFile(
                val uris: List<Uri>? = null
            ) : Type(
                title = R.string.ocr_write_to_file,
                subtitle = R.string.ocr_write_to_file_sub
            )

            @Serializable
            @SerialName("WriteToMetadata")
            data class WriteToMetadata(
                val uris: List<Uri>? = null
            ) : Type(
                title = R.string.ocr_write_to_metadata,
                subtitle = R.string.ocr_write_to_metadata_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        Extraction(),
                        WriteToFile(),
                        WriteToMetadata()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("GradientMaker")
    data class GradientMaker(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 18,
        title = R.string.gradient_maker,
        subtitle = R.string.gradient_maker_sub,
    )

    @Serializable
    @SerialName("Watermarking")
    data class Watermarking(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 19,
        title = R.string.watermarking,
        subtitle = R.string.watermarking_sub,
    )

    @Serializable
    @SerialName("GifTools")
    data class GifTools(
        val type: Type? = null
    ) : Screen(
        id = 20,
        title = R.string.gif_tools,
        subtitle = R.string.gif_tools_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is GifToImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview
                    is ImageToGif -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Gif
                    is GifToWebp -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Webp
                }

            @Serializable
            @SerialName("GifToImage")
            data class GifToImage(
                val gifUri: Uri? = null
            ) : Type(
                title = R.string.gif_type_to_image,
                subtitle = R.string.gif_type_to_image_sub
            )

            @Serializable
            @SerialName("ImageToGif")
            data class ImageToGif(
                val imageUris: List<Uri>? = null
            ) : Type(
                title = R.string.gif_type_to_gif,
                subtitle = R.string.gif_type_to_gif_sub
            )

            @Serializable
            @SerialName("GifToWebp")
            data class GifToWebp(
                val gifUris: List<Uri>? = null
            ) : Type(
                title = R.string.gif_type_to_webp,
                subtitle = R.string.gif_type_to_webp_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        ImageToGif(),
                        GifToImage(),
                        GifToWebp()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("ApngTools")
    data class ApngTools(
        val type: Type? = null
    ) : Screen(
        id = 21,
        title = R.string.apng_tools,
        subtitle = R.string.apng_tools_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is ApngToImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview
                    is ImageToApng -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Apng
                }

            @Serializable
            @SerialName("ApngToImage")
            data class ApngToImage(
                val apngUri: Uri? = null
            ) : Type(
                title = R.string.apng_type_to_image,
                subtitle = R.string.apng_type_to_image_sub
            )

            @Serializable
            @SerialName("ImageToApng")
            data class ImageToApng(
                val imageUris: List<Uri>? = null
            ) : Type(
                title = R.string.apng_type_to_apng,
                subtitle = R.string.apng_type_to_apng_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        ImageToApng(),
                        ApngToImage()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("Zip")
    data class Zip(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 22,
        title = R.string.zip,
        subtitle = R.string.zip_sub
    )

    @Serializable
    @SerialName("SvgMaker")
    data class SvgMaker(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 24,
        title = R.string.images_to_svg,
        subtitle = R.string.images_to_svg_sub
    )

    @Serializable
    @SerialName("FormatConversion")
    data class FormatConversion(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 25,
        title = R.string.format_conversion,
        subtitle = R.string.format_conversion_sub
    )

    @Serializable
    @SerialName("DocumentScanner")
    data object DocumentScanner : Screen(
        id = 26,
        title = R.string.document_scanner,
        subtitle = R.string.document_scanner_sub
    )

    @Serializable
    @SerialName("ScanCode")
    data object ScanCode : Screen(
        id = 1090,
        title = R.string.scan_code,
        subtitle = R.string.scan_code_sub
    )

    @Serializable
    @SerialName("ScanQrCode")
    data class ScanQrCode(
        val qrCodeContent: String? = null,
        val uriToAnalyze: Uri? = null
    ) : Screen(
        id = 27,
        title = R.string.qr_code_generator,
        subtitle = R.string.qr_code_generator_sub
    )

    @Serializable
    @SerialName("ImageStacking")
    data class ImageStacking(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 28,
        title = R.string.image_stacking,
        subtitle = R.string.image_stacking_sub
    )

    @Serializable
    @SerialName("ImageSplitting")
    data class ImageSplitting(
        val uri: Uri? = null
    ) : Screen(
        id = 29,
        title = R.string.image_splitting,
        subtitle = R.string.image_splitting_sub
    )

    @Serializable
    @SerialName("ColorTools")
    data object ColorTools : Screen(
        id = 30,
        title = R.string.color_tools,
        subtitle = R.string.color_tools_sub
    )

    @Serializable
    @SerialName("WebpTools")
    data class WebpTools(
        val type: Type? = null
    ) : Screen(
        id = 31,
        title = R.string.webp_tools,
        subtitle = R.string.webp_tools_sub
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) {

            val icon: ImageVector
                get() = when (this) {
                    is WebpToImage -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImagePreview
                    is ImageToWebp -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Webp
                }

            @Serializable
            @SerialName("WebpToImage")
            data class WebpToImage(
                val webpUri: Uri? = null
            ) : Type(
                title = R.string.webp_type_to_image,
                subtitle = R.string.webp_type_to_image_sub
            )

            @Serializable
            @SerialName("ImageToWebp")
            data class ImageToWebp(
                val imageUris: List<Uri>? = null
            ) : Type(
                title = R.string.webp_type_to_webp,
                subtitle = R.string.webp_type_to_webp_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        ImageToWebp(),
                        WebpToImage()
                    )
                }
            }
        }
    }

    @Serializable
    @SerialName("NoiseGeneration")
    data object NoiseGeneration : Screen(
        id = 32,
        title = R.string.noise_generation,
        subtitle = R.string.noise_generation_sub
    )

    @Serializable
    @SerialName("CollageMaker")
    data class CollageMaker(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 33,
        title = R.string.collage_maker,
        subtitle = R.string.collage_maker_sub
    )

    @Serializable
    @SerialName("MarkupLayers")
    data class MarkupLayers(
        val uri: Uri? = null
    ) : Screen(
        id = 34,
        title = R.string.markup_layers,
        subtitle = R.string.markup_layers_sub
    )

    @Serializable
    @SerialName("TextCard")
    data object TextCard : Screen(
        id = 1097,
        title = com.shifenmiao.core.R.string.text_card,
        subtitle = com.shifenmiao.core.R.string.text_card_sub
    )

    @Serializable
    @SerialName("Base64Tools")
    data class Base64Tools(
        val uri: Uri? = null
    ) : Screen(
        id = 35,
        title = R.string.base_64_tools,
        subtitle = R.string.base_64_tools_sub
    )

    @Serializable
    @SerialName("ChecksumTools")
    data class ChecksumTools(
        val uri: Uri? = null,
    ) : Screen(
        id = 36,
        title = R.string.checksum_tools,
        subtitle = R.string.checksum_tools_sub
    )

    @Serializable
    @SerialName("MeshGradients")
    data object MeshGradients : Screen(
        id = -9,
        title = 0,
        subtitle = 0
    )

    @Serializable
    @SerialName("EditExif")
    data class EditExif(
        val uri: Uri? = null,
    ) : Screen(
        id = 37,
        title = R.string.edit_exif_screen,
        subtitle = R.string.edit_exif_screen_sub
    )

    @Serializable
    @SerialName("ImageCutter")
    data class ImageCutter(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 38,
        title = R.string.image_cutting,
        subtitle = R.string.image_cutting_sub
    )

    @Serializable
    @SerialName("AudioCoverExtractor")
    data class AudioCoverExtractor(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 39,
        title = R.string.audio_cover_extractor,
        subtitle = R.string.audio_cover_extractor_sub
    )

    @Serializable
    @SerialName("WallpapersExport")
    data object WallpapersExport : Screen(
        id = 40,
        title = R.string.wallpapers_export,
        subtitle = R.string.wallpapers_export_sub
    )

    @Serializable
    @SerialName("AsciiArt")
    data class AsciiArt(
        val uri: Uri? = null,
    ) : Screen(
        id = 41,
        title = R.string.ascii_art,
        subtitle = R.string.ascii_art_sub
    )

    @Serializable
    @SerialName("DataSync")
    data object DataSync : Screen(
        id = -17,
        title = R.string.data_sync,
        subtitle = R.string.data_sync_sub
    )

    @Serializable
    @SerialName("App")
    data object App : Screen(
        id = 1000,
        title = com.shifenmiao.core.R.string.home,
        subtitle = com.shifenmiao.core.R.string.home_description,
    )

    @Serializable
    @SerialName("Online")
    class Online : Screen(
        id = 1002,
        title = com.shifenmiao.core.R.string.online,
        subtitle = com.shifenmiao.core.R.string.online_description
    )

    @Serializable
    @SerialName("Profile")
    class Profile(val uri: Uri? = null) : Screen(
        id = 1003,
        title = com.shifenmiao.core.R.string.profile,
        subtitle = com.shifenmiao.core.R.string.profile_description,
    )

    @Serializable
    @SerialName("Calendar")
    class Calendar(
        val type: Type? = null,
    ) : Screen(
        id = 1004,
        title = com.shifenmiao.core.R.string.calendar,
        subtitle = com.shifenmiao.core.R.string.calendar_description,
    ) {
        @Serializable
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int,
        ) {
            val icon: ImageVector
                get() = when (this) {
                    is CalendarView -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar
                    is BaZi -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic
                    is Auspicious -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable
                    is Convert -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompareArrows
                }

            @Serializable
            @SerialName("CalendarView")
            data class CalendarView(
                val year: Int = -1,
                val month: Int = -1,
                val day: Int = -1,
            ) : Type(
                title = com.shifenmiao.core.R.string.calendar_view,
                subtitle = com.shifenmiao.core.R.string.calendar_view_description,
            )

            @Serializable
            @SerialName("BaZi")
            data class BaZi(
                val year: Int = -1,
                val month: Int = -1,
                val day: Int = -1,
                val hour: Int = -1,
            ) : Type(
                title = com.shifenmiao.core.R.string.bazi,
                subtitle = com.shifenmiao.core.R.string.bazi_description,
            )

            @Serializable
            @SerialName("Auspicious")
            data class Auspicious(
                val isAvoidMode: Boolean = false,
            ) : Type(
                title = com.shifenmiao.core.R.string.auspicious_day,
                subtitle = com.shifenmiao.core.R.string.auspicious_day_description,
            )

            @Serializable
            @SerialName("Convert")
            data class Convert(
                val isSolarToLunar: Boolean = true,
            ) : Type(
                title = com.shifenmiao.core.R.string.lunar_convert,
                subtitle = com.shifenmiao.core.R.string.lunar_convert_description,
            )
        }
    }

    @Serializable
    @SerialName("Schedule")
    class Schedule(
        val linkedTaskId: String? = null,
        val focusDateMillis: Long? = null,
    ) : Screen(
        id = 1073,
        title = com.shifenmiao.core.R.string.schedule,
        subtitle = com.shifenmiao.core.R.string.schedule_description,
    )

    @Serializable
    @SerialName("Marquee")
    class Marquee : Screen(
        id = 1005,
        title = com.shifenmiao.core.R.string.marquee,
        subtitle = com.shifenmiao.core.R.string.marquee_description,
    )

    @Serializable
    @SerialName("Feedback")
    class Feedback : Screen(
        id = -10,
        title = com.shifenmiao.core.R.string.feedback_title,
        subtitle = 0,
    )

    @Serializable
    @SerialName("WebView")
    class WebView(
        val webViewParams: WebViewParams
    ) : Screen(
        id = 1007,
        title = com.shifenmiao.core.R.string.profile_description,
        subtitle = 0,
        name = webViewParams.title,
        description = webViewParams.title
    )

    @Serializable
    @SerialName("MiniProgram")
    class MiniProgram(val dataItem: DataItem) : Screen(
        id = 1008,
        title = com.shifenmiao.core.R.string.mini_program,
        subtitle = 0,
        name = dataItem.title.orEmpty(),
        description = dataItem.description.orEmpty()
    )

    @Serializable
    @SerialName("BuyCoffee")
    class BuyCoffee : Screen(
        id = 1009,
        title = com.shifenmiao.core.R.string.buy_coffee,
        subtitle = com.shifenmiao.core.R.string.buy_coffee_description,
    )

    @Serializable
    @SerialName("UserInfo")
    class UserInfo : Screen(
        id = -11,
        title = com.shifenmiao.core.R.string.profile_user_info_title,
        subtitle = com.shifenmiao.core.R.string.profile_user_info_title,
    )

    @Serializable
    @SerialName("Demo")
    class Demo : Screen(
        id = 1011,
        title = com.shifenmiao.core.R.string.demo_title,
        subtitle = com.shifenmiao.core.R.string.profile_user_info_title,
    )

    @Serializable
    @SerialName("AgentScreen")
    class AgentScreen(
        val agent: Agent,
        val isPreview: Boolean = false,
    ) : Screen(
        id = 1014,
        title = com.shifenmiao.core.R.string.one_box,
        subtitle = com.shifenmiao.core.R.string.one_box_description,
        name = agent.title ?: "",
        description = agent.description ?: ""
    )

    @Serializable
    @SerialName("AgentDetail")
    class AgentDetail(val chatObject: AIChatObject = AIChatObject()) : Screen(
        id = 1015,
        title = com.shifenmiao.core.R.string.empty_string,
        subtitle = com.shifenmiao.core.R.string.empty_string
    )

    @Serializable
    @SerialName("AiChatScreen")
    class AiChatScreen(val conversation: Conversation = Conversation()) : Screen(
        id = 1016,
        title = com.shifenmiao.core.R.string.ai_chat_title,
        subtitle = com.shifenmiao.core.R.string.ai_chat_description,
        name = conversation.title,
        description = conversation.placeholder
    )

    @Serializable
    @SerialName("AIDuelChatScreen")
    class AIDuelChatScreen(
        val conversation: Conversation = Conversation(
            title = AppContext.getString(com.shifenmiao.core.R.string.ai_duel_chat_title)
        )
    ) : Screen(
        id = 1046,
        title = com.shifenmiao.core.R.string.ai_duel_chat_title,
        subtitle = com.shifenmiao.core.R.string.ai_duel_chat_description,
        name = conversation.title,
        description = conversation.placeholder
    )

    @Serializable
    @SerialName("AboutUs")
    class AboutUs(val showContactUs: Boolean = false) : Screen(
        id = -12,
        title = com.shifenmiao.core.R.string.profile_item_about,
        subtitle = com.shifenmiao.core.R.string.profile_item_about,
    )

    @Serializable
    @SerialName("AboutAIModel")
    data object AboutAIModel : Screen(
        id = -13,
        title = com.shifenmiao.core.R.string.profile_item_about_model,
        subtitle = com.shifenmiao.core.R.string.ai_model_description,
    )

    @Serializable
    @SerialName("NewApp")
    class NewApp(
        val initialTab: HomeTabKey? = null,
    ) : Screen(
        id = 1019,
        title = com.shifenmiao.core.R.string.home,
        subtitle = com.shifenmiao.core.R.string.home_description
    )

    @Serializable
    @SerialName("AIGCImage")
    class AIGCImage : Screen(
        id = 1020,
        title = com.shifenmiao.core.R.string.ai_aigc_image_title,
        subtitle = com.shifenmiao.core.R.string.ai_aigc_image_description
    )

    @Serializable
    @SerialName("Search")
    class Search : Screen(
        id = 1021,
        title = com.shifenmiao.core.R.string.search,
        subtitle = com.shifenmiao.core.R.string.search_description
    )

    @Serializable
    @SerialName("Prompt")
    class Prompt : Screen(
        id = 1022,
        title = com.shifenmiao.core.R.string.prompt,
        subtitle = com.shifenmiao.core.R.string.prompt_description
    )

    @Serializable
    @SerialName("EditPrompt")
    class EditPrompt(val promptId: Int = 0) : Screen(
        id = 1023,
        title = com.shifenmiao.core.R.string.edit_prompt,
        subtitle = com.shifenmiao.core.R.string.prompt_description
    )

    @Serializable
    @SerialName("CreateFeedback")
    class CreateFeedback(val blogType: Int = 1) : Screen(
        id = -14,
        title = com.shifenmiao.core.R.string.feedback_create,
        subtitle = com.shifenmiao.core.R.string.feedback_description
    )

    @Serializable
    @SerialName("BlogDetail")
    class BlogDetail(val screenParams: ScreenParams? = null) : Screen(
        id = 1025,
        title = com.shifenmiao.core.R.string.blog_title,
        subtitle = com.shifenmiao.core.R.string.blog_escription,
        name = screenParams?.title ?: "",
        description = screenParams?.description ?: ""
    )

    @Serializable
    @SerialName("AITabChatScreen")
    class AITabChatScreen(val conversation: Conversation = Conversation()) : Screen(
        id = 1026,
        title = com.shifenmiao.core.R.string.ai_tab_chat_title,
        subtitle = com.shifenmiao.core.R.string.ai_chat_description,
        name = conversation.title,
        description = ""
    )

    @Serializable
    @SerialName("AIHistoryCenter")
    class AIHistoryCenter(
        val initialFilter: AIConversationEntryType? = null
    ) : Screen(
        id = 1027,
        title = com.shifenmiao.core.R.string.ai_history_center_title,
        subtitle = com.shifenmiao.core.R.string.ai_history_center_subtitle,
        description = ""
    )

    @Serializable
    @SerialName("NoteItem")
    class NoteItem(
        val itemEntityParams: ItemEntityParams = ItemEntityParams(),
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1028,
        title = com.shifenmiao.core.R.string.item_note_title,
        subtitle = com.shifenmiao.core.R.string.item_note_description,
        description = ""
    )

    @Serializable
    @SerialName("DecisionWheelScreen")
    class DecisionWheelScreen : Screen(
        id = 1029,
        title = com.shifenmiao.core.R.string.decision_wheel_title,
        subtitle = com.shifenmiao.core.R.string.decision_wheel_description,
        description = ""
    )

    @Serializable
    @SerialName("FileTransfer")
    class FileTransfer : Screen(
        id = 1030,
        title = com.shifenmiao.core.R.string.file_transfer_title,
        subtitle = com.shifenmiao.core.R.string.file_transfer_description,
        description = ""
    )

    @Serializable
    @SerialName("FileExplorer")
    class FileExplorer(val screenParams: ScreenParams? = null) : Screen(
        id = 1031,
        title = com.shifenmiao.core.R.string.file_explorer,
        subtitle = com.shifenmiao.core.R.string.file_explorer_escription,
        name = screenParams?.title ?: "",
        description = screenParams?.description ?: ""
    )

    @Serializable
    @SerialName("Reorderable")
    class Reorderable(val type: ReorderableType = ReorderableType.CATEGORY) : Screen(
        id = -15,
        title = com.shifenmiao.core.R.string.reorderable_category,
        subtitle = com.shifenmiao.core.R.string.reorderable_category_escription,
    )

    @Serializable
    @SerialName("FileBrowser")
    class FileBrowser(
        val initialUri: Uri? = null
    ) : Screen(
        id = 1033,
        title = com.shifenmiao.core.R.string.file_browser,
        subtitle = com.shifenmiao.core.R.string.file_browser_escription,
    )

    @Serializable
    @SerialName("CloudStorage")
    class CloudStorage : Screen(
        id = 1072,
        title = com.shifenmiao.core.R.string.cloud_storage_title,
        subtitle = com.shifenmiao.core.R.string.cloud_storage_description,
    )

    @Serializable
    @SerialName("MarkTodoRouter")
    class MarkTodoRouter(
        val type: MarkTodoType? = null
    ) : Screen(
        id = 1034,
        title = com.shifenmiao.core.R.string.mark_todo,
        subtitle = com.shifenmiao.core.R.string.mark_todo_escription,
    ) {
        @Serializable
        sealed interface MarkTodoType {
            @Serializable
            @SerialName("MarkTodoDashboard")
            data object Dashboard : MarkTodoType

            @Serializable
            @SerialName("CategoryDetail")
            data class CategoryDetail(
                val categoryId: String,
                val categoryTitle: String
            ) : MarkTodoType

            @Serializable
            @SerialName("AddTodo")
            data class AddTodo(val initialCategoryId: String? = null) : MarkTodoType

            @Serializable
            @SerialName("AddCategory")
            data class AddCategory(val editingCategoryId: String? = null) : MarkTodoType

            @Serializable
            @SerialName("TodoDetail")
            data class TodoDetail(
                val taskId: String,
                val categoryId: String
            ) : MarkTodoType
        }
    }

    @Serializable
    @SerialName("LifeTime")
    data object LifeTime : Screen(
        id = 1036,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeSettings")
    data object LifeTimeSettings : Screen(
        id = -16,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeAddEvent")
    data object LifeTimeAddEvent : Screen(
        id = 1039,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeWelcome")
    data object LifeTimeWelcome : Screen(
        id = 1040,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeAddMilestone")
    data object LifeTimeAddMilestone : Screen(
        id = 1041,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeMilestoneDetail")
    data class LifeTimeMilestoneDetail(
        val milestoneId: Long
    ) : Screen(
        id = 1042,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
        name = "LifeTimeMilestoneDetail"
    )

    @Serializable
    @SerialName("LifeTimeAddCountdown")
    data object LifeTimeAddCountdown : Screen(
        id = 1062,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
    )

    @Serializable
    @SerialName("LifeTimeCountdownDetail")
    data class LifeTimeCountdownDetail(
        val countdownId: Long
    ) : Screen(
        id = 1063,
        title = com.shifenmiao.core.R.string.lifetime,
        subtitle = com.shifenmiao.core.R.string.lifetime_description,
        name = "LifeTimeCountdownDetail"
    )

    @Serializable
    @SerialName("CameraWatermark")
    data class CameraWatermark(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 1037,
        title = com.shifenmiao.core.R.string.camera_watermark,
        subtitle = com.shifenmiao.core.R.string.camera_watermark_description,
    )

    @Serializable
    @SerialName("IdPhoto")
    data class IdPhoto(
        val uris: List<Uri>? = null
    ) : Screen(
        id = 1074,
        title = com.shifenmiao.core.R.string.id_photo,
        subtitle = com.shifenmiao.core.R.string.id_photo_description,
    )

    @Serializable
    @SerialName("ImageViewer")
    data class ImageViewer(
        val imageViewerInfo: ImageViewerInfo? = null
    ) : Screen(
        id = 1075,
        title = com.shifenmiao.core.R.string.image_viewer,
        subtitle = com.shifenmiao.core.R.string.image_viewer_description,
    )

    @Serializable
    @SerialName("MarkdownEditor")
    data class MarkdownEditor(
        val initialUri: Uri? = null,
        val editDraftId: Long = 0L,
        val editTitle: String? = null
    ) : Screen(
        id = 1076,
        title = com.shifenmiao.core.R.string.markdown_editor,
        subtitle = com.shifenmiao.core.R.string.markdown_editor_description,
    )

    @Serializable
    @SerialName("CreateNote")
    class CreateNote(
        val draftId: Long = 0L,
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1077,
        title = com.shifenmiao.core.R.string.note_create,
        subtitle = com.shifenmiao.core.R.string.note_create_description,
        description = ""
    )

    @Serializable
    @SerialName("CreateHtml")
    class CreateHtml(
        val draftId: Long = 0L,
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1078,
        title = com.shifenmiao.core.R.string.html_create,
        subtitle = com.shifenmiao.core.R.string.html_create_description,
        description = ""
    )

    @Serializable
    @SerialName("PreviewHtml")
    class PreviewHtml(
        val itemEntityParams: ItemEntityParams = ItemEntityParams(),
        val localUri: Uri? = null,
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1051,
        title = com.shifenmiao.core.R.string.html_preview,
        subtitle = com.shifenmiao.core.R.string.html_preview_description,
        description = ""
    )

    @Serializable
    @SerialName("EditPromptItem")
    class EditPromptItem(
        val draftId: Long = 0L,
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1079,
        title = com.shifenmiao.core.R.string.edit_prompt,
        subtitle = com.shifenmiao.core.R.string.edit_prompt,
        description = ""
    )

    @Serializable
    @SerialName("OcrDocument")
    class OcrDocument : Screen(
        id = 1043,
        title = com.shifenmiao.core.R.string.ocr_document,
        subtitle = com.shifenmiao.core.R.string.ocr_document_description,
        description = ""
    )

    @Serializable
    @SerialName("DocConvertDocument")
    class DocConvertDocument : Screen(
        id = 1045,
        title = com.shifenmiao.core.R.string.doc_convert_document,
        subtitle = com.shifenmiao.core.R.string.doc_convert_document_description,
        description = ""
    )

    @Serializable
    @SerialName("MarkdownRenderWebView")
    data class MarkdownRenderWebView(
        val initialUri: Uri? = null
    ) : Screen(
        id = 1044,
        title = com.shifenmiao.core.R.string.markdown_editor,
        subtitle = 0,
        description = ""
    )

    @Serializable
    @SerialName("AgentJsonEditor")
    data class AgentJsonEditor(
        val editDraftId: Long = 0L,
        val editTitle: String? = null
    ) : Screen(
        id = 1067,
        title = com.shifenmiao.core.R.string.code_editor_title,
        subtitle = 0,
        description = ""
    )

    @Serializable
    @SerialName("CodeEditor")
    data class CodeEditor(
        val initialUri: Uri? = null,
        val editDraftId: Long = 0L,
        val editTitle: String? = null
    ) : Screen(
        id = 1086,
        title = com.shifenmiao.core.R.string.code_editor_title,
        subtitle = 0,
        description = ""
    )

    @Serializable
    @SerialName("Altitude")
    data object Altitude : Screen(
        id = 1047,
        title = com.shifenmiao.core.R.string.altitude,
        subtitle = com.shifenmiao.core.R.string.altitude_description,
    )

    @Serializable
    @SerialName("SpeedTest")
    data object SpeedTest : Screen(
        id = 1048,
        title = com.shifenmiao.core.R.string.speed_test,
        subtitle = com.shifenmiao.core.R.string.speed_test_description,
    )

    @Serializable
    @SerialName("UnitConverter")
    data class UnitConverter(
        val initialTab: String = "converter",
        val initialCategory: String? = null,
    ) : Screen(
        id = 1049,
        title = com.shifenmiao.core.R.string.unit_converter,
        subtitle = com.shifenmiao.core.R.string.unit_converter_description,
    )

    @Serializable
    @SerialName("DshClient")
    data object DshClient : Screen(
        id = 1096,
        title = com.shifenmiao.core.R.string.dsh_client,
        subtitle = com.shifenmiao.core.R.string.dsh_client_description,
    )

    @Serializable
    @SerialName("Compass")
    data object Compass : Screen(
        id = 1080,
        title = com.shifenmiao.core.R.string.compass,
        subtitle = com.shifenmiao.core.R.string.compass_description,
    )

    @Serializable
    @SerialName("DeadPixelTest")
    data object DeadPixelTest : Screen(
        id = 1081,
        title = com.shifenmiao.core.R.string.dead_pixel_test,
        subtitle = com.shifenmiao.core.R.string.dead_pixel_test_description,
    )

    @Serializable
    @SerialName("MeasurementTools")
    data object MeasurementTools : Screen(
        id = 1085,
        title = com.shifenmiao.core.R.string.measurement_tools,
        subtitle = com.shifenmiao.core.R.string.measurement_tools_description,
    )

    @Serializable
    @SerialName("Bookkeeping")
    data class Bookkeeping(
        val type: Type? = null
    ) : Screen(
        id = 1052,
        title = com.shifenmiao.core.R.string.bookkeeping,
        subtitle = com.shifenmiao.core.R.string.bookkeeping_description,
    ) {
        @Serializable
        sealed class Type {
            @Serializable
            @SerialName("BookkeepingAddRecord")
            data class AddRecord(
                val editingRecordId: String? = null,
            ) : Type()
        }
    }

    @Serializable
    @SerialName("HabitTracker")
    data class HabitTracker(
        val type: Type? = null
    ) : Screen(
        id = 1092,
        title = com.shifenmiao.core.R.string.habit_tracker,
        subtitle = com.shifenmiao.core.R.string.habit_tracker_description,
    ) {
        @Serializable
        sealed class Type {
            @Serializable
            @SerialName("HabitTrackerMain")
            data object Main : Type()

            @Serializable
            @SerialName("HabitTrackerEdit")
            data class Edit(val habitId: String? = null) : Type()
        }
    }

    @Serializable
    @SerialName("LoanCalculator")
    data object LoanCalculator : Screen(
        id = 1053,
        title = com.shifenmiao.core.R.string.loan_calculator,
        subtitle = com.shifenmiao.core.R.string.loan_calculator_description,
    )

    @Serializable
    @SerialName("DiceRoller")
    data object DiceRoller : Screen(
        id = 1054,
        title = com.shifenmiao.core.R.string.dice_roller_title,
        subtitle = com.shifenmiao.core.R.string.dice_roller_description,
    )

    @Serializable
    @SerialName("Teleprompter")
    data class Teleprompter(
        val type: Type? = null
    ) : Screen(
        id = 1055,
        title = com.shifenmiao.core.R.string.teleprompter_title,
        subtitle = com.shifenmiao.core.R.string.teleprompter_description,
    ) {
        @Serializable
        sealed class Type {

            @Serializable
            @SerialName("TeleprompterEdit")
            data class Edit(val scriptId: String) : Type()

            @Serializable
            @SerialName("TeleprompterPlay")
            data class Play(val scriptId: String) : Type()
        }
    }

    @Serializable
    @SerialName("CreateAIAgent")
    data class CreateAIAgent(
        val editDraftId: Long? = null,
    ) : Screen(
        id = 1056,
        title = com.shifenmiao.core.R.string.create_ai_agent_title,
        subtitle = com.shifenmiao.core.R.string.create_ai_agent_description,
    )

    @Serializable
    @SerialName("CreateAIChatPrompt")
    data class CreateAIChatPrompt(
        val draftId: Long = 0L,
    ) : Screen(
        id = 1057,
        title = com.shifenmiao.core.R.string.create_ai_chat_prompt_title,
        subtitle = com.shifenmiao.core.R.string.create_ai_chat_prompt_description,
    )

    @Serializable
    @SerialName("Game2048")
    data object Game2048 : Screen(
        id = 1058,
        title = com.shifenmiao.core.R.string.game_2048_title,
        subtitle = com.shifenmiao.core.R.string.game_2048_description,
    )

    @Serializable
    @SerialName("XiangqiRouter")
    data class XiangqiRouter(
        val type: Type? = null
    ) : Screen(
        id = 1083,
        title = com.shifenmiao.core.R.string.xiangqi_title,
        subtitle = com.shifenmiao.core.R.string.xiangqi_description,
    ) {
        @Serializable
        sealed class Type {
            @Serializable
            @SerialName("XiangqiLibrary")
            data object Library : Type()

            @Serializable
            @SerialName("XiangqiGame")
            data class Game(
                val gameId: String,
            ) : Type()

            @Serializable
            @SerialName("XiangqiAnalysis")
            data class Analysis(
                val gameId: String,
                val initialPly: Int = -1,
            ) : Type()

            @Serializable
            @SerialName("XiangqiJoinOnlineRoom")
            data class JoinOnlineRoom(
                val roomId: String,
            ) : Type()
        }
    }

    @Serializable
    @SerialName("Minesweeper")
    data object Minesweeper : Screen(
        id = 1082,
        title = com.shifenmiao.core.R.string.minesweeper_title,
        subtitle = com.shifenmiao.core.R.string.minesweeper_description,
    )

    @Serializable
    @SerialName("DisplaySettings")
    data object DisplaySettings : Screen(
        id = -22,
        title = com.shifenmiao.core.R.string.profile_item_dispaly,
        subtitle = 0
    )

    @Serializable
    @SerialName("ThemeSettings")
    data object ThemeSettings : Screen(
        id = -18,
        title = com.shifenmiao.core.R.string.theme_settings_title,
        subtitle = com.shifenmiao.core.R.string.theme_settings_description,
    )

    @Serializable
    @SerialName("AIFeatureSettings")
    data object AIFeatureSettings : Screen(
        id = -25,
        title = com.shifenmiao.core.R.string.profile_item_ai_feature_settings,
        subtitle = 0,
    )

    @Serializable
    @SerialName("AuthCodeSettings")
    data object AuthCodeSettings : Screen(
        id = -27,
        title = com.shifenmiao.core.R.string.auth_code_settings_title,
        subtitle = com.shifenmiao.core.R.string.auth_code_settings_subtitle,
    )

    @Serializable
    @SerialName("AISettings")
    data class AISettings(
        val type: Type? = null
    ) : Screen(
        id = -19,
        title = com.shifenmiao.core.R.string.profile_item_ai_service_and_models,
        subtitle = 0,
    ) {
        @Serializable
        sealed class Type {

            @Serializable
            @SerialName("AISettingsEngineDetail")
            data class EngineDetail(
                val engineName: String,
                val requestProtocol: String,
            ) : Type()

            @Serializable
            @SerialName("AISettingsWorkingModel")
            data object WorkingModel : Type()

            @Serializable
            @SerialName("AISettingsAddEngine")
            data object AddEngine : Type()
        }
    }

    @Serializable
    @SerialName("Survive30s")
    data object Survive30s : Screen(
        id = 1060,
        title = com.shifenmiao.core.R.string.survive_30s_screen_title,
        subtitle = com.shifenmiao.core.R.string.survive_30s_screen_description,
    )

    @Serializable
    @SerialName("WebBrowser")
    data class WebBrowser(
        val url: String = ""
    ) : Screen(
        id = 1064,
        title = com.shifenmiao.core.R.string.web_browser_title,
        subtitle = com.shifenmiao.core.R.string.web_browser_description,
    )

    @Serializable
    @SerialName("OpenFilePicker")
    data class OpenFilePicker(
        val mimeTypes: List<String> = listOf("*/*"),
        val allowMultiple: Boolean = false,
        @Transient val onResult: ScreenCallback? = null
    ) : Screen(
        id = 1071,
        title = 0,
        subtitle = 0,
        name = "OpenFilePicker"
    )

    @Serializable
    @SerialName("TokenUsage")
    data object TokenUsage : Screen(
        id = -20,
        title = com.shifenmiao.core.R.string.profile_item_ai_usage,
        subtitle = 0,
    )

    @Serializable
    @SerialName("AIStreamAnswer")
    data class AIStreamAnswer(
        val systemPrompt: String = "",
        val question: String = "",
        val label: String = "",
        val useStreaming: Boolean = true,
        val cachePolicy: StreamAnswerCachePolicy = StreamAnswerCachePolicy.NONE,
    ) : Screen(
        id = 1066,
        title = com.shifenmiao.core.R.string.ai_chat_title,
        subtitle = 0,
    )

    @Serializable
    @SerialName("SystemPromptManagement")
    data object SystemPromptManagement : Screen(
        id = -23,
        title = com.shifenmiao.core.R.string.profile_item_ai_reply_style,
        subtitle = 0,
    )

    @Serializable
    @SerialName("TTSSettings")
    data object TTSSettings : Screen(
        id = -24,
        title = com.shifenmiao.core.R.string.profile_item_tts_settings,
        subtitle = 0,
    )

    @Serializable
    @SerialName("ImageGenerationSettings")
    data object ImageGenerationSettings : Screen(
        id = -26,
        title = com.shifenmiao.core.R.string.profile_item_image_generation_settings,
        subtitle = 0,
    )

    @Serializable
    @SerialName("SystemPromptDetail")
    data class SystemPromptDetail(
        val promptId: Int,
    ) : Screen(
        id = 1068,
        title = com.shifenmiao.core.R.string.profile_item_ai_reply_style,
        subtitle = 0,
    )

    @Serializable
    @SerialName("VipLevel")
    class VipLevel : Screen(
        id = -21,
        title = com.shifenmiao.core.R.string.vip_level_title,
        subtitle = com.shifenmiao.core.R.string.vip_level_description,
    )

    @Serializable
    @SerialName("Community")
    data object Community : Screen(
        id = 1084,
        title = com.shifenmiao.core.R.string.community_title,
        subtitle = com.shifenmiao.core.R.string.community_title,
    )

    @Serializable
    @SerialName("PasswordVault")
    data class PasswordVault(
        val type: Type? = null,
    ) : Screen(
        id = 1087,
        title = com.shifenmiao.core.R.string.password_vault,
        subtitle = com.shifenmiao.core.R.string.password_vault_description,
    ) {
        @Serializable
        sealed class Type {
            @Serializable
            @SerialName("PasswordVaultList")
            data object List : Type()

            @Serializable
            @SerialName("PasswordVaultDetail")
            data class Detail(
                val entryId: String,
            ) : Type()

            @Serializable
            @SerialName("PasswordVaultAdd")
            data object Add : Type()

            @Serializable
            @SerialName("PasswordVaultEdit")
            data class Edit(
                val entryId: String,
            ) : Type()
        }
    }

    @Serializable
    @SerialName("A2UI")
    data object A2UI : Screen(
        id = 1088,
        title = com.shifenmiao.core.R.string.a2ui_screen,
        subtitle = com.shifenmiao.core.R.string.a2ui_screen_description,
    )

    @Serializable
    @SerialName("BlessingWall")
    data class BlessingWall(
        /**
         * 目标日期（yyyy-MM-dd）；null 表示今天。
         * 传入历史日期时页面为只读模式，展示当天数据。
         */
        val date: String? = null,
        /**
         * 初始展示的 tab，对应 BlessingType.key；null 或非法值默认第一页。
         */
        val initialType: String? = null,
    ) : Screen(
        id = 1089,
        title = com.shifenmiao.core.R.string.blessing_wall,
        subtitle = com.shifenmiao.core.R.string.blessing_wall_sub,
    )

    @Serializable
    @SerialName("BlessingWallRecord")
    data object BlessingWallRecord : Screen(
        id = 1091,
        title = 0,
        subtitle = 0,
    )

    @Serializable
    @SerialName("Poem")
    data class Poem(
        /**
         * 诗词 id；非空时直达该诗词详情（从本地库加载），null 时随机取一首。
         */
        val poemId: Long? = null,
    ) : Screen(
        id = 1093,
        title = com.shifenmiao.core.R.string.poem,
        subtitle = com.shifenmiao.core.R.string.poem_sub,
    )

    @Serializable
    @SerialName("PoemSearch")
    data class PoemSearch(
        /**
         * 初始搜索关键词；非空时进入页面即自动搜索。
         */
        val initialQuery: String? = null,
    ) : Screen(
        id = 1094,
        title = com.shifenmiao.core.R.string.poem_search,
        subtitle = 0,
    )

    companion object : ScreenConstants by ScreenConstantsImpl
}

data class ScreenGroup(
    val entries: List<Screen>,
    @StringRes val title: Int,
    val selectedIcon: ImageVector,
    val baseIcon: ImageVector
) {
    fun icon(isSelected: Boolean) = if (isSelected) selectedIcon else baseIcon
}
