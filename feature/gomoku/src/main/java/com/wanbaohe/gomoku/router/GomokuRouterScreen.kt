package com.wanbaohe.gomoku.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.wanbaohe.boardgame.model.BoardGameTab
import com.wanbaohe.boardgame.ui.BoardGameTabScaffold
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.di.GomokuOnlineEntryPoint
import com.wanbaohe.gomoku.router.screenLogic.GomokuRouterComponent
import com.wanbaohe.gomoku.screen.GomokuAnalysisScreen
import com.wanbaohe.gomoku.screen.GomokuEmptyQuickPanel
import com.wanbaohe.gomoku.screen.GomokuGameScreen
import com.wanbaohe.gomoku.screen.GomokuLibraryScreen
import com.wanbaohe.gomoku.screen.GomokuSettingsScreen
import com.wanbaohe.gomoku.screen.NewGameDropMenu
import com.wanbaohe.gomoku.screen.OnlineMatchScreen
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSportsEsports

@Composable
fun GomokuRouterScreen(component: GomokuRouterComponent) {
    val childStack by component.childStack.subscribeAsState()
    val activeRoute = childStack.active.configuration
    val activeTab = component.tabOf(activeRoute)
    val tabs = listOf(
        BoardGameTab(
            id = GomokuRouterComponent.Tab.Play.name,
            label = stringResource(R.string.gomoku_tab_play),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSportsEsports,
        ),
        BoardGameTab(
            id = GomokuRouterComponent.Tab.Analyze.name,
            label = stringResource(R.string.gomoku_tab_analyze),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics,
        ),
        BoardGameTab(
            id = GomokuRouterComponent.Tab.Library.name,
            label = stringResource(R.string.gomoku_tab_library),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
        ),
        BoardGameTab(
            id = GomokuRouterComponent.Tab.Settings.name,
            label = stringResource(R.string.gomoku_tab_settings),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
        ),
    )

    BoardGameTabScaffold(
        title = stringResource(R.string.gomoku_library_title),
        tabs = tabs,
        selectedTabId = activeTab.name,
        onTabSelected = { tab ->
            GomokuRouterComponent.Tab.entries
                .firstOrNull { it.name == tab.id }
                ?.let(component::selectTab)
        },
        onGoBack = component.onGoBack,
        onBack = {
            when {
                component.canPop(activeRoute) -> component.navigateBackFrom(activeRoute)
                activeTab != GomokuRouterComponent.Tab.Play -> component.selectTab(GomokuRouterComponent.Tab.Play)
                else -> component.onGoBack()
            }
        },
        headerActions = if (activeTab != GomokuRouterComponent.Tab.Settings) {
            { NewGameDropMenu(component = component.libraryComponent) }
        } else {
            null
        },
    ) {
        Children(
            stack = childStack,
            modifier = Modifier.fillMaxSize(),
        ) { child ->
            val contentModifier = Modifier.fillMaxSize()
            when (val instance = child.instance) {
                GomokuRouterComponent.Child.PlayHome -> GomokuEmptyQuickPanel(
                    component = component.libraryComponent,
                    headline = stringResource(R.string.gomoku_empty_play_title),
                    subline = stringResource(R.string.gomoku_empty_play_message),
                    modifier = contentModifier,
                )

                GomokuRouterComponent.Child.AnalysisHome -> GomokuEmptyQuickPanel(
                    component = component.libraryComponent,
                    headline = stringResource(R.string.gomoku_empty_analyze_title),
                    subline = stringResource(R.string.gomoku_empty_analyze_message),
                    modifier = contentModifier,
                )

                is GomokuRouterComponent.Child.Library -> GomokuLibraryScreen(
                    component = instance.component,
                    modifier = contentModifier,
                    showChrome = false,
                )

                is GomokuRouterComponent.Child.Game -> key(instance.component) {
                    GomokuGameScreen(
                        component = instance.component,
                        modifier = contentModifier,
                        showChrome = false,
                    )
                }

                is GomokuRouterComponent.Child.Analysis -> key(instance.component) {
                    GomokuAnalysisScreen(
                        component = instance.component,
                        modifier = contentModifier,
                        showChrome = false,
                    )
                }

                GomokuRouterComponent.Child.Settings -> GomokuSettingsScreen(
                    component = component,
                    modifier = contentModifier,
                )
            }
        }
    }

    val pendingJoinRoomId = component.pendingJoinRoomId
    if (pendingJoinRoomId.isNotBlank()) {
        val context = LocalContext.current
        val entryPoint = remember {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                GomokuOnlineEntryPoint::class.java,
            )
        }
        val onlinePlay = remember { entryPoint.gomokuOnlinePlayUseCase() }

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
