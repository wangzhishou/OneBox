package com.wanbaohe.markuplayers.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.model.imageprocess.ImageProcessOp
import com.shifenmiao.model.imageprocess.ImageProcessRect
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
 * 「AI 处理」面板能力项:core 侧 [ImageProcessOp] 的 UI 映射
 * (文案/图标/是否需框选),实际执行走 core/network 的 BaiduImageProcessRepository。
 * 顺序即面板展示顺序;[needsRect] 为 true 的能力(图像修复)需先框选区域,
 * 归一化矩形随请求换算为图片像素坐标。
 */
enum class AiImageOp(
    val processOp: ImageProcessOp,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val icon: ImageVector,
    val needsRect: Boolean = false,
) {
    Dehaze(
        processOp = ImageProcessOp.Dehaze,
        nameRes = R.string.markup_ai_dehaze,
        descRes = R.string.markup_ai_dehaze_sub,
        icon = Icons.Outlined.LineAir
    ),
    ContrastEnhance(
        processOp = ImageProcessOp.ContrastEnhance,
        nameRes = R.string.markup_ai_contrast_enhance,
        descRes = R.string.markup_ai_contrast_enhance_sub,
        icon = Icons.Outlined.LineContrast
    ),
    QualityEnhance(
        processOp = ImageProcessOp.QualityEnhance,
        nameRes = R.string.markup_ai_quality_enhance,
        descRes = R.string.markup_ai_quality_enhance_sub,
        icon = Icons.Outlined.LineHighQuality
    ),
    StretchRestore(
        processOp = ImageProcessOp.StretchRestore,
        nameRes = R.string.markup_ai_stretch_restore,
        descRes = R.string.markup_ai_stretch_restore_sub,
        icon = Icons.Outlined.LineAspectRatio
    ),
    Inpainting(
        processOp = ImageProcessOp.Inpainting,
        nameRes = R.string.markup_ai_inpainting,
        descRes = R.string.markup_ai_inpainting_sub,
        icon = Icons.Outlined.LineHealing,
        needsRect = true
    ),
    DefinitionEnhance(
        processOp = ImageProcessOp.DefinitionEnhance,
        nameRes = R.string.markup_ai_definition_enhance,
        descRes = R.string.markup_ai_definition_enhance_sub,
        icon = Icons.Outlined.LineAutoFix
    ),
    ColorEnhance(
        processOp = ImageProcessOp.ColorEnhance,
        nameRes = R.string.markup_ai_color_enhance,
        descRes = R.string.markup_ai_color_enhance_sub,
        icon = Icons.Outlined.LinePaletteTools
    ),
    RemoveMoire(
        processOp = ImageProcessOp.RemoveMoire,
        nameRes = R.string.markup_ai_remove_moire,
        descRes = R.string.markup_ai_remove_moire_sub,
        icon = Icons.Outlined.LineGridOn
    ),
    DocRepair(
        processOp = ImageProcessOp.DocRepair,
        nameRes = R.string.markup_ai_doc_repair,
        descRes = R.string.markup_ai_doc_repair_sub,
        icon = Icons.Outlined.LineDocumentScanner
    ),
    Segment(
        processOp = ImageProcessOp.Segment,
        nameRes = R.string.markup_ai_segment,
        descRes = R.string.markup_ai_segment_sub,
        icon = Icons.Outlined.LineContentCut
    ),
}

/** 框选矩形转 core 侧图像处理归一化矩形(坐标语义一致,仅类型不同) */
internal fun NormalizedRect.toImageProcessRect() =
    ImageProcessRect(left = left, top = top, right = right, bottom = bottom)

// AI 图像处理单次积分成本函数已下沉至 core/base:
// com.shifenmiao.base.utils.aiImageProcessPointsCost(供各模块共用)
