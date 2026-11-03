package com.wanbaohe.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.Cameraswitch
import com.t8rin.imagetoolbox.core.resources.icons.FlashAuto
import com.t8rin.imagetoolbox.core.resources.icons.FlashOff
import com.t8rin.imagetoolbox.core.resources.icons.FlashOn
import com.t8rin.imagetoolbox.core.resources.icons.GridOff
import com.t8rin.imagetoolbox.core.resources.icons.GridOn
import com.t8rin.imagetoolbox.core.resources.icons.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.camera.R
import com.wanbaohe.core.ui.widget.glass.GlassFilledIconButton
import com.wanbaohe.core.ui.widget.glass.GlassIconButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
internal fun CameraCaptureScreen(
    engine: CameraEngine,
    aspectRatio: CameraAspectRatio,
    flashMode: Int,
    showGrid: Boolean,
    isCapturing: Boolean,
    thumbnail: Bitmap?,
    onAspectRatioChange: (CameraAspectRatio) -> Unit,
    onFlashModeChange: (Int) -> Unit,
    onShowGridChange: (Boolean) -> Unit,
    onSwitchCamera: () -> Unit,
    onCapture: () -> Unit,
    onThumbnailClick: () -> Unit,
    onBack: () -> Unit,
    onError: (Throwable) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CameraTopBar(
            title = stringResource(R.string.camera_capture_title),
            onBack = onBack,
            backIcon = Icons.Outlined.ArrowBack,
            actions = {
                val actionColors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                GlassIconButton(
                    onClick = {
                        onFlashModeChange(
                            when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
                                ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        )
                    },
                    colors = actionColors,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Outlined.FlashAuto
                            ImageCapture.FLASH_MODE_ON -> Icons.Outlined.FlashOn
                            else -> Icons.Outlined.FlashOff
                        },
                        contentDescription = stringResource(
                            when (flashMode) {
                                ImageCapture.FLASH_MODE_AUTO -> R.string.camera_flash_auto
                                ImageCapture.FLASH_MODE_ON -> R.string.camera_flash_on
                                else -> R.string.camera_flash_off
                            }
                        )
                    )
                }
                GlassIconButton(
                    onClick = { onShowGridChange(!showGrid) },
                    colors = actionColors,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = if (showGrid) Icons.Outlined.GridOn else Icons.Outlined.GridOff,
                        contentDescription = stringResource(
                            if (showGrid) R.string.camera_grid_off else R.string.camera_grid_on
                        )
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val previewModifier = aspectRatio.value?.let { ratio ->
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
            } ?: Modifier.fillMaxSize()

            Box(modifier = previewModifier.clipToBounds()) {
                CameraPreview(
                    engine = engine,
                    aspectRatio = aspectRatio,
                    onError = onError,
                    modifier = Modifier.fillMaxSize()
                )
                if (showGrid) CameraGrid(modifier = Modifier.fillMaxSize())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CameraRatioSelector(
                selected = aspectRatio,
                ratios = listOf(
                    CameraAspectRatio.ORIGINAL,
                    CameraAspectRatio.FULL_SCREEN,
                    CameraAspectRatio.RATIO_4_3,
                    CameraAspectRatio.RATIO_1_1,
                    CameraAspectRatio.RATIO_16_9
                ),
                enabled = !isCapturing,
                onSelected = onAspectRatioChange
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassSurface(
                    onClick = onThumbnailClick,
                    modifier = Modifier.size(52.dp),
                    enabled = thumbnail != null && !isCapturing,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 0.dp,
                    style = GlassStyle.Thin,
                    borderWidth = 0.dp
                ) {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail.asImageBitmap(),
                            contentDescription = stringResource(R.string.camera_last_photo),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.padding(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GlassFilledIconButton(
                    onClick = onCapture,
                    enabled = !isCapturing,
                    modifier = Modifier.size(82.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    style = GlassStyle.None,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = stringResource(R.string.camera_shutter),
                        modifier = Modifier.size(34.dp)
                    )
                }

                GlassFilledIconButton(
                    onClick = onSwitchCamera,
                    enabled = !isCapturing,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    style = GlassStyle.None,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cameraswitch,
                        contentDescription = stringResource(R.string.camera_switch),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val color = Color.White.copy(alpha = 0.55f)
        val stroke = 1.dp.toPx()
        drawLine(color, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), stroke)
        drawLine(
            color,
            Offset(size.width * 2f / 3f, 0f),
            Offset(size.width * 2f / 3f, size.height),
            stroke
        )
        drawLine(color, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), stroke)
        drawLine(
            color,
            Offset(0f, size.height * 2f / 3f),
            Offset(size.width, size.height * 2f / 3f),
            stroke
        )
    }
}
