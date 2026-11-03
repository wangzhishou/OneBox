package com.wanbaohe.setting.ai.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.openai.OpenAIModelItem
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.wanbaohe.base.ui.ClearTextFieldTrailingIcon
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR

// 分组卡片容器(无标题分组), 模型服务新增/编辑页共用
@Composable
internal fun SettingCard(content: @Composable () -> Unit) {
    OneBoxSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            content()
        }
    }
}

// 可折叠分组头: 中号加粗标题 + 行尾旋转箭头, 整行可点; summary 用于收起时展示当前取值
@Composable
internal fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    summary: String? = null,
) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!summary.isNullOrBlank()) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
        } else {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        }
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .rotate(arrowRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 模型网格选择卡片:选中高亮(onPrimaryContainer 描边 + 对勾) */
@Composable
internal fun SelectGridCard(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * 行内单选选择器：标签 + 当前取值 + 行尾箭头，点开菜单换一个。
 *
 * 协议这类「选项少但文案长」的控件不再横排卡片：LazyRow 里第二张卡片会被卡片边缘
 * 裁掉、又看不出还能横滑，用户既看不清选项也看不清当前选的是什么。选择器把当前值
 * 摆在明面上，完整选项收进菜单，长文案不会被裁。
 */
@Composable
internal fun <T> SelectorField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    // 只有一个选项时(如 Jev/Pikafish 锁定入口)不做成可展开: 展示当前值即可
    val canSwitch = options.size > 1
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OneBoxListItem(
            // 菜单宽度跟着这一行走, 和卡片左右边界对齐。
            // enabled = false: menuAnchor 默认自己在每次抬手时切一次展开态, 会和行的
            // onClick 叠成"开了又关"; 这里只借它的锚点测量/焦点能力, 开合交给行本身。
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = false)
                .fillMaxWidth(),
            contained = true,
            onClick = if (canSwitch) {
                { expanded = !expanded }
            } else {
                null
            },
            headlineContent = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            subtitle = {
                Text(
                    text = optionLabel(selected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingContent = if (canSwitch) {
                {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                null
            },
        )
        if (canSwitch) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = MaterialTheme.shapes.large,
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                // 协议全名较长, 用 bodyMedium 才放得进一行; 与行内当前值同字号
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            expanded = false
                            if (!isSelected) onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun protocolLabel(protocol: AiRequestProtocol): String {
    return when (protocol) {
        AiRequestProtocol.OPENAI_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_openai)
        AiRequestProtocol.RESPONSES_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_responses)
        AiRequestProtocol.ANTHROPIC_COMPATIBLE -> stringResource(R.string.ai_engine_protocol_anthropic)
        AiRequestProtocol.OWN_PROXY -> stringResource(R.string.ai_engine_protocol_proxy)
        // 仅云端协议出现在本选择器；
        // LOCAL_ON_DEVICE 由独立的"本地模型管理"页处理（Phase 2）。
        AiRequestProtocol.LOCAL_ON_DEVICE -> stringResource(R.string.ai_engine_protocol_local_on_device)
        AiRequestProtocol.JEV -> stringResource(R.string.ai_engine_protocol_jev)
        AiRequestProtocol.PIKAFISH -> stringResource(R.string.ai_engine_protocol_pikafish)
    }
}

/**
 * 服务商模型选择弹窗（新增服务页用）：搜索 + 单选。
 *
 * 手输模型 ID 容易打错；拉一次列表后点一下即选中并关闭。加载/失败/空态都在弹窗内
 * 呈现，失败时可直接重试，不用退回页面再点一次按钮。
 */
@Composable
internal fun ProviderModelsPickerDialog(
    models: List<OpenAIModelItem>,
    isLoading: Boolean,
    error: String?,
    selectedModelId: String,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onSelect: (OpenAIModelItem) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredModels = remember(models, query) {
        val keyword = query.trim()
        if (keyword.isBlank()) models else models.filter { it.id.contains(keyword, ignoreCase = true) }
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ai_engine_remote_models_picker_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!isLoading && error == null && models.isNotEmpty()) {
                    OneBoxOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(stringResource(CoreR.string.search)) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            ClearTextFieldTrailingIcon(
                                value = query,
                                onClear = { query = "" },
                            )
                        },
                    )
                }
                when {
                    isLoading -> Text(
                        text = stringResource(R.string.ai_engine_remote_models_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )

                    error != null -> Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.ai_engine_remote_models_load_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        if (error.isNotBlank()) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OneSecondaryButton(
                            text = stringResource(R.string.ai_engine_remote_models_retry),
                            onClick = onRefresh,
                        )
                    }

                    filteredModels.isEmpty() -> Text(
                        text = stringResource(R.string.ai_engine_remote_models_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )

                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(filteredModels, key = { it.id }) { model ->
                            val metadata = listOf(model.ownedBy, model.obj)
                                .filter { it.isNotBlank() }
                                .joinToString(" · ")
                            OneBoxListItem(
                                contained = true,
                                onClick = { onSelect(model) },
                                headlineContent = {
                                    Text(
                                        text = model.id,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (model.id == selectedModelId) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                subtitle = if (metadata.isBlank()) {
                                    null
                                } else {
                                    {
                                        Text(
                                            text = metadata,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                },
                                trailingContent = if (model.id == selectedModelId) {
                                    {
                                        Icon(
                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OneSecondaryButton(
                text = stringResource(CoreR.string.button_cancel),
                onClick = onDismiss,
            )
        },
    )
}

@Composable
internal fun authTypeLabel(authType: AuthType): String {
    return when (authType) {
        AuthType.BEARER -> stringResource(R.string.ai_engine_auth_type_bearer)
        AuthType.API_KEY -> stringResource(R.string.ai_engine_auth_type_api_key)
        AuthType.NONE -> stringResource(R.string.ai_engine_auth_type_none)
    }
}
