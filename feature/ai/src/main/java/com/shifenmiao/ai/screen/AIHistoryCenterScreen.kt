package com.shifenmiao.ai.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AIHistoryCenterComponent
import com.shifenmiao.ai.model.AIHistoryItem
import com.shifenmiao.ai.ui.AiBottomSheetSearchField
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.state.PageState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInsights
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRename

private val historyCenterDateFormat by lazy {
    SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
}

@Composable
fun AIHistoryCenterScreen(
    appComponent: AppComponent,
    component: AIHistoryCenterComponent,
) {
    val pageState by component.pageState.collectAsState()
    val query by component.query.collectAsState()
    val selectedFilter by component.selectedFilter.collectAsState()
    val items by component.items.collectAsState()
    val onNavigate = LocalOnNavigate.current
    var renameTarget by remember { mutableStateOf<AIHistoryItem?>(null) }
    var renameValue by rememberSaveable { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<AIHistoryItem?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(R.string.ai_history_center_title),
        onGoBack = appComponent.onGoBack,
        actions = {
            IconButton(
                onClick = { onNavigate(Screen.TokenUsage) }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights,
                    contentDescription = stringResource(R.string.profile_item_ai_usage)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AiBottomSheetSearchField(
                value = query,
                onValueChange = component::onQueryChange,
                placeholder = stringResource(R.string.ai_history_search_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HistoryFilterRow(
                selected = selectedFilter,
                onSelected = component::onFilterChange
            )
            when (pageState) {
                PageState.INITIALIZING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    if (items.isEmpty()) {
                        EmptyBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(items, key = { it.conversationId }) { item ->
                                HistoryConversationCard(
                                    item = item,
                                    onClick = { component.openConversation(item, onNavigate) },
                                    onRename = {
                                        renameTarget = item
                                        renameValue = item.title
                                    },
                                    onDelete = {
                                        deleteTarget = item
                                        showDeleteDialog.value = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    EnhancedAlertDialog(
        visible = renameTarget != null,
        onDismissRequest = { renameTarget = null },
        title = {
            Text(text = stringResource(R.string.ai_history_rename_title))
        },
        text = {
            OutlinedTextField(
                value = renameValue,
                onValueChange = { renameValue = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(R.string.ai_history_rename_hint))
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    renameTarget?.let {
                        component.renameConversation(it.conversationId, renameValue)
                    }
                    renameTarget = null
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { renameTarget = null }) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )

    if (showDeleteDialog.value && deleteTarget != null) {
        ConfirmDialog(
            title = stringResource(R.string.ai_chat_delete_title),
            message = stringResource(R.string.ai_history_delete_message),
            confirmButtonText = stringResource(R.string.button_confirm),
            dismissButtonText = stringResource(R.string.button_cancel),
            onConfirm = {
                deleteTarget?.let {
                    component.deleteConversation(it.conversationId)
                }
                deleteTarget = null
            },
            onDismiss = {
                deleteTarget = null
            },
            showDialog = showDeleteDialog,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun HistoryFilterRow(
    selected: AIConversationEntryType?,
    onSelected: (AIConversationEntryType?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassFilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text(text = stringResource(R.string.ai_history_filter_all)) }
        )
        AIConversationEntryType.entries.forEach { type ->
            GlassFilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(text = type.label()) }
            )
        }
    }
}

@Composable
private fun HistoryConversationCard(
    item: AIHistoryItem,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val primaryTitle = item.primaryTitle()
    val subtitleLabel = item.appTitle.ifBlank { item.entryType.label() }
    val actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = primaryTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subtitleLabel.isNotBlank()) {
                        SubtitleBadge(
                            text = subtitleLabel,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Text(
                        text = historyCenterDateFormat.format(Date(item.lastActiveAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.ai_history_message_count, item.messageCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnhancedIconButton(
                        onClick = onRename,
                        modifier = Modifier.size(32.dp),
                        forceMinimumInteractiveComponentSize = false
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRename,
                            contentDescription = stringResource(R.string.ai_history_rename_title),
                            tint = actionIconTint
                        )
                    }
                    EnhancedIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                        forceMinimumInteractiveComponentSize = false
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = actionIconTint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        containerAlpha = 0.45f
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AIConversationEntryType.label(): String = when (this) {
    AIConversationEntryType.CHAT -> stringResource(R.string.ai_history_filter_chat)
    AIConversationEntryType.PROMPT -> stringResource(R.string.type_prompt)
    AIConversationEntryType.AGENT -> stringResource(R.string.type_agent)
    AIConversationEntryType.DUEL -> stringResource(R.string.ai_history_filter_duel)
    AIConversationEntryType.ASSISTANT -> stringResource(R.string.ai_history_filter_assistant)
    else -> {
        stringResource(R.string.ai_stream_answer_title)
    }
}

@Composable
private fun AIHistoryItem.primaryTitle(): String {
    return title.ifBlank {
        appTitle.ifBlank { entryType.label() }
    }
}

