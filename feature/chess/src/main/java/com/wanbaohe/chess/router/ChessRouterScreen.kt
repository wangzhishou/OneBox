package com.wanbaohe.chess.router

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
import com.wanbaohe.chess.R
import com.wanbaohe.chess.di.ChessOnlineEntryPoint
import com.wanbaohe.chess.router.screenLogic.ChessRouterComponent
import com.wanbaohe.chess.screen.ChessAnalysisScreen
import com.wanbaohe.chess.screen.ChessEmptyQuickPanel
import com.wanbaohe.chess.screen.ChessGameScreen
import com.wanbaohe.chess.screen.ChessLibraryScreen
import com.wanbaohe.chess.screen.ChessSettingsScreen
import com.wanbaohe.chess.screen.NewGameDropMenu
import com.wanbaohe.chess.screen.OnlineMatchScreen
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSportsEsports

@Composable
fun ChessRouterScreen(component: ChessRouterComponent) {
    val childStack by component.childStack.subscribeAsState()
    val activeRoute = childStack.active.configuration
    val activeTab = component.tabOf(activeRoute)
    val tabs = listOf(
        BoardGameTab(
            id = ChessRouterComponent.Tab.Play.name,
            label = stringResource(R.string.chess_tab_play),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSportsEsports,
        ),
        BoardGameTab(
            id = ChessRouterComponent.Tab.Analyze.name,
            label = stringResource(R.string.chess_tab_analyze),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics,
        ),
        BoardGameTab(
            id = ChessRouterComponent.Tab.Library.name,
            label = stringResource(R.string.chess_tab_library),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
        ),
        BoardGameTab(
            id = ChessRouterComponent.Tab.Settings.name,
            label = stringResource(R.string.chess_tab_settings),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
        ),
    )

    BoardGameTabScaffold(
        title = stringResource(R.string.chess_library_title),
        tabs = tabs,
        selectedTabId = activeTab.name,
        onTabSelected = { tab ->
            ChessRouterComponent.Tab.entries
                .firstOrNull { it.name == tab.id }
                ?.let(component::selectTab)
        },
        onGoBack = component.onGoBack,
        onBack = {
            when {
                component.canPop(activeRoute) -> component.navigateBackFrom(activeRoute)
                activeTab != ChessRouterComponent.Tab.Play -> component.selectTab(ChessRouterComponent.Tab.Play)
                else -> component.onGoBack()
            }
        },
        headerActions = if (activeTab != ChessRouterComponent.Tab.Settings) {
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
                ChessRouterComponent.Child.PlayHome -> ChessEmptyQuickPanel(
                    component = component.libraryComponent,
                    headline = stringResource(R.string.chess_empty_play_title),
                    subline = stringResource(R.string.chess_empty_play_message),
                    modifier = contentModifier,
                )

                ChessRouterComponent.Child.AnalysisHome -> ChessEmptyQuickPanel(
                    component = component.libraryComponent,
                    headline = stringResource(R.string.chess_empty_analyze_title),
                    subline = stringResource(R.string.chess_empty_analyze_message),
                    modifier = contentModifier,
                )

                is ChessRouterComponent.Child.Library -> ChessLibraryScreen(
                    component = instance.component,
                    modifier = contentModifier,
                    showChrome = false,
                )

                is ChessRouterComponent.Child.Game -> key(instance.component) {
                    ChessGameScreen(
                        component = instance.component,
                        modifier = contentModifier,
                        showChrome = false,
                    )
                }

                is ChessRouterComponent.Child.Analysis -> key(instance.component) {
                    ChessAnalysisScreen(
                        component = instance.component,
                        modifier = contentModifier,
                        showChrome = false,
                    )
                }

                ChessRouterComponent.Child.Settings -> ChessSettingsScreen(
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
                ChessOnlineEntryPoint::class.java,
            )
        }
        val onlinePlay = remember { entryPoint.chessOnlinePlayUseCase() }

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
