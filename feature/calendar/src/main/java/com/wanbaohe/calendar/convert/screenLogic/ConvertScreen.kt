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

package com.wanbaohe.calendar.convert.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.utils.capturable.capturable
import com.wanbaohe.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.ConversionTab
import kotlinx.coroutines.launch

@Composable
fun ConvertScreen(component: ConvertComponent) {
    val state by component.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    val adaptedState = CalendarUiState(
        isConvertSolarToLunar = state.isSolarToLunar,
        convertYear = state.year,
        convertMonth = state.month,
        convertDay = state.day,
        convertHour = state.hour,
        isConvertLunarLeapMonth = state.isLunarLeapMonth,
        convertResult = state.convertResult,
        convertSolarResult = state.convertSolarResult,
        convertTimeSlot = state.timeSlot,
        convertFotoData = state.fotoData,
        convertTaoData = state.taoData,
    )

    ConversionTab(
        state = adaptedState,
        onUpdateDate = component::updateDate,
        onUpdateHour = component::updateHour,
        onConvert = component::performConversion,
        onToggleMode = component::toggleMode,
        onToggleLeapMonth = component::toggleLeapMonth,
        onShare = {
            scope.launch {
                component.shareBitmap(captureController.bitmap())
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .capturable(captureController)
    )
}