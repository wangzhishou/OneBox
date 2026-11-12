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

package com.shifenmiao.ai.agent.tool

import kotlinx.serialization.Serializable

@Serializable
data class AgentUserQuestionRequest(
    val toolCallId: String = "",
    val toolName: String = "",
    val presentation: AgentUserQuestionPresentation = AgentUserQuestionPresentation.dialog,
    val title: String = "",
    val message: String = "",
    val questions: List<AgentUserQuestionItem> = emptyList(),
    val interactionOwnerId: String? = null,
    // 按钮文案留空时由 UI 层回退到本地化默认文案（继续/取消），不要在这里写死中文
    val confirmText: String = "",
    val cancelText: String = ""
)
