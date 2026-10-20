package com.wanbaohe.householditems.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolderCustom
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.component.HouseholdItemsComponent
import com.wanbaohe.householditems.model.LocationTreeNode
import com.wanbaohe.householditems.model.defaultHouseholdCategories
import com.wanbaohe.householditems.model.flattenLocationTree
import com.wanbaohe.householditems.model.locationIcon
import com.wanbaohe.householditems.model.locationPathOf
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// 添加/编辑物品(独立页面,支持外部跳转:Screen.HouseholdItems.Type.EditItem)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ItemEditScreen(
    component: HouseholdItemsComponent,
    onGoBack: () -> Unit,
) {
    val uiState by component.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker(
        onSuccess = { uri: Uri -> component.onEditorPhotoPicked(uri.toString()) },
    )

    BaseScreen(
        title = stringResource(
            if (uiState.editingItemId == null) R.string.household_editor_add_title
            else R.string.household_editor_edit_title
        ),
        onGoBack = onGoBack,
        actions = {
            IconButton(
                onClick = component::submitItem,
                enabled = uiState.editorName.isNotBlank(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.household_save),
                    tint = if (uiState.editorName.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        supportGlassEffect = true,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    // 底部保存按钮避开系统导航栏(手势条/三键),同记账 AddRecordScreen 惯例
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── 名称(必填) ──
                GlassOutlinedTextField(
                    value = uiState.editorName,
                    onValueChange = component::onEditorNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.household_editor_name_label)) },
                    placeholder = { Text(stringResource(R.string.household_editor_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )

                // ── 分类(预设 chips + 自定义输入) ──
                CategoryField(
                    category = uiState.editorCategory,
                    onCategoryChange = component::onEditorCategoryChange,
                )

                // ── 层级位置 ──
                LocationField(
                    selectedLocationId = uiState.editorLocationId,
                    selectedIconKey = uiState.locations
                        .firstOrNull { it.id == uiState.editorLocationId }?.iconKey,
                    locationPath = locationPathOf(uiState.locations, uiState.editorLocationId)
                        .joinToString(" / ") { it.name },
                    onPickClick = { showLocationPicker = true },
                    onClear = { component.onEditorLocationChange(null) },
                )

                // ── 保质期 ──
                ExpireField(
                    expireDate = uiState.editorExpireDate,
                    onPickClick = { showDatePicker = true },
                    onClear = { component.onEditorExpireDateChange(null) },
                )

                // ── 图片 ──
                PhotoField(
                    photoUri = uiState.editorPhotoUri,
                    photoPath = uiState.editorPhotoPath,
                    onPickClick = imagePicker::pickImage,
                    onRemove = component::onEditorPhotoRemoved,
                )

                // ── 备注 ──
                GlassOutlinedTextField(
                    value = uiState.editorNote,
                    onValueChange = component::onEditorNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.household_editor_note_label)) },
                    placeholder = { Text(stringResource(R.string.household_editor_note_hint)) },
                    minLines = 2,
                    shape = RoundedCornerShape(16.dp),
                )

                // ── 保存 ──
                EnhancedButton(
                    onClick = component::submitItem,
                    enabled = uiState.editorName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.household_save))
                }
            }
        },
        showNavigationBarsPadding = false,
    )

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = uiState.editorExpireDate ?: LocalDate.now(),
            onDateSelected = { date ->
                component.onEditorExpireDateChange(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            tree = uiState.locationTree,
            selectedLocationId = uiState.editorLocationId,
            onSelect = { locationId ->
                component.onEditorLocationChange(locationId)
                showLocationPicker = false
            },
            onCreateLocation = { name, parentId ->
                component.addLocation(name, parentId)
            },
            onDismiss = { showLocationPicker = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 表单字段
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryField(
    category: String,
    onCategoryChange: (String) -> Unit,
) {
    // 16 个默认分类(名称按当前 locale 解析),宫格选择 + 自定义输入
    val presets = defaultHouseholdCategories.map { it to stringResource(it.nameResId) }
    val isCustom = category.isNotBlank() && presets.none { it.second == category }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.household_editor_category_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            maxItemsInEachRow = 4,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { (def, name) ->
                val selected = category == name
                GlassCard(
                    onClick = { onCategoryChange(if (selected) "" else name) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    style = GlassStyle.Medium,
                    border = if (selected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        null
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = def.icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        // 自定义分类:输入后自动脱离预设选中态
        GlassOutlinedTextField(
            value = if (isCustom) category else "",
            onValueChange = onCategoryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.household_category_custom_hint)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun LocationField(
    selectedLocationId: String?,
    selectedIconKey: String?,
    locationPath: String,
    onPickClick: () -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.household_editor_location_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassCard(
            onClick = onPickClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            style = GlassStyle.Medium,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = locationIcon(selectedIconKey, selectedLocationId ?: ""),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = locationPath.ifBlank { stringResource(R.string.household_editor_location_none) },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (selectedLocationId != null) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.household_editor_expire_clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpireField(
    expireDate: LocalDate?,
    onPickClick: () -> Unit,
    onClear: () -> Unit,
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.household_editor_expire_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassCard(
            onClick = onPickClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            style = GlassStyle.Medium,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = expireDate?.format(formatter)
                        ?: stringResource(R.string.household_editor_expire_pick),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
                if (expireDate != null) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.household_editor_expire_clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoField(
    photoUri: String?,
    photoPath: String?,
    onPickClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val model: Any? = when {
        photoUri != null -> photoUri.toUri()
        !photoPath.isNullOrBlank() -> File(photoPath)
        else -> null
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.household_editor_photo_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.household_editor_photo_remove))
                }
            } else {
                TextButton(onClick = onPickClick) {
                    Text(stringResource(R.string.household_editor_photo_add))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 层级位置选择器:树形列表逐级选择,可就地新建位置
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LocationPickerDialog(
    tree: List<LocationTreeNode>,
    selectedLocationId: String?,
    onSelect: (String?) -> Unit,
    onCreateLocation: (String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val flatNodes = remember(tree) { flattenLocationTree(tree) }
    var newLocationParentId by remember { mutableStateOf<String?>(null) }
    var showNewLocationInput by remember { mutableStateOf(false) }
    var newLocationName by remember { mutableStateOf("") }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.household_editor_location_pick)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.household_cancel))
            }
        },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                ) {
                    // 未设置位置
                    item(key = "none") {
                        LocationPickerRow(
                            name = stringResource(R.string.household_editor_location_none),
                            icon = Icons.Outlined.LineFolderCustom,
                            depth = 0,
                            selected = selectedLocationId == null,
                            onClick = { onSelect(null) },
                            onAddChild = null,
                        )
                    }
                    items(flatNodes, key = { it.location.id }) { node ->
                        LocationPickerRow(
                            name = node.location.name,
                            icon = locationIcon(node.location.iconKey, node.location.id),
                            depth = node.depth,
                            selected = selectedLocationId == node.location.id,
                            onClick = { onSelect(node.location.id) },
                            onAddChild = {
                                newLocationParentId = node.location.id
                                showNewLocationInput = true
                            },
                        )
                    }
                }

                // ── 就地新建位置 ──
                if (showNewLocationInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassOutlinedTextField(
                            value = newLocationName,
                            onValueChange = { newLocationName = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.household_location_name_hint)) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                        )
                        TextButton(
                            onClick = {
                                if (newLocationName.isNotBlank()) {
                                    onCreateLocation(newLocationName.trim(), newLocationParentId)
                                    newLocationName = ""
                                    showNewLocationInput = false
                                }
                            },
                        ) {
                            Text(stringResource(R.string.household_confirm))
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            newLocationParentId = null
                            showNewLocationInput = true
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(stringResource(R.string.household_location_add_root))
                    }
                }
            }
        },
    )
}

@Composable
private fun LocationPickerRow(
    name: String,
    icon: ImageVector,
    depth: Int,
    selected: Boolean,
    onClick: () -> Unit,
    onAddChild: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(start = (depth * 20).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .padding(start = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onAddChild != null) {
            IconButton(onClick = onAddChild) {
                Icon(
                    imageVector = Icons.Outlined.LineAddCircleOutline,
                    contentDescription = stringResource(R.string.household_location_add_child),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
