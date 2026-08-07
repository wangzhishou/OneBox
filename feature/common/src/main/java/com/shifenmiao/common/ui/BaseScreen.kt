package com.shifenmiao.common.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    onGoBack: () -> Unit = {},
    colors: TopAppBarColors = topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
    background: @Composable (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = {
        IconButton(
            onClick = {
                onGoBack()
            }
        ) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = "Back")
        }
    },
    actions: @Composable (RowScope.() -> Unit?)? = null,
    foreground: @Composable BoxScope.() -> Unit = {},
    supportGlassEffect: Boolean = true,
    isShowDefaultActions: Boolean = true,
    showNavigationBarsPadding: Boolean = true,
    isBackHandler: Boolean = true,
    immersiveModeState: ImmersiveModeState? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    BaseScreen(
        modifier = modifier,
        title = {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp),
                    velocity = 30.dp,
                    repeatDelayMillis = 1000
                ),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        },
        colors = colors,
        backgroundImage = background,
        onGoBack = onGoBack,
        navigationIcon = navigationIcon,
        actions = actions,
        foreground = foreground,
        supportGlassEffect = supportGlassEffect,
        isShowDefaultActions = isShowDefaultActions,
        showNavigationBarsPadding = showNavigationBarsPadding,
        isBackHandler = isBackHandler,
        immersiveModeState = immersiveModeState,
        scrollBehavior = scrollBehavior,
        content = content
    )
}

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onGoBack: () -> Unit,
    colors: TopAppBarColors = topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
    navigationIcon: @Composable (() -> Unit)? = {
        IconButton(
            onClick = {
                onGoBack()
            }
        ) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = "Back")
        }
    },
    backgroundImage: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit?)? = null,
    foreground: @Composable (BoxScope.() -> Unit) = {},
    supportGlassEffect: Boolean = true,
    isShowDefaultActions: Boolean = true,
    showNavigationBarsPadding: Boolean = false,
    isBackHandler: Boolean = true,
    immersiveModeState: ImmersiveModeState? = null,
    type: EnhancedTopAppBarType = EnhancedTopAppBarType.Center,
    containerColor: Color? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    // 判断玻璃效果是否真正激活：需要参数开启 + 全局设置启用
    val settingsState = LocalSettingsState.current
    val isGlassActive = supportGlassEffect && settingsState.isGlassAlphaEnabled

    // 玻璃模式：全部透明，让背景内容完全透出；非玻璃模式：恢复原始配色
    val resolvedTopBarColors = if (isGlassActive) {
        colors.copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    } else colors

    // 是否显示 UI（非沉浸式模式）
    val isUiVisible = immersiveModeState?.isUiVisible ?: true

    Box(
        modifier = modifier
            .fillMaxSize()
            .let { if (showNavigationBarsPadding) it.navigationBarsPadding() else it }
            .let { if(containerColor != null) it.then(Modifier.background(containerColor)) else it }
    ) {
        if (backgroundImage != null) {
            backgroundImage()
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar 使用沉浸式动画
            ImmersiveTopContent(visible = isUiVisible) {
                EnhancedTopAppBar(
                    type = type,
                    title = title,
                    isGlassActive = supportGlassEffect,
                    navigationIcon = {
                        if (navigationIcon != null) {
                            navigationIcon()
                        }
                    },
                    actions = {
                        if (actions != null) {
                            this.actions()
                        } else if (isShowDefaultActions) {
                            TopAppBarEmoji()
                        }
                    },
                    colors = resolvedTopBarColors,
                    scrollBehavior = scrollBehavior,
                    // 玻璃模式下禁用底部分割线/阴影，避免与玻璃效果冲突
                    drawHorizontalStroke = !isGlassActive,
                )
            }
            if (content != null) {
                content()
            }
        }
        foreground()
    }
    if (isBackHandler) {
        BackHandler {
            // 如果处于沉浸式模式，先退出沉浸式模式
            if (immersiveModeState?.isImmersive == true) {
                immersiveModeState.exitImmersive()
            } else {
                onGoBack()
            }
        }
    }
}
