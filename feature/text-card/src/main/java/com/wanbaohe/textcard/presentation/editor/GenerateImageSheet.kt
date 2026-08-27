package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.AI_IMAGE_POINTS_SOURCE
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * AI 生成图片 Sheet(走 core:image-generation 的活动配置,默认代理通道):
 * 输入描述 → 生成 → 成功后新增为图片图层并关弹层;生成中按钮禁用,
 * 全屏 LoadingDialog 由编辑页统一挂(可取消)。
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
                        text = stringResource(R.string.textcard_add_image_layer),
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
                GlassOutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(stringResource(R.string.textcard_generate_image_hint))
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
                        // 积分在生成成功后由组件扣除,失败不扣
                        ActionUtils.ensureLoginAndCheckPoints(
                            source = AI_IMAGE_POINTS_SOURCE,
                            point = aiImageProcessPointsCost()
                        ) {
                            component.generateImageLayer(prompt, onSuccess = onDismiss)
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
