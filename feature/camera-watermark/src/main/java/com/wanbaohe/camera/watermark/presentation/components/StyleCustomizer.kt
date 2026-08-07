package com.wanbaohe.camera.watermark.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorPickerSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.LogoType
import com.wanbaohe.camera.watermark.domain.WatermarkMetadata
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import com.wanbaohe.camera.watermark.util.localizedTemplateName
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 样式模板管理面板
 * 显示预置模板列表，支持编辑、新增模板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleCustomizer(
    style: WatermarkStyle,
    onStyleChanged: (WatermarkStyle) -> Unit,
    modifier: Modifier = Modifier,
    metadata: WatermarkMetadata = WatermarkMetadata.EMPTY,
    templates: List<WatermarkStyle> = emptyList(),
    onSaveTemplate: (WatermarkStyle) -> Unit = {},
    onDeleteTemplate: (Long) -> Unit = {},
    onBatchDeleteTemplates: (List<Long>) -> Unit = {},
    onResetPresets: () -> Unit = {}
) {
    // 编辑模板弹窗状态
    var showEditSheet by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<WatermarkStyle?>(null) }

    // 新增模板弹窗状态
    var showAddSheet by remember { mutableStateOf(false) }

    // 编辑模式状态
    var isEditMode by remember { mutableStateOf(false) }
    // 批量选中的模板 ID（用于批量删除）
    var selectedForDelete by remember { mutableStateOf(setOf<Long>()) }

    // 退出编辑模式时清空选中
    fun exitEditMode() {
        isEditMode = false
        selectedForDelete = emptySet()
    }

    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding()
    ) {
        // 标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.camera_watermark_template),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isEditMode) {
                    // 恢复默认按钮
                    FilledTonalButton(
                        onClick = {
                            onResetPresets()
                            exitEditMode()
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.camera_watermark_reset_presets))
                    }

                    // 批量删除按钮
                    FilledTonalButton(
                        onClick = {
                            if (selectedForDelete.isNotEmpty()) {
                                onBatchDeleteTemplates(selectedForDelete.toList())
                            }
                            exitEditMode()
                        },
                        enabled = selectedForDelete.isNotEmpty(),
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedForDelete.isEmpty()) stringResource(R.string.camera_watermark_delete) else stringResource(R.string.camera_watermark_delete_count, selectedForDelete.size))
                    }

                    // 取消按钮
                    FilledTonalButton(
                        onClick = { exitEditMode() },
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getPrimaryButtonColors()
                    ) {
                        Text(stringResource(R.string.camera_watermark_cancel))
                    }
                } else {
                    // 普通模式：显示编辑和新增按钮
                    FilledTonalButton(
                        onClick = { isEditMode = true },
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getSurfaceContainerButtonColors()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.camera_watermark_edit))
                    }

                    FilledTonalButton(
                        onClick = { showAddSheet = true },
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getPrimaryButtonColors()
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.camera_watermark_add))
                    }
                }
            }
        }

        // 模板列表（可滚动）- 按 id 降序排列，新增的排上面
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            templates.sortedByDescending { it.id }.forEach { template ->
                val isSelectedForDelete = selectedForDelete.contains(template.id)

                TemplateListItem(
                    template = template,
                    isSelected = template.id == style.id,
                    isEditMode = isEditMode,
                    isSelectedForDelete = isSelectedForDelete,
                    onSelect = {
                        if (isEditMode) {
                            // 编辑模式下点击切换选中状态（用于批量删除）
                            selectedForDelete = if (isSelectedForDelete) {
                                selectedForDelete - template.id
                            } else {
                                selectedForDelete + template.id
                            }
                        } else {
                            onStyleChanged(template)
                        }
                    },
                    onEdit = {
                        editingTemplate = template
                        showEditSheet = true
                    },
                    onDelete = { onDeleteTemplate(template.id) }
                )
            }
        }
    }

    // 编辑模板弹窗
    if (showEditSheet && editingTemplate != null) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = {
                showEditSheet = false
                editingTemplate = null
            },
            dragHandle = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.camera_watermark_edit_template)) },
                    navigationIcon = {},
                    actions = {}
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            TemplateEditor(
                template = editingTemplate!!,
                metadata = metadata,
                onSave = { updatedTemplate ->
                    onSaveTemplate(updatedTemplate)
                    onStyleChanged(updatedTemplate)
                    showEditSheet = false
                    editingTemplate = null
                },
                onCancel = {
                    showEditSheet = false
                    editingTemplate = null
                }
            )
        }
    }

    // 新增模板弹窗
    if (showAddSheet) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = { showAddSheet = false },
            dragHandle = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.camera_watermark_add_template)) },
                    navigationIcon = {},
                    actions = {}
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            TemplateEditor(
                template = WatermarkStyle(
                    id = System.currentTimeMillis(),
                    name = stringResource(R.string.camera_watermark_new_template_default_name)
                ),
                metadata = metadata,
                isNew = true,
                onSave = { newTemplate ->
                    onSaveTemplate(newTemplate)
                    onStyleChanged(newTemplate)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}

/**
 * 模板列表项（扁平化设计）
 */
@Composable
private fun TemplateListItem(
    template: WatermarkStyle,
    isSelected: Boolean,
    isEditMode: Boolean,
    isSelectedForDelete: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = when {
        isSelectedForDelete -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 编辑模式下显示选择框
        if (isEditMode) {
            Checkbox(
                checked = isSelectedForDelete,
                onCheckedChange = { onSelect() },
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Logo 预览
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            LogoPreview(
                logoType = template.logoType,
                customLogoPath = template.customLogoPath,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 模板信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                // 预置模板名称本地化，用户自建模板显示库中的名称
                text = localizedTemplateName(template),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = when (template.logoType) {
                    LogoType.LEICA -> stringResource(R.string.camera_watermark_logo_leica)
                    LogoType.WANBAOHE -> stringResource(R.string.camera_watermark_logo_wanbaohe)
                    LogoType.APPLE -> stringResource(R.string.camera_watermark_logo_apple)
                    LogoType.GOOGLE -> stringResource(R.string.camera_watermark_logo_google)
                    LogoType.HUAWEI -> stringResource(R.string.camera_watermark_logo_huawei)
                    LogoType.OPPO -> stringResource(R.string.camera_watermark_logo_oppo)
                    LogoType.VIVO -> stringResource(R.string.camera_watermark_logo_vivo)
                    LogoType.XIAOMI -> stringResource(R.string.camera_watermark_logo_xiaomi)
                    LogoType.ONEPLUS -> stringResource(R.string.camera_watermark_logo_oneplus)
                    LogoType.CUSTOM -> stringResource(R.string.camera_watermark_logo_custom)
                    LogoType.NONE -> stringResource(R.string.camera_watermark_logo_none)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 选中标记（非编辑模式）
        if (!isEditMode && isSelected) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // 编辑和删除按钮（仅编辑模式显示）
        if (isEditMode) {
            // 编辑按钮
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.camera_watermark_edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.camera_watermark_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Logo 预览
 */
@Composable
private fun LogoPreview(
    logoType: LogoType,
    customLogoPath: String?,
    modifier: Modifier = Modifier
) {
    when (logoType) {
        LogoType.LEICA -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.leica_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_leica),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.WANBAOHE -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = com.shifenmiao.core.R.drawable.logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_wanbaohe),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.APPLE -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.apple_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_apple),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.GOOGLE -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_google),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.HUAWEI -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.huawei_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_huawei),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.OPPO -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.oppo_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_oppo),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.VIVO -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.vivo_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_vivo),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.XIAOMI -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.xiaomi_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_xiaomi),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }


        LogoType.ONEPLUS -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.oneplus_logo),
                contentDescription = stringResource(R.string.camera_watermark_logo_oneplus),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }
        LogoType.CUSTOM -> {
            if (customLogoPath != null) {
                AsyncImage(
                    model = customLogoPath.toUri(),
                    contentDescription = stringResource(R.string.camera_watermark_logo_custom),
                    modifier = modifier,
                    contentScale = ContentScale.Fit
                )
            }
        }
        LogoType.NONE -> {
            // 不显示 Logo
        }
    }
}

/**
 * 模板编辑器（简洁扁平化设计）
 */
@Composable
internal fun TemplateEditor(
    template: WatermarkStyle,
    metadata: WatermarkMetadata,
    isNew: Boolean = false,
    onSave: (WatermarkStyle) -> Unit,
    onCancel: () -> Unit
) {
    var editedTemplate by remember(template.id) { mutableStateOf(template) }

    // 使用复用的图片选择对话框状态
    var showLogoPickerDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 可滚动内容区域
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(
                space = AppTheme.dimens.spaceNormal,
                alignment = Alignment.Top
            )
        ) {


            // 模板名称
            OutlinedTextField(
                value = editedTemplate.name,
                onValueChange = { editedTemplate = editedTemplate.copy(name = it) },
                label = { Text(stringResource(R.string.camera_watermark_template_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logo 选择
            Text(
                text = stringResource(R.string.camera_watermark_logo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            // Logo 选项横向滚动
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_leica),
                        logoType = LogoType.LEICA,
                        isSelected = editedTemplate.logoType == LogoType.LEICA,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.LEICA,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_wanbaohe),
                        logoType = LogoType.WANBAOHE,
                        isSelected = editedTemplate.logoType == LogoType.WANBAOHE,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.WANBAOHE,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_apple),
                        logoType = LogoType.APPLE,
                        isSelected = editedTemplate.logoType == LogoType.APPLE,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.APPLE,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_google),
                        logoType = LogoType.GOOGLE,
                        isSelected = editedTemplate.logoType == LogoType.GOOGLE,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.GOOGLE,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_huawei),
                        logoType = LogoType.HUAWEI,
                        isSelected = editedTemplate.logoType == LogoType.HUAWEI,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.HUAWEI,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_oppo),
                        logoType = LogoType.OPPO,
                        isSelected = editedTemplate.logoType == LogoType.OPPO,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.OPPO,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_vivo),
                        logoType = LogoType.VIVO,
                        isSelected = editedTemplate.logoType == LogoType.VIVO,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.VIVO,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_xiaomi),
                        logoType = LogoType.XIAOMI,
                        isSelected = editedTemplate.logoType == LogoType.XIAOMI,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.XIAOMI,
                                customLogoPath = null
                            )
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_custom),
                        logoType = LogoType.CUSTOM,
                        customLogoPath = editedTemplate.customLogoPath,
                        isSelected = editedTemplate.logoType == LogoType.CUSTOM,
                        onClick = {
                            showLogoPickerDialog = true
                        }
                    )
                }

                item {
                    LogoOptionChip(
                        title = stringResource(R.string.camera_watermark_brand_none),
                        logoType = LogoType.NONE,
                        isSelected = editedTemplate.logoType == LogoType.NONE,
                        onClick = {
                            editedTemplate = editedTemplate.copy(
                                logoType = LogoType.NONE,
                                customLogoPath = null
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 自定义内容区域
            Text(
                text = stringResource(R.string.camera_watermark_custom_content),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            // 左上角 - 相机信息
            OutlinedTextField(
                value = editedTemplate.customContent.topLeft ?: "",
                onValueChange = { newValue ->
                    editedTemplate = editedTemplate.copy(
                        customContent = editedTemplate.customContent.copy(
                            topLeft = newValue.ifEmpty { null }
                        )
                    )
                },
                label = { Text(stringResource(R.string.camera_watermark_camera_info_tl)) },
                placeholder = { Text(metadata.getCameraInfo().ifEmpty { stringResource(R.string.camera_watermark_camera_info_hint) }) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )



            // 右上角 - 拍摄参数
            OutlinedTextField(
                value = editedTemplate.customContent.topRight ?: "",
                onValueChange = { newValue ->
                    editedTemplate = editedTemplate.copy(
                        customContent = editedTemplate.customContent.copy(
                            topRight = newValue.ifEmpty { null }
                        )
                    )
                },
                label = { Text(stringResource(R.string.camera_watermark_params_info_tr)) },
                placeholder = { Text(metadata.getParamsInfo().ifEmpty { stringResource(R.string.camera_watermark_params_info_hint) }) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )



            // 左下角 - 时间/签名
            OutlinedTextField(
                value = editedTemplate.customContent.bottomLeft ?: "",
                onValueChange = { newValue ->
                    editedTemplate = editedTemplate.copy(
                        customContent = editedTemplate.customContent.copy(
                            bottomLeft = newValue.ifEmpty { null }
                        )
                    )
                },
                label = { Text(stringResource(R.string.camera_watermark_time_sign_bl)) },
                placeholder = { Text(metadata.dateTime.ifEmpty { stringResource(R.string.camera_watermark_time_sign_hint) }) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )



            // 右下角 - GPS
            OutlinedTextField(
                value = editedTemplate.customContent.bottomRight ?: "",
                onValueChange = { newValue ->
                    editedTemplate = editedTemplate.copy(
                        customContent = editedTemplate.customContent.copy(
                            bottomRight = newValue.ifEmpty { null }
                        )
                    )
                },
                label = { Text(stringResource(R.string.camera_watermark_location_br)) },
                placeholder = { Text(metadata.getGpsInfo().ifEmpty { stringResource(R.string.camera_watermark_location_hint) }) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 显示分隔线
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.camera_watermark_show_divider),
                    style = MaterialTheme.typography.bodyMedium
                )
                GlassSwitch(
                    checked = editedTemplate.showDivider,
                    onCheckedChange = { editedTemplate = editedTemplate.copy(showDivider = it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 颜色设置
            Text(
                text = stringResource(R.string.camera_watermark_color_settings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            // 背景色
            ColorSettingItem(
                label = stringResource(R.string.camera_watermark_bg_color),
                color = Color(editedTemplate.backgroundColor),
                onColorChange = {
                    editedTemplate = editedTemplate.copy(backgroundColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                }
            )



            // 主文字颜色
            ColorSettingItem(
                label = stringResource(R.string.camera_watermark_primary_text_color),
                color = Color(editedTemplate.primaryTextColor),
                onColorChange = {
                    editedTemplate = editedTemplate.copy(primaryTextColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                }
            )



            // 次要文字颜色
            ColorSettingItem(
                label = stringResource(R.string.camera_watermark_secondary_text_color),
                color = Color(editedTemplate.secondaryTextColor),
                onColorChange = {
                    editedTemplate = editedTemplate.copy(secondaryTextColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                }
            )



            // 分隔线颜色（仅在启用分隔线时显示）
            if (editedTemplate.showDivider) {
                ColorSettingItem(
                    label = stringResource(R.string.camera_watermark_divider_color),
                    color = Color(editedTemplate.dividerColor),
                    onColorChange = {
                        editedTemplate = editedTemplate.copy(dividerColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                    }
                )


            }

            // 尺寸设置
            Text(
                text = stringResource(R.string.camera_watermark_size_settings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            // 水印高度
            SliderSettingItem(
                label = stringResource(R.string.camera_watermark_watermark_height),
                value = editedTemplate.watermarkHeight.toFloat(),
                valueRange = 60f..400f,
                unit = "dp",
                onValueChange = {
                    editedTemplate = editedTemplate.copy(watermarkHeight = it.toInt())
                }
            )



            // 水平内边距
            SliderSettingItem(
                label = stringResource(R.string.camera_watermark_padding_horizontal),
                value = editedTemplate.paddingHorizontal.toFloat(),
                valueRange = 8f..48f,
                unit = "dp",
                onValueChange = {
                    editedTemplate = editedTemplate.copy(paddingHorizontal = it.toInt())
                }
            )



            // 垂直内边距
            SliderSettingItem(
                label = stringResource(R.string.camera_watermark_padding_vertical),
                value = editedTemplate.paddingVertical.toFloat(),
                valueRange = 4f..32f,
                unit = "dp",
                onValueChange = {
                    editedTemplate = editedTemplate.copy(paddingVertical = it.toInt())
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 字体设置
            Text(
                text = stringResource(R.string.camera_watermark_font_settings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 主文字大小
            SliderSettingItem(
                label = stringResource(R.string.camera_watermark_primary_font_size),
                value = editedTemplate.primaryFontSize.toFloat(),
                valueRange = 1f..60f,
                unit = "%",
                onValueChange = {
                    editedTemplate = editedTemplate.copy(primaryFontSize = it.toInt())
                }
            )

            // 次要文字大小
            SliderSettingItem(
                label = stringResource(R.string.camera_watermark_secondary_font_size),
                value = editedTemplate.secondaryFontSize.toFloat(),
                valueRange = 1f..40f,
                unit = "%",
                onValueChange = {
                    editedTemplate = editedTemplate.copy(secondaryFontSize = it.toInt())
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 判断样式是否与默认值不同（只比较样式相关字段，不比较 id、name 等）
            val isStyleModified = editedTemplate.backgroundColor != WatermarkStyle.DEFAULT.backgroundColor ||
                    editedTemplate.primaryTextColor != WatermarkStyle.DEFAULT.primaryTextColor ||
                    editedTemplate.secondaryTextColor != WatermarkStyle.DEFAULT.secondaryTextColor ||
                    editedTemplate.dividerColor != WatermarkStyle.DEFAULT.dividerColor ||
                    editedTemplate.showDivider != WatermarkStyle.DEFAULT.showDivider ||
                    editedTemplate.watermarkHeight != WatermarkStyle.DEFAULT.watermarkHeight ||
                    editedTemplate.paddingHorizontal != WatermarkStyle.DEFAULT.paddingHorizontal ||
                    editedTemplate.paddingVertical != WatermarkStyle.DEFAULT.paddingVertical ||
                    editedTemplate.primaryFontSize != WatermarkStyle.DEFAULT.primaryFontSize ||
                    editedTemplate.secondaryFontSize != WatermarkStyle.DEFAULT.secondaryFontSize

            // 恢复默认样式按钮（只在样式有变化时显示）
            if (isStyleModified) {
                FilledTonalButton(
                    onClick = {
                        // 保留 id、name、logoType、customLogoPath、customContent，其他恢复默认
                        editedTemplate = WatermarkStyle.DEFAULT.copy(
                            id = editedTemplate.id,
                            name = editedTemplate.name,
                            logoType = editedTemplate.logoType,
                            customLogoPath = editedTemplate.customLogoPath,
                            customContent = editedTemplate.customContent,
                            createdAt = editedTemplate.createdAt
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.camera_watermark_reset_style))
                }
            }

        }

        // 固定在底部的按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
        ) {
            FilledTonalButton(
                onClick = onCancel,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                colors = AppTheme.colors.getSurfaceContainerButtonColors()
            ) {
                Text(stringResource(R.string.camera_watermark_cancel))
            }
            FilledTonalButton(
                onClick = { onSave(editedTemplate) },
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getPrimaryButtonColors()
            ) {
                Text(if (isNew) stringResource(R.string.camera_watermark_create) else stringResource(R.string.camera_watermark_save))
            }
        }
    }

    // 自定义 Logo 图片选择对话框（复用 common 模块的组件）
    AddImagePickingDialogWithPicker(
        visible = showLogoPickerDialog,
        onDismiss = { showLogoPickerDialog = false },
        picker = Picker.Single,
        onImagesPicked = { uris ->
            uris.firstOrNull()?.let { uri ->
                editedTemplate = editedTemplate.copy(
                    logoType = LogoType.CUSTOM,
                    customLogoPath = uri.toString()
                )
            }
        }
    )
}

/**
 * Logo 选项 Chip（简洁扁平设计）
 */
@Composable
private fun LogoOptionChip(
    title: String,
    logoType: LogoType,
    isSelected: Boolean,
    onClick: () -> Unit,
    customLogoPath: String? = null
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Logo 图标
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isSelected && logoType != LogoType.CUSTOM -> {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                logoType == LogoType.CUSTOM && customLogoPath != null -> {
                    AsyncImage(
                        model = customLogoPath.toUri(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                logoType == LogoType.CUSTOM -> {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                logoType != LogoType.NONE -> {
                    LogoPreview(
                        logoType = logoType,
                        customLogoPath = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * 颜色设置项
 */
@Composable
private fun ColorSettingItem(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showColorPicker = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.small
                )
                .clickable { showColorPicker = true }
        )
    }

    if (showColorPicker) {
        ColorPickerSheet(
            visible = true,
            onDismiss = { showColorPicker = false },
            color = color,
            onColorSelected = {
                onColorChange(it)
                showColorPicker = false
            },
            allowAlpha = false
        )
    }
}

/**
 * 滑动条设置项
 */
@Composable
private fun SliderSettingItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCustomSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
