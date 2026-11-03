package com.wanbaohe.camera

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.camera.R
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassIconButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
internal fun CameraTopBar(
    title: String,
    onBack: () -> Unit,
    backIcon: ImageVector,
    actions: @Composable RowScope.() -> Unit = {}
) {
    GlassSurface(
        style = GlassStyle.Thin,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        borderWidth = 0.dp
    ) {
        CenterAlignedTopAppBar(
            title = { Text(title) },
            navigationIcon = {
                GlassIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    glassBorderWidth = 0.dp
                ) {
                    Icon(
                        imageVector = backIcon,
                        contentDescription = stringResource(R.string.camera_back)
                    )
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
internal fun CameraRatioSelector(
    selected: CameraAspectRatio,
    ratios: List<CameraAspectRatio>,
    enabled: Boolean,
    onSelected: (CameraAspectRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ratios.forEach { ratio ->
            GlassFilterChip(
                selected = selected == ratio,
                onClick = { onSelected(ratio) },
                enabled = enabled,
                label = { Text(stringResource(ratio.labelRes)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = null,
                border = null,
                glassBorderWidth = 0.dp,
                glassContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

internal val CameraAspectRatio.labelRes: Int
    get() = when (this) {
        CameraAspectRatio.ORIGINAL -> R.string.camera_ratio_original
        CameraAspectRatio.FULL_SCREEN -> R.string.camera_ratio_fullscreen
        CameraAspectRatio.FREE -> R.string.camera_ratio_free
        CameraAspectRatio.RATIO_4_3 -> R.string.camera_ratio_4_3
        CameraAspectRatio.RATIO_1_1 -> R.string.camera_ratio_1_1
        CameraAspectRatio.RATIO_16_9 -> R.string.camera_ratio_16_9
        CameraAspectRatio.RATIO_9_16 -> R.string.camera_ratio_9_16
    }
