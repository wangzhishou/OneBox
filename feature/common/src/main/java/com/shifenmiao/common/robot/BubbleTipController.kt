package com.shifenmiao.common.robot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shifenmiao.core.R
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.utils.appContext
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val COMPLETION_HINT_DURATION_MILLIS = 2400L

@Composable
fun rememberBubbleTipState(): BubbleTipState {
    val defaultMessage = remember {
        appContext.getString(R.string.robot_bubble_hint_chat)
    }
    val completedMessage = remember {
        appContext.getString(R.string.robot_bubble_completed)
    }

    return remember(defaultMessage, completedMessage) {
        BubbleTipState(
            defaultMessage = defaultMessage,
            completedMessage = completedMessage,
            hasUserClickedInitially = AppSharedStorage.loadHasRobotClicked(),
        )
    }
}

class BubbleTipState(
    private val defaultMessage: String,
    private val completedMessage: String,
    hasUserClickedInitially: Boolean,
) {
    private var hasUserClicked by mutableStateOf(hasUserClickedInitially)

    var isVisible by mutableStateOf(!hasUserClickedInitially)
        private set

    var currentMessage by mutableStateOf(defaultMessage)
        private set

    fun markUserClicked() {
        hasUserClicked = true
        hide()
    }

    suspend fun showCompletedHint() {
        currentMessage = completedMessage
        isVisible = true
        delay(COMPLETION_HINT_DURATION_MILLIS.milliseconds)
        hide()
    }

    fun hide() {
        isVisible = false
    }
}
