package com.wanbaohe.decisionwheel.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAssistant
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelEditorComponent
import com.wanbaohe.decisionwheel.component.WheelOption
import com.wanbaohe.decisionwheel.ui.DecisionWheelPalettePickerSheet
import kotlinx.coroutines.delay

/**
 * 与盘面上"已被抽后移除摘掉"的扇区同一个灰。
 * 编辑页和主页必须用同一个颜色，否则用户得靠猜才能把两边对上号。
 */
private val removedOptionColor = Color(0xFF9E9E9E)

@Composable
fun DecisionWheelEditorScreen(
    component: DecisionWheelEditorComponent
) {
    val uiState by component.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var undoMessage by remember { mutableStateOf<String?>(null) }
    val removedTip = stringResource(R.string.option_removed)

    val baseColor = uiState.options.firstOrNull { it.color != Color.Unspecified }?.color
        ?: AppTheme.colorScheme.primary

    BaseScreen(
        title = stringResource(R.string.edit_wheel),
        onGoBack = component::goBack,
        actions = {
            IconButton(
                onClick = component::save,
                enabled = uiState.canSave
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.save),
                    tint = if (uiState.canSave) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        // 不再整体 verticalScroll：选项列表自己滚，底部操作条固定在屏幕底部。
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassOutlinedTextField(
                value = uiState.title,
                onValueChange = component::updateTitle,
                label = { Text(stringResource(R.string.wheel_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.getMediumShape(),
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )

            val removedCount = uiState.options.count { !it.enabled }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.options_list_count, uiState.options.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // 被"抽后移除"摘掉的选项在这里是看不见的：它们跟正常选项长得一模一样，
                // 保存后仍然是灰扇区。不放个入口，用户只能回主页点"全部恢复"。
                if (removedCount > 0) {
                    GlassTextButton(
                        onClick = component::restoreAllOptions,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.restore_all_action),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                GlassTonalButton(
                    onClick = { showPaletteSheet = true },
                    colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LineTheme,
                        contentDescription = stringResource(R.string.palette_scheme),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.palette_scheme), fontSize = 13.sp)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.options, key = { _, option -> option.id }) { index, option ->
                    OptionEditRow(
                        option = option,
                        canDelete = uiState.options.size > 2,
                        canMoveUp = index > 0,
                        canMoveDown = index < uiState.options.lastIndex,
                        onRename = { component.renameOption(option.id, it) },
                        onWeightChange = { component.setWeight(option.id, it) },
                        onDelete = {
                            if (component.removeOption(option.id)) {
                                undoMessage = removedTip.format(option.name)
                            }
                        },
                        onMoveUp = { component.moveOption(index, index - 1) },
                        onMoveDown = { component.moveOption(index, index + 1) },
                        onRestore = { component.setOptionEnabled(option.id, true) }
                    )
                }
            }

            AnimatedVisibility(visible = undoMessage != null) {
                UndoBar(
                    text = undoMessage.orEmpty(),
                    onUndo = {
                        component.undoRemove()
                        undoMessage = null
                    },
                    onDismiss = {
                        component.consumeUndo()
                        undoMessage = null
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassTonalButton(
                    onClick = { showAddDialog = true },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add_option),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.add_option))
                }

                GlassTonalButton(
                    onClick = { showAiDialog = true },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LineAutoFix,
                        contentDescription = stringResource(R.string.ai_create_options),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ai_create_options))
                }

                // 弹窗里那次是"就地生成一批"，这个是把人送去 AI 助手继续聊
                GlassTonalButton(
                    onClick = component::openAiAssistant,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LineAssistant,
                        contentDescription = stringResource(R.string.ai_assistant_action),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ai_assistant_action))
                }
            }
        }
    }

    if (showAddDialog) {
        AddOptionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name ->
                component.addOption(name)
                showAddDialog = false
            }
        )
    }

    if (showAiDialog) {
        val fallbackTopic = stringResource(R.string.ai_create_fallback_topic)
        // 填充词带上当前转盘名：用户点开就能直接生成，不用先想怎么描述
        val initialPrompt = stringResource(
            R.string.ai_create_filler,
            uiState.title.ifBlank { fallbackTopic }
        )
        AiCreateDialog(
            initialPrompt = initialPrompt,
            generating = uiState.aiGenerating,
            suggestions = uiState.aiSuggestions,
            error = uiState.aiError,
            onDismiss = {
                showAiDialog = false
                component.clearAiState()
            },
            onGenerate = component::requestAiOptions,
            onApply = {
                component.applyAiSuggestions()
                showAiDialog = false
            }
        )
    }

    DecisionWheelPalettePickerSheet(
        visible = showPaletteSheet,
        onDismiss = { showPaletteSheet = false },
        initialBaseColor = baseColor,
        previewCount = uiState.options.size,
        onConfirm = { color ->
            component.applyPalette(color)
            showPaletteSheet = false
        }
    )
}

@Composable
private fun OptionEditRow(
    option: WheelOption,
    canDelete: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRename: (String) -> Unit,
    onWeightChange: (Float) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRestore: () -> Unit
) {
    var draftName by remember(option.id) { mutableStateOf(option.name) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.listRowShape,
        containerAlpha = OneBoxDesignSystem.sectionGlassStyle.backgroundAlpha,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(option.color)
                )

                GlassOutlinedTextField(
                    value = draftName,
                    onValueChange = {
                        draftName = it
                        if (it.isNotBlank()) onRename(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    singleLine = true,
                    shape = AppTheme.shapes.getMediumShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = onDelete, enabled = canDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = if (canDelete) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.option_weight),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                WeightStepper(
                    weight = option.weight,
                    onChange = onWeightChange
                )

                if (!option.enabled) {
                    GlassTextButton(
                        onClick = onRestore,
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.option_removed_badge),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                    Text("▲", fontSize = 12.sp, color = if (canMoveUp) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                    Text("▼", fontSize = 12.sp, color = if (canMoveDown) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WeightStepper(
    weight: Float,
    onChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { onChange(weight - 0.5f) },
            modifier = Modifier.size(28.dp)
        ) {
            Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = weight.trimText(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { onChange(weight + 0.5f) },
            modifier = Modifier.size(28.dp)
        ) {
            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UndoBar(
    text: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(text) {
        delay(5000)
        onDismiss()
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.listRowShape,
        containerAlpha = OneBoxDesignSystem.sectionGlassStyle.backgroundAlpha,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.undo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    onClick = onUndo,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            )
        }
    }
}

@Composable
private fun AddOptionDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.add_new_option), fontWeight = FontWeight.Bold)
        },
        text = {
            GlassOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.option_name)) },
                placeholder = { Text(stringResource(R.string.option_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.getMediumShape(),
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )
        },
        confirmButton = {
            GlassTonalButton(
                onClick = { if (name.isNotBlank()) onAdd(name) },
                enabled = name.isNotBlank(),
                colors = AppTheme.colors.getPrimaryButtonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            GlassTonalButton(
                onClick = onDismiss,
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * AI 创建选项。
 *
 * 输入框里预填了一句能直接用的描述（见 [R.string.ai_create_filler]），
 * 生成结果先摊开给用户看过再决定要不要加 —— AI 会一本正经地胡说，直接落盘是不负责任的。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiCreateDialog(
    initialPrompt: String,
    generating: Boolean,
    suggestions: List<String>,
    error: String?,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit,
    onApply: () -> Unit
) {
    var text by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    val engineMissing = error?.contains("engine", ignoreCase = true) == true

    androidx.compose.material3.AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.ai_create_title), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassOutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.ai_create_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )

                if (generating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.ai_generating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!generating && error != null) {
                    Text(
                        text = stringResource(
                            if (engineMissing) R.string.ai_engine_not_configured
                            else R.string.ai_create_failed
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (suggestions.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GlassTonalButton(
                onClick = { if (suggestions.isEmpty()) onGenerate(text) else onApply() },
                enabled = !generating && text.isNotBlank(),
                colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = if (suggestions.isEmpty()) {
                        stringResource(R.string.ai_create_options)
                    } else {
                        stringResource(R.string.ai_add_count, suggestions.size)
                    }
                )
            }
        },
        dismissButton = {
            GlassTonalButton(
                onClick = onDismiss,
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun Float.trimText(): String =
    if (this % 1f == 0f) toInt().toString() else String.format(java.util.Locale.US, "%.1f", this)
