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

package com.wanbaohe.dynamicui.action

import com.wanbaohe.dynamicui.ir.ActionSpec

/**
 * ActionHandler – the extension point for every type of side-effect.
 *
 * Implement this interface to add new action types. Register via [ActionRegistry]
 * (or Hilt @IntoMap for automatic discovery).
 *
 * [handle] returns [ActionResult]:
 * - [ActionResult.Success]: consumed and succeeded
 * - [ActionResult.Error]: consumed but failed (triggers onError chain)
 * - [ActionResult.Unhandled]: let next handler try
 */
interface ActionHandler {
    /** The action type strings this handler can process (e.g. ["navigate", "push"]). */
    val supportedTypes: Set<String>

    /** Execute the action and return an explicit dispatch result. */
    suspend fun handle(action: ActionSpec, context: ActionContext): ActionResult
}

sealed interface ActionResult {
    data object Success : ActionResult
    data class Error(val message: String? = null) : ActionResult
    data object Unhandled : ActionResult
}
