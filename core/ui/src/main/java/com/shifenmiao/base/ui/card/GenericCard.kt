package com.shifenmiao.base.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore

data class CardAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val autoHideAfterClick: Boolean = true // 点击后自动隐藏动作面板
)


// 动作面板每行列数
private const val ACTION_GRID_COLUMNS = 3

/** 「更多」和关闭: 用卡片主题的文字色, 比正文再透一点。 */
private const val CARD_CHROME_ICON_ALPHA = 0.7f

/**
 * 铺满整张卡片的动作面板。底是 surfaceContainerLow。
 * 操作块保留每张卡片自己的主题色, 质感走玻璃。
 * 右上角关闭, 点空白也可收起。
 */
@Composable
private fun ActionGridOverlay(
    actions: List<CardAction>,
    visible: Boolean,
    onHide: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    buttonContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    buttonContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    var clickedActionIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(200)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onHide
                )
        ) {
            val handleActionClick: (Int, CardAction) -> Unit = { index, action ->
                clickedActionIndex = index
                action.onClick()
                scope.launch {
                    delay(200) // 给用户一点视觉反馈时间
                    if (action.autoHideAfterClick) {
                        onHide()
                        delay(100)
                    }
                    clickedActionIndex = null
                }
            }

            // 滚动放在外层 Box 上:内容不足一屏时居中,超出时从顶部完整滚动;
            // 不能在可滚动 Column 上用 Arrangement.Center, 会裁掉首尾内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AppTheme.dimens.paddingNormal,
                            end = AppTheme.dimens.paddingNormal,
                            // 顶部给右上角关闭按钮留位
                            top = 40.dp,
                            bottom = AppTheme.dimens.paddingNormal
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    actions.chunked(ACTION_GRID_COLUMNS).forEachIndexed { rowIndex, rowActions ->
                        if (rowIndex > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowActions.forEachIndexed { columnIndex, action ->
                                val index = rowIndex * ACTION_GRID_COLUMNS + columnIndex
                                val isClicked = clickedActionIndex == index
                                val scale by animateFloatAsState(
                                    targetValue = if (isClicked) 0.85f else 1f,
                                    animationSpec = tween(150),
                                    label = "actionScale"
                                )

                                val tileShape = RoundedCornerShape(16.dp)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .scale(scale)
                                        .glassBackground(
                                            style = GlassStyle.Regular,
                                            shape = tileShape,
                                            color = buttonContainerColor,
                                        )
                                        .clickable { handleActionClick(index, action) }
                                        .padding(horizontal = 4.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.contentDescription,
                                        tint = buttonContentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = action.contentDescription,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = buttonContentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            // 末行不足整行时用空白占位,保持网格对齐
                            repeat(ACTION_GRID_COLUMNS - rowActions.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onHide,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(com.shifenmiao.core.R.string.close),
                    tint = buttonContentColor.copy(alpha = CARD_CHROME_ICON_ALPHA),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun GenericTonalCard(
    id: Int,
    themeColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    iconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    actionContainerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    actionContentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    iconName: String? = null,
    maxTitleLines: Int = 1,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    actions: List<CardAction> = emptyList(),
    palette: TonalCardPalette? = null,
    stateBar: @Composable ((showActions: Boolean, onToggleActions: () -> Unit) -> Unit)? = null,
    supportingContentColor: Color = MaterialTheme.colorScheme.outline,
    // 搜索场景的高亮文本(由调用方预先计算); 为 null 时按普通字符串渲染, 行为与之前一致
    highlightedTitle: AnnotatedString? = null,
    highlightedDescription: AnnotatedString? = null,
) {
    var showActions by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val resolvedPalette = remember(
        palette,
        themeColor,
        iconContentColor,
        iconContainerColor,
        actionContainerColor,
        actionContentColor,
        colorScheme,
        supportingContentColor
    ) {
        palette ?: TonalCardPalette(
            containerColor = themeColor,
            iconContentColor = iconContentColor,
            iconContainerColor = iconContainerColor,
            actionContainerColor = actionContainerColor,
            actionContentColor = actionContentColor,
            tagBackgroundColor = colorScheme.primaryContainer,
            tagTextColor = colorScheme.onPrimaryContainer,
            titleColor = colorScheme.onSurface,
            descriptionColor = colorScheme.onSurfaceVariant,
            supportingContentColor = supportingContentColor,
            accentColor = colorScheme.onPrimaryContainer,
        )
    }
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .glassBackground(
                style = GlassStyle.Regular,
                shape = shape,
                color = resolvedPalette.containerColor
            )
            .combinedClickable(
                onClick = {
                    showActions = false
                    onClick.invoke()
                },
                onLongClick = {
                    if (actions.isNotEmpty()) {
                        showActions = !showActions
                    }
                    onLongClick?.invoke()
                }
            )
    } else {
        modifier
            .clip(shape)
            .glassBackground(
                style = GlassStyle.Regular,
                shape = shape,
                color = resolvedPalette.containerColor,
            )
    }

    Box(modifier = cardModifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal)
                // 高度锁死, 描述固定三行也落在这 188dp 里, 双列才能对齐
                .height(188.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 左上角功能图标, 右上角「更多」打开整卡操作面板。
            // 不用外链箭头: 这些入口都在应用内打开, ↗ 会被读成「离开本应用」。
            Box(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (iconName != null) {
                    val iconBg = resolvedPalette.iconContainerColor
                    val contentTint = resolvedPalette.iconContentColor
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !showActions,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(200))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            IconAvatar(
                                iconName = iconName,
                                size = 40.dp,
                                shape = RoundedCornerShape(16.dp),
                                containerColor = iconBg,
                                tint = contentTint,
                                iconSizeRatio = 0.6f,
                            )

                            IconButton(
                                onClick = { showActions = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                                    contentDescription = stringResource(
                                        com.shifenmiao.core.R.string.card_more_actions
                                    ),
                                    tint = resolvedPalette.iconContentColor.copy(
                                        alpha = CARD_CHROME_ICON_ALPHA
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Middle section: title + description
            Column(modifier = Modifier.fillMaxWidth()) {
                val titleText = highlightedTitle ?: AnnotatedString(title)
                if (maxTitleLines == 1) {
                    Text(
                        // fillMaxWidth 先把宽度钉在卡片内;超长标题自动降字号,仍放不下才省略号,
                        // 不再用 marquee —— 长语种(德语等)会永远滚动、静止帧只剩残词
                        modifier = Modifier.fillMaxWidth(),
                        text = titleText,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 13.sp,
                            maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                            stepSize = 0.5.sp,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = resolvedPalette.titleColor,
                    )
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = titleText,
                        maxLines = maxTitleLines,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeight = MaterialTheme.typography.titleMedium.fontSize * 1.2
                        ),
                        overflow = TextOverflow.Ellipsis,
                        color = resolvedPalette.titleColor,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                CardDescription(
                    text = highlightedDescription ?: AnnotatedString(description),
                    color = resolvedPalette.descriptionColor,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom section: stateBar
            if (stateBar != null) {
                stateBar(showActions) { showActions = !showActions }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 长按 / 点击箭头后动作面板铺满整张卡片, 右上角为关闭按钮
        if (actions.isNotEmpty()) {
            ActionGridOverlay(
                actions = actions,
                visible = showActions,
                onHide = { showActions = false },
                shape = shape,
                // 卡片位于 LazyVerticalStaggeredGrid 中, 高度约束为无限,
                // 必须用 matchParentSize 跟随卡片实际高度, 否则内部滚动容器会崩溃
                modifier = Modifier.matchParentSize(),
                buttonContainerColor = resolvedPalette.iconContainerColor,
                buttonContentColor = resolvedPalette.iconContentColor,
            )
        }
    }
}

/**
 * 描述固定三行高。短文留白, 超出从最后一行渐隐。
 * 行高写死, 避免和卡片的 188dp 互相把网格撑开。
 */
@Composable
private fun CardDescription(
    text: AnnotatedString,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var overflows by remember(text) { mutableStateOf(false) }
    val descriptionStyle = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp)
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .then(if (overflows) Modifier.fadedBottomEdge() else Modifier),
        textAlign = TextAlign.Left,
        style = descriptionStyle,
        color = color,
        minLines = 3,
        maxLines = 3,
        overflow = TextOverflow.Clip,
        onTextLayout = { overflows = it.hasVisualOverflow },
    )
}

/** 把文本自身的底部淡出, 玻璃底色会透出来, 不依赖卡片填色。 */
private fun Modifier.fadedBottomEdge(): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    0.68f to Color.Black,
                    1f to Color.Transparent,
                ),
                blendMode = BlendMode.DstIn,
            )
        }


@Composable
fun PlaceholderCard(
    onClick: (() -> Unit)? = null,
    title: String? = null,
    description: String? = null,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .heightIn(216.dp)
            .glassThin(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .clickable {
                onClick?.invoke()
            }
    ) {
        if (title == null) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.contentColorFor(
                    MaterialTheme.colorScheme.surfaceContainer
                ).copy(alpha = 0.3f),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
