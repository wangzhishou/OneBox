package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.shifenmiao.common.markdown.rememberInAppLinkHandler
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.EditorHistoryResult
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.CancellationException
import java.text.DateFormat

@Composable
internal fun EditorHistoryPicker(
    actionKey: Any,
    enabled: Boolean,
    load: suspend () -> List<EditorHistoryResult>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(actionKey) { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    IconButton(
        enabled = enabled,
        onClick = {
            focus.clearFocus()
            expanded = true
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.LineHistory,
            contentDescription = stringResource(R.string.history),
        )
    }
    if (expanded && enabled) {
        val results by produceState<Result<List<EditorHistoryResult>>?>(null, actionKey) {
            value = try {
                Result.success(load())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Result.failure(failure)
            }
        }
        EnhancedAlertDialog(
            visible = true,
            placeAboveAll = true,
            onDismissRequest = { expanded = false },
            title = { Text(stringResource(R.string.history)) },
            confirmButton = {
                OneSecondaryButton(stringResource(R.string.button_cancel), onClick = { expanded = false })
            },
            text = {
                EditorHistoryResults(results) { answer ->
                    expanded = false
                    onSelect(answer)
                }
            },
        )
    }
}

@Composable
private fun EditorHistoryResults(
    results: Result<List<EditorHistoryResult>>?,
    onSelect: (String) -> Unit,
) {
    val rows = results?.getOrNull()
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    when {
        results == null -> LinearProgressIndicator(Modifier.fillMaxWidth())
        rows == null -> Text(stringResource(com.shifenmiao.online.R.string.editor_history_load_failed))
        rows.isEmpty() -> Text(
            stringResource(com.shifenmiao.online.R.string.editor_history_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        else -> LazyColumn(
            Modifier.fillMaxWidth().heightIn(max = 288.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(rows, key = { it.id }) { item ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(item.answer) },
                ) {
                    Column(
                        Modifier.padding(AppTheme.dimens.cardContentPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        OneBoxMarkdownContent(
                            message = item.answer,
                            modifier = Modifier.fillMaxWidth(),
                            paddingValues = PaddingValues(0.dp),
                            enableScroll = false,
                            enableSelection = false,
                            onLinkClick = rememberInAppLinkHandler(),
                        )
                        Text(
                            listOf(dateFormat.format(item.createdAt), item.model).filter { it.isNotBlank() }.joinToString(" · "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
