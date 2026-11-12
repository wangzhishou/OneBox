/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.ai.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            context = context
        )
    }
    AiExecutionTimelineCard(
        uiModel = uiModel,
        onPrimaryAction = onContinueIteration,
        onSecondaryAction = onCancelTool,
        initiallyExpanded = isExpandedToolCall,
        onExpandedChange = { AppSharedStorage.saveIsExpandedToolCall(it) },
        modifier = modifier
    )
}

