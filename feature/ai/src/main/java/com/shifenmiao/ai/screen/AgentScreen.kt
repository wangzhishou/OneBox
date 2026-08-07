package com.shifenmiao.ai.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AgentComponent
import com.shifenmiao.base.components.CenterErrorBox
import com.shifenmiao.base.ui.CenterLoadingBox
import com.shifenmiao.base.ui.DisableContainer
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.state.ChatUIState
import com.shifenmiao.model.state.PageState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.a2ui.ui.A2uiDynamicContent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload

@Composable
fun AgentScreen(
    agentComponent: AgentComponent,
    appComponent: AppComponent,
    isPreview: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val agentState = agentComponent.agentState.collectAsState()
    val chatUIState by agentComponent.chatUIState.collectAsState()
    val localUrlNavigator = LocalUrlNavigator.current
    val isAdmin = LoginUtils.isAdmin()
    BaseScreen(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            focusManager.clearFocus()
        },
        title = agentState.value.title.orEmpty(),
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (chatUIState.showHistory) {
                            agentComponent.hideHistory()
                        } else {
                            appComponent.onGoBack()
                        }
                    }
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                if (isAdmin && !chatUIState.showHistory && !isPreview) {
                    IconButton(
                        onClick = {
                            agentComponent.pushAgentToRemote(
                                onSuccess = {
                                    AppToastHost.showToast(getString(R.string.agent_push_success))
                                },
                                onFailure = { message ->
                                    AppToastHost.showToast(message)
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
                            contentDescription = getString(R.string.agent_push_to_remote),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        actions = {
            DisableContainer(
                enabled = chatUIState.pageState != PageState.INITIALIZING
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingTooSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!chatUIState.showHistory && !isPreview) {
                        val canEditAgent = agentState.value.source != Source.REMOTE || isAdmin
                        if (canEditAgent) {
                            IconButton(
                                onClick = {
                                    agentComponent.prepareEditDraft(onReady = { draftId ->
                                        localUrlNavigator.navigate(
                                            Screen.CreateAIAgent(editDraftId = draftId)
                                        )
                                    }, onFailure = { message ->
                                        AppToastHost.showToast(message)
                                    })
                                }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                    contentDescription = getString(R.string.create_ai_agent_edit),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                localUrlNavigator.navigate(
                                    Screen.AIHistoryCenter(
                                        initialFilter = AIConversationEntryType.AGENT
                                    )
                                )
                            }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

        },
        onGoBack = {
            if (chatUIState.showHistory) {
                agentComponent.hideHistory()
            } else {
                appComponent.onGoBack()
            }
        },
        background = {
            AgentContent(
                agentComponent = agentComponent,
                chatUIState = chatUIState,
                oneBoxState = agentState.value,
                appComponent = appComponent
            )
        }
    )
}

@Composable
fun AgentContent(
    agentComponent: AgentComponent,
    chatUIState: ChatUIState,
    oneBoxState: Agent,
    appComponent: AppComponent,
) {
    val onNavigate = LocalOnNavigate.current
    when (chatUIState.pageState) {
        PageState.INITIALIZING, PageState.IDLE -> {
            if (!oneBoxState.dynamicBody.isNullOrBlank()) {
                val bodyJson = oneBoxState.dynamicBody!!
                val dynamicUiContent: @Composable () -> Unit = {
                    A2uiDynamicContent(
                        json = bodyJson,
                        onSubmit = { promptText ->
                            agentComponent.submitToAIChat(
                                onNavigate = onNavigate, promptText = promptText
                            )
                        },
                        renderProvider = agentComponent.a2uiRenderProvider,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Box(
                    modifier = Modifier.imePadding().navigationBarsPadding().statusBarsPadding().padding(
                        top = TopAppBarDefaults.MediumAppBarCollapsedHeight
                    ).fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    dynamicUiContent()
                }
            } else if (chatUIState.chatActive) {
                CenterLoadingBox()
            }
        }

        PageState.ERROR -> {
            CenterErrorBox(
                onRetry = {
                    agentComponent.initData()
                }, errorMessage = chatUIState.errorMessage, onGoBack = appComponent.onGoBack
            )
        }
    }

}


@Composable
fun InputWithButton(
    onButtonClick: (String) -> Unit
) {
    val inputText = rememberSaveable { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = inputText.value,
            onValueChange = { inputText.value = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onButtonClick(inputText.value) }, modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}
