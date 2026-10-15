package com.wanbaohe.textcard.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.textcard.domain.render.CardLayout
import java.util.UUID

/** 元素整体变换:归一化偏移 + 缩放 + 旋转(度),与字号等基础样式叠乘,互不冲突 */
interface ElementTransform {
    val offsetX: Float
    val offsetY: Float
    val scale: Float
    val rotation: Float
}

/**
 * 卡片上的文字块(任意多条,默认标题 + 正文两块;每条一个图层)。
 *
 * @param id 稳定 id,图层/选中/编辑/拖动都按它寻址
 * @param baseTopRatio 基准 top(相对画布宽),新块在上一块基础上递增
 * @param sizeScale 字号缩放倍率,作用于块的基础字号比例 [baseSizeRatio](与手势 scale 叠乘)
 * @param widthRatio 文字框宽(相对画布宽),文字在框内折行;拖框手柄改它,字号不变。
 * [widthManuallySet]=false 时它是框宽上限,框随文字宽度收缩;拖宽后为固定框宽
 * @param widthManuallySet 是否被 8 向手柄手动拖宽过:false = 框宽随内容收缩(wrap),true = 固定 [widthRatio] 宽
 * @param heightRatio 文字框最小高(相对画布高),0 = 随内容自适应;实际框高 = max(内容高, 设定高)
 * @param letterSpacingEm 字间距(em),直接给 TextPaint.letterSpacing / Compose letterSpacing
 * @param lineSpacingMultiplier 行距倍率,StaticLayout setLineSpacing 的 multiplier
 * @param offsetX 相对基准位置的归一化水平偏移(相对画布宽)
 * @param offsetY 相对基准位置的归一化垂直偏移(相对画布高)
 * @param scale 手势整体缩放(绕内容中心)
 * @param rotation 手势旋转(度,绕内容中心)
 * @param alpha 元素级不透明度(0..1),与颜色自身 alpha 叠乘
 * @param isUnderline 下划线(块级;选区级局部样式不含它,选区激活时面板置灰)
 * @param isStrikethrough 删除线(块级,同上)
 * @param backgroundColor 文字底色(逐行高亮,0 = 无背景;预览 TextStyle.background,导出 BackgroundColorSpan)
 * @param outlineColor 描边颜色([outlineWidthRatio] > 0 时生效)
 * @param outlineWidthRatio 描边粗细(相对画布宽的比例),0 = 无描边;
 * 预览经 OutlinedText 双线叠绘(描边层垫底),导出经 STROKE 画笔二遍绘制
 * @param styleSpans 块内局部样式区间(字符下标;字段 null = 继承块级样式),
 * 由文字设置面板的选区作用域产生;编辑中文字增删由 Compose TrackedRange 跟随迁移。
 * 不变量:同属性区间互斥(写入侧 applySpanStyle 保证)——em 字号在 Android 端
 * 转 RelativeSizeSpan,重叠区间会相乘而非覆盖,互斥是防止字号爆炸的前提
 */
data class TextBlock(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val baseSizeRatio: Float = CardLayout.BODY_BASE_SIZE_RATIO,
    val baseTopRatio: Float = CardLayout.BODY_BASE_TOP_RATIO,
    val font: FontType? = null,
    val sizeScale: Float = 1f,
    val widthRatio: Float = CardLayout.DEFAULT_TEXT_WIDTH_RATIO,
    val widthManuallySet: Boolean = false,
    val heightRatio: Float = 0f,
    val letterSpacingEm: Float = 0f,
    val lineSpacingMultiplier: Float = 1.2f,
    val alignment: CardTextAlignment = CardTextAlignment.Left,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val color: Long = 0xFF1A1A1A,
    val alpha: Float = 1f,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val backgroundColor: Long = 0,
    val outlineColor: Long = 0xFFFFFFFF,
    val outlineWidthRatio: Float = 0f,
    val styleSpans: List<TextStyleSpan> = emptyList(),
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform

/**
 * 块内局部样式区间(富文本):[start, end) 为 content 字符下标;
 * 四个字段均为可空覆盖,null = 继承块级(块粗体时 bold=false 表示该段反粗)。
 * [sizeScale] 为相对块级字号的倍率(0.5..4,编辑器/预览经 em 单位实现,导出经 RelativeSizeSpan)。
 * 同属性区间互斥(见 [TextBlock.styleSpans] 说明);异属性区间重叠时后加的生效。
 */
data class TextStyleSpan(
    val start: Int,
    val end: Int,
    val color: Long? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val sizeScale: Float? = null,
) {
    /** 钳制到 [length] 内;退化为空区间返回 null(调用方过滤) */
    fun clamped(length: Int): TextStyleSpan? {
        val s = start.coerceIn(0, length)
        val e = end.coerceIn(0, length)
        return if (s < e) copy(start = s, end = e) else null
    }

    /** 转 Compose SpanStyle(仅设置非空字段;字号倍率用 em,相对块级 textStyle 字号) */
    fun toSpanStyle(): SpanStyle = SpanStyle(
        color = color?.let(::Color) ?: Color.Unspecified,
        fontSize = sizeScale?.em ?: TextUnit.Unspecified,
        fontWeight = bold?.let { if (it) FontWeight.Bold else FontWeight.Normal },
        fontStyle = italic?.let { if (it) FontStyle.Italic else FontStyle.Normal }
    )
}

/** 编辑器样式层读回(getSpanStyles)→ 模型 span;四字段全空(未设置)或空区间返回 null */
fun AnnotatedString.Range<SpanStyle>.toTextStyleSpan(): TextStyleSpan? {
    if (start >= end) return null
    val spanColor = item.color
        .takeIf { it != Color.Unspecified }
        ?.let { it.toArgb().toLong() and 0xFFFF_FFFFL }
    val spanSize = item.fontSize.takeIf { it.isEm }?.value
    val spanBold = when (item.fontWeight) {
        FontWeight.Bold -> true
        FontWeight.Normal -> false
        else -> null
    }
    val spanItalic = when (item.fontStyle) {
        FontStyle.Italic -> true
        FontStyle.Normal -> false
        else -> null
    }
    if (spanColor == null && spanSize == null && spanBold == null && spanItalic == null) return null
    return TextStyleSpan(
        start = start,
        end = end,
        color = spanColor,
        bold = spanBold,
        italic = spanItalic,
        sizeScale = spanSize
    )
}

/** 预览渲染用:content + 局部样式叠加成 AnnotatedString(块级粗斜由 textStyle 承担,这里只叠加区间覆盖) */
fun TextBlock.buildAnnotatedContent(): AnnotatedString = buildAnnotatedString {
    append(content)
    styleSpans.forEach { span ->
        span.clamped(content.length)?.let { clamped ->
            addStyle(clamped.toSpanStyle(), clamped.start, clamped.end)
        }
    }
}

/**
 * endTextEdit trim 时同步调整局部样式区间:平移前导空白,钳制到新长度,丢弃空区间;
 * 内容被替换成占位文案(trimmed 与原内容无关联)时直接清空。
 */
fun List<TextStyleSpan>.adjustForTrim(original: String, trimmed: String): List<TextStyleSpan> {
    if (isEmpty()) return this
    if (trimmed.isEmpty() || !original.contains(trimmed)) return emptyList()
    val lead = original.indexOf(trimmed)
    return mapNotNull { span ->
        span.copy(start = span.start - lead, end = span.end - lead).clamped(trimmed.length)
    }
}

/** 选区当前有效加粗态(块级为底,区间覆盖):工具栏 B 切换判定。选区非法/塌陷时回退块级 */
fun TextBlock.isRangeEffectivelyBold(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, blockLevel = isBold) { it.bold }

/** 选区当前有效斜体态,同 [isRangeEffectivelyBold] */
fun TextBlock.isRangeEffectivelyItalic(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, blockLevel = isItalic) { it.italic }

/** 选区当前有效颜色(最后一个覆盖选区且带颜色的 span,否则块级颜色):工具栏色板当前值 */
fun TextBlock.effectiveColorAt(start: Int, end: Int): Long =
    styleSpans.lastOrNull { it.color != null && it.start < end && it.end > start }?.color ?: color

/** 选区当前有效字号倍率(最后一个覆盖选区且带倍率的 span,否则 1):工具栏字号步进基准 */
fun TextBlock.effectiveSizeScaleAt(start: Int, end: Int): Float =
    styleSpans.lastOrNull { it.sizeScale != null && it.start < end && it.end > start }
        ?.sizeScale ?: 1f

/** 有效样式判定:以覆盖选区的最后一个 span 为准(追加式语义,后加覆盖先加) */
private fun TextBlock.isRangeEffectivelyStyled(
    start: Int,
    end: Int,
    blockLevel: Boolean,
    field: (TextStyleSpan) -> Boolean?,
): Boolean {
    if (start >= end) return blockLevel
    val lastCovering = styleSpans.lastOrNull { it.start <= start && it.end >= end }
        ?: styleSpans.lastOrNull { it.start < end && it.end > start }
    return lastCovering?.let(field) ?: blockLevel
}

enum class CardTextAlignment {
    Left, Center, Right, Justify
}

/**
 * 装饰贴纸(支持多个;每个装饰一个图层,可独立拖动/缩放/旋转)。
 * 来源:emoji(core emoji 表下标)、素材贴纸(assetPath,assets/stickers/ 下的 SVG)
 * 或 AI 生成贴纸(imagePath,本地文件绝对路径);三者恰有一个非空,
 * 与图片创作共享 StickerSource 落地。
 * offsetX/offsetY 为贴纸左上角的归一化坐标(X 相对画布宽、Y 相对画布高)。
 * alpha 为元素级不透明度(0..1)。
 */
data class DecorationSpec(
    val id: String = UUID.randomUUID().toString(),
    val emojiIndex: Int? = null,
    /** 素材贴纸:assets 内相对路径(如 stickers/decor/flower.svg) */
    val assetPath: String? = null,
    /** AI 生成贴纸:本地文件绝对路径 */
    val imagePath: String? = null,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform {
    companion object {
        /** 新装饰默认落左上角(留 DECORATION_MARGIN_RATIO 边距,不贴边) */
        fun defaultPositionFor(canvas: CanvasSpec, emojiIndex: Int): DecorationSpec {
            val margin = CardLayout.DECORATION_MARGIN_RATIO
            return DecorationSpec(
                emojiIndex = emojiIndex,
                offsetX = margin,
                offsetY = margin * canvas.aspectRatio
            )
        }
    }
}

/**
 * AI 生成图片元素(支持多个;每个一个图层,可独立拖动/缩放/旋转,同装饰)。
 * offsetX/offsetY 为元素左上角的归一化坐标(X 相对画布宽、Y 相对画布高),
 * 图片在 IMAGE_ELEMENT_SIZE_RATIO 正方形框内 fit 居中。
 *
 * 生成是异步的:发起时先加 [ImageElementStatus.Loading] 占位图层(uri 为空),
 * 用户可继续其它操作;成功回填 uri 转 [ImageElementStatus.Ready],失败转
 * [ImageElementStatus.Error](错误态直接画在图层上,可删除)。导出只画 Ready。
 */
data class ImageElementSpec(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val status: ImageElementStatus = ImageElementStatus.Ready,
    /** true = 元素铺满整张画布(生成背景图场景),图层垫底于文字之下 */
    val fullCanvas: Boolean = false,
    /** AI 编辑历史:历次编辑前的图片本地路径(旧→新) */
    val historyUris: List<String> = emptyList(),
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform {
    companion object {
        /** 新图片元素默认落画布中心 */
        fun defaultPositionFor(canvas: CanvasSpec, uri: String): ImageElementSpec {
            val size = CardLayout.IMAGE_ELEMENT_SIZE_RATIO
            return ImageElementSpec(
                uri = uri,
                offsetX = 0.5f - size / 2,
                offsetY = 0.5f - size * canvas.aspectRatio / 2
            )
        }

        /** 铺满画布的图片元素(占位/生成背景图用),offset 0 起点 */
        fun fullCanvas(uri: String, status: ImageElementStatus): ImageElementSpec =
            ImageElementSpec(uri = uri, status = status, fullCanvas = true)
    }
}

enum class ImageElementStatus {
    Loading, Ready, Error
}
