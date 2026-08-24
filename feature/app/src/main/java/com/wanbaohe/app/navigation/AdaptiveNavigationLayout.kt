package com.wanbaohe.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.screen.AIChatBody
import com.shifenmiao.base.utils.Navigation
import com.shifenmiao.common.components.ImportProgressDialog
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.utils.animation.toolboxPredictiveBackAnimation
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue
import com.t8rin.imagetoolbox.core.ui.utils.helper.isShellPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.feature.root.presentation.components.navigation.NavigationChild
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.wanbaohe.app.components.AddMenuFloatingPanel
import com.wanbaohe.app.components.AppBottomNavigationBar
import com.wanbaohe.app.components.NavigationRailBar
import com.wanbaohe.app.ui.MainBackHandler
import kotlinx.coroutines.launch
import java.util.Date

// 导航状态数据类
@Immutable
private data class NavigationState(
    val currentScreen: Screen?,
    val isShowBottomBar: Boolean,
    val currentTabIndex: Int,
    val bottomBarHeight: androidx.compose.ui.unit.Dp
)

@Composable
fun AdaptiveNavigationLayout(
    rootComponent: RootComponent,
    appComponent: AppComponent,
) {
    val childStack by rootComponent.childStack.subscribeAsState()
    val navigationState = rememberNavigationState(childStack)
    val isPortrait by isShellPortraitOrientationAsState()
    val tabEntries = Navigation.rememberTabEntries()
    val currentScreen = childStack.active.configuration

    DrawerNavigationContainer(
        appComponent = appComponent,
        rootComponent = rootComponent,
        currentScreen = currentScreen,
    ) {
        if (isPortrait) {
            NavigationBottomLayout(
                childStack = childStack,
                rootComponent = rootComponent,
                appComponent = appComponent,
                navigationState = navigationState,
                tabEntries = tabEntries
            )
        } else {
            NavigationRailLayout(
                appComponent = appComponent,
                childStack = childStack,
                rootComponent = rootComponent,
                currentScreen = currentScreen,
                navigationState = navigationState,
                tabEntries = tabEntries
            )
        }
    }

    ImportProgressDialog(appComponent)
}

@Composable
private fun rememberNavigationState(
    childStack: ChildStack<Screen, NavigationChild>
): NavigationState {
    val currentScreen = remember(childStack) { childStack.items.lastOrNull()?.configuration }
    val tabEntries = Navigation.rememberTabEntries()
    val isShowBottomBar by remember(tabEntries, currentScreen) {
        derivedStateOf { Navigation.isShowBottomBar(currentScreen, tabEntries) }
    }
    val currentTabIndex by remember(tabEntries, currentScreen) {
        derivedStateOf { Navigation.getTopLevelDestinationIndex(currentScreen, tabEntries) }
    }

    // 将 @Composable 调用移到 remember 外部
    val navigationHeight = AppTheme.dimens.navigationHeight
    return remember(currentScreen, isShowBottomBar, currentTabIndex, navigationHeight) {
        NavigationState(currentScreen, isShowBottomBar, currentTabIndex, navigationHeight)
    }
}


@Composable
private fun DrawerNavigationContainer(
    appComponent: AppComponent,
    rootComponent: RootComponent,
    currentScreen: Screen,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val uiState by appComponent.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val onCloseDrawer = remember(scope, appComponent, drawerState) {
        {
            scope.launch {
                appComponent.hideDrawer()
                drawerState.close()
            }
        }
    }

    DrawerStateEffects(
        drawerState = drawerState,
        appComponent = appComponent,
        showDrawer = uiState.showDrawer
    )

    LocalLayoutDirection.ProvidesValue(LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContentWrapper(
                    appComponent = appComponent,
                    loginComponent = rootComponent.loginComponent,
                    onCloseDrawer = onCloseDrawer
                )
            },
            gesturesEnabled = uiState.showDrawer
        ) {
            LocalLayoutDirection.ProvidesValue(LayoutDirection.Ltr) {
                content()
                MainBackHandler(
                    drawerState = drawerState,
                    rootComponent = rootComponent,
                    currentScreen = currentScreen
                )
            }
        }
    }
}

@Composable
private fun DrawerStateEffects(
    drawerState: DrawerState,
    appComponent: AppComponent,
    showDrawer: Boolean
) {
    LaunchedEffect(showDrawer) {
        if (showDrawer) {
            appComponent.toggleRobotVisibility(false)
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isClosed && showDrawer) {
            appComponent.hideDrawer()
        }
    }
}

@Composable
private fun NavigationBottomLayout(
    childStack: ChildStack<Screen, NavigationChild>,
    rootComponent: RootComponent,
    appComponent: AppComponent,
    navigationState: NavigationState,
    tabEntries: List<Screen>,
) {
    val isKeyboardOpen = rememberIsKeyboardOpen()
    LocalLayoutDirection.ProvidesValue(LayoutDirection.Ltr) {
        BottomNavigationLayout(
            childStack = childStack,
            rootComponent = rootComponent,
            navigationState = navigationState,
            tabEntries = tabEntries,
            isKeyboardOpen = isKeyboardOpen,
            appComponent = appComponent,
        )
    }
}

@Composable
private fun rememberIsKeyboardOpen(): Boolean {
    val density = LocalDensity.current
    val windowIme = WindowInsets.ime

    return remember {
        derivedStateOf { windowIme.getBottom(density) > 0 }
    }.value
}

@Composable
private fun NavigationRailLayout(
    childStack: ChildStack<Screen, NavigationChild>,
    rootComponent: RootComponent,
    currentScreen: Screen,
    navigationState: NavigationState,
    tabEntries: List<Screen>,
    appComponent: AppComponent,
) {
    var addMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val overlayBottomPadding =
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 24.dp

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigationRailBar(
            rootComponent = rootComponent,
            screenList = tabEntries,
            currentTabPageIndex = navigationState.currentTabIndex,
            modifier = Modifier,
            footer = {
                LandscapeRailUtilityActions(
                    onOpenCreateMenu = {
                        addMenuExpanded = !addMenuExpanded
                    },
                    onOpenDrawer = {
                        appComponent.showDrawer()
                    }
                )
            }
        )
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            NavigationContent(
                childStack = childStack,
                currentScreen = currentScreen,
                rootComponent = rootComponent,
                modifier = Modifier.fillMaxSize(),
                appComponent = appComponent,
            )
        }
        WideScreenAssistantPane(
            rootComponent = rootComponent,
            appComponent = appComponent,
            modifier = Modifier.navigationBarsPadding().statusBarsPadding()
                .fillMaxHeight()
                .width(420.dp)
                .padding(
                    end = OneBoxDesignSystem.screenPadding,
                )
        )
    }

    QuickCreateFloatingPanel(
        expanded = addMenuExpanded,
        onDismiss = { addMenuExpanded = false },
        bottomBarHeight = overlayBottomPadding,
        rootComponent = rootComponent,
        onNavigateToAiApp = {
            rootComponent.globalAIChatComponent.changeConversationId(Date().time.toString())
        },
    )
}

@Composable
private fun BottomNavigationLayout(
    childStack: ChildStack<Screen, NavigationChild>,
    rootComponent: RootComponent,
    appComponent: AppComponent,
    navigationState: NavigationState,
    tabEntries: List<Screen>,
    isKeyboardOpen: Boolean,
) {
    val showBottomBar = navigationState.isShowBottomBar && !isKeyboardOpen
    val systemBarsBottom = if (showBottomBar) {
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }
    val contentBottomPadding =
        if (showBottomBar) navigationState.bottomBarHeight + systemBarsBottom else 0.dp

    // ADD 菜单展开状态
    var addMenuExpanded by rememberSaveable { mutableStateOf(false) }

    NavigationContent(
        appComponent = appComponent,
        childStack = childStack,
        currentScreen = childStack.active.configuration,
        rootComponent = rootComponent,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentBottomPadding)
    )
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        QuickCreateFloatingPanel(
            expanded = addMenuExpanded,
            onDismiss = { addMenuExpanded = false },
            bottomBarHeight = contentBottomPadding,
            rootComponent = rootComponent,
            onNavigateToAiApp = appComponent::showAIChat,
        )

        AppBottomNavigationBar(
            height = navigationState.bottomBarHeight,
            screenList = tabEntries,
            showBottomBar = showBottomBar,
            currentTabPageIndex = navigationState.currentTabIndex,
            addMenuExpanded = addMenuExpanded,
            onToggleAddMenu = { addMenuExpanded = !addMenuExpanded },
            onTabClick = rootComponent::navigateTo
        )
    }
}

@Composable
private fun NavigationContent(
    childStack: ChildStack<Screen, NavigationChild>,
    currentScreen: Screen,
    rootComponent: RootComponent,
    appComponent: AppComponent,
    modifier: Modifier = Modifier
) {

    // Hide robot when navigating away from NewApp screen.
    // Must be in LaunchedEffect — side effects are forbidden during Composition.

    LaunchedEffect(currentScreen.simpleName) {
        if (currentScreen.simpleName != Screen.NewApp().simpleName) {
            appComponent.toggleRobotVisibility(false)
        }
    }

    Children(
        stack = childStack,
        modifier = modifier,
        animation = toolboxPredictiveBackAnimation(
            backHandler = rootComponent.backHandler,
            onBack = rootComponent::navigateBack
        ),
        content = { child ->
            child.instance.Content()
        }
    )
}

@Composable
private fun QuickCreateFloatingPanel(
    expanded: Boolean,
    onDismiss: () -> Unit,
    bottomBarHeight: androidx.compose.ui.unit.Dp,
    rootComponent: RootComponent,
    onNavigateToAiApp: () -> Unit,
) {
    AddMenuFloatingPanel(
        expanded = expanded,
        onDismiss = onDismiss,
        bottomBarHeight = bottomBarHeight,
        onNavigateToQrCode = {
            onDismiss()
            rootComponent.navigateToNew(Screen.ScanCode)
        },
        onNavigateToNotebook = {
            onDismiss()
            rootComponent.navigateToNew(Screen.CreateNote())
        },
        onNavigateToMarkdown = {
            onDismiss()
            rootComponent.navigateToNew(Screen.MarkdownEditor())
        },
        onNavigateToHtml = {
            onDismiss()
            rootComponent.navigateToNew(Screen.CreateHtml())
        },
        onNavigateToPrompt = {
            onDismiss()
            rootComponent.navigateToNew(Screen.EditPromptItem())
        },
        onNavigateToTodoList = {
            onDismiss()
            rootComponent.navigateToNew(Screen.MarkTodoRouter())
        },
        onNavigateToAiApp = {
            onDismiss()
            onNavigateToAiApp()
        },
        onNavigateToCreateAIAgent = {
            onDismiss()
            rootComponent.navigateToNew(Screen.CreateAIAgent())
        },
        onNavigateToCreateAIPrompt = {
            onDismiss()
            rootComponent.navigateToNew(Screen.CreateAIChatPrompt())
        },
        onNavigateToHabitTracker = {
            onDismiss()
            rootComponent.navigateToNew(Screen.HabitTracker())
        },
        onNavigateToBlessingWall = {
            onDismiss()
            rootComponent.navigateToNew(Screen.BlessingWall())
        },
        onNavigateToImageCreation = {
            onDismiss()
            rootComponent.navigateToNew(Screen.MarkupLayers())
        },
        onNavigateToTextCard = {
            onDismiss()
            rootComponent.navigateToNew(Screen.TextCard)
        },
    )
}

@Composable
private fun LandscapeRailUtilityActions(
    onOpenCreateMenu: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    NavigationRailItem(
        selected = false,
        onClick = onOpenCreateMenu,
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(id = com.shifenmiao.core.R.string.nav_add),
            )
        },
        label = {
            Text(text = stringResource(id = com.shifenmiao.core.R.string.nav_add))
        }
    )
    NavigationRailItem(
        selected = false,
        onClick = onOpenDrawer,
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawer,
                contentDescription = stringResource(id = com.shifenmiao.core.R.string.quick_setting),
            )
        },
        label = {
            Text(text = stringResource(id = com.shifenmiao.core.R.string.quick_setting))
        }
    )
}

@Composable
private fun WideScreenAssistantPane(
    rootComponent: RootComponent,
    appComponent: AppComponent,
    modifier: Modifier = Modifier,
) {
    var aiChatComponent by remember { mutableStateOf<AIChatComponent?>(null) }
    LaunchedEffect(Unit) {
        StartupTrace.mark("WideScreenAssistantPane.ai_lazy.start")
        aiChatComponent = rootComponent.globalAIChatComponent
        StartupTrace.mark("WideScreenAssistantPane.ai_lazy.end")
    }
    val loaded = aiChatComponent

    GlassSurface(
        modifier = modifier,
        style = OneBoxDesignSystem.drawerGlassStyle,
        shape = OneBoxDesignSystem.sectionCardShape,
        color = MaterialTheme.colorScheme.surface,
        borderWidth = 0.dp,
    ) {
        if (loaded == null) {
            AIAssistantPanePlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            AIAssistantPaneContent(
                appComponent = appComponent,
                aiChatComponent = loaded,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AIAssistantPanePlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = com.shifenmiao.core.R.string.ai_tab_chat_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AIAssistantPaneContent(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    modifier: Modifier = Modifier,
) {
    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val conversation by aiChatComponent.conversation.collectAsState()

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    if (chatUIState.showHistory) {
                        aiChatComponent.hideHistory()
                    } else {
                        aiChatComponent.showHistory()
                    }
                }
            ) {
                Icon(
                    imageVector = if (chatUIState.showHistory) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = com.shifenmiao.core.R.string.ai_tab_chat_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when (conversation.entryType) {
                        AIConversationEntryType.ASSISTANT -> stringResource(id = com.shifenmiao.core.R.string.ai_chat_description)
                        AIConversationEntryType.CHAT -> stringResource(id = com.shifenmiao.core.R.string.ai_chat_title)
                        else -> conversation.title.ifBlank {
                            stringResource(id = com.shifenmiao.core.R.string.ai_stream_answer_title)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = {
                    aiChatComponent.changeConversationId(Date().time.toString())
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                    contentDescription = stringResource(id = com.shifenmiao.core.R.string.nav_add),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)

        AIChatBody(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 4.dp),
        )
    }
}

