package com.wanbaohe.xiangqi.router

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveBottomContent
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.xiangqi.screen.NewGameDropMenu
import com.wanbaohe.xiangqi.screen.XiangqiAnalysisScreen
import com.wanbaohe.xiangqi.screen.XiangqiEmptyQuickPanel
import com.wanbaohe.xiangqi.screen.XiangqiGameScreen
import com.wanbaohe.xiangqi.screen.XiangqiLibraryScreen
import com.wanbaohe.xiangqi.screen.XiangqiSettingsScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wanbaohe.xiangqi.di.XiangqiOnlineEntryPoint
import com.wanbaohe.xiangqi.screen.OnlineMatchScreen
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSportsEsports

@Composable
fun XiangqiRouterScreen(component: XiangqiRouterComponent) {
    val immersiveState = rememberImmersiveModeState()
    val childStack by component.childStack.subscribeAsState()
    val activeRoute = childStack.active.configuration
    val activeTab = component.tabOf(activeRoute)
    val tabs = listOf(
        BottomNavItem(
            id = XiangqiRouterComponent.Tab.Play.name,
            label = stringResource(R.string.xiangqi_tab_play),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSportsEsports,
        ),
        BottomNavItem(
            id = XiangqiRouterComponent.Tab.Analyze.name,
            label = stringResource(R.string.xiangqi_tab_analyze),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics,
        ),
        BottomNavItem(
            id = XiangqiRouterComponent.Tab.Library.name,
            label = stringResource(R.string.xiangqi_tab_library),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
        ),
        BottomNavItem(
            id = XiangqiRouterComponent.Tab.Settings.name,
            label = stringResource(R.string.xiangqi_tab_settings),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
        ),
    )

    CompositionLocalProvider(LocalXiangqiImmersiveModeState provides immersiveState) {
        BackHandler(enabled = true) {
            when {
                immersiveState.isImmersive -> immersiveState.exitImmersive()
                component.canPop(activeRoute) -> component.navigateBackFrom(activeRoute)
                activeTab != XiangqiRouterComponent.Tab.Play -> component.selectTab(XiangqiRouterComponent.Tab.Play)
                else -> component.onGoBack()
            }
        }
        BaseScreen(
            title = stringResource(R.string.xiangqi_library_title),
            onGoBack = component.onGoBack,
            isBackHandler = false,
            showNavigationBarsPadding = false,
            immersiveModeState = immersiveState,
            actions = {
                if (activeTab != XiangqiRouterComponent.Tab.Settings && !immersiveState.isImmersive) {
                    NewGameDropMenu(component = component.libraryComponent)
                }
            },
        ) {
        // 之前的"自动打开最近对局"逻辑会让系统返回键无法停在 EmptyQuickPanel,
        // 改为完全由用户主动选择历史 / FAB 进入对局。

        Column(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
                .then(
                    if (immersiveState.isImmersive) Modifier.statusBarsPadding() else Modifier
                )
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize(),
            ) { child ->
                val contentModifier = Modifier.fillMaxSize()
                when (val instance = child.instance) {
                        XiangqiRouterComponent.Child.PlayHome -> XiangqiEmptyQuickPanel(
                            component = component.libraryComponent,
                            headline = stringResource(R.string.xiangqi_empty_play_title),
                            subline = stringResource(R.string.xiangqi_empty_play_message),
                            modifier = contentModifier,
                        )

                        XiangqiRouterComponent.Child.AnalysisHome -> XiangqiEmptyQuickPanel(
                            component = component.libraryComponent,
                            headline = stringResource(R.string.xiangqi_empty_analyze_title),
                            subline = stringResource(R.string.xiangqi_empty_analyze_message),
                            modifier = contentModifier,
                        )

                        is XiangqiRouterComponent.Child.Library -> XiangqiLibraryScreen(
                            component = instance.component,
                            modifier = contentModifier,
                            showChrome = false,
                        )

                is XiangqiRouterComponent.Child.Game -> key(instance.component) {
                            XiangqiGameScreen(
                        component = instance.component,
                                modifier = contentModifier,
                                showChrome = false,
                            )
                        }

                is XiangqiRouterComponent.Child.Analysis -> key(instance.component) {
                            XiangqiAnalysisScreen(
                        component = instance.component,
                                modifier = contentModifier,
                                showChrome = false,
                            )
                        }

                        XiangqiRouterComponent.Child.Settings -> XiangqiSettingsScreen(
                            component = component,
                            modifier = contentModifier,
                        )
            }
                            }
        }

        ImmersiveBottomContent(visible = immersiveState.isUiVisible) {
            BottomNavigationBar(
                items = tabs,
                selectedItemId = activeTab.name,
                onItemClick = { item ->
                    XiangqiRouterComponent.Tab.entries
                        .firstOrNull { it.name == item.id }
                        ?.let(component::selectTab)
                },
            )
        }
        }

        val pendingJoinRoomId = component.pendingJoinRoomId
        if (pendingJoinRoomId.isNotBlank()) {
            val context = LocalContext.current
            val entryPoint = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    XiangqiOnlineEntryPoint::class.java,
                )
            }
            val onlinePlay = remember { entryPoint.onlinePlayUseCase() }

            OnlineMatchScreen(
                onlinePlay = onlinePlay,
                onDismiss = component::clearPendingJoinRoom,
                onMatchReady = { roomId, mySide, opponentName, opponentAvatarUrl, initialFen ->
                    component.clearPendingJoinRoom()
                    component.libraryComponent.createOnlineGame(roomId, mySide, opponentName, opponentAvatarUrl, initialFen)
                },
                initialRoomId = pendingJoinRoomId,
            )
        }
    }
}

@Composable
private fun XiangqiTabEmptyState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.Medium,
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GlassTonalButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}
