package com.wanbaohe.setting.memory.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.MemoryLimits
import com.shifenmiao.database.ai.entity.MemoryEntryEntity
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.setting.memory.component.MemoryManagementComponent
import com.wanbaohe.settings.R as SettingsR
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryManagementScreen(
    component: MemoryManagementComponent,
) {
    BaseScreen(
        title = stringResource(R.string.profile_item_ai_memory),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        MemoryManagementContent(
            component = component,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun MemoryManagementContent(
    component: MemoryManagementComponent,
    modifier: Modifier = Modifier,
) {
    val globalEnabled by component.globalMemoryEnabled.collectAsState()
    val profileEntries by component.profileEntries.collectAsState()
    val logEntries by component.logEntries.collectAsState()

    // 编辑弹窗状态：null = 关闭；entry.id = 0 表示新增
    var editingEntry by remember { mutableStateOf<MemoryEntryEntity?>(null) }
    var deletingEntry by remember { mutableStateOf<MemoryEntryEntity?>(null) }
    var showClearLogConfirm by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    // log 按日期分组（最新在前），与注入语义（日期桶）一致
    val logBuckets = remember(logEntries) {
        logEntries.groupBy { dateFormat.format(Date(it.createdAt)) }
    }
    val logClearedText = stringResource(SettingsR.string.memory_log_cleared)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OneBoxDesignSystem.screenPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
    ) {
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

        // 全局总开关
        OneBoxSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(SettingsR.string.memory_global_switch_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(SettingsR.string.memory_global_switch_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = globalEnabled,
                    onCheckedChange = { component.setGlobalMemoryEnabled(it) }
                )
            }
        }

        // ─── profile 档案区（agent 只读、用户维护） ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OneBoxSectionHeader(
                    title = stringResource(SettingsR.string.memory_profile_section),
                    supporting = stringResource(SettingsR.string.memory_profile_supporting),
                )
            }
            IconButton(
                onClick = {
                    editingEntry = MemoryEntryEntity(kind = MemoryEntryEntity.KIND_PROFILE, content = "")
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(SettingsR.string.memory_add_entry),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        if (profileEntries.isEmpty()) {
            Text(
                text = stringResource(SettingsR.string.memory_profile_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        profileEntries.forEach { entry ->
            MemoryEntryCard(
                entry = entry,
                metaText = timeFormat.format(Date(entry.updatedAt)),
                onEdit = { editingEntry = entry },
                onDelete = { deletingEntry = entry },
                onToggleEnabled = { enabled -> component.setEntryEnabled(entry, enabled) },
            )
        }

        // ─── log 日志区（按日期分组，最新在前） ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OneBoxSectionHeader(
                    title = stringResource(SettingsR.string.memory_log_section),
                    supporting = stringResource(SettingsR.string.memory_log_supporting),
                )
            }
            IconButton(
                onClick = {
                    editingEntry = MemoryEntryEntity(kind = MemoryEntryEntity.KIND_LOG, content = "")
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(SettingsR.string.memory_add_entry),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            IconButton(
                onClick = { showClearLogConfirm = true },
                enabled = logEntries.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(SettingsR.string.memory_clear_log),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (logEntries.isEmpty()) {
            Text(
                text = stringResource(SettingsR.string.memory_log_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        logBuckets.forEach { (date, entries) ->
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            entries.forEach { entry ->
                MemoryEntryCard(
                    entry = entry,
                    metaText = buildString {
                        append(timeFormat.format(Date(entry.createdAt)))
                        entry.sourceConversationId?.takeIf { it.isNotBlank() }?.let {
                            append(" · ")
                            append(stringResource(SettingsR.string.memory_source_conversation, it))
                        }
                    },
                    onEdit = { editingEntry = entry },
                    onDelete = { deletingEntry = entry },
                    onToggleEnabled = { enabled -> component.setEntryEnabled(entry, enabled) },
                )
            }
        }

        Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
    }

    // 新增/编辑弹窗
    editingEntry?.let { entry ->
        var content by remember(entry) { mutableStateOf(entry.content) }
        val tooLongText = stringResource(SettingsR.string.memory_entry_too_long, MemoryLimits.MAX_ENTRY_CHARS)
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { editingEntry = null },
            title = {
                Text(
                    text = stringResource(
                        if (entry.id == 0L) SettingsR.string.memory_add_entry_title
                        else SettingsR.string.memory_edit_entry_title
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = {
                        Text(text = stringResource(SettingsR.string.memory_entry_content_hint))
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = content.trim()
                        // 与 memory_write 工具统一的单条限长
                        if (trimmed.length > MemoryLimits.MAX_ENTRY_CHARS) {
                            AppToastHost.showToast(tooLongText)
                            return@TextButton
                        }
                        if (trimmed.isNotEmpty()) {
                            component.saveEntry(entry.copy(content = trimmed))
                        }
                        editingEntry = null
                    }
                ) {
                    Text(text = stringResource(R.string.button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // 删除确认
    deletingEntry?.let { entry ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deletingEntry = null },
            title = { Text(text = stringResource(SettingsR.string.memory_delete_confirm_title)) },
            text = {
                Text(
                    text = entry.content,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.deleteEntry(entry.id)
                        deletingEntry = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.button_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // 清空 log 确认
    if (showClearLogConfirm) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showClearLogConfirm = false },
            title = { Text(text = stringResource(SettingsR.string.memory_clear_log_title)) },
            text = { Text(text = stringResource(SettingsR.string.memory_clear_log_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.clearLog()
                        showClearLogConfirm = false
                        AppToastHost.showToast(logClearedText)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.button_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogConfirm = false }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        )
    }
}

@Composable
private fun MemoryEntryCard(
    entry: MemoryEntryEntity,
    metaText: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
) {
    OneBoxSectionCard {
        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyMedium,
            color = if (entry.enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = metaText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = entry.enabled,
                onCheckedChange = onToggleEnabled,
            )
        }
    }
}
