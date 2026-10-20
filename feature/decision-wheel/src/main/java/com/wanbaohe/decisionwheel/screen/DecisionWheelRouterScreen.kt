package com.wanbaohe.decisionwheel.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCasino
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelRouterComponent
import com.wanbaohe.decisionwheel.component.WheelTab

@Composable
fun DecisionWheelRouterScreen(
    router: DecisionWheelRouterComponent
) {
    val stack by router.childStack.subscribeAsState()
    val tab by router.currentTab.collectAsState()

    // 栈顶是 Spin（= 没有压入编辑/列表页）时才显示底部 tab 栏
    val showTabs = stack.active.configuration is DecisionWheelRouterComponent.Route.Spin

    val bottomInset: Dp = if (showTabs) {
        AppTheme.dimens.navigationHeight +
            WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (tab == WheelTab.SPIN || !showTabs) {
            Children(
                stack = router.childStack,
                modifier = Modifier.fillMaxSize()
            ) { child ->
                when (val instance = child.instance) {
                    is DecisionWheelRouterComponent.Child.Spin ->
                        DecisionWheelSpinScreen(
                            component = instance.component,
                            bottomInset = bottomInset
                        )

                    is DecisionWheelRouterComponent.Child.Editor ->
                        DecisionWheelEditorScreen(component = instance.component)

                    DecisionWheelRouterComponent.Child.WheelList ->
                        DecisionWheelListScreen(router = router)
                }
            }
        } else {
            when (tab) {
                WheelTab.HISTORY -> DecisionWheelHistoryScreen(
                    router = router,
                    bottomInset = bottomInset
                )

                WheelTab.SETTINGS -> DecisionWheelSettingsScreen(
                    router = router,
                    bottomInset = bottomInset
                )

                WheelTab.SPIN -> Unit
            }
        }

        if (showTabs) {
            // BottomNavigationBar 内部把内容套在 AnimatedVisibility 里，传进去的 modifier
            // 只作用到内层 Box —— align 必须落在外层这层包裹 Box 上，否则栏会跑到屏幕顶部。
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                DecisionWheelBottomBar(
                    current = tab,
                    onSelect = router::switchTab
                )
            }
        }
    }
}

@Composable
private fun DecisionWheelBottomBar(
    current: WheelTab,
    onSelect: (WheelTab) -> Unit
) {
    val tabs = listOf(
        WheelTab.SPIN to BottomNavItem(
            id = WheelTab.SPIN.ordinal.toString(),
            label = stringResource(R.string.wheel_tab),
            icon = Icons.Outlined.LineCasino
        ),
        WheelTab.HISTORY to BottomNavItem(
            id = WheelTab.HISTORY.ordinal.toString(),
            label = stringResource(R.string.history),
            icon = Icons.Outlined.LineHistory
        ),
        WheelTab.SETTINGS to BottomNavItem(
            id = WheelTab.SETTINGS.ordinal.toString(),
            label = stringResource(R.string.wheel_settings),
            icon = Icons.Outlined.LineSettings
        )
    )

    BottomNavigationBar(
        items = tabs.map { it.second },
        selectedItemId = current.ordinal.toString(),
        onItemClick = { item ->
            val index = item.id.toIntOrNull() ?: return@BottomNavigationBar
            onSelect(tabs.getOrNull(index)?.first ?: return@BottomNavigationBar)
        },
        modifier = Modifier.fillMaxWidth()
    )
}
