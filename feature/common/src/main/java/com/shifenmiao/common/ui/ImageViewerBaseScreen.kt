package com.shifenmiao.common.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload

/**
 * 图片查看器基础屏幕
 *
 * ## 模块位置
 * - **模块**: `feature/common`
 * - **包路径**: `com.shifenmiao.common.ui`
 * - **文件**: `ImageViewerBaseScreen.kt`
 *
 * ## 功能概述
 * 提供全屏沉浸式图片查看体验：
 * - 全屏黑色背景
 * - 透明顶部导航栏
 * - 沉浸式模式支持
 * - 自定义 TopAppBar 内容
 *
 * ## 使用示例
 * ```kotlin
 * ImageViewerBaseScreen(
 *     onDismiss = { /* 关闭 */ },
 *     immersiveModeState = immersiveModeState,
 *     title = { PageIndicator(pageCount = 10, currentPage = 0) },
 *     actions = {
 *         IconButton(onClick = { /* 下载 */ }) {
 *             Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload, contentDescription = "Download")
 *         }
 *     }
 * ) {
 *     // 图片内容
 * }
 * ```
 *
 * @param onDismiss 关闭回调
 * @param modifier Modifier
 * @param immersiveModeState 沉浸式模式状态
 * @param backDirectlyExit 返回键是否直接退出（true: 直接退出，false: 先退出沉浸模式再退出）
 * @param backgroundColor 背景颜色
 * @param title 标题内容（居中显示）
 * @param actions 右侧操作按钮
 * @param content 主体内容
 */
@Composable
fun ImageViewerBaseScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    immersiveModeState: ImmersiveModeState? = null,
    backDirectlyExit: Boolean = false,
    backgroundColor: Color = Color.Black.copy(alpha = 0.9f),
    title: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val isUiVisible = immersiveModeState?.isUiVisible ?: true

    BackHandler {
        if (!backDirectlyExit && immersiveModeState?.isImmersive == true) {
            immersiveModeState.exitImmersive()
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 主体内容（居中）
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }

        // 顶部导航栏（沉浸式动画）
        ImmersiveTopContent(visible = isUiVisible) {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .systemBarsPadding()
                    .background(Color.Transparent),
                title = title,
                navigationIcon = {
                    ViewerBackButton(onClick = onDismiss)
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    }
}

/**
 * 图片查看器返回按钮
 * 半透明背景的圆形按钮
 */
@Composable
fun ViewerBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
            contentDescription = "Close"
        )
    }
}

/**
 * 图片查看器操作按钮
 * 半透明背景的圆形按钮，用于下载、分享等操作
 */
@Composable
fun ViewerActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        content()
    }
}

