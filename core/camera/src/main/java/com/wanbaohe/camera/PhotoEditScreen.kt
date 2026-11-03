package com.wanbaohe.camera

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Flip
import com.t8rin.imagetoolbox.core.resources.icons.RotateRight
import com.t8rin.imagetoolbox.core.resources.icons.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropType
import com.wanbaohe.camera.R
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.glass.GlassFilledIconButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton

@Composable
internal fun PhotoEditScreen(
    bitmap: Bitmap,
    aspectRatio: CameraAspectRatio,
    isProcessing: Boolean,
    onAspectRatioChange: (CameraAspectRatio) -> Unit,
    onRotate: () -> Unit,
    onFlip: () -> Unit,
    onReset: () -> Unit,
    onCropStart: () -> Unit,
    onCropped: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    var cropRequested by remember { mutableStateOf(false) }
    val cropProperties = remember(aspectRatio) {
        CropDefaults.properties(
            cropType = CropType.Dynamic,
            cropOutlineProperty = com.smarttoolfactory.cropper.settings.CropOutlineProperty(
                OutlineType.Rect,
                RectCropShape(id = 0, title = OutlineType.Rect.name)
            ),
            aspectRatio = aspectRatio.value?.let(::AspectRatio) ?: AspectRatio.Original,
            fixedAspectRatio = aspectRatio != CameraAspectRatio.FREE &&
                    aspectRatio != CameraAspectRatio.ORIGINAL &&
                    aspectRatio != CameraAspectRatio.FULL_SCREEN,
            overlayRatio = 0.92f,
            handleSize = 52f,
            maxZoom = 8f,
            fling = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CameraTopBar(
            title = stringResource(R.string.camera_crop_title),
            onBack = onBack,
            backIcon = Icons.Outlined.ArrowBack,
            actions = {
                GlassFilledIconButton(
                    onClick = {
                        onCropStart()
                        cropRequested = true
                    },
                    enabled = !isProcessing && !cropRequested,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    style = GlassStyle.None,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.camera_done)
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            ImageCropper(
                modifier = Modifier.fillMaxSize(),
                imageBitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.camera_crop_title),
                cropProperties = cropProperties,
                cropStyle = CropDefaults.style(
                    overlayColor = Color(0x99000000),
                    handleColor = MaterialTheme.colorScheme.primaryContainer,
                    backgroundColor = Color(0xFF121212)
                ),
                crop = cropRequested,
                onCropStart = onCropStart,
                onZoomChange = {},
                onCropSuccess = { cropped ->
                    cropRequested = false
                    onCropped(cropped.asAndroidBitmap())
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CameraRatioSelector(
                selected = aspectRatio,
                ratios = listOf(
                    CameraAspectRatio.FREE,
                    CameraAspectRatio.RATIO_1_1,
                    CameraAspectRatio.RATIO_4_3,
                    CameraAspectRatio.RATIO_16_9,
                    CameraAspectRatio.RATIO_9_16
                ),
                enabled = !isProcessing && !cropRequested,
                onSelected = onAspectRatioChange
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditAction(
                    icon = Icons.Outlined.RotateRight,
                    label = stringResource(R.string.camera_rotate),
                    enabled = !isProcessing && !cropRequested,
                    onClick = onRotate
                )
                EditAction(
                    icon = Icons.Outlined.Flip,
                    label = stringResource(R.string.camera_flip),
                    enabled = !isProcessing && !cropRequested,
                    onClick = onFlip
                )
                EditAction(
                    icon = Icons.Outlined.Undo,
                    label = stringResource(R.string.camera_reset),
                    enabled = !isProcessing && !cropRequested,
                    onClick = onReset
                )
            }
            GlassTonalButton(
                onClick = {
                    onCropStart()
                    cropRequested = true
                },
                enabled = !isProcessing && !cropRequested,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                style = GlassStyle.None,
                borderWidth = 0.dp
            ) {
                Text(stringResource(R.string.camera_done))
            }
        }
    }
}

@Composable
private fun EditAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            style = GlassStyle.None,
            glassBorderWidth = 0.dp
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
