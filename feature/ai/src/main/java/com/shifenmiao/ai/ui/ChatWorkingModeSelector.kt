package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.R
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow

private data class WorkingModeUi(
    val title: String,
    val description: String,
)

@Composable
internal fun ChatWorkingModeSelector(
    currentMode: ChatWorkingMode,
    onModeSelected: (ChatWorkingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = ChatWorkingMode.entries
    val currentModeUi = currentMode.toUi()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassSegmentedButtonRow(
            options = modes,
            selectedOption = currentMode,
            onOptionSelected = onModeSelected,
            modifier = Modifier.fillMaxWidth(),
            label = { mode ->
                val modeUi = mode.toUi()
                Text(
                    text = modeUi.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            },
            buttonHeight = 40.dp,
        )
        Text(
            text = currentModeUi.description,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChatWorkingMode.toUi(): WorkingModeUi {
    return when (this) {
        ChatWorkingMode.ASK -> WorkingModeUi(
            title = stringResource(R.string.ai_chat_working_mode_ask_title),
            description = stringResource(R.string.ai_chat_working_mode_ask_description)
        )
        ChatWorkingMode.PLAN -> WorkingModeUi(
            title = stringResource(R.string.ai_chat_working_mode_plan_title),
            description = stringResource(R.string.ai_chat_working_mode_plan_description)
        )
        ChatWorkingMode.AGENT -> WorkingModeUi(
            title = stringResource(R.string.ai_chat_working_mode_agent_title),
            description = stringResource(R.string.ai_chat_working_mode_agent_description)
        )
    }
}

