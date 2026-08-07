package com.shifenmiao.base.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Add

data class CardAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val autoHideAfterClick: Boolean = true // 点击后自动隐藏 ActionBar
)


@Composable
private fun ActionBar(
    actions: List<CardAction>,
    visible: Boolean,
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
    actionContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    actionContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    var clickedActionIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEachIndexed { index, action ->
                val isClicked = clickedActionIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isClicked) 0.85f else 1f,
                    animationSpec = tween(150),
                    label = "actionScale"
                )

                GlassTonalIconButton(
                    onClick = {
                        clickedActionIndex = index
                        action.onClick()
                        // 如果需要自动隐藏，延迟后隐藏
                        if (action.autoHideAfterClick) {
                            scope.launch {
                                delay(200) // 给用户一点视觉反馈时间
                                onHide()
                                delay(100)
                                clickedActionIndex = null
                            }
                        } else {
                            scope.launch {
                                delay(200)
                                clickedActionIndex = null
                            }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scale),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = actionContainerColor,
                        contentColor = actionContentColor
                    ),
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (action != actions.last()) {
                    Spacer(modifier = Modifier.size(4.dp))
                }
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
    supportingContentColor: Color = MaterialTheme.colorScheme.outline
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
            accentColor = colorScheme.primary,
        )
    }
    val settingsState = LocalSettingsState.current

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
                .heightIn(min = 188.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: icon at left, action bar overlaid on long-press
            Box(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (iconName != null) {
                    val isGlass = settingsState.isGlassAlphaEnabled
                    val (iconBg, contentTint) = if (isGlass) {
                        resolvedPalette.iconContainerColor.copy(alpha = 0.92f) to resolvedPalette.iconContentColor
                    } else {
                        resolvedPalette.iconContainerColor to resolvedPalette.iconContentColor
                    }
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
                                size = if (isGlass) 48.dp else 40.dp,
                                shape = RoundedCornerShape(if (isGlass) 12.dp else 16.dp),
                                containerColor = iconBg,
                                tint = contentTint,
                                iconSizeRatio = 0.6f,
                            )


                            IconButton(
                                onClick = { showActions = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(12.dp),
                                    painter = painterResource(id = com.shifenmiao.core.R.drawable.forward_24px),
                                    contentDescription = "Arrow",
                                    tint = supportingContentColor
                                )
                            }
                        }
                    }
                }
                if (actions.isNotEmpty()) {
                    ActionBar(
                        actions = actions,
                        visible = showActions,
                        onHide = { showActions = false },
                        modifier = Modifier.fillMaxWidth(),
                        actionContainerColor = resolvedPalette.actionContainerColor,
                        actionContentColor = resolvedPalette.actionContentColor,
                    )
                }
            }

            // Middle section: title + description
            Column(modifier = Modifier.fillMaxWidth()) {
                if (maxTitleLines == 1) {
                    Text(
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            spacing = MarqueeSpacing(30.dp),
                            velocity = 30.dp,
                            repeatDelayMillis = 1000
                        ),
                        text = title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Clip,
                        color = resolvedPalette.titleColor,
                    )
                } else {
                    Text(
                        text = title,
                        maxLines = maxTitleLines,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeight = MaterialTheme.typography.titleMedium.fontSize * 1.2
                        ),
                        overflow = TextOverflow.Ellipsis,
                        color = resolvedPalette.titleColor,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.labelSmall,
                    color = resolvedPalette.descriptionColor,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
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
    }
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    MaterialTheme.colorScheme.surfaceContainerHigh
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
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
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
