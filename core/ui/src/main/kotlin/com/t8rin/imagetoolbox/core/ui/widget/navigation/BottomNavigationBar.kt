package com.t8rin.imagetoolbox.core.ui.widget.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium

@Immutable
data class BottomNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector?,
    val selectedIcon: ImageVector? = icon,
    val contentDescription: String = label,
    val selectedContainerColor: Color? = null,
    val selectedContentColor: Color? = null,
    val unselectedContentColor: Color? = null,
    val enabled: Boolean = true,
)

@Immutable
data class BottomNavCenterAction(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label,
    val expanded: Boolean = false,
    val selectedContainerColor: Color? = null,
    val selectedContentColor: Color? = null,
    val unselectedContentColor: Color? = null,
)

@Immutable
data class BottomNavigationBarStyle(
    val containerGlassStyle: GlassStyle = GlassStyle.Regular,
    val containerShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val outerHorizontalPadding: Dp = 0.dp,
    val innerHorizontalPadding: Dp = 3.dp,
    val outerTopPadding: Dp = 2.dp,
    val innerTopPadding: Dp = 4.dp,
    val selectedItemGlassStyle: GlassStyle = GlassStyle.Dense,
    val tabItemShape: Shape = RoundedCornerShape(12.dp),
    val tabHorizontalPadding: Dp = 12.dp,
    val tabVerticalPadding: Dp = 5.dp,
    val tabIconSize: Dp = 22.dp,
    val centerButtonOffsetY: Dp = (-2).dp,
    val centerButtonSize: Dp = 54.dp,
    val centerIconSize: Dp = 32.dp,
)

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    selectedItemId: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = AppTheme.dimens.navigationHeight,
    showBar: Boolean = true,
    navigationBarsPadding: Boolean = true,
    imePadding: Boolean = false,
    centerAction: BottomNavCenterAction? = null,
    onCenterActionClick: (() -> Unit)? = null,
    tabTextStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(lineHeight = 24.sp),
    style: BottomNavigationBarStyle = BottomNavigationBarStyle(),
) {
    val systemBarsBottom = if (navigationBarsPadding) {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }
    val bottomBarHeight = remember(height, systemBarsBottom) {
        height + systemBarsBottom
    }
    AnimatedVisibility(
        visible = showBar,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = style.outerHorizontalPadding)
                .padding(top = style.outerTopPadding),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight)
                    .glassBackground(
                        style = style.containerGlassStyle,
                        shape = style.containerShape,
                        color = MaterialTheme.colorScheme.surface,
                    )
                    .let { if (navigationBarsPadding) it.navigationBarsPadding() else it }
                    .let { if (imePadding) it.imePadding() else it }
                    .padding(top = style.innerTopPadding)
                    .padding(horizontal = style.innerHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (centerAction == null) {
                    items.forEachIndexed { index, item ->
                        BottomNavigationTabContainer {
                            BottomNavigationTabItem(
                                item = item,
                                isSelected = selectedItemId == item.id,
                                colorIndex = index,
                                tabTextStyle = tabTextStyle,
                                style = style,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                } else {
                    val leftCount = items.size / 2
                    items.take(leftCount).forEachIndexed { index, item ->
                        BottomNavigationTabContainer {
                            BottomNavigationTabItem(
                                item = item,
                                isSelected = selectedItemId == item.id,
                                colorIndex = index,
                                tabTextStyle = tabTextStyle,
                                style = style,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }

                    BottomNavigationTabContainer {
                        CenterActionButton(
                            action = centerAction,
                            style = style,
                            onClick = onCenterActionClick,
                        )
                    }

                    items.drop(leftCount).forEachIndexed { relativeIndex, item ->
                        val absoluteIndex = relativeIndex + leftCount
                        BottomNavigationTabContainer {
                            BottomNavigationTabItem(
                                item = item,
                                isSelected = selectedItemId == item.id,
                                colorIndex = absoluteIndex,
                                tabTextStyle = tabTextStyle,
                                style = style,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavigationTabContainer(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun BottomNavigationTabItem(
    item: BottomNavItem,
    isSelected: Boolean,
    colorIndex: Int,
    tabTextStyle: TextStyle,
    style: BottomNavigationBarStyle,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val defaultSelectedBg = MaterialTheme.colorScheme.primaryContainer
    val defaultSelectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val selectedBgColor = item.selectedContainerColor ?: defaultSelectedBg
    val selectedContentColor = item.selectedContentColor ?: defaultSelectedContent
    val unselectedContentColor =
        item.unselectedContentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                enabled = item.enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(style.tabItemShape)
                .indication(interactionSource, ripple(bounded = true))
                .then(
                    if (isSelected) {
                        Modifier.glassBackground(
                            style = style.selectedItemGlassStyle,
                            color = selectedBgColor,
                            shape = style.tabItemShape,
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(
                    horizontal = style.tabHorizontalPadding,
                    vertical = style.tabVerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val imageVector = if (isSelected) item.selectedIcon else item.icon
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = item.contentDescription,
                    modifier = Modifier.size(style.tabIconSize),
                    tint = if (isSelected) selectedContentColor else unselectedContentColor,
                )
            }
            Text(
                text = item.label,
                style = tabTextStyle,
                maxLines = 1,
                // 标签超长时不换行不省略,Marquee 滚动展示(与列表 Item 标题一致)
                modifier = Modifier.basicMarquee(),
                textAlign = TextAlign.Center,
                color = if (isSelected) selectedContentColor else unselectedContentColor,
            )
        }
    }
}

@Composable
private fun CenterActionButton(
    action: BottomNavCenterAction,
    style: BottomNavigationBarStyle,
    onClick: (() -> Unit)?,
) {
    val rotation by animateFloatAsState(
        targetValue = if (action.expanded) 45f else 0f,
        animationSpec = tween(300),
        label = "center_action_icon_rotation",
    )
    val defaultSelectedBg = MaterialTheme.colorScheme.primaryContainer
    val defaultSelectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val selectedBgColor = action.selectedContainerColor ?: defaultSelectedBg
    val selectedContentColor = action.selectedContentColor ?: defaultSelectedContent

    Column(
        modifier = Modifier
            .offset(y = style.centerButtonOffsetY)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(style.centerButtonSize)
                .glassDense(
                    shape = CircleShape,
                    color = selectedBgColor,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                modifier = Modifier
                    .size(style.centerIconSize)
                    .rotate(rotation),
                tint = selectedContentColor,
            )
        }

        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            color = if (action.expanded) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
