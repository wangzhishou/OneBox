package com.t8rin.imagetoolbox.core.ui.widget.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium

/**
 * 底栏是否该有自己的底色。
 *
 * 只有页面铺了背景层(渐变背景 / 自定义背景图)时才需要 —— 此时底栏要压住背后的
 * 色块或图片,否则内容会从底栏位置透出来。纯色页面下底栏不画任何底色,与页面
 * 完全连成一体。
 *
 * 判定与 [com.t8rin.imagetoolbox.core.ui.widget.glass.effectiveGlassBorderAlpha]
 * 的"没有背景层就不画描边"保持一致。
 *
 * - [BackdropState.None]:无背景层,不画底;
 * - [BackdropState.Glass]:有背景层且玻璃开关打开,画毛玻璃;
 * - [BackdropState.Solid]:有背景层但玻璃关闭,画不透明 surface 实色底。
 */
@Composable
private fun rememberBackdropState(): BackdropState {
    val settingsState = LocalSettingsState.current
    val hasBackdrop = settingsState.isMeshGradientBackgroundEnabled ||
        settingsState.customBackgroundImageUri != null
    return when {
        !hasBackdrop -> BackdropState.None
        settingsState.isGlassAlphaEnabled -> BackdropState.Glass
        else -> BackdropState.Solid
    }
}

private enum class BackdropState { None, Glass, Solid }

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
    /** 在图标右上角显示一个小红点（例如"我的"页里躺着一个新版本）。 */
    val hasBadge: Boolean = false,
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
    /** 容器内容左右的内边距:胶囊贴合文字后,避免多 tab 时首尾胶囊顶到容器边缘 */
    val innerHorizontalPadding: Dp = 10.dp,
    val outerTopPadding: Dp = 2.dp,
    val innerTopPadding: Dp = 4.dp,
    val selectedItemGlassStyle: GlassStyle = GlassStyle.Dense,
    val tabItemShape: Shape = RoundedCornerShape(12.dp),
    /** 选中胶囊的最大宽度:文字短时胶囊贴合文字,超长不突破该上限,内部跑马灯滚动 */
    val tabMaxWidth: Dp = 96.dp,
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
    val backdropState = rememberBackdropState()
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
                    .then(
                        when (backdropState) {
                            BackdropState.None -> Modifier
                            BackdropState.Glass -> Modifier.glassBackground(
                                style = style.containerGlassStyle,
                                shape = style.containerShape,
                                color = MaterialTheme.colorScheme.surface,
                            )

                            BackdropState.Solid -> Modifier.background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = style.containerShape,
                            )
                        }
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
    val backdropState = rememberBackdropState()
    val defaultSelectedBg = MaterialTheme.colorScheme.primaryContainer
    val defaultSelectedContent = MaterialTheme.colorScheme.onPrimaryContainer
    val selectedBgColor = item.selectedContainerColor ?: defaultSelectedBg
    val selectedContentColor = item.selectedContentColor ?: defaultSelectedContent
    val unselectedContentColor =
        item.unselectedContentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                .widthIn(max = style.tabMaxWidth)
                .clip(style.tabItemShape)
                .indication(interactionSource, ripple(bounded = true))
                .then(
                    if (isSelected) {
                        if (backdropState == BackdropState.Glass) {
                            Modifier.glassBackground(
                                style = style.selectedItemGlassStyle,
                                color = selectedBgColor,
                                shape = style.tabItemShape,
                            )
                        } else {
                            Modifier.background(
                                color = selectedBgColor,
                                shape = style.tabItemShape,
                            )
                        }
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
                BadgedBox(
                    badge = {
                        if (item.hasBadge) {
                            Badge(containerColor = MaterialTheme.colorScheme.error)
                        }
                    }
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = item.contentDescription,
                        modifier = Modifier.size(style.tabIconSize),
                        tint = if (isSelected) selectedContentColor else unselectedContentColor,
                    )
                }
            }
            Text(
                text = item.label,
                style = tabTextStyle,
                maxLines = 1,
                // 按可用宽度自动降字号:日语 アシスタント / プロフィール 等长标签整词显示,不再被硬裁成残词。
                // 注意不能同时用 basicMarquee —— marquee 会用无界宽度测量,autoSize 会失效。
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 9.sp,
                    maxFontSize = tabTextStyle.fontSize,
                    stepSize = 0.5.sp,
                ),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip,
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
    val backdropState = rememberBackdropState()
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
                .then(
                    if (backdropState == BackdropState.Glass) {
                        Modifier.glassDense(
                            shape = CircleShape,
                            color = selectedBgColor,
                        )
                    } else {
                        Modifier.background(
                            color = selectedBgColor,
                            shape = CircleShape,
                        )
                    }
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
