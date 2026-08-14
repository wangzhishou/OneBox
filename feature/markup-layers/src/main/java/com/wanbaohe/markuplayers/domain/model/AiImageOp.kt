package com.wanbaohe.markuplayers.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.core.constants.UrlConstants
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAir
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAspectRatio
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContrast
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDocumentScanner
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGridOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHealing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHighQuality
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.wanbaohe.markuplayers.R

/**
 * 百度 AI 图像处理能力(经 Go 网关代理,详见
 * [com.shifenmiao.network.api.BaiduImageProcessApiService])。
 * 顺序即「AI 处理」面板展示顺序;[needsRect] 为 true 的能力(图像修复)
 * 需先框选区域,归一化矩形随请求换算为图片像素坐标。
 */
enum class AiImageOp(
    val path: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val icon: ImageVector,
    val needsRect: Boolean = false,
) {
    Dehaze(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_DEHAZE_PATH,
        nameRes = R.string.markup_ai_dehaze,
        descRes = R.string.markup_ai_dehaze_sub,
        icon = Icons.Outlined.LineAir
    ),
    ContrastEnhance(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_CONTRAST_ENHANCE_PATH,
        nameRes = R.string.markup_ai_contrast_enhance,
        descRes = R.string.markup_ai_contrast_enhance_sub,
        icon = Icons.Outlined.LineContrast
    ),
    QualityEnhance(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_QUALITY_ENHANCE_PATH,
        nameRes = R.string.markup_ai_quality_enhance,
        descRes = R.string.markup_ai_quality_enhance_sub,
        icon = Icons.Outlined.LineHighQuality
    ),
    StretchRestore(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_STRETCH_RESTORE_PATH,
        nameRes = R.string.markup_ai_stretch_restore,
        descRes = R.string.markup_ai_stretch_restore_sub,
        icon = Icons.Outlined.LineAspectRatio
    ),
    Inpainting(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_INPAINTING_PATH,
        nameRes = R.string.markup_ai_inpainting,
        descRes = R.string.markup_ai_inpainting_sub,
        icon = Icons.Outlined.LineHealing,
        needsRect = true
    ),
    DefinitionEnhance(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_DEFINITION_ENHANCE_PATH,
        nameRes = R.string.markup_ai_definition_enhance,
        descRes = R.string.markup_ai_definition_enhance_sub,
        icon = Icons.Outlined.LineAutoFix
    ),
    ColorEnhance(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_COLOR_ENHANCE_PATH,
        nameRes = R.string.markup_ai_color_enhance,
        descRes = R.string.markup_ai_color_enhance_sub,
        icon = Icons.Outlined.LinePaletteTools
    ),
    RemoveMoire(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_REMOVE_MOIRE_PATH,
        nameRes = R.string.markup_ai_remove_moire,
        descRes = R.string.markup_ai_remove_moire_sub,
        icon = Icons.Outlined.LineGridOn
    ),
    DocRepair(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_DOC_REPAIR_PATH,
        nameRes = R.string.markup_ai_doc_repair,
        descRes = R.string.markup_ai_doc_repair_sub,
        icon = Icons.Outlined.LineDocumentScanner
    ),
    Segment(
        path = UrlConstants.BAIDU_IMAGE_PROCESS_SEGMENT_PATH,
        nameRes = R.string.markup_ai_segment,
        descRes = R.string.markup_ai_segment_sub,
        icon = Icons.Outlined.LineContentCut
    ),
}
