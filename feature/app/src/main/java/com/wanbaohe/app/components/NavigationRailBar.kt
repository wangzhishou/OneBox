package com.wanbaohe.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.wanbaohe.app.update.rememberUpdateAvailable


@Composable
fun NavigationRailBar(
    rootComponent: RootComponent,
    screenList: List<Screen>,
    currentTabPageIndex: Int,
    modifier: Modifier,
    footer: @Composable (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        GlassSurface(
            modifier = modifier.navigationBarsPadding().statusBarsPadding()
                .fillMaxHeight(),
            style = OneBoxDesignSystem.drawerGlassStyle,
            shape = OneBoxDesignSystem.sectionCardShape,
            color = MaterialTheme.colorScheme.surface,
            borderWidth = 0.dp,
        ) {
            NavigationRail(
                // 固定 80dp: 自绘 item 的 fillMaxWidth 在没有宽度约束时会把 rail 撑满全屏
                modifier = Modifier
                    .width(80.dp)
                    .padding(vertical = OneBoxDesignSystem.compactSpacing),
                containerColor = Color.Transparent
            ) {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                val hasUpdateAvailable = rememberUpdateAvailable()
                screenList.forEachIndexed { index, screen ->
                    val isSelected = index == currentTabPageIndex
                    RailNavigationItem(
                        selected = isSelected,
                        icon = if (isSelected) screen.twoToneIcon else screen.icon,
                        label = stringResource(screen.title),
                        contentDescription = stringResource(screen.subtitle),
                        showBadge = hasUpdateAvailable && screen is Screen.Profile,
                        onClick = { rootComponent.navigateTo(screen) },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                footer?.invoke()
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            }
        }
    }
}

/**
 * 竖屏底栏(AppBottomNavigationBar)的选中态是 primaryContainer 胶囊 + 12dp 圆角 + Dense 玻璃;
 * rail 原来用 M3 NavigationRailItem 默认的 secondaryContainer 指示器, 底色和形状都对不上,
 * 这里按底栏的规格自绘, 保证横竖屏选中样式一致。
 */
@Composable
private fun RailNavigationItem(
    selected: Boolean,
    icon: ImageVector?,
    label: String,
    contentDescription: String,
    showBadge: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .indication(interactionSource, ripple(bounded = true))
                .then(
                    if (selected) {
                        if (currentBackdropState() == BackdropState.Glass) {
                            Modifier.glassBackground(
                                style = GlassStyle.Dense,
                                color = selectedContainerColor,
                                shape = shape,
                            )
                        } else {
                            Modifier.background(color = selectedContainerColor, shape = shape)
                        }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (icon != null) {
                BadgedBox(
                    badge = {
                        if (showBadge) {
                            Badge(containerColor = MaterialTheme.colorScheme.errorContainer)
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(22.dp),
                        tint = contentColor,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/** 与底栏一致的背景判定: 有渐变/自定义背景图才走玻璃, 纯色页面直接铺 primaryContainer。 */
private enum class BackdropState { None, Glass }

@Composable
private fun currentBackdropState(): BackdropState {
    val settingsState = LocalSettingsState.current
    val hasBackdrop = settingsState.isMeshGradientBackgroundEnabled ||
        settingsState.customBackgroundImageUri != null
    return if (hasBackdrop) BackdropState.Glass else BackdropState.None
}
