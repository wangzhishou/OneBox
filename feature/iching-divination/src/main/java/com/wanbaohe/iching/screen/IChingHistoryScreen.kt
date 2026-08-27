package com.wanbaohe.iching.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.iching.R
import com.wanbaohe.iching.component.IChingHistoryComponent
import com.wanbaohe.iching.data.IChingHistoryRecord
import java.text.DateFormat
import java.util.Date

@Composable
fun IChingHistoryScreen(component: IChingHistoryComponent) {
    val state by component.uiState.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(R.string.iching_history_title),
        onGoBack = component.onGoBack,
        actions = {
            if (state.records.isNotEmpty()) {
                EnhancedIconButton(
                    onClick = { showClearConfirm = true },
                    containerColor = Color.Transparent,
                    enableAutoShadowAndBorder = false,
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClear,
                        contentDescription = stringResource(R.string.iching_history_clear),
                    )
                }
            }
        },
        supportGlassEffect = true,
    ) {
        if (state.records.isEmpty()) {
            Text(
                text = stringResource(R.string.iching_history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize().padding(top = 96.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.records, key = IChingHistoryRecord::id) { record ->
                    HistoryRecordCard(
                        record = record,
                        onClick = { component.openRecord(record.id) },
                        onDelete = { component.deleteRecord(record.id) },
                    )
                }
            }
        }
    }

    EnhancedAlertDialog(
        visible = showClearConfirm,
        onDismissRequest = { showClearConfirm = false },
        title = { Text(stringResource(R.string.iching_history_clear)) },
        text = { Text(stringResource(R.string.iching_history_clear_confirm)) },
        confirmButton = {
            GlassTextButton(
                onClick = {
                    showClearConfirm = false
                    component.clearHistory()
                },
                contentColor = MaterialTheme.colorScheme.error,
            ) { Text(stringResource(R.string.iching_confirm)) }
        },
        dismissButton = {
            GlassTextButton(onClick = { showClearConfirm = false }) {
                Text(stringResource(R.string.iching_cancel))
            }
        },
    )
}

@Composable
private fun HistoryRecordCard(
    record: IChingHistoryRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        containerAlpha = 0.18f,
        borderWidth = 0.6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HistoryHexagram(record.lineValues)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = record.primaryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = record.question.ifBlank { stringResource(R.string.iching_history_no_question) },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(formatHistoryTime(record.createdAt))
                        record.changedName?.let { append(" · ").append(it) }
                        if (record.aiContent.isNotBlank()) append(" · AI")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            EnhancedIconButton(
                onClick = onDelete,
                containerColor = Color.Transparent,
                enableAutoShadowAndBorder = false,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.iching_history_delete),
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
private fun HistoryHexagram(lineValues: List<Int>) {
    GlassSurface(
        modifier = Modifier.size(width = 68.dp, height = 72.dp),
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        borderWidth = 0.6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            lineValues.asReversed().forEach { value ->
                val isYang = value == 7 || value == 9
                val color = if (value == 6 || value == 9) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.onSurface
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    if (isYang) {
                        Box(Modifier.fillMaxWidth().height(3.dp).background(color, RoundedCornerShape(99.dp)))
                    } else {
                        Box(Modifier.weight(1f).height(3.dp).background(color, RoundedCornerShape(99.dp)))
                        Box(Modifier.width(5.dp))
                        Box(Modifier.weight(1f).height(3.dp).background(color, RoundedCornerShape(99.dp)))
                    }
                }
            }
        }
    }
}

private fun formatHistoryTime(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))

