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
