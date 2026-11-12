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

import com.shifenmiao.ai.agent.callback.ToolCallback
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.navigation.ScreenCallbackResult

data class ScreenNavigationExecution(
    val mode: String,
    val callbackResult: ScreenCallbackResult? = null
)

object ScreenNavigationToolSupport {

    suspend fun navigate(
        callback: ToolCallback,
        screen: Screen,
        awaitResult: Boolean
    ): ScreenNavigationExecution {
        val callbackCapableBuilder = buildCallbackCapableScreen(screen)
        if (awaitResult && callbackCapableBuilder != null) {
            val result = callback.navigateToScreen<ScreenCallbackResult> { onResult ->
                callbackCapableBuilder(onResult)
            }
            return ScreenNavigationExecution(
                mode = "await_result",
                callbackResult = result
            )
        }

        callback.openScreen(screen)
        return ScreenNavigationExecution(mode = "fire_and_forget")
    }

    private fun buildCallbackCapableScreen(
        screen: Screen
    ): (((ScreenCallbackResult) -> Unit) -> Screen)? {
        return when (screen) {
            is Screen.CreateHtml -> { onResult ->
                Screen.CreateHtml(
                    draftId = screen.draftId,
                    onResult = onResult
                )
            }
            is Screen.WebBrowser -> { _ ->
                Screen.WebBrowser(
                    url = screen.url,
                    bookmarkItemId = screen.bookmarkItemId,
                    localUri = screen.localUri,
                )
            }
            is Screen.EditPromptItem -> { onResult ->
                Screen.EditPromptItem(
                    draftId = screen.draftId,
                    onResult = onResult
                )
            }
            is Screen.NoteItem -> { onResult ->
                Screen.NoteItem(
                    itemEntityParams = screen.itemEntityParams,
                    onResult = onResult
                )
            }
            is Screen.CreateNote -> { onResult ->
                Screen.CreateNote(
                    draftId = screen.draftId,
                    onResult = onResult
                )
            }
            is Screen.OpenFilePicker -> { onResult ->
                Screen.OpenFilePicker(
                    mimeTypes = screen.mimeTypes,
                    allowMultiple = screen.allowMultiple,
                    onResult = onResult
                )
            }
            else -> null
        }
    }
}
