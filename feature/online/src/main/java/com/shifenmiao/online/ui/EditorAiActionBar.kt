package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.database.ai.EditorHistoryResult
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.online.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
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
    applyAction: @Composable RowScope.() -> Unit = {},
) {
    val buttonHeight = 48.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorHistoryPicker(
            actionKey = actionKey,
            enabled = !isGenerating,
            load = loadHistory,
            onSelect = onHistorySelected,
        )
        val switchEngine = stringResource(R.string.editor_ai_switch_engine)
        val engineName = engine?.model?.name?.takeIf { it.isNotBlank() }
            ?: engine?.title?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.note_ai_no_engine)
        IconButton(
            enabled = !isGenerating,
            onClick = onSwitchEngine,
        ) {
            Icon(
                imageVector = Icons.Outlined.LineRobot,
                contentDescription = "$switchEngine: $engineName",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.weight(1f))
        applyAction()
        if (isGenerating) {
            OneBoxDangerButton(
                text = stringResource(R.string.note_ai_stop),
                leadingIcon = Icons.Outlined.LineStop,
                onClick = onStop,
                modifier = Modifier.height(buttonHeight),
            )
        } else if (hasResult) {
            IconButton(
                onClick = onGenerate,
                enabled = canGenerate && engine?.name?.isNotBlank() == true,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.note_ai_regenerate),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        } else {
            OnePrimaryButton(
                text = stringResource(R.string.note_ai_panel_generate),
                leadingIcon = Icons.Outlined.LineMagic,
                onClick = onGenerate,
                enabled = canGenerate && engine?.name?.isNotBlank() == true,
                modifier = Modifier.height(buttonHeight),
            )
        }
    }
}
