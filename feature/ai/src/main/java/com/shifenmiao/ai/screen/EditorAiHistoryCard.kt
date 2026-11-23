package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.R
import com.shifenmiao.database.ai.EditorAiUsage
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassCard
import java.text.DateFormat
import java.util.Date

@Composable
internal fun EditorAiHistoryCard(call: EditorAiUsage) {
    val action = if (call.source == "note") {
        if (call.action == "Metadata") R.string.editor_history_note_metadata else R.string.editor_history_note_rewrite
    } else when (call.action) {
        "Create" -> R.string.editor_history_prompt_create
        "Enhance" -> R.string.editor_history_prompt_enhance
        "Format" -> R.string.editor_history_prompt_format
        else -> R.string.editor_history_prompt_condense
    }
    val status = when (call.status) {
        "success" -> R.string.editor_history_success
        "stopped" -> R.string.editor_history_stopped
        else -> R.string.editor_history_failed
    }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(AppTheme.dimens.cardContentPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(action), style = MaterialTheme.typography.titleMedium)
            Text(
                listOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(call.createdAt)),
                    call.model, stringResource(status)).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                call.totalTokens?.let { stringResource(R.string.editor_history_tokens, it) }
                    ?: stringResource(R.string.editor_history_unknown),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
