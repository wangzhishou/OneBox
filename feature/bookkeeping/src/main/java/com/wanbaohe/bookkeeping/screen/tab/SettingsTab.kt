package com.wanbaohe.bookkeeping.screen.tab

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
// (Rules feature removed)
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.category.CategoryManagementDialog
import com.shifenmiao.common.components.category.ManageableItem
import com.shifenmiao.model.Source
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import com.wanbaohe.bookkeeping.model.BookkeepingCategoryUi
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapHoriz
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArchive
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTrendingUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUploadFile

// ─────────────────────────────────────────────────────────────────────────────
// 设置 Tab 入口
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 设置 Tab：美化后的分类管理页面。
 * - 三个分类区块（支出/入账/不计入收支），每块用卡片展示
 * - FlowRow chip 展示所有分类，分类多时自动换行
 * - 卡片内置"快速新增"输入框
 * - "管理"按钮打开 CategoryManagementDialog，支持拖拽排序/重命名/删除
 */
@Composable
internal fun SettingsTab(
    component: BookkeepingComponent,
    modifier: Modifier = Modifier,
) {
    val allCategories by component.allCategoriesFlow.collectAsState()

    val context = LocalComponentActivity.current
    val backupSuccessText = stringResource(R.string.bookkeeping_backup_success)
    val backupFailedText = stringResource(R.string.bookkeeping_backup_failed)
    val restoreFailedText = stringResource(R.string.bookkeeping_restore_failed)
    val restoreEmptyCategoriesText = stringResource(R.string.bookkeeping_restore_empty_categories)
    val csvExportSuccessText = stringResource(R.string.bookkeeping_csv_export_success)
    val csvExportFailedText = stringResource(R.string.bookkeeping_csv_export_failed)
    val csvImportFailedText = stringResource(R.string.bookkeeping_csv_import_failed)
    val backupFileName = stringResource(
        R.string.bookkeeping_backup_file_name,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
    )
    val csvFileName = stringResource(
        R.string.bookkeeping_csv_file_name,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
    )

    var managingType by remember { mutableStateOf<BookkeepingRecordType?>(null) }

    val backupCreator = rememberFileCreator(
        mimeType = MimeType.Json,
        onSuccess = { uri ->
            component.exportBackup(
                onSuccess = { json ->
                    if (context.writeTextToUri(uri, json)) {
                        AppToastHost.showToast(backupSuccessText)
                    } else {
                        AppToastHost.showToast(backupFailedText)
                    }
                },
                onFailure = {
                    AppToastHost.showToast(backupFailedText)
                },
            )
        }
    )
    val restorePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.ImportText,
        onSuccess = { uris ->
            val uri = uris.firstOrNull() ?: return@rememberFilePicker
            val json = context.readTextFromUri(uri)
            if (json.isNullOrBlank()) {
                AppToastHost.showToast(restoreFailedText)
                return@rememberFilePicker
            }
            component.restoreBackup(
                json = json,
                onSuccess = { result ->
                    AppToastHost.showToast(
                        context.getString(
                            R.string.bookkeeping_restore_success,
                            result.categoryCount,
                            result.recordCount,
                        )
                    )
                },
                onFailure = { throwable ->
                    val message = if (throwable.message == "categories_empty") {
                        restoreEmptyCategoriesText
                    } else {
                        restoreFailedText
                    }
                    AppToastHost.showToast(message)
                },
            )
        }
    )

    val csvExportCreator = rememberFileCreator(
        mimeType = MimeType.Csv,
        onSuccess = { uri ->
            component.exportCsv(
                context = context,
                uri = uri,
                onSuccess = { AppToastHost.showToast(csvExportSuccessText) },
                onFailure = { AppToastHost.showToast(csvExportFailedText) },
            )
        }
    )

    val csvImportPicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.Csv,
        onSuccess = { uris ->
            val uri = uris.firstOrNull() ?: return@rememberFilePicker
            component.importCsv(
                context = context,
                uri = uri,
                onSuccess = { count ->
                    AppToastHost.showToast(
                        context.getString(R.string.bookkeeping_csv_import_success, count)
                    )
                },
                onFailure = { AppToastHost.showToast(csvImportFailedText) },
            )
        }
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                borderWidth = 0.dp,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                containerAlpha = 0.62f,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.bookkeeping_category_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.bookkeeping_visual_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            CategorySectionCard(
                title = stringResource(R.string.bookkeeping_expense),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingDown,
                accentColor = MaterialTheme.colorScheme.secondary,
                chipContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                onChipContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surface,
                categories = allCategories.expense,
                onManage = { managingType = BookkeepingRecordType.EXPENSE },
                onAdd = { component.addCategory(it, BookkeepingRecordType.EXPENSE) },
            )
        }

        item {
            CategorySectionCard(
                title = stringResource(R.string.bookkeeping_income),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTrendingUp,
                accentColor = MaterialTheme.colorScheme.primary,
                chipContainerColor = MaterialTheme.colorScheme.primaryContainer,
                onChipContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.surface,
                categories = allCategories.income,
                onManage = { managingType = BookkeepingRecordType.INCOME },
                onAdd = { component.addCategory(it, BookkeepingRecordType.INCOME) },
            )
        }

        item {
            CategorySectionCard(
                title = stringResource(R.string.bookkeeping_excluded),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapHoriz,
                accentColor = MaterialTheme.colorScheme.tertiary,
                chipContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onChipContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                containerColor = MaterialTheme.colorScheme.surface,
                categories = allCategories.excluded,
                onManage = { managingType = BookkeepingRecordType.EXCLUDED },
                onAdd = { component.addCategory(it, BookkeepingRecordType.EXCLUDED) },
            )
        }

        item {
            Text(
                text = stringResource(R.string.bookkeeping_tool_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            SettingsActionCard(
                title = stringResource(R.string.bookkeeping_tool_backup_title),
                description = stringResource(R.string.bookkeeping_tool_backup_desc),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArchive,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                onIconContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { backupCreator.make(backupFileName) },
            )
        }

        item {
            SettingsActionCard(
                title = stringResource(R.string.bookkeeping_tool_restore_title),
                description = stringResource(R.string.bookkeeping_tool_restore_desc),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUploadFile,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onIconContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { restorePicker.pickFile() },
            )
        }

        item {
            SettingsActionCard(
                title = stringResource(R.string.bookkeeping_tool_csv_export_title),
                description = stringResource(R.string.bookkeeping_tool_csv_export_desc),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArchive,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                onIconContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { csvExportCreator.make(csvFileName) },
            )
        }

        item {
            SettingsActionCard(
                title = stringResource(R.string.bookkeeping_tool_csv_import_title),
                description = stringResource(R.string.bookkeeping_tool_csv_import_desc),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUploadFile,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onIconContainerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { csvImportPicker.pickFile() },
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    managingType?.let { type ->
        val rawList = when (type) {
            BookkeepingRecordType.EXPENSE  -> allCategories.expense
            BookkeepingRecordType.INCOME   -> allCategories.income
            BookkeepingRecordType.EXCLUDED -> allCategories.excluded
        }
        val manageableItems = rawList.map {
            BookkeepingManageableItem(it, if (it.isDefault) Source.SYSTEM else Source.LOCAL) }
        val dialogTitle = when (type) {
            BookkeepingRecordType.EXPENSE  -> "${stringResource(R.string.bookkeeping_expense)}${stringResource(R.string.bookkeeping_category_settings)}"
            BookkeepingRecordType.INCOME   -> "${stringResource(R.string.bookkeeping_income)}${stringResource(R.string.bookkeeping_category_settings)}"
            BookkeepingRecordType.EXCLUDED -> stringResource(R.string.bookkeeping_excluded)
        }
        CategoryManagementDialog(
            items = manageableItems,
            title = dialogTitle,
            onDismiss = { managingType = null },
            onAdd = { name -> component.addCategory(name, type) },
            onDelete = { item -> component.removeCategory(item.categoryId) },
            onRename = { item, newName -> component.renameCategory(item.categoryId, newName) },
            onReorder = { orderedItems ->
                component.reorderCategories(orderedItems.map { it.categoryId })
            },
        )
    }

}

// ─────────────────────────────────────────────────────────────────────────────
// 分类区块卡片
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategorySectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    chipContainerColor: Color,
    onChipContainerColor: Color,
    containerColor: Color,
    categories: List<BookkeepingCategoryUi>,
    onManage: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var showAddInput by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        borderWidth = 0.dp,
        containerAlpha = 0.92f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                    // 数量气泡：与 chip 保持同一对 *Container/on*Container 配色，
                    // 在深浅色模式下都有稳定对比度，避免 alpha 叠加产生的对比漂移。
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(chipContainerColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${categories.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = onChipContainerColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // 极致扁平：裸 IconButton，无 background / 无 clip
                    IconButton(
                        onClick = {
                            showAddInput = !showAddInput
                            if (!showAddInput) addName = ""
                        },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.bookkeeping_add_new_category),
                            modifier = Modifier.size(20.dp),
                            tint = accentColor,
                        )
                    }
                    OutlinedButton(
                        onClick = onManage,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.bookkeeping_category_manage),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }

            if (categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.bookkeeping_category_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.forEach { cat ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = chipContainerColor,
                                labelColor = onChipContainerColor,
                            ),
                            border = null,
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }
            }

            if (showAddInput) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        placeholder = { Text(stringResource(R.string.bookkeeping_new_category)) },
                        colors = AppTheme.colors.getOutlinedTextFieldColors(),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    IconButton(
                        onClick = {
                            if (addName.isNotBlank()) {
                                onAdd(addName)
                                addName = ""
                                showAddInput = false
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconContainerColor: Color,
    onIconContainerColor: Color,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        borderWidth = 0.dp,
        containerAlpha = 0.94f,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconContainerColor)
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onIconContainerColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Context.writeTextToUri(uri: Uri, content: String): Boolean {
    return runCatching {
        contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(content)
        } ?: error("output_stream_null")
    }.isSuccess
}

private fun Context.readTextFromUri(uri: Uri): String? {
    return runCatching {
        contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readText()
        }
    }.getOrNull()
}

// ─────────────────────────────────────────────────────────────────────────────
// ManageableItem 包装器（桥接 BookkeepingCategoryUi ↔ CategoryManagementDialog）
// ─────────────────────────────────────────────────────────────────────────────

private data class BookkeepingManageableItem(
    private val categoryUi: BookkeepingCategoryUi,
    override val source: Source,
) : ManageableItem {
    /** 对外暴露原始 String id，用于 Component 回调 */
    val categoryId: String get() = categoryUi.id

    /** ManageableItem 要求 Int，用 hashCode 保证 LazyColumn key 唯一性 */
    override val id: Int get() = categoryUi.id.hashCode()
    override val name: String get() = categoryUi.name
    override val order: Int get() = categoryUi.sortOrder

    /** 预置分类不可编辑/删除 */
    override val canEdit: Boolean? get() = !categoryUi.isDefault
}
