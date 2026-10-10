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
