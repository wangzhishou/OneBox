package com.wanbaohe.textcard.domain.render

/**
 * 卡片排版几何与颜色常量:预览(Compose)与导出(android Canvas)双端共用。
 * 所有比例为相对画布宽度的比值。
 */
object CardLayout {

    /** 内容区四周内边距(相对画布宽) */
    const val CONTENT_PADDING_RATIO = 0.075f

    /** 标题基础字号(相对画布宽) */
    const val TITLE_BASE_SIZE_RATIO = 0.075f

    /** 正文基础字号(相对画布宽) */
    const val BODY_BASE_SIZE_RATIO = 0.032f

    /** 正文基准 top(相对画布宽):文字块可独立拖动,正文不再跟随标题高度流动排布 */
    const val BODY_BASE_TOP_RATIO = 0.30f

    /** 新增文字块的基准 top 递增步进(相对画布宽) */
    const val NEW_BLOCK_STEP_RATIO = 0.12f

    /** 装饰贴纸边长(相对画布宽) */
    const val DECORATION_SIZE_RATIO = 0.24f

    /** 装饰贴纸距角落的外边距(相对画布宽) */
    const val DECORATION_MARGIN_RATIO = 0.05f

    // ---- 文字框尺寸 ----

    /** 文字块默认框宽(相对画布宽):内容区宽 */
    const val DEFAULT_TEXT_WIDTH_RATIO = 1f - CONTENT_PADDING_RATIO * 2

    /** 文字块最小框宽(相对画布宽),拖拽手柄时下限 */
    const val MIN_TEXT_WIDTH_RATIO = 0.2f

    /** 文字块最小框高(相对画布高),拖拽手柄时下限;实际框高 = max(内容高, 设定高) */
    const val MIN_TEXT_HEIGHT_RATIO = 0.04f

    /** 卡片底色(背景层半透明时透出) */
    const val CARD_BASE_COLOR = 0xFFFFFFFF

    // ---- 纸张纹理 ----

    /** 横线/方格纸线条颜色 */
    const val PAPER_LINE_COLOR = 0xFFD9DEE8

    /** 横线纸行距(相对画布宽) */
    const val PAPER_LINE_SPACING_RATIO = 0.055f

    /** 方格纸格距(相对画布宽) */
    const val PAPER_GRID_SPACING_RATIO = 0.055f

    /** 牛皮纸底色 / 边框色 */
    const val PAPER_KRAFT_COLOR = 0xFFE3C9A0
    const val PAPER_KRAFT_EDGE_COLOR = 0xFFC9A777

    /** 信纸底色 / 顶部装饰条色 */
    const val PAPER_LETTER_COLOR = 0xFFFBF6EC
    const val PAPER_LETTER_ACCENT_COLOR = 0xFFD98C8C

    /** 彩色纸底色 / 波点色 */
    const val PAPER_COLORFUL_COLOR = 0xFFFFF3E8
    const val PAPER_COLORFUL_DOT_COLOR = 0xFFF2C9A8

    /** 纯色纸近似用的边框宽度(相对画布宽) */
    const val PAPER_EDGE_WIDTH_RATIO = 0.012f
}
