package com.wanbaohe.setting.skill.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.SkillImportValidator
import com.shifenmiao.database.ai.SkillLocalStore
import com.shifenmiao.database.ai.SkillUsagePolicy
import com.shifenmiao.database.ai.entity.SkillEntity
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.shareText
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.setting.skill.component.SkillManagementComponent
import com.wanbaohe.settings.R as SettingsR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

@Composable
fun SkillManagementScreen(
    component: SkillManagementComponent,
) {
    BaseScreen(
        title = stringResource(R.string.profile_item_ai_skill),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        SkillManagementContent(
            component = component,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun SkillManagementContent(
    component: SkillManagementComponent,
    modifier: Modifier = Modifier,
) {
    val globalEnabled by component.globalSkillsEnabled.collectAsState()
    val skills by component.skills.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 删除确认状态（正文编辑在全屏详情页 Screen.SkillDetail）
    var deletingSkill by remember { mutableStateOf<SkillEntity?>(null) }
    var showImportMenu by remember { mutableStateOf(false) }
    // 元数据弹窗：null 关闭；NON_NULL = 编辑该技能的 name/description（仅 LOCAL）
    var metadataDialogSkill by remember { mutableStateOf<SkillEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val importSuccessText = stringResource(SettingsR.string.skill_import_success)
    val importFailedText = stringResource(SettingsR.string.skill_import_failed)
    val bundledConflictText = stringResource(SettingsR.string.skill_import_bundled_conflict)
    val tooLargeText = stringResource(SettingsR.string.skill_import_too_large)
    val slugTooLongText = stringResource(SettingsR.string.skill_import_slug_too_long)
    val fileTooLargeText = stringResource(SettingsR.string.skill_import_file_too_large)
    val invalidNameText = stringResource(SettingsR.string.skill_metadata_invalid_name)
    val invalidDescText = stringResource(SettingsR.string.skill_metadata_invalid_description)
    val nameConflictText = stringResource(SettingsR.string.skill_metadata_name_conflict)

    // 导入结果（剪贴板 / 文件 / 新建共用）：拒绝原因 → 用户提示
    fun showImportResult(rejection: SkillImportValidator.Rejection?) {
        AppToastHost.showToast(
            when (rejection) {
                null -> importSuccessText
                SkillImportValidator.Rejection.BUNDLED_NAME_CONFLICT -> bundledConflictText
                SkillImportValidator.Rejection.BODY_TOO_LARGE -> tooLargeText
                SkillImportValidator.Rejection.SLUG_TOO_LONG -> slugTooLongText
                else -> importFailedText
            }
        )
    }

    // 从文件导入 SKILL.md（SAF）：IO 线程限量读取（超限按文件过大拒绝），
    // 读完后走与剪贴板相同的导入管线
    val fileImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    readTextCapped(input, SkillImportValidator.MAX_IMPORT_FILE_BYTES)
                }
            }.getOrNull()
            if (text == null) {
                withContext(Dispatchers.Main) {
                    AppToastHost.showToast(fileTooLargeText)
                }
            } else {
                component.importFromContent(text, ::showImportResult)
            }
        }
    }

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
                        text = stringResource(SettingsR.string.skill_global_switch_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(SettingsR.string.skill_global_switch_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = globalEnabled,
                    onCheckedChange = { component.setGlobalSkillsEnabled(it) }
                )
            }
        }

        // ─── 技能列表 ───
        // 标题占满剩余宽度，操作收进两个图标按钮（新建 + 导入菜单），
        // 长文案下也不会互相挤压换行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OneBoxSectionHeader(
                    title = stringResource(SettingsR.string.skill_list_section),
                    supporting = stringResource(SettingsR.string.skill_list_supporting),
                )
            }
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(SettingsR.string.skill_new),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Box {
                IconButton(onClick = { showImportMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(SettingsR.string.skill_import_clipboard),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = { showImportMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(SettingsR.string.skill_import_clipboard))
                        },
                        onClick = {
                            showImportMenu = false
                            val clipText = clipboardManager.getText()?.text.orEmpty()
                            component.importFromContent(clipText, ::showImportResult)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(SettingsR.string.skill_import_file))
                        },
                        onClick = {
                            showImportMenu = false
                            fileImportLauncher.launch(
                                arrayOf("text/*", "text/markdown", "application/octet-stream")
                            )
                        }
                    )
                }
            }
        }

        if (skills.isEmpty()) {
            Text(
                text = stringResource(SettingsR.string.skill_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        skills.forEach { skill ->
            OneBoxSectionCard(onClick = { component.onNavigate(Screen.SkillDetail(skill.id)) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Switch(
                        checked = skill.enabled,
                        onCheckedChange = { component.setSkillEnabled(skill, it) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    SourceBadge(source = skill.source)
                    UsageBadge(frequency = component.usageFrequency(skill.useCount))
                    Spacer(modifier = Modifier.weight(1f))
                    // 元数据编辑（name/description）与删除仅 LOCAL；分享正文走系统分享
                    if (skill.source == SkillEntity.SOURCE_LOCAL) {
                        IconButton(onClick = { metadataDialogSkill = skill }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(SettingsR.string.skill_edit_metadata_title),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { deletingSkill = skill }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = { context.shareText(skill.body) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
    }

    // 新建：先填 name/description 创建空正文技能，成功后直接跳进正文编辑器
    if (showCreateDialog) {
        SkillMetadataDialog(
            title = stringResource(SettingsR.string.skill_detail_title_new),
            initialName = "",
            initialDescription = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                val markdown = buildString {
                    appendLine("---")
                    appendLine("name: ${name.trim()}")
                    appendLine("description: ${description.trim()}")
                    appendLine("---")
                    appendLine()
                }
                component.importFromContent(markdown) { rejection ->
                    if (rejection == null) {
                        showCreateDialog = false
                        // 导入语义保证创建的 id 就是校验过的 name
                        component.onNavigate(Screen.SkillDetail(name.trim()))
                    } else {
                        showImportResult(rejection)
                    }
                }
            }
        )
    }

    // 元数据编辑（name/description，不动正文；slug 变更走事务内主键迁移）
    metadataDialogSkill?.let { skill ->
        SkillMetadataDialog(
            title = stringResource(SettingsR.string.skill_edit_metadata_title),
            initialName = skill.name,
            initialDescription = skill.description,
            onDismiss = { metadataDialogSkill = null },
            onConfirm = { name, description ->
                component.updateMetadata(skill, name, description) { result ->
                    when (result) {
                        SkillLocalStore.MetadataResult.SUCCESS,
                        SkillLocalStore.MetadataResult.NOT_EDITABLE ->
                            metadataDialogSkill = null

                        SkillLocalStore.MetadataResult.INVALID_NAME ->
                            AppToastHost.showToast(invalidNameText)

                        SkillLocalStore.MetadataResult.INVALID_DESCRIPTION ->
                            AppToastHost.showToast(invalidDescText)

                        SkillLocalStore.MetadataResult.NAME_CONFLICT ->
                            AppToastHost.showToast(nameConflictText)
                    }
                }
            }
        )
    }

    // 删除确认（仅 LOCAL 会触发）
    deletingSkill?.let { skill ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deletingSkill = null },
            title = { Text(text = stringResource(SettingsR.string.skill_delete_confirm_title)) },
            text = { Text(text = skill.name) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.deleteSkill(skill)
                        deletingSkill = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.button_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSkill = null }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        )
    }
}

@Composable
private fun SourceBadge(source: String) {
    val (label, tint) = when (source) {
        SkillEntity.SOURCE_BUNDLED ->
            stringResource(SettingsR.string.skill_source_bundled) to MaterialTheme.colorScheme.primary

        SkillEntity.SOURCE_REMOTE ->
            stringResource(SettingsR.string.skill_source_remote) to MaterialTheme.colorScheme.tertiary

        else ->
            stringResource(SettingsR.string.skill_source_local) to MaterialTheme.colorScheme.secondary
    }
    Badge(text = label, tint = tint)
}

@Composable
private fun UsageBadge(frequency: SkillUsagePolicy.UsageFrequency) {
    val label = when (frequency) {
        SkillUsagePolicy.UsageFrequency.NEVER ->
            stringResource(SettingsR.string.skill_usage_never)

        SkillUsagePolicy.UsageFrequency.LOW ->
            stringResource(SettingsR.string.skill_usage_low)

        SkillUsagePolicy.UsageFrequency.REGULAR ->
            stringResource(SettingsR.string.skill_usage_regular)

        SkillUsagePolicy.UsageFrequency.HIGH ->
            stringResource(SettingsR.string.skill_usage_high)
    }
    Badge(text = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Badge(text: String, tint: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}

/** 元数据弹窗（name/description 两字段）：失败保留输入，成功由调用方关闭 */
@Composable
private fun SkillMetadataDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                GlassOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(text = stringResource(SettingsR.string.skill_name_hint))
                    }
                )
                GlassOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp, max = 160.dp),
                    minLines = 3,
                    maxLines = 4,
                    placeholder = {
                        Text(text = stringResource(SettingsR.string.skill_description_hint))
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, description) }) {
                Text(text = stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.button_cancel))
            }
        }
    )
}

/** 限量读取文本：超过 [maxBytes] 返回 null（按文件过大拒绝），不全文读入后再校验 */
private fun readTextCapped(input: InputStream, maxBytes: Int): String? {
    val buffer = ByteArray(maxBytes + 1)
    var total = 0
    while (total <= maxBytes) {
        val read = input.read(buffer, total, maxBytes + 1 - total)
        if (read < 0) break
        total += read
    }
    if (total > maxBytes) return null
    return String(buffer, 0, total, Charsets.UTF_8)
}
