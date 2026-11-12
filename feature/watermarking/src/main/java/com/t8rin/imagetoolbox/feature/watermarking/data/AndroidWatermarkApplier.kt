/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

@file:Suppress("UnnecessaryVariable")

package com.t8rin.imagetoolbox.feature.watermarking.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.applyCanvas
import coil3.transform.RoundedCornersTransformation
import com.t8rin.imagetoolbox.core.data.image.utils.drawBitmap
import com.t8rin.imagetoolbox.core.data.image.utils.toAndroidBlendMode
import com.t8rin.imagetoolbox.core.data.image.utils.toPorterDuffMode
import com.t8rin.imagetoolbox.core.data.utils.asDomain
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageScaler
import com.t8rin.imagetoolbox.core.domain.image.ImageTransformer
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.domain.image.model.ResizeType
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.model.Position
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.t8rin.imagetoolbox.core.domain.utils.timestamp
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.t8rin.imagetoolbox.feature.watermarking.domain.DigitalParams
import com.t8rin.imagetoolbox.feature.watermarking.domain.HiddenWatermark
import com.t8rin.imagetoolbox.feature.watermarking.domain.TextParams
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkApplier
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkParams
import com.t8rin.imagetoolbox.feature.watermarking.domain.WatermarkingType
import com.watermark.androidwm.WatermarkBuilder
import com.watermark.androidwm.WatermarkDetector
import com.watermark.androidwm.bean.WatermarkImage
import com.watermark.androidwm.bean.WatermarkText
import com.watermark.androidwm.listener.BuildFinishListener
import com.watermark.androidwm.listener.DetectFinishListener
import com.watermark.androidwm.task.DetectionReturnValue
import com.watermark.androidwm.utils.BitmapUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.roundToInt

internal class AndroidWatermarkApplier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val imageTransformer: ImageTransformer<Bitmap>,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder, WatermarkApplier<Bitmap> {

    override suspend fun applyWatermark(
        image: Bitmap,
        originalSize: Boolean,
        params: WatermarkParams
    ): Bitmap? = withContext(defaultDispatcher) {
        when (val type = params.watermarkingType) {
            is WatermarkingType.Text -> {
                WatermarkBuilder
                    .create(context, image, !originalSize)
                    .loadWatermarkText(
                        WatermarkText(type.text)
                            .setPositionX(params.positionX.toDouble())
                            .setPositionY(params.positionY.toDouble())
                            .setRotation(params.rotation.toDouble())
                            .setTextAlpha(
                                (params.alpha * 255).roundToInt()
                            )
                            .setTextSize(
                                type.params.size.toDouble()
                            )
                            .setBackgroundColor(type.params.backgroundColor)
                            .setTextColor(type.params.color)
                            .apply {
                                type.params.font.toTypeface()?.let(::setTextTypeface)
                            }
                            .setTextStyle(
                                Paint.Style.FILL
                            )
                    )
                    .setTileMode(params.isRepeated)
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setBlendMode(
                                params.overlayMode.toAndroidBlendMode()
                            )
                        } else {
                            setPorterDuffMode(
                                params.overlayMode.toPorterDuffMode()
                            )
                        }
                    }
                    .generateImage(type.digitalParams)
            }

            is WatermarkingType.Image -> {
                // 图案尺寸按背景图比例算,小图或水印体积很小时会算成 0 像素,
                // 而 Coil 的 Size(0, 0) 会抛 IllegalArgumentException: px must be > 0
                val watermarkSize = IntegerSize(
                    (image.width * type.size).toInt().coerceAtLeast(1),
                    (image.height * type.size).toInt().coerceAtLeast(1)
                )
                imageGetter.getImage(
                    data = type.imageData,
                    size = watermarkSize
                )?.let { watermarkSource ->
                    WatermarkBuilder
                        .create(context, image, !originalSize)
                        .loadWatermarkImage(
                            WatermarkImage(watermarkSource)
                                .setPositionX(params.positionX.toDouble())
                                .setPositionY(params.positionY.toDouble())
                                .setRotation(params.rotation.toDouble())
                                .setImageAlpha(
                                    (params.alpha * 255).roundToInt()
                                )
                                .setSize(type.size.toDouble())
                        )
                        .setTileMode(params.isRepeated)
                        .apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                setBlendMode(
                                    params.overlayMode.toAndroidBlendMode()
                                )
                            } else {
                                setPorterDuffMode(
                                    params.overlayMode.toPorterDuffMode()
                                )
                            }
                        }
                        .generateImage(type.digitalParams)
                }
            }

            is WatermarkingType.Stamp.Text -> {
                drawStamp(
                    image = image,
                    alpha = params.alpha,
                    overlayMode = params.overlayMode,
                    position = type.position,
                    params = type.params,
                    text = type.text,
                    padding = type.padding
                )
            }

            is WatermarkingType.Stamp.Time -> {
                drawStamp(
                    image = image,
                    alpha = params.alpha,
                    overlayMode = params.overlayMode,
                    position = type.position,
                    params = type.params,
                    text = timestamp(type.format),
                    padding = type.padding
                )
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun checkHiddenWatermark(
        image: Bitmap
    ): HiddenWatermark? = runSuspendCatching {
        suspendCancellableCoroutine { continuation ->
            WatermarkDetector
                .create(image, true)
                .detect(
                    object : DetectFinishListener {
                        override fun onSuccess(
                            returnValue: DetectionReturnValue
                        ) = continuation.resume(
                            returnValue.watermarkBitmap
                                ?.let(HiddenWatermark::Image)
                                ?: returnValue.watermarkString?.takeIf { it.isNotEmpty() }
                                    ?.let(HiddenWatermark::Text)
                        )

                        override fun onFailure(message: String?) = continuation.resume(null)
                    }
                )
        }
    }.getOrNull()

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun WatermarkBuilder.generateImage(
        params: DigitalParams
    ): Bitmap? = runSuspendCatching {
        if (params.isInvisible) {
            suspendCancellableCoroutine { continuation ->
                setInvisibleWMListener(
                    params.isLSB,
                    object : BuildFinishListener<Bitmap> {
                        override fun onSuccess(image: Bitmap) = continuation.resume(image)
                        override fun onFailure(reason: String) = continuation.resume(null)
                    }
                )
            }
        } else {
            watermark?.outputImage
        }
    }.getOrNull()

    private suspend fun drawStamp(
        image: Bitmap,
        alpha: Float,
        overlayMode: BlendingMode,
        position: Position,
        padding: Float,
        params: TextParams,
        text: String,
    ): Bitmap = coroutineScope {
        image.copy(Bitmap.Config.ARGB_8888, true).applyCanvas {
            val watermark = WatermarkText(text)
                .setTextAlpha(
                    (alpha * 255).roundToInt()
                )
                .setTextSize(
                    params.size.toDouble()
                )
                .setBackgroundColor(params.backgroundColor)
                .setTextColor(params.color)
                .apply {
                    params.font.toTypeface()?.let(::setTextTypeface)
                }
                .setTextStyle(
                    Paint.Style.FILL
                )

            val verticalPadding = padding - (6f * padding / 20f)
            val horizontalPadding = padding

            // androidwm 的 BitmapUtils.resizeBitmap 把文字位图缩放成
            // 「背景图宽度 × size」后再交给 Bitmap.createBitmap,而 createBitmap 会对
            // 缩放后的宽高取整;源图很小(例如 4×4 的测试图)或水印体积很小时,
            // 取整结果会变成 0,于是抛 IllegalArgumentException: width and height must be > 0。
            // 这种输入下印章本身就不到 1 像素、不可见,跳过绘制即可,不能让整个保存流程崩溃。
            val processedText = runCatching {
                BitmapUtils.textAsBitmap(context, watermark, image)
            }.getOrNull()
                ?.takeIf { it.width > 0 && it.height > 0 }
                ?: return@applyCanvas

            val scaled = imageScaler.scaleImage(
                image = processedText,
                width = (processedText.width - 2 * horizontalPadding).roundToInt(),
                height = (processedText.height - 2 * verticalPadding).roundToInt(),
                resizeType = ResizeType.Flexible
            )
            val textBitmap = if (params.backgroundColor != Color.Transparent.toArgb()) {
                imageTransformer.transform(
                    image = scaled,
                    transformations = listOf(
                        RoundedCornersTransformation(12f).asDomain()
                    )
                ) ?: scaled
            } else {
                scaled
            }

            drawBitmap(
                bitmap = textBitmap,
                position = position,
                paint = Paint().apply {
                    if (Build.VERSION.SDK_INT >= 29) {
                        blendMode = overlayMode.toAndroidBlendMode()
                    } else {
                        xfermode = PorterDuffXfermode(overlayMode.toPorterDuffMode())
                    }
                },
                verticalPadding = verticalPadding,
                horizontalPadding = horizontalPadding
            )
        }
    }

}