package com.wanbaohe.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.camera.R
import com.wanbaohe.core.ui.widget.glass.GlassFilledIconButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton

@Composable
internal fun PhotoConfirmScreen(
    bitmap: Bitmap,
    aspectRatio: CameraAspectRatio,
    editingEnabled: Boolean,
    isProcessing: Boolean,
    onRetake: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CameraTopBar(
            title = stringResource(R.string.camera_confirm_title),
            onBack = onBack,
            backIcon = Icons.Outlined.ArrowBack,
            actions = {
                GlassFilledIconButton(
                    onClick = onConfirm,
                    enabled = !isProcessing,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    style = GlassStyle.None,
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.camera_use_photo)
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
            val ratio = aspectRatio.value
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = if (ratio != null) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                } else {
                    Modifier.fillMaxSize()
                },
                contentScale = if (ratio != null || aspectRatio == CameraAspectRatio.FULL_SCREEN) {
                    ContentScale.Crop
                } else {
                    ContentScale.Fit
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassTonalButton(
                onClick = onRetake,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderWidth = 0.dp
            ) {
                Text(stringResource(R.string.camera_retake))
            }
            if (editingEnabled) {
                GlassTonalButton(
                    onClick = onEdit,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderWidth = 0.dp
                ) {
                    Text(stringResource(R.string.camera_edit))
                }
            }
            GlassTonalButton(
                onClick = onConfirm,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
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
