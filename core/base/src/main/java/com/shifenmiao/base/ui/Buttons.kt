package com.shifenmiao.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.state.PlayState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.hapticsCombinedClickable
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.VolumeOff
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePause

/**
 * 自定义尺寸的浮动操作按钮
 */
@Composable
fun CustomFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    width: Dp = 56.dp,  // Material3 FAB 默认宽度
    height: Dp = 56.dp, // Material3 FAB 默认高度
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Int = 24,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .size(size.dp)
            .glassBackground(
                color = containerColor,
                shape = shape
            )
            .hapticsCombinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size((size * 2 / 3).dp),
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
        )
    }
}

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    state: PlayState,
    loadingClick: () -> Unit = {},
    playingClick: () -> Unit = {},
    pausedClick: () -> Unit = {},
    defaultClick: () -> Unit = {},
    resumeClick: () -> Unit = {},
    errorClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            PlayState.LOADING -> {
                // 这里可以显示一个加载图标
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            }

            PlayState.PLAYING,
            PlayState.RESUME -> {
                ActionButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePause,
                    contentDescription = "Pause",
                ) {
                    if (state == PlayState.PLAYING) {
                        playingClick()
                    } else {
                        resumeClick()
                    }
                }
                Text(
                    modifier = Modifier
                        .height(18.dp)
                        .wrapContentWidth(),
                    text = stringResource(R.string.created_by_ai),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }

            PlayState.DEFAULT -> {
                ActionButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.VolumeUp,
                    contentDescription = "Pause",
                ) {
                    defaultClick()
                }
            }

            PlayState.PAUSED -> {
                ActionButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = "Pause",
                ) {
                    pausedClick()
                }
                Text(
                    modifier = Modifier
                        .height(18.dp)
                        .wrapContentWidth(),
                    text = stringResource(R.string.created_by_ai),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            }

            PlayState.ERROR -> {
                ActionButton(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.VolumeOff,
                    contentDescription = "VolumeOff",
                ) {
                    errorClick()
                }
                Text(
                    modifier = Modifier
                        .height(18.dp)
                        .wrapContentWidth(),
                    text = stringResource(R.string.created_by_ai_failed),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )

            }
        }
    }
}