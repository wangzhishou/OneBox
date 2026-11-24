package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.database.ai.EditorHistoryResult
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton

@Composable
internal fun EditorAiActionBar(
    actionKey: Any,
    engine: AiEngine?,
    isGenerating: Boolean,
    canGenerate: Boolean,
    hasResult: Boolean,
    onSwitchEngine: () -> Unit,
    onGenerate: () -> Unit,
    onStop: () -> Unit,
    loadHistory: suspend () -> List<EditorHistoryResult>,
    onHistorySelected: (String) -> Unit,
    applyAction: @Composable () -> Unit = {},
) {
    val buttonHeight = 48.dp
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        applyAction()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditorHistoryPicker(
                actionKey = actionKey,
                enabled = !isGenerating,
                load = loadHistory,
                onSelect = onHistorySelected,
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                AiEditorEngineSwitcher(
                    engine = engine,
                    enabled = !isGenerating,
                    onClick = onSwitchEngine,
                    modifier = Modifier.height(buttonHeight),
                )
            }
            if (isGenerating) {
                OneBoxDangerButton(
                    text = stringResource(R.string.note_ai_stop),
                    leadingIcon = Icons.Outlined.LineStop,
                    onClick = onStop,
                    modifier = Modifier.height(buttonHeight),
                )
            } else {
                OnePrimaryButton(
                    text = stringResource(
                        if (hasResult) R.string.note_ai_regenerate else R.string.note_ai_panel_generate
                    ),
                    leadingIcon = Icons.Outlined.LineMagic,
                    onClick = onGenerate,
                    enabled = canGenerate && engine?.name?.isNotBlank() == true,
                    modifier = Modifier.height(buttonHeight),
                )
            }
        }
    }
}
