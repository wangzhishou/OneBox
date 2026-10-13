package com.wanbaohe.householditems.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconPickerSheet
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.component.HouseholdItemsComponent
import com.wanbaohe.householditems.model.LocationTreeNode
import com.wanbaohe.householditems.model.flattenLocationTree
import com.wanbaohe.householditems.model.locationIcon

// ─────────────────────────────────────────────────────────────────────────────
// 位置管理(位置 tab):缩进卡片树,父级行可折叠/展开子树
// 层级感:按深度向右缩进 + 逐级递减的图标/字号/颜色;顶级行 primary 描边突出
// ─────────────────────────────────────────────────────────────────────────────

private val INDENT_PER_DEPTH = 20.dp
private val ROW_HEIGHT = 52.dp

@Composable
fun LocationManageTab(
    component: HouseholdItemsComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()

    /** 收起的位置 id 集合(默认全部展开,仅 rememberSaveable 不持久化) */
    var collapsedIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    // 扁平化全量节点 + 各节点可见性(祖先被收起则整棵子树隐藏,行级 AnimatedVisibility 做折叠动画)
    val flatNodes = remember(uiState.locationTree) { flattenLocationTree(uiState.locationTree) }
    val visibleIds = remember(uiState.locationTree, collapsedIds) {
        visibleLocationIds(uiState.locationTree, collapsedIds.toSet())
    }

    /** 待输入名称的目标:nameInputParent = 父位置 id(null = 顶级);nameInputRenameId 非空表示重命名 */
    var nameInputParent by remember { mutableStateOf<String?>(null) }
    var nameInputRenameId by remember { mutableStateOf<String?>(null) }
    var showNameInput by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    /** 对话框中选中的图标(IconRegistry key);null = 默认映射 */
    var nameInputIconKey by remember { mutableStateOf<String?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (uiState.locationDeleteBlocked) {
            item(key = "blocked") {
                Text(
                    text = stringResource(R.string.household_location_delete_blocked),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }

        if (flatNodes.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(R.string.household_locations_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
        items(flatNodes, key = { it.location.id }) { node ->
            AnimatedVisibility(
                visible = node.location.id in visibleIds,
                modifier = Modifier.animateItem(),
            ) {
                LocationTreeRow(
                    node = node,
                    collapsed = node.location.id in collapsedIds,
                    onToggleCollapse = {
                        collapsedIds = if (node.location.id in collapsedIds) {
                            collapsedIds - node.location.id
                        } else {
                            collapsedIds + node.location.id
                        }
                    },
                    onAddChild = {
                        nameInputParent = node.location.id
                        nameInputRenameId = null
                        nameInput = ""
                        nameInputIconKey = null
                        showNameInput = true
                    },
                    onRename = {
                        nameInputParent = null
                        nameInputRenameId = node.location.id
                        nameInput = node.location.name
                        nameInputIconKey = node.location.iconKey
                        showNameInput = true
                    },
                    onDelete = { component.deleteLocation(node.location.id) },
                )
            }
        }

        item(key = "add_root") {
            GlassTonalButton(
                onClick = {
                    nameInputParent = null
                    nameInputRenameId = null
                    nameInput = ""
                    nameInputIconKey = null
                    showNameInput = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineAddCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.household_location_add_root),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }

    if (showNameInput) {
        val isRename = nameInputRenameId != null
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showNameInput = false },
            title = {
                Text(
                    stringResource(
                        if (isRename) R.string.household_location_rename
                        else R.string.household_location_add_root
                    )
                )
            },
            text = {
                GlassOutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.household_location_name_hint)) },
                    singleLine = true,
                    // 左侧图标:显示当前选中图标,点击打开图标选择器
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = locationIcon(nameInputIconKey, ""),
                                contentDescription = stringResource(R.string.household_location_pick_icon),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = nameInput.trim()
                        if (name.isNotEmpty()) {
                            val renameId = nameInputRenameId
                            if (renameId != null) {
                                component.renameLocation(renameId, name, nameInputIconKey)
                            } else {
                                component.addLocation(name, nameInputParent, nameInputIconKey)
                            }
                            showNameInput = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.household_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameInput = false }) {
                    Text(stringResource(R.string.household_cancel))
                }
            },
        )
    }

    // 图标选择器(复用 core/ui IconPickerSheet,习惯打卡同款)
    IconPickerSheet(
        visible = showIconPicker,
        onDismiss = { showIconPicker = false },
        onIconSelected = { key -> nameInputIconKey = key },
        selectedIconName = nameInputIconKey,
    )
}

/**
 * 计算可见节点 id 集合:某节点被收起时,其整棵子树隐藏(该节点自身仍可见)。
 */
private fun visibleLocationIds(
    tree: List<LocationTreeNode>,
    collapsedIds: Set<String>,
): Set<String> {
    val result = mutableSetOf<String>()
    fun walk(nodes: List<LocationTreeNode>, ancestorCollapsed: Boolean) {
        nodes.forEach { node ->
            if (!ancestorCollapsed) result += node.location.id
            walk(node.children, ancestorCollapsed || node.location.id in collapsedIds)
        }
    }
    walk(tree, false)
    return result
}

/**
 * 树形行:每行一张玻璃卡片,按层级向右缩进(原型稿样式);
 * 父级行左侧带展开/收起 chevron,叶子行留同宽占位对齐;
 * 图标/字号/颜色逐级递减,操作按钮(添加子级/重命名/删除)靠右。
 */
@Composable
private fun LocationTreeRow(
    node: LocationTreeNode,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onAddChild: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val depth = node.depth
    val isTop = depth == 0

    // 逐级递减的图标/字号/颜色
    val iconSize = when (depth) {
        0 -> 22.dp
        1 -> 20.dp
        else -> 18.dp
    }
    val textStyle = if (depth >= 2) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    val textColor = if (depth >= 2) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textWeight = if (isTop) FontWeight.SemiBold else FontWeight.Normal

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = INDENT_PER_DEPTH * depth, top = 3.dp, bottom = 3.dp),
        shape = RoundedCornerShape(14.dp),
        style = GlassStyle.Medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ROW_HEIGHT)
                .padding(start = 4.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 展开/收起 chevron:父级可点,叶子行留同宽占位对齐
            if (node.children.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleCollapse),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (collapsed) {
                            Icons.Outlined.LineChevronRight
                        } else {
                            Icons.Outlined.LineExpandMore
                        },
                        contentDescription = stringResource(
                            if (collapsed) R.string.household_location_expand
                            else R.string.household_location_collapse
                        ),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Box(modifier = Modifier.size(28.dp))
            }
            Icon(
                imageVector = locationIcon(node.location.iconKey, node.location.id),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(iconSize),
                tint = if (isTop) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
            Text(
                text = node.location.name,
                style = textStyle,
                fontWeight = textWeight,
                color = textColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 紧凑操作按钮:图标 18dp,触摸区域 36dp
            IconButton(onClick = onAddChild, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.LineAddCircleOutline,
                    contentDescription = stringResource(R.string.household_location_add_child),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.household_location_rename),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.household_location_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
