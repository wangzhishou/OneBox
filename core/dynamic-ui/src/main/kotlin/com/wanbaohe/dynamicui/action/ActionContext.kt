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

import android.content.Context
import com.wanbaohe.dynamicui.state.UiStateScope

/** Navigation callback – decoupled from any specific navigation library. */
typealias NavigationCallback = (screen: String, params: Map<String, String>) -> Unit

/**
 * Internal state keys reserved by the dynamic-ui runtime.
 *
 * Keep a compatibility layer for legacy `__*` keys so existing JSON configs
 * continue working while new integrations migrate to `_dyn.*`.
 */
object DynamicUiInternalState {
    const val PREFIX = "_dyn"

    const val ERROR = "$PREFIX.error"
    const val LOADING = "$PREFIX.loading"
    const val BACK = "$PREFIX.back"
    const val NAVIGATE = "$PREFIX.navigate"
    const val NAVIGATE_ARGS = "$PREFIX.navigateArgs"

    const val DIALOG_VISIBLE = "$PREFIX.dialog.visible"
    const val DIALOG_TITLE = "$PREFIX.dialog.title"
    const val DIALOG_MESSAGE = "$PREFIX.dialog.message"
    const val DIALOG_CONFIRM = "$PREFIX.dialog.confirm"
    const val DIALOG_DISMISS = "$PREFIX.dialog.dismiss"
    const val DIALOG_ON_CONFIRM_TYPE = "$PREFIX.dialog.onConfirmType"

    const val PICKER_VISIBLE = "$PREFIX.picker.visible"
    const val PICKER_TYPE = "$PREFIX.picker.type"
    const val PICKER_PARAMS = "$PREFIX.picker.params"

    const val VALIDATION_ERRORS = "$PREFIX.validationErrors"

    fun validationErrorKey(nodeId: String) = "$PREFIX.errors.$nodeId"

    // Legacy compatibility keys
    const val LEGACY_ERROR = "__error"
    const val LEGACY_LOADING = "__loading"
    const val LEGACY_BACK = "__back"
    const val LEGACY_NAVIGATE = "__navigate"
    const val LEGACY_NAVIGATE_ARGS = "__navigateArgs"

    const val LEGACY_DIALOG_VISIBLE = "__dialog.visible"
    const val LEGACY_DIALOG_TITLE = "__dialog.title"
    const val LEGACY_DIALOG_MESSAGE = "__dialog.message"
    const val LEGACY_DIALOG_CONFIRM = "__dialog.confirm"
    const val LEGACY_DIALOG_DISMISS = "__dialog.dismiss"
    const val LEGACY_DIALOG_ON_CONFIRM_TYPE = "__dialog.onConfirmType"
}

/**
 * ActionContext bundles all dependencies an [ActionHandler] might need at execution time.
 *
 * Passed from the host Composable into the [ActionEngine] on every event dispatch.
 */
data class ActionContext(
    /** Android application context (for Toast, clipboard, etc.) */
    val androidContext: Context,
    /** Optional navigator for screen transitions */
    val onNavigate: NavigationCallback? = null,
    /** Optional host action callback for app-level interactions */
    val onHostAction: ((actionName: String, params: Map<String, String>, stateScope: UiStateScope) -> Unit)? = null,
    /** Optional back navigation callback */
    val onBack: (() -> Unit)? = null,
    /** The active state scope (read + write) */
    val stateScope: UiStateScope,
    /** Optional per-item context for list renderers */
    val itemContext: Map<String, Any?> = emptyMap(),
    /** Extra host-provided metadata (user session, feature flags…) */
    val extras: Map<String, Any?> = emptyMap(),
)
