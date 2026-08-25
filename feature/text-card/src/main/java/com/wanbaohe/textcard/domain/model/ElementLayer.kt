package com.wanbaohe.textcard.domain.model

/**
 * 元素图层:每个元素(文字块/装饰)一层,列表顺序即 z 序(底层在前)。
 * 背景层钉在最底,不进本列表(显隐由 TextCardRenderState.backgroundVisible 单独持有)。
 *
 * @param elementId 对应 TextBlock.id / DecorationSpec.id
 */
data class ElementLayer(
    val elementId: String,
    val kind: Kind,
    val visible: Boolean = true,
    val locked: Boolean = false,
) {
    enum class Kind {
        Text, Decoration
    }
}
