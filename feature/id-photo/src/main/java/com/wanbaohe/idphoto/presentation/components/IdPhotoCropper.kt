package com.wanbaohe.idphoto.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropProperties
import com.smarttoolfactory.cropper.util.drawWithLayer
import com.t8rin.imagetoolbox.core.ui.theme.primaryContainerFixed
import com.wanbaohe.idphoto.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * 证件照裁剪组件
 * 基于 ImageCropper 库，支持固定宽高比裁剪
 */
@Composable
fun IdPhotoCropper(
    bitmap: ImageBitmap?,
    cropProperties: CropProperties,
    backgroundColor: Color,
    onCropped: (Bitmap?, Long) -> Unit,
    cropTrigger: Long = 0L,
    maskColor: Color = Color.Unspecified,
    showFaceGuide: Boolean = true,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val scope = rememberCoroutineScope()
    var crop by remember { mutableStateOf(false) }
    var isCropping by remember { mutableStateOf(false) }
    var cropToken by remember { mutableStateOf(0L) }
    val actualMaskColor = if (maskColor == Color.Unspecified) {
        backgroundColor
    } else maskColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            ImageCropper(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                imageBitmap = bitmap,
                contentDescription = stringResource(R.string.id_photo_crop),
                cropProperties = cropProperties,
                cropStyle = CropDefaults.style(
                    drawGrid = false,  // 隐藏裁剪框网格
                    drawOverlay = false,
                    backgroundColor = Color.Transparent
                ),
                onCropStart = { isCropping = true },
                onZoomChange = { _ -> /* 缩放变化 */ },
                crop = crop,
                onCropSuccess = { croppedBitmap: ImageBitmap ->
                    scope.launch {
                        onCropped(croppedBitmap.asAndroidBitmap(), cropToken)
                        crop = false
                        isCropping = false
                    }
                }
            )

            Canvas(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                val aspect = cropProperties.aspectRatio.value
                val coefficient = cropProperties.overlayRatio

                val overlayWidthMax = size.width * coefficient
                val overlayHeightMax = size.height * coefficient

                var overlayWidth = overlayWidthMax
                var overlayHeight = overlayWidthMax / aspect

                if (overlayHeight > overlayHeightMax) {
                    overlayHeight = overlayHeightMax
                    overlayWidth = overlayHeight * aspect
                }

                val offsetX = (size.width - overlayWidth) / 2f
                val offsetY = (size.height - overlayHeight) / 2f

                drawWithLayer {
                    drawRect(actualMaskColor)
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(offsetX, offsetY),
                        size = Size(overlayWidth, overlayHeight),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )
                }

                val borderStrokePx = 10.dp.toPx()
                drawRect(
                    color = AppTheme.colorScheme.surfaceContainerHighest,
                    topLeft = Offset(offsetX, offsetY),
                    size = Size(overlayWidth, overlayHeight),
                    style = Stroke(width = borderStrokePx)
                )

                if (showFaceGuide) {
                    clipRect(
                        left = offsetX,
                        top = offsetY,
                        right = offsetX + overlayWidth,
                        bottom = offsetY + overlayHeight
                    ) {
                        val guideColor = AppTheme.colorScheme.surface.copy(alpha = 0.18f)
                        val guideWidth = overlayWidth * 0.62f
                        val guideCenterX = offsetX + overlayWidth / 2f

                        val headRadius = (overlayWidth.coerceAtMost(overlayHeight) * 0.16f)
                        val headCenter = Offset(
                            x = guideCenterX,
                            y = offsetY + overlayHeight * 0.30f
                        )
                        drawCircle(
                            color = guideColor,
                            radius = headRadius,
                            center = headCenter
                        )

                        val shouldersTop = offsetY + overlayHeight * 0.50f
                        val shouldersHeight = overlayHeight * 0.36f
                        val shouldersLeft = guideCenterX - guideWidth / 2f
                        drawRoundRect(
                            color = guideColor,
                            topLeft = Offset(shouldersLeft, shouldersTop),
                            size = Size(guideWidth, shouldersHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                x = guideWidth * 0.22f,
                                y = guideWidth * 0.22f
                            )
                        )
                    }
                }
            }
        } else {
            // 空状态
            Text(
                text = stringResource(R.string.id_photo_select_image_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 自动裁剪预览（当图片或裁剪属性改变时）
    LaunchedEffect(bitmap, cropProperties.aspectRatio, cropTrigger) {
        if (bitmap != null) {
            while (isCropping) {
                delay(16)
            }
            cropToken = if (cropTrigger != 0L) cropTrigger else System.nanoTime()
            crop = false
            yield()
            crop = true
        }
    }
}
