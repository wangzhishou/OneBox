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

package com.wanbaohe.xiangqi.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights

@Composable
fun XiangqiOpponentPicker(
    config: GameAiPlayerConfig,
    workingEngine: AiEngine,
    allEngines: List<AiEngine>,
    modelsByProvider: Map<String, List<AiModel>>,
    localEnginePackaged: Boolean,
    localEngineState: XiangqiEngineWeights.InstallState,
    onSourceSelected: (XiangqiAiSource) -> Unit,
    onModelSelected: (AiEngine, AiModel) -> Unit,
    onDismiss: () -> Unit,
    onDownloadLocalEngine: () -> Unit,
    onCancelLocalEngineDownload: () -> Unit,
) {
    var pickingModel by remember { mutableStateOf(false) }
    if (pickingModel) {
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines.filter { it.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE },
            modelsByProvider = modelsByProvider,
            selectedEngineName = if (config.engineName.isNotBlank()) {
                AiEngine.buildIdentityKey(config.engineName, AiRequestProtocol.fromValue(config.engineProtocol))
            } else workingEngine.identityKey(),
            selectedModelName = config.model?.name ?: workingEngine.model.name,
            title = stringResource(R.string.xiangqi_settings_pick_ai_model),
            onSelected = { engine, model -> onModelSelected(engine, model); onDismiss() },
            onDismiss = { selected -> if (!selected) pickingModel = false },
        )
    } else {
        XiangqiAiPickerBottomSheet(
            visible = true,
            selected = config.source,
            workingModelTitle = (config.model ?: workingEngine.model).let { it.title.ifBlank { it.name } },
            title = stringResource(R.string.xiangqi_settings_ai_picker_title),
            localEnginePackaged = localEnginePackaged,
            localEngineState = localEngineState,
            onSelected = { onSourceSelected(it); onDismiss() },
            onDismiss = onDismiss,
            onDownloadLocalEngine = onDownloadLocalEngine,
            onCancelLocalEngineDownload = onCancelLocalEngineDownload,
            onPickWorkingModel = { pickingModel = true },
        )
    }
}
