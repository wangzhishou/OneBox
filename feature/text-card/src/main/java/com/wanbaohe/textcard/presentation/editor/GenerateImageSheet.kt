package com.wanbaohe.textcard.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiEditImage
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiGenerateImageSheet
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.AI_IMAGE_POINTS_SOURCE
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * AI 生成图片 Sheet(薄壳):UI 复用 core/ui 共享 [AiGenerateImageSheet],
 * 这里只做文案装配与登录+积分预检包装(通过后才真正生成;
 * 占位图层落地即关弹窗(onStarted),积分在生成成功后由组件扣除)。
 * 选中 Ready 图片元素时进入「AI 编辑图片」图生图模式(带历史回退)。
 */
@Composable
fun GenerateImageSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    val editTarget = component.selectedReadyImageElement()

    AiGenerateImageSheet(
        visible = visible,
        title = stringResource(R.string.textcard_add_image_layer),
        editTitle = stringResource(R.string.textcard_edit_image_title),
        promptHint = stringResource(R.string.textcard_generate_image_hint),
        editPromptHint = stringResource(R.string.textcard_edit_image_hint),
        generateLabel = stringResource(R.string.textcard_generate_image_action),
        pointsHint = stringResource(
            R.string.textcard_generate_points_hint,
            aiImageProcessPointsCost()
        ),
        emptyHint = stringResource(R.string.textcard_generate_image_empty),
        currentLabel = stringResource(R.string.textcard_edit_image_current),
        historyLabel = stringResource(R.string.textcard_edit_image_history),
        isGenerating = component.isGeneratingImage,
        editImage = editTarget?.let { target ->
            AiEditImage(
                uri = target.uri,
                historyUris = target.historyUris,
                onRevert = { uri -> component.revertImageElement(target.id, uri) }
            )
        },
        onGenerate = { prompt ->
            // 登录 + 积分预检(同 markup-layers AI 能力):通过后才真正生成
            ActionUtils.ensureLoginAndCheckPoints(
                source = AI_IMAGE_POINTS_SOURCE,
                point = aiImageProcessPointsCost()
            ) {
                component.generateImageLayer(prompt, onStarted = onDismiss)
            }
        },
        onDismiss = onDismiss
    )
}
