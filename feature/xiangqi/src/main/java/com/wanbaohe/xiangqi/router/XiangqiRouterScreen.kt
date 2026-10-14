package com.wanbaohe.xiangqi.router

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.boardgame.model.BoardGameTab
import com.wanbaohe.boardgame.ui.BoardGameTabScaffold
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.xiangqi.screen.NewGameDropMenu
import com.wanbaohe.xiangqi.screen.XiangqiAnalysisScreen
import com.wanbaohe.xiangqi.screen.XiangqiEmptyQuickPanel
import com.wanbaohe.xiangqi.screen.XiangqiGameScreen
import com.wanbaohe.xiangqi.screen.XiangqiPlayHomeScreen
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
    val soundOn = component.xiangqiSettings.collectAsState().value.soundEnabled
    val activeGame = (childStack.active.instance as? XiangqiRouterComponent.Child.Game)?.component
    val preparingSetup = childStack.active.instance == XiangqiRouterComponent.Child.PlayHome &&
        component.preparationSetupDraft != null
    val editingGame = activeGame?.setupDraft != null
    val isSettingUp = preparingSetup || editingGame
    val cancelSetup: () -> Unit = {
        if (preparingSetup) component.cancelPreparationSetup() else activeGame?.cancelSetup()
    }
    val tabs = listOf(
        BoardGameTab(
            id = XiangqiRouterComponent.Tab.Play.name,
            label = stringResource(R.string.xiangqi_tab_play),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSportsEsports,
        ),
        BoardGameTab(
            id = XiangqiRouterComponent.Tab.Analyze.name,
            label = stringResource(R.string.xiangqi_tab_analyze),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics,
        ),
        BoardGameTab(
            id = XiangqiRouterComponent.Tab.Library.name,
            label = stringResource(R.string.xiangqi_tab_library),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
        ),
        BoardGameTab(
            id = XiangqiRouterComponent.Tab.Settings.name,
            label = stringResource(R.string.xiangqi_tab_settings),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
        ),
    )

    CompositionLocalProvider(LocalXiangqiImmersiveModeState provides immersiveState) {
        BoardGameTabScaffold(
            title = stringResource(R.string.xiangqi_library_title),
            tabs = tabs,
            selectedTabId = activeTab.name,
            onTabSelected = { tab ->
                XiangqiRouterComponent.Tab.entries.firstOrNull { it.name == tab.id }?.let(component::selectTab)
            },
            onGoBack = {
                if (isSettingUp) cancelSetup() else component.navigateBack()
            },
            onBack = {
                when {
                    isSettingUp -> cancelSetup()
                    component.canPop(activeRoute) -> component.navigateBack()
                    activeTab != XiangqiRouterComponent.Tab.Play -> component.selectTab(XiangqiRouterComponent.Tab.Play)
                    else -> component.navigateBack()
                }
            },
            immersiveState = immersiveState,
            showTabs = !isSettingUp,
            headerActions = if (
                activeRoute is XiangqiRouterComponent.Route.Game ||
                activeTab != XiangqiRouterComponent.Tab.Settings
            ) {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (activeRoute is XiangqiRouterComponent.Route.Game) {
                            IconButton(onClick = { component.updateSoundEnabled(!soundOn) }) {
                                Icon(
                                    imageVector = if (soundOn) {
                                        Icons.AutoMirrored.Outlined.VolumeUp
                                    } else {
                                        Icons.AutoMirrored.Outlined.VolumeOff
                                    },
                                    contentDescription = stringResource(
                                        if (soundOn) R.string.xiangqi_analysis_sound_on
                                        else R.string.xiangqi_analysis_sound_off
                                    ),
                                )
                            }
                        }
                        if (activeTab != XiangqiRouterComponent.Tab.Settings) {
                            NewGameDropMenu(component = component.libraryComponent)
                        }
                    }
                }
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
                    XiangqiRouterComponent.Child.PlayHome -> XiangqiPlayHomeScreen(
                        component = component,
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
