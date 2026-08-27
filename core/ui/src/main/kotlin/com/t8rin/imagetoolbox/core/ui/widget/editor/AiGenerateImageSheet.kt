package com.t8rin.imagetoolbox.core.ui.widget.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton

/**
 * AI 生成图片共享弹层(纯 UI 参数化,不依赖 core/base):
 * 登录/积分预检由各宿主在 [onGenerate] 外包。
 * [editImage] 非空 = 图生图编辑模式(标题/提示词占位切换 + 编辑历史条);
 * 空描述 toast 在内部处理,不回调。
 */
@Composable
fun AiGenerateImageSheet(
    visible: Boolean,
    title: String,
    editTitle: String,
    promptHint: String,
    editPromptHint: String,
    generateLabel: String,
    pointsHint: String,
    emptyHint: String,
    currentLabel: String,
    historyLabel: String,
    isGenerating: Boolean,
    editImage: AiEditImage?,
    onGenerate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var prompt by rememberSaveable { mutableStateOf("") }

    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (editImage != null) editTitle else title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            )
        },
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                // 编辑模式:当前图 + 历史版本横排(点历史版本回退,当前图进历史)
                editImage?.let { target ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        AiEditHistoryThumb(
                            uri = target.uri,
                            label = currentLabel,
                            highlighted = true,
                            onClick = {}
                        )
                        if (target.historyUris.isNotEmpty()) {
                            Text(
                                text = historyLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(target.historyUris.reversed()) { uri ->
                                    AiEditHistoryThumb(
                                        uri = uri,
                                        label = null,
                                        highlighted = false,
                                        onClick = { target.onRevert(uri) }
                                    )
                                }
                            }
                        }
                    }
                }
                GlassOutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(if (editImage != null) editPromptHint else promptHint)
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                OnePrimaryButton(
                    text = generateLabel,
                    onClick = {
                        if (prompt.isBlank()) {
                            // 空描述直接提示,不进登录/积分预检
                            AppToastHost.showToast(
                                message = emptyHint,
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                                duration = ToastDuration.Short
                            )
                            return@OnePrimaryButton
                        }
                        onGenerate(prompt)
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Text(
                    text = pointsHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp, bottom = 12.dp)
                )
            }
        }
    )
}

/** 图生图编辑目标:当前图 + 历史版本 + 回退动作 */
class AiEditImage(
    val uri: String,
    val historyUris: List<String>,
    val onRevert: (String) -> Unit,
)

/** 编辑历史缩略图:48dp 圆角 Crop,当前版主色描边;label 非空时下方带小字 */
@Composable
private fun AiEditHistoryThumb(
    uri: String,
    label: String?,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Picture(
            model = uri,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            showTransparencyChecker = false,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (highlighted) 2.dp else 1.dp,
                    color = if (highlighted) {
                        MaterialTheme.colorScheme.primary
                    } else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
        )
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
