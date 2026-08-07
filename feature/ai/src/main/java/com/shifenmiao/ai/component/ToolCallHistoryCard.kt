package com.shifenmiao.ai.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.shifenmiao.ai.execution.component.AiExecutionTimelineCard
import com.shifenmiao.ai.execution.presenter.AiExecutionPresenter
import com.shifenmiao.storage.AppSharedStorage

@Composable
fun ToolCallHistoryCard(
    toolCallsJson: String? = null,
    agentToolCallStatus: AgentToolCallUIState? = null,
    onCancelTool: (() -> Unit)? = null,
    onContinueIteration: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isExpandedToolCall by AppSharedStorage.isExpandedToolCall.collectAsState()
    val uiModel = if (agentToolCallStatus != null) {
        AiExecutionPresenter.present(agentToolCallStatus, context)
    } else {
        AiExecutionPresenter.presentHistory(
            toolCallsJson = toolCallsJson.orEmpty(),
            context = context,
            gson = Gson()
        )
    }
    AiExecutionTimelineCard(
        uiModel = uiModel,
        onPrimaryAction = onContinueIteration,
        onSecondaryAction = onCancelTool,
        initiallyExpanded = isExpandedToolCall,
        forceExpanded = agentToolCallStatus != null,
        onExpandedChange = { AppSharedStorage.saveIsExpandedToolCall(it) },
        modifier = modifier
    )
}

