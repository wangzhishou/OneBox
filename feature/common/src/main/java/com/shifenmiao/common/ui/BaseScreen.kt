/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.common.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.other.TopAppBarEmoji
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
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_button))
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
    // 传入后，点击标题栏会把内容滚动区一次性送回首条；不传则标题栏无任何点击行为
    onTitleClick: (() -> Unit)? = null,
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
        onTitleClick = onTitleClick,
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
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_button))
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
    // 传入后，点击标题栏会把内容滚动区一次性送回首条；不传则标题栏无任何点击行为
    onTitleClick: (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    // 判断玻璃效果是否真正激活：需要参数开启 + 全局设置启用
    val settingsState = LocalSettingsState.current
    val isGlassActive = supportGlassEffect && settingsState.isGlassAlphaEnabled

    // 标题栏点击只负责「一次性回到首条内容」：不接管子按钮的点击(它们会先消费事件)，
    // 也不触碰页面自身的自动跟随 / nestedScroll / 悬浮回顶按钮等既有滚动行为。
    // 用 onTap 而非 onDoubleTap：单击即时响应无双击判定延迟，双击只会重复一次幂等滚动。
    val currentOnTitleClick by rememberUpdatedState(onTitleClick)
    val titleBarTapModifier = if (onTitleClick != null) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { currentOnTitleClick?.invoke() })
        }
    } else Modifier

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
                    modifier = titleBarTapModifier,
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
