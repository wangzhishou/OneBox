package com.wanbaohe.boardgame.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveBottomContent
import com.shifenmiao.common.ui.ImmersiveModeState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.boardgame.model.BoardGameTab

/**
 * 提供给对弈子屏幕(对局/分析)的沉浸式模式状态。
 * Router 顶层创建,Game/Analysis Screen 可通过该 CompositionLocal toggle 全屏。
 */
val LocalBoardGameImmersiveModeState = compositionLocalOf<ImmersiveModeState?> { null }

/**
 * 棋类模块页面骨架:BaseScreen + 底部 tab 导航 + 沉浸式支持,棋种无关。
 * 内容区(Decompose Children 等)由调用方以 [content] slot 传入。
 *
 * @param onBack 自定义返回处理(出栈/回第一个 tab 等);null 时直接 [onGoBack]
 * @param headerActions 标题栏动作(如「新对局」菜单);沉浸模式下自动隐藏
 */
@Composable
fun BoardGameTabScaffold(
    title: String,
    tabs: List<BoardGameTab>,
    selectedTabId: String,
    onTabSelected: (BoardGameTab) -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
    immersiveState: ImmersiveModeState = rememberImmersiveModeState(),
    backHandlerEnabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    headerActions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBoardGameImmersiveModeState provides immersiveState) {
        BackHandler(enabled = backHandlerEnabled) {
            if (immersiveState.isImmersive) immersiveState.exitImmersive()
            else (onBack ?: onGoBack).invoke()
        }
        BaseScreen(
            modifier = modifier,
            title = title,
            onGoBack = onGoBack,
            isBackHandler = false,
            showNavigationBarsPadding = false,
            immersiveModeState = immersiveState,
            actions = {
                if (!immersiveState.isImmersive) {
                    headerActions?.invoke()
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds()
                    .then(
                        if (immersiveState.isImmersive) Modifier.statusBarsPadding() else Modifier
                    )
            ) {
                content()
            }

            ImmersiveBottomContent(visible = immersiveState.isUiVisible) {
                BottomNavigationBar(
                    items = tabs.map { BottomNavItem(id = it.id, label = it.label, icon = it.icon) },
                    selectedItemId = selectedTabId,
                    onItemClick = { item ->
                        tabs.firstOrNull { it.id == item.id }?.let(onTabSelected)
                    },
                )
            }
        }
    }
}
