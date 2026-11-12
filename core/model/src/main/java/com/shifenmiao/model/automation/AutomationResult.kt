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

package com.shifenmiao.model.automation

/**
 * Result of executing an [AIAction].
 *
 * Promoted from feature/visual-automation/model/AutomationResult.kt to core/model
 * so it can be referenced from AgentTool results without a module-level dependency.
 */
sealed class AutomationResult {
    data class Success(val action: AIAction) : AutomationResult()
    data class Failure(val message: String) : AutomationResult()
}