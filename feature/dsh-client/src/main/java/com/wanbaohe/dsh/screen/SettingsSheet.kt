package com.wanbaohe.dsh.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.CredentialStatus
import com.wanbaohe.dsh.session.ProviderEntry
import com.wanbaohe.dsh.session.SettingsConflictException
import com.wanbaohe.dsh.session.SettingsStore
import com.wanbaohe.dsh.wire.model.DiscoveredModelView
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

/**
 * settings/credentials/llm 配置面(对齐 Flutter settings_screen.dart 的特权分区,
 * DSH-PROTOCOL §6):
 * - 入口由 PrivilegeScope 门控(ChatScreen 顶栏;LAN 直连不渲染入口,本 sheet 不再复查)
 * - providers 目录:状态点(绿=密钥已配置 / 红=引用缺失 / 无=无引用)+ 名称 +
 *   可路由/自定义标签;展开行编辑:
 *   API 密钥(credentials.set/unset,只写)、baseURL(settings.mutate 单字段 CAS)、
 *   获取可用模型(llm.discoverModels)
 * - settings-conflict → 内联提示「配置已在别处修改,已重新加载」
 * - 通用区:打开配置文件(settings.openDocument)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    store: SettingsStore,
    onDismiss: () -> Unit
) {
    val snapshot by store.snapshot.collectAsState()
    var expandedProvider by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.dsh_settings_title),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            when {
                snapshot.loading -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }

                snapshot.providers.isEmpty() -> Text(
                    text = stringResource(R.string.dsh_settings_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                ) {
                    items(snapshot.providers.size, key = { snapshot.providers[it].providerId }) { i ->
                        ProviderRow(
                            entry = snapshot.providers[i],
                            expanded = expandedProvider == snapshot.providers[i].providerId,
                            store = store,
                            onToggle = {
                                val id = snapshot.providers[i].providerId
                                expandedProvider = if (expandedProvider == id) null else id
                            }
                        )
                    }
                    item(key = "footer") {
                        SettingsFooter(store)
                    }
                }
            }
        }
    }
}

/** 提供方行:状态点 + 名称 + 标签;展开为编辑区 */
@Composable
private fun ProviderRow(
    entry: ProviderEntry,
    expanded: Boolean,
    store: SettingsStore,
    onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 凭据徽标:绿=已配置 / 红=引用缺失 / 无引用=无点
            when (entry.credentialStatus) {
                CredentialStatus.Configured -> CredentialDot(AppTheme.colors.getPrimaryColor())
                CredentialStatus.Missing -> CredentialDot(MaterialTheme.colorScheme.error)
                CredentialStatus.None -> Spacer(Modifier.size(8.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(entry.providerId)
                        append(" · ")
                        append(
                            stringResource(
                                if (entry.routable) {
                                    R.string.dsh_settings_routable
                                } else {
                                    R.string.dsh_settings_not_routable
                                }
                            )
                        )
                        if (entry.custom) {
                            append(" · ")
                            append(stringResource(R.string.dsh_settings_custom))
                        }
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowDown
                } else {
                    Icons.Default.KeyboardArrowRight
                },
                contentDescription = stringResource(
                    if (expanded) R.string.dsh_node_collapse else R.string.dsh_node_expand
                ),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            ProviderEditor(entry = entry, store = store)
        }
    }
}

@Composable
private fun CredentialDot(color: androidx.compose.ui.graphics.Color) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/** 提供方编辑区:API 密钥(只写)/ baseURL(CAS mutate)/ 获取可用模型 */
@Composable
private fun ProviderEditor(entry: ProviderEntry, store: SettingsStore) {
    val scope = rememberCoroutineScope()
    var busy by remember(entry.providerId) { mutableStateOf(false) }
    var error by remember(entry.providerId) { mutableStateOf<String?>(null) }
    var keyDialogOpen by remember(entry.providerId) { mutableStateOf(false) }
    var baseUrlDialogOpen by remember(entry.providerId) { mutableStateOf(false) }
    var discovered by remember(entry.providerId) { mutableStateOf<List<DiscoveredModelView>?>(null) }
    val conflictText = stringResource(R.string.dsh_settings_conflict)

    /** 动作公共回环:busy + 错误内联;settings-conflict 固定文案 */
    fun run(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                block()
            } catch (_: SettingsConflictException) {
                error = conflictText
            } catch (e: Throwable) {
                error = e.message
            }
            busy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, end = 16.dp, bottom = 10.dp)
    ) {
        // API 密钥行(credentials 域;无引用 = 不可配置)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.credentialRef
                    ?: stringResource(R.string.dsh_settings_no_credential),
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.credentialRef != null) {
                TextButton(onClick = { keyDialogOpen = true }, enabled = !busy) {
                    Text(stringResource(R.string.dsh_settings_set_key), fontSize = 12.sp)
                }
                if (entry.credentialStatus == CredentialStatus.Configured) {
                    TextButton(
                        onClick = { run { store.unsetCredential(entry.credentialRef) } },
                        enabled = !busy
                    ) {
                        Text(
                            text = stringResource(R.string.dsh_settings_unset_key),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        // baseURL 行(settings.mutate 单字段 set,CAS)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.field("baseURL")
                    ?: stringResource(R.string.dsh_settings_no_base_url),
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = { baseUrlDialogOpen = true }, enabled = !busy) {
                Text(stringResource(R.string.dsh_settings_edit_base_url), fontSize = 12.sp)
            }
        }
        // 获取可用模型(llm.discoverModels,带已保存配置)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = {
                    discovered = null
                    run {
                        discovered = store.discoverModels(
                            settingsNs = entry.namespace,
                            provider = entry.providerId
                        ).models
                    }
                },
                enabled = !busy
            ) {
                Text(stringResource(R.string.dsh_settings_discover_models), fontSize = 12.sp)
            }
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            }
        }
        discovered?.let { models ->
            if (models.isEmpty()) {
                Text(
                    text = stringResource(R.string.dsh_settings_discover_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                for (model in models) {
                    Text(
                        text = model.name?.takeIf { it.isNotBlank() } ?: model.id,
                        modifier = Modifier.padding(top = 2.dp),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (keyDialogOpen && entry.credentialRef != null) {
        TextInputDialog(
            title = stringResource(R.string.dsh_settings_set_key_title, entry.credentialRef),
            initial = "",
            onDismiss = { keyDialogOpen = false },
            onConfirm = { value ->
                keyDialogOpen = false
                if (value.isNotBlank()) {
                    run { store.setCredential(entry.credentialRef, value.trim()) }
                }
            }
        )
    }
    if (baseUrlDialogOpen) {
        TextInputDialog(
            title = stringResource(R.string.dsh_settings_edit_base_url),
            initial = entry.field("baseURL").orEmpty(),
            onDismiss = { baseUrlDialogOpen = false },
            onConfirm = { value ->
                baseUrlDialogOpen = false
                run {
                    store.setField(
                        entry.namespace,
                        entry.settingsPath + "baseURL",
                        JsonPrimitive(value.trim())
                    )
                }
            }
        )
    }
}

/** 通用区:打开配置文件(settings.openDocument;结果内联交代) */
@Composable
private fun SettingsFooter(store: SettingsStore) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    val failedText = stringResource(R.string.dsh_settings_open_document_failed)
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        TextButton(onClick = {
            scope.launch {
                message = try {
                    if (store.openDocument().opened) {
                        null // 成功无回执文本:静默(桌面端已弹出编辑器)
                    } else {
                        failedText
                    }
                } catch (e: Throwable) {
                    e.message
                }
            }
        }) {
            Text(stringResource(R.string.dsh_settings_open_document))
        }
        message?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** 通用单字段输入对话框(密钥/baseURL 共用) */
@Composable
private fun TextInputDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}
