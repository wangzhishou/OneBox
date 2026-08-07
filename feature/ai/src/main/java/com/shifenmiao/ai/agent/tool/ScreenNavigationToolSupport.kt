package com.shifenmiao.ai.agent.tool

import com.shifenmiao.ai.agent.callback.ToolCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult

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
            is Screen.PreviewHtml -> { onResult ->
                Screen.PreviewHtml(
                    itemEntityParams = screen.itemEntityParams,
                    localUri = screen.localUri,
                    onResult = onResult
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
