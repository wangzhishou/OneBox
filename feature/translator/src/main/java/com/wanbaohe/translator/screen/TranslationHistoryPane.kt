package com.wanbaohe.translator.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.wanbaohe.translator.component.HistoryFilter
import com.wanbaohe.translator.component.TranslatorUiState
import com.wanbaohe.translator.domain.TranslationRecord
import com.wanbaohe.translator.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
internal fun TranslationHistoryPane(
    state: TranslatorUiState,
    records: List<TranslationRecord>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (HistoryFilter) -> Unit,
    onOpen: (TranslationRecord) -> Unit,
    onSpeak: (TranslationRecord) -> Unit,
    onFavorite: (TranslationRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        GlassCard(
            style = GlassStyle.Thin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BasicTextField(
                    value = state.historyQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (state.historyQuery.isBlank()) {
                            Text(
                                stringResource(R.string.translation_history_search),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    },
                )
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(HistoryFilter.entries) { filter ->
                EngineFilterChip(
                    text = stringResource(filter.labelRes()),
                    isSelected = state.historyFilter == filter,
                    onClick = { onFilterChange(filter) },
                )
            }
        }
        if (records.isEmpty()) {
            Text(
                text = stringResource(R.string.translation_history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp),
            )
            return@Column
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            records.groupBy { historyGroup(it.createdAt) }.forEach { (group, groupRecords) ->
                item(key = "header_$group") {
                    Text(
                        text = stringResource(group.labelRes()),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
                items(groupRecords, key = { it.id }) { record ->
                    HistoryCard(
                        record = record,
                        onOpen = { onOpen(record) },
                        onCopy = {
                            copyText(context, record.translatedText)
                            AppToastHost.showToast(com.t8rin.imagetoolbox.core.resources.R.string.copied)
                        },
                        onSpeak = { onSpeak(record) },
                        onFavorite = { onFavorite(record) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    record: TranslationRecord,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onFavorite: () -> Unit,
) {
    GlassCard(onClick = onOpen, style = GlassStyle.Regular, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${stringResource(record.sourceLanguage.labelRes())} → ${stringResource(record.targetLanguage.labelRes())}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = record.sourceText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = record.translatedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${timeFormat().format(Date(record.createdAt))} · ${record.modelName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onSpeak) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.translation_read_aloud),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.translation_copy),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (record.isFavorite) Icons.Rounded.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(
                            if (record.isFavorite) R.string.translation_unfavorite else R.string.translation_favorite
                        ),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private enum class HistoryGroup { TODAY, YESTERDAY, EARLIER }

private fun historyGroup(timestamp: Long): HistoryGroup {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    val dayDifference = now.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR)
    return when {
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) && dayDifference == 0 -> HistoryGroup.TODAY
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) && dayDifference == 1 -> HistoryGroup.YESTERDAY
        else -> HistoryGroup.EARLIER
    }
}

private fun HistoryGroup.labelRes(): Int = when (this) {
    HistoryGroup.TODAY -> R.string.translation_filter_today
    HistoryGroup.YESTERDAY -> R.string.translation_yesterday
    HistoryGroup.EARLIER -> R.string.translation_earlier
}

private fun timeFormat(): SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun copyText(context: Context, text: String) {
    context.getSystemService<ClipboardManager>()?.setPrimaryClip(
        ClipData.newPlainText("translation", text)
    )
}
