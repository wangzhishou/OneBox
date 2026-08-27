package com.wanbaohe.textcard.presentation.editor

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.AI_IMAGE_POINTS_SOURCE
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * AI 生成图片 Sheet(走 core:image-generation 的活动配置,默认代理通道):
 * 输入描述 → 生成 → 预检通过后画布立即落一个 Loading 占位图层并关闭弹层,
 * 不阻塞其它操作;生成中再次打开本弹层,生成按钮保持禁用。
 * 结果(成功换图/失败标错)由组件写回占位图层。
 */
@Composable
fun GenerateImageSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var prompt by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    // 选中了 Ready 图片元素 → 「AI 编辑图片」图生图模式(带编辑历史回退)
    val editTarget = component.selectedReadyImageElement()

    EnhancedModalBottomSheet(
        visible = true,
        // 标题栏与面板弹层(EditorPanelSheet)同款:居中标题 + 右侧关闭
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
                        text = stringResource(
                            if (editTarget != null) {
                                R.string.textcard_edit_image_title
                            } else R.string.textcard_add_image_layer
                        ),
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
                editTarget?.let { target ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        HistoryThumb(
                            uri = target.uri,
                            label = stringResource(R.string.textcard_edit_image_current),
                            highlighted = true,
                            onClick = {}
                        )
                        if (target.historyUris.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.textcard_edit_image_history),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(target.historyUris.reversed()) { uri ->
                                    HistoryThumb(
                                        uri = uri,
                                        label = null,
                                        highlighted = false,
                                        onClick = {
                                            component.revertImageElement(target.id, uri)
                                        }
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
                        Text(
                            stringResource(
                                if (editTarget != null) {
                                    R.string.textcard_edit_image_hint
                                } else R.string.textcard_generate_image_hint
                            )
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryButton(
                    text = stringResource(R.string.textcard_generate_image_action),
                    onClick = {
                        if (prompt.isBlank()) {
                            // 空描述直接提示,不进登录/积分预检
                            AppToastHost.showToast(
                                message = context.getString(R.string.textcard_generate_image_empty),
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                                duration = ToastDuration.Short
                            )
                            return@PrimaryButton
                        }
                        // 登录 + 积分预检(同 markup-layers AI 能力):通过后才真正生成;
                        // 占位图层落地即关弹窗(onStarted),积分在生成成功后由组件扣除
                        ActionUtils.ensureLoginAndCheckPoints(
                            source = AI_IMAGE_POINTS_SOURCE,
                            point = aiImageProcessPointsCost()
                        ) {
                            component.generateImageLayer(prompt, onStarted = onDismiss)
                        }
                    },
                    enable = !component.isGeneratingImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                // 单次消耗提示(值跟随远程配置 aiImageProcessPoints,默认 200)
                Text(
                    text = stringResource(
                        R.string.textcard_generate_points_hint,
                        aiImageProcessPointsCost()
                    ),
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

/** 编辑历史缩略图:48dp 圆角 Crop,当前版主色描边;label 非空时下方带小字 */
@Composable
private fun HistoryThumb(
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
