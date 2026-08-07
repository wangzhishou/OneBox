package com.shifenmiao.common.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.shifenmiao.base.ui.tab.TabPosition
import com.shifenmiao.base.ui.tab.ScrollableTabRow
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun <T> GenericScrollableTabRow(
    pagerState: PagerState,
    items: List<T>,
    coroutineScope: CoroutineScope,
    indicatorHeight: Dp = 40.dp,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    edgePadding: Dp = AppTheme.dimens.paddingNormal,
    indicatorShape: Shape,
    iconSpacing: Dp = 5.dp,
    iconSize: Dp = 12.dp,
    getTitle: @Composable (T) -> String?,
    getIcon: @Composable (T) -> ImageVector? = { null },
) {
    val lastIndex = (items.size - 1).coerceAtLeast(0)
    val currentPage = pagerState.currentPage.coerceIn(0, lastIndex)
    val scrollPosition by remember(pagerState, lastIndex) {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, lastIndex.toFloat())
        }
    }
    val isPagerScrolling by remember(pagerState) {
        derivedStateOf { pagerState.isScrollInProgress }
    }

    val horizontalPadding = 16.dp
    val verticalPadding = 2.dp
    val tabSpacing = 7.dp

    val unselectedBg = MaterialTheme.colorScheme.surfaceContainer
    val selectedBg = MaterialTheme.colorScheme.primaryContainer
    val selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    ScrollableTabRow(
        modifier = modifier.padding(top = 0.dp, bottom = 4.dp),
        edgePadding = edgePadding,
        selectedTabIndex = currentPage,
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            val highlightBounds = tabPositions.calculateHighlightBounds(
                scrollPosition = scrollPosition,
                tabSpacing = tabSpacing,
            )

            if (highlightBounds != null) {
                val animatedLeft by animateDpAsState(
                    targetValue = highlightBounds.left,
                    animationSpec = if (isPagerScrolling) {
                        snap()
                    } else {
                        spring(
                            dampingRatio = 0.78f,
                            stiffness = 650f,
                        )
                    },
                    label = "tabIndicatorLeft",
                )
                val animatedWidth by animateDpAsState(
                    targetValue = highlightBounds.width,
                    animationSpec = if (isPagerScrolling) {
                        snap()
                    } else {
                        spring(
                            dampingRatio = 0.82f,
                            stiffness = 700f,
                        )
                    },
                    label = "tabIndicatorWidth",
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = animatedLeft.roundToPx(), y = 0) }
                            .width(animatedWidth)
                            .height(indicatorHeight)
                            .glassDense(shape = indicatorShape, color = selectedBg)
                            .clip(indicatorShape),
                    )
                }
            }
        },
        trailingContent = trailingContent,
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = currentPage == index
            val selectionFraction = tabSelectionFraction(
                index = index,
                scrollPosition = scrollPosition,
            )
            val textColor = lerpColor(
                start = unselectedTextColor,
                stop = selectedTextColor,
                fraction = selectionFraction,
            )
            val iconProgress = selectedContentFraction(selectionFraction)
            val iconSlotWidth = lerp(0.dp, iconSize, iconProgress)
            val iconSpacerWidth = lerp(0.dp, iconSpacing, iconProgress)

            val interactionSource = remember { MutableInteractionSource() }
            val icon = getIcon(item)

            Box(
                modifier = Modifier
                    .height(indicatorHeight)
                    .padding(start = if (index == 0) 0.dp else tabSpacing, end = tabSpacing)
                    .glassThin(shape = indicatorShape, color = unselectedBg)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        coroutineScope.launch {
                            if (index < items.size) {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .width(iconSlotWidth)
                                .clipToBounds(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (iconProgress > 0f) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = textColor.copy(alpha = iconProgress),
                                    modifier = Modifier.size(iconSize),
                                )
                            }
                        }
                        if (iconSpacerWidth > 0.dp) {
                            Spacer(modifier = Modifier.width(iconSpacerWidth))
                        }
                    }
                    val title = getTitle(item)
                    if (title != null) {
                        Text(
                            text = title,
                            style = if (isSelected) {
                                MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            } else {
                                MaterialTheme.typography.titleSmall
                            },
                            color = textColor,
                        )
                    }
                }
            }
        }
    }
}

private data class TabHighlightBounds(
    val left: Dp,
    val width: Dp,
)

private fun List<TabPosition>.calculateHighlightBounds(
    scrollPosition: Float,
    tabSpacing: Dp,
): TabHighlightBounds? {
    if (isEmpty()) return null

    val boundedPosition = scrollPosition.coerceIn(0f, lastIndex.toFloat())
    val startIndex = floor(boundedPosition).toInt().coerceIn(indices)
    val endIndex = ceil(boundedPosition).toInt().coerceIn(indices)
    val fraction = boundedPosition - startIndex

    val startBounds = this[startIndex].toHighlightBounds(index = startIndex, tabSpacing = tabSpacing)
    val endBounds = this[endIndex].toHighlightBounds(index = endIndex, tabSpacing = tabSpacing)

    return TabHighlightBounds(
        left = lerp(startBounds.left, endBounds.left, fraction),
        width = lerp(startBounds.width, endBounds.width, fraction),
    )
}

private fun TabPosition.toHighlightBounds(
    index: Int,
    tabSpacing: Dp,
): TabHighlightBounds {
    val startInset = if (index == 0) 0.dp else tabSpacing
    val adjustedWidth = width - startInset - tabSpacing

    return TabHighlightBounds(
        left = left + startInset,
        width = if (adjustedWidth > 0.dp) adjustedWidth else 0.dp,
    )
}

private fun tabSelectionFraction(
    index: Int,
    scrollPosition: Float,
): Float = (1f - (scrollPosition - index).absoluteValue.coerceIn(0f, 1f))

private fun selectedContentFraction(selectionFraction: Float): Float {
    val revealThreshold = 0.55f
    return ((selectionFraction - revealThreshold) / (1f - revealThreshold)).coerceIn(0f, 1f)
}


