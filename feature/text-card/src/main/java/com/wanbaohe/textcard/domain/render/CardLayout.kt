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

    /** 模板预览的正文起始位置;正式排版由排版引擎根据真实行高计算 */
    const val BODY_BASE_TOP_RATIO = 0.30f

    // ---- 文字框尺寸 ----

    /** 文字块默认框宽(相对画布宽):内容区宽 */
    const val DEFAULT_TEXT_WIDTH_RATIO = 1f - CONTENT_PADDING_RATIO * 2

    /** 卡片底色(背景层半透明时透出) */
    const val CARD_BASE_COLOR = 0xFFFFFFFF

    // ---- 自定义背景图 ----

    /**
     * 背景图过扫系数:Crop 铺满后再放大,为拖动留出余量,
     * 避免图片与画布宽高比接近时一拖动就露出卡片底色(预览/导出双端同步)
     */
    const val BACKGROUND_IMAGE_OVERSCAN = 1.15f

    /** 背景图归一化拖动偏移上限:过扫余量的一半,钳制在不出边的范围内 */
    const val BACKGROUND_IMAGE_MAX_OFFSET = (BACKGROUND_IMAGE_OVERSCAN - 1f) / 2f

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
