package com.shifenmiao.common.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxLeadingIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem

/**
 * OneBox 风格的 PreferenceItem 桥接组件
 *
 * 将原有的 PreferenceItem 包装为 OneBoxListItem 风格，统一设置项视觉语言。
 * 当 [useOneBoxStyle] 为 true 时使用 OneBoxListItem，否则回退到原生 PreferenceItem 以保持兼容。
 */
@Composable
fun OneBoxPreferenceItem(
    title: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    useOneBoxStyle: Boolean = true,
    shape: Shape = ShapeDefaults.extraLarge,
    containerColor: Color = Color.Unspecified,
) {
    if (useOneBoxStyle) {
        OneBoxListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            },
            modifier = modifier,
            subtitle = if (subtitle != null) {
                {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            } else null,
            leadingContent = if (startIcon != null) {
                {
                    OneBoxLeadingIconBadge(
                        icon = startIcon,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            } else null,
            trailingContent = if (endIcon != null) {
                {
                    Icon(
                        imageVector = endIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            onClick = if (enabled) onClick else null,
            contained = true,
            shape = OneBoxDesignSystem.listRowShape,
        )
    } else {
        PreferenceItem(
            onClick = onClick,
            title = title,
            enabled = enabled,
            subtitle = subtitle,
            startIcon = startIcon,
            endIcon = endIcon,
            shape = shape,
            containerColor = containerColor,
            modifier = modifier
        )
    }
}
