package com.shifenmiao.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.settings.presentation.model.PicturePickerMode
import com.t8rin.imagetoolbox.core.ui.utils.GridFlowRowArrangement
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.Lightbulb
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEnergyLeaf


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoreInputSection(
    appComponent: AppComponent,
    chatInputComponent: ChatInputComponent,
    enabledToolCount: Int = 0,
    onShowToolCenter: () -> Unit = {},
) {
    val context = LocalContext.current
    val currentAIModel by appComponent.aiEngineManager.currentAIModel.collectAsState()
    val isEnableWebSearch by AIChatStorage.isEnableWebSearch.collectAsState()
    val inputState by chatInputComponent.chatInputState.collectAsState()

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
        // 第一区域：模型选择 + 功能开关, 功能开关：最多2个，两端对齐
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = GridFlowRowArrangement(fullCount = 2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            ModelSelectorChip(
                modifier = Modifier.width(160.dp),
                modelTitle = currentAIModel.title.ifBlank { currentAIModel.name },
                onClick = {
                    chatInputComponent.showModelPicker()
                }
            )
            if (currentAIModel.canNetwork) {
                FeatureToggleChip(
                    modifier = Modifier.width(160.dp),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
                    title = stringResource(R.string.network_search),
                    isEnabled = isEnableWebSearch,
                    onClick = {
                        AIChatStorage.saveIsEnableWebSearch(!isEnableWebSearch)
                    }
                )
            }
            if (currentAIModel.canReasoning) {
                FeatureToggleChip(
                    modifier = Modifier.width(160.dp),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb,
                    title = stringResource(R.string.ai_reasoning),
                    isEnabled = AIChatStorage.isEnableReasoning.collectAsState().value,
                    onClick = {
                        AIChatStorage.saveIsEnableReasoning(!AIChatStorage.isEnableReasoning.value)
                    }
                )
            }
            // 图片压缩开关（模型支持图片时始终显示）
            if (currentAIModel.canImage) {
                FeatureToggleChip(
                    modifier = Modifier.width(160.dp),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                    title = stringResource(R.string.ai_input_image_compression),
                    isEnabled = inputState.enableImageCompression,
                    onClick = {
                        chatInputComponent.toggleImageCompression()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 第二区域：功能操作按钮（FlowRow 自动换行）
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = GridFlowRowArrangement(fullCount = 4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 4
        ) {
            // 提示词
            CircleActionButton(
                icon = {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = stringResource(R.string.ai_input_prompts),
                onClick = { chatInputComponent.showPromptPicker() }
            )
            // 图片选择
            if (currentAIModel.canImage || currentAIModel.canUploadFile) {
                CircleActionButton(
                    icon = {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = stringResource(R.string.ai_input_image),
                    onClick = { imagePicker.pickFile() }
                )
            }
            // 拍照
            if (currentAIModel.canImage || currentAIModel.canUploadFile) {
                CircleActionButton(
                    icon = {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = stringResource(R.string.ai_input_take_photo),
                    onClick = {
                        cameraImagePicker.pickImageWithMode(
                            picker = Picker.Single,
                            picturePickerMode = PicturePickerMode.CameraCapture
                        )
                    }
                )
            }
            // 文件选择
            if (currentAIModel.canUploadFile) {
                CircleActionButton(
                    icon = {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = stringResource(R.string.ai_input_file),
                    onClick = { filePicker.pickFile() }
                )
            }
            // 最近
            CircleActionButton(
                icon = {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = stringResource(R.string.ai_input_recent),
                onClick = { chatInputComponent.showRecentPicker() }
            )
            // 工具
            CircleActionButton(
                icon = {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = if (enabledToolCount > 0) {
                    stringResource(R.string.ai_input_tools_with_count, enabledToolCount)
                } else {
                    stringResource(R.string.ai_input_tools)
                },
                onClick = onShowToolCenter
            )
            // 系统
            CircleActionButton(
                icon = {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = stringResource(R.string.ai_input_system),
                onClick = { appComponent.showAIChatSettings() }
            )

        }
    }
}

@Composable
private fun CircleActionButton(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .glassBackground(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 模型选择 Chip：🌱 + 模型名(小字体、ellipsis、限宽) + ▼
 */
@Composable
private fun ModelSelectorChip(
    modifier: Modifier = Modifier,
    modelTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEnergyLeaf,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = modelTitle,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
            contentDescription = "Select model",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * 功能切换 Chip：单行展示图标 + 标题 + Switch。
 */
@Composable
private fun FeatureToggleChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
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
}
