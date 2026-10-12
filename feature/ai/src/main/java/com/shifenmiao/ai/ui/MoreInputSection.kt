package com.shifenmiao.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.ai.providerAccentColor
import com.shifenmiao.common.ui.ai.providerBrandIcon
import com.shifenmiao.core.R
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.settings.presentation.model.PicturePickerMode
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBuild
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.Lightbulb
import com.t8rin.imagetoolbox.core.resources.icons.Refresh


@Composable
fun MoreInputSection(
    appComponent: AppComponent,
    chatInputComponent: ChatInputComponent,
    enabledToolCount: Int = 0,
    onShowToolCenter: () -> Unit = {},
) {
    val context = LocalContext.current
    val currentAIModel by appComponent.aiEngineManager.currentAIModel.collectAsState()
    val currentAIEngine by appComponent.aiEngineManager.currentAIEngine.collectAsState()
    val isEnableWebSearch by AIChatStorage.isEnableWebSearch.collectAsState()
    val isEnableReasoning by AIChatStorage.isEnableReasoning.collectAsState()
    val inputState by chatInputComponent.chatInputState.collectAsState()
    val loadingLocalModel by chatInputComponent.localLlmLoadingModelName.collectAsState()

    val imagePicker = rememberFilePicker(
        type = FileType.Multiple,
        mimeType = MimeType.UploadImage
    ) { uris ->
        uris.forEach { uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            chatInputComponent.addAttachment(uri, mimeType)
        }
    }

    // 文件选择器
    val filePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.All
    ) { uris ->
        uris.forEach { uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            chatInputComponent.addAttachment(uri, mimeType)
        }
    }

    // 拍照（系统相机）
    val cameraImagePicker = rememberImagePicker(picker = Picker.Single) { uris ->
        uris.forEach { uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "image/*"
            chatInputComponent.addAttachment(uri, mimeType)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        // 端侧模型加载提示:prepare(预热或首次推理前)需要数秒,把不可见的等待变成可解释的等待
        loadingLocalModel?.let { modelName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.local_model_loading_hint, modelName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val optionItems = buildList {
            add(
                MoreInputOption.Model(
                    title = currentAIModel.title.ifBlank { currentAIModel.name },
                    providerValue = currentAIModel.provider.value,
                    fallbackIconName = currentAIEngine.iconName,
                    onClick = { chatInputComponent.showModelPicker() }
                )
            )
            if (currentAIModel.canNetwork) {
                add(
                    MoreInputOption.Toggle(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
                        title = stringResource(R.string.network_search),
                        isEnabled = isEnableWebSearch,
                        onClick = { AIChatStorage.saveIsEnableWebSearch(!isEnableWebSearch) }
                    )
                )
            }
            if (currentAIModel.canReasoning) {
                add(
                    MoreInputOption.Toggle(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb,
                        title = stringResource(R.string.ai_reasoning),
                        isEnabled = isEnableReasoning,
                        onClick = { AIChatStorage.saveIsEnableReasoning(!isEnableReasoning) }
                    )
                )
            }
            if (currentAIModel.canImage) {
                add(
                    MoreInputOption.Toggle(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                        title = stringResource(R.string.ai_input_image_compression),
                        isEnabled = inputState.enableImageCompression,
                        onClick = { chatInputComponent.toggleImageCompression() }
                    )
                )
            }
        }
        EqualColumnGrid(
            items = optionItems,
            columns = 2,
            horizontalGap = 8.dp,
            verticalGap = 8.dp,
        ) { option, cellModifier ->
            when (option) {
                is MoreInputOption.Model -> ModelSelectorChip(
                    modifier = cellModifier,
                    modelTitle = option.title,
                    providerValue = option.providerValue,
                    fallbackIconName = option.fallbackIconName,
                    onClick = option.onClick
                )
                is MoreInputOption.Toggle -> FeatureToggleChip(
                    modifier = cellModifier,
                    icon = option.icon,
                    title = option.title,
                    isEnabled = option.isEnabled,
                    onClick = option.onClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val canPickMedia = currentAIModel.canImage || currentAIModel.canUploadFile
        val actionItems = buildList {
            add(
                PanelAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb,
                    label = stringResource(R.string.ai_input_prompts),
                    onClick = { chatInputComponent.showPromptPicker() }
                )
            )
            if (canPickMedia) {
                add(
                    PanelAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                        label = stringResource(R.string.ai_input_image),
                        onClick = { imagePicker.pickFile() }
                    )
                )
                add(
                    PanelAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraAlt,
                        label = stringResource(R.string.ai_input_take_photo),
                        onClick = {
                            cameraImagePicker.pickImageWithMode(
                                picker = Picker.Single,
                                picturePickerMode = PicturePickerMode.CameraCapture
                            )
                        }
                    )
                )
            }
            if (currentAIModel.canUploadFile) {
                add(
                    PanelAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                        label = stringResource(R.string.ai_input_file),
                        onClick = { filePicker.pickFile() }
                    )
                )
            }
            add(
                PanelAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    label = stringResource(R.string.ai_input_recent),
                    onClick = { chatInputComponent.showRecentPicker() }
                )
            )
            add(
                PanelAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBuild,
                    label = if (enabledToolCount > 0) {
                        stringResource(R.string.ai_input_tools_with_count, enabledToolCount)
                    } else {
                        stringResource(R.string.ai_input_tools)
                    },
                    onClick = onShowToolCenter
                )
            )
            add(
                PanelAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    label = stringResource(R.string.ai_input_system),
                    onClick = { appComponent.showAIChatSettings() }
                )
            )
        }
        EqualColumnGrid(
            items = actionItems,
            columns = 4,
            horizontalGap = 4.dp,
            verticalGap = 12.dp,
        ) { action, cellModifier ->
            CircleActionButton(
                modifier = cellModifier,
                icon = action.icon,
                text = action.label,
                onClick = action.onClick
            )
        }
    }
}

private sealed interface MoreInputOption {
    data class Model(
        val title: String,
        val providerValue: String,
        val fallbackIconName: String,
        val onClick: () -> Unit,
    ) : MoreInputOption

    data class Toggle(
        val icon: ImageVector,
        val title: String,
        val isEnabled: Boolean,
        val onClick: () -> Unit,
    ) : MoreInputOption
}

private data class PanelAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

/**
 * 等宽网格：每行固定 [columns] 列，末行不足时用空白补齐，列与列对齐。
 */
@Composable
private fun <T> EqualColumnGrid(
    items: List<T>,
    columns: Int,
    horizontalGap: Dp,
    verticalGap: Dp,
    cell: @Composable (item: T, modifier: Modifier) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(verticalGap)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(horizontalGap),
            ) {
                rowItems.forEach { item ->
                    cell(
                        item,
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CircleActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .glassBackground(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 模型选择：左侧是当前模型的厂商标，右侧是切换按钮，模型名独占下方。
 */
@Composable
private fun ModelSelectorChip(
    modifier: Modifier = Modifier,
    modelTitle: String,
    providerValue: String,
    fallbackIconName: String,
    onClick: () -> Unit
) {
    InputOptionCard(modifier = modifier, onClick = onClick) {
        OptionHeaderRow {
            ModelBrandMark(
                providerValue = providerValue,
                fallbackIconName = fallbackIconName,
                monogram = modelTitle,
            )
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.ai_chat_model_select),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = modelTitle,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 当前模型的厂商标。没有收录的厂商用引擎图标，再没有就用名称首字母。
 */
@Composable
private fun ModelBrandMark(
    providerValue: String,
    fallbackIconName: String,
    monogram: String,
) {
    val brandIcon = providerBrandIcon(providerValue)
    val accent = providerAccentColor(providerValue)
        ?: MaterialTheme.colorScheme.onPrimaryContainer
    when {
        brandIcon != null -> Icon(
            imageVector = brandIcon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
        )
        else -> {
            val fallback = IconRegistry.resolve(fallbackIconName)
            if (fallback != null) {
                Icon(
                    imageVector = fallback,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = monogram.filter { it.isLetterOrDigit() }.take(1).ifBlank { "?" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = accent,
                )
            }
        }
    }
}

/**
 * 功能开关：图标和开关在上，标题独占下方，长文案按词折成两行。
 */
@Composable
private fun FeatureToggleChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    InputOptionCard(modifier = modifier, onClick = onClick) {
        OptionHeaderRow {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            GlassSwitch(
                checked = isEnabled,
                onCheckedChange = { onClick() },
                thumbContent = {
                    if (isEnabled) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
                colors = AppTheme.colors.switchColors()
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InputOptionCard(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content,
    )
}

@Composable
private fun OptionHeaderRow(
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        content = { content() },
    )
}
