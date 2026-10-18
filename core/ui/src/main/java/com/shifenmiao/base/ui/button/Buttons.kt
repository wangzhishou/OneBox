package com.shifenmiao.base.ui.button

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxPillButton
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuestionMark

@Composable
fun CancelButton(
    text: String = stringResource(id = R.string.button_cancel),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = {
            Icon(
                modifier = Modifier.width(16.dp),
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    )
}

@Composable
fun ConfirmButton(
    text: String = stringResource(id = R.string.button_confirm),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        leadingIcon = {
            Icon(
                modifier = Modifier.width(16.dp),
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    )
}

@Composable
fun SecondaryButton(
    text: String = "",
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onClick: () -> Unit = {},
) {
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enable,
        leadingIcon = icon,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        textStyle = style,
    )
}

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    enable: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enable,
        leadingIcon = icon,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun SecondarySmallButton(
    modifier: Modifier = Modifier,
    text: String,
    enable: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enable,
        leadingIcon = icon,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        textStyle = MaterialTheme.typography.labelSmall,
        contentPadding = PaddingValues(
            horizontal = AppTheme.dimens.paddingSmall,
            vertical = AppTheme.dimens.paddingExtraSmall,
        ),
    )
}

@Composable
fun SmallClickableWithIconAndText(
    modifier: Modifier = Modifier,
    iconVector: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuestionMark,
    iconContentDescription: String = "",
    text: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = modifier.clickable {
            onClick.invoke()
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = iconContentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            modifier = Modifier.padding(start = AppTheme.dimens.paddingSmall),
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ==================== 固定高度按钮组件 ====================

/**
 * 小尺寸固定高度按钮（24dp）
 * 文字大小会根据按钮高度自动调整，不受系统字体缩放影响
 */
@Composable
fun SmallFixedHeightButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    colors: ButtonColors = AppTheme.colors.filledTonalButtonColors(),
    onLongClick: (() -> Unit)? = null
) {
    FixedHeightButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        colors = colors,
        onLongClick = onLongClick,
        buttonHeight = 24.dp,
        textHeightRatio = 0.4f,
        iconHeightRatio = 0.5f,
        horizontalPadding = 8.dp
    )
}

/**
 * 固定高度按钮基础组件
 * 文字和图标大小根据按钮高度按比例计算，不受系统字体缩放影响
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param enabled 是否启用
 * @param icon 可选图标
 * @param colors 按钮颜色
 * @param onLongClick 长按回调（可选）
 * @param buttonHeight 按钮高度
 * @param textHeightRatio 文字高度占按钮高度的比例
 * @param iconHeightRatio 图标高度占按钮高度的比例
 * @param horizontalPadding 水平内边距
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixedHeightButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    colors: ButtonColors = AppTheme.colors.filledTonalButtonColors(),
    onLongClick: (() -> Unit)? = null,
    buttonHeight: Dp = 32.dp,
    textHeightRatio: Float = 0.4f,
    iconHeightRatio: Float = 0.5f,
    horizontalPadding: Dp = 12.dp
) {
    val density = LocalDensity.current
    val textSize = with(density) { (buttonHeight * textHeightRatio).toSp() }
    val iconSize = buttonHeight * iconHeightRatio

    val baseModifier = modifier.height(buttonHeight)
    val finalModifier = if (onLongClick != null) {
        baseModifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            enabled = enabled
        )
    } else {
        baseModifier
    }

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = finalModifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = textSize,
            lineHeight = textSize,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
    }
}

