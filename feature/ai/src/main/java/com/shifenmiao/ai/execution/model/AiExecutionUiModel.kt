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

package com.shifenmiao.ai.execution.model

enum class AiExecutionPhase {
    HIDDEN,
    RUNNING,
    WAITING_USER_ACTION,
    WAITING_FINAL_RESPONSE,
    COMPLETED,
    FAILED,
    PAUSED
}

enum class ExecutionStepStatus {
    DONE,
    RUNNING,
    WAITING_USER,
    FAILED,
    PENDING
}

data class DeepLinkItemUiModel(
    val uri: String,
    val label: String,
    val guidance: String? = null,
    val primary: Boolean = false,
)

data class ExecutionStepUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val status: ExecutionStepStatus = ExecutionStepStatus.PENDING,
    val detail: String? = null,
    val debugInfo: String? = null,
    val isSystemStep: Boolean = false,
    val arguments: String? = null,
    val result: String? = null,
    val deepLinks: List<DeepLinkItemUiModel> = emptyList(),
)

data class AiExecutionUiModel(
    val phase: AiExecutionPhase = AiExecutionPhase.HIDDEN,
    val title: String = "",
    val summary: String? = null,
    val currentStepTitle: String? = null,
    val progressText: String? = null,
    val steps: List<ExecutionStepUiModel> = emptyList(),
    val primaryActionLabel: String? = null,
    val secondaryActionLabel: String? = null
) {
    val isVisible: Boolean
        get() = phase != AiExecutionPhase.HIDDEN && (title.isNotBlank() || steps.isNotEmpty())
}

