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

package com.wanbaohe.calendar.bazi.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shifenmiao.base.utils.ActionUtils
import com.wanbaohe.core.ui.utils.capturable.capturable
import com.wanbaohe.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.BaZiTab
import com.wanbaohe.calendar.ui.BaZiTabSkeleton
import kotlinx.coroutines.launch

@Composable
fun BaZiScreen(component: BaZiComponent) {
    val state by component.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    val adaptedState = CalendarUiState(
        baZiYear = state.year,
        baZiMonth = state.month,
        baZiDay = state.day,
        baZiHour = state.hour,
        baZiData = state.baZiData,
        daYunList = state.daYunList,
        fortuneData = state.fortuneData,
        isDataReady = state.isDataReady,
    )

    if (!state.isDataReady) {
        BaZiTabSkeleton(modifier = Modifier.fillMaxSize())
    } else {
        BaZiTab(
            state = adaptedState,
            onSelectDate = component::updateDate,
            onSelectHour = component::updateHour,
            onShare = {
                scope.launch {
                    component.shareBitmap(captureController.bitmap())
                }
            },
            onAiBaZiClick = {
                ActionUtils.showLogin {
                    component.openBaZiAI()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .capturable(captureController)
        )
    }
}