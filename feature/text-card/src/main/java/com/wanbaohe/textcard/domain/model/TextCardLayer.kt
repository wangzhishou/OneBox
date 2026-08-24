package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

/**
 * 文字卡片的固定三层模型(范围裁剪:不做自由图层增删)。
 * 列表顺序即 z 序:index 0 在最底层,末尾在最顶层。
 */
sealed class TextCardLayer {

    abstract val visible: Boolean
    abstract val locked: Boolean

    abstract fun copyState(visible: Boolean, locked: Boolean): TextCardLayer

    @get:StringRes
    abstract val nameRes: Int

    data class Background(
        override val visible: Boolean = true,
        override val locked: Boolean = false,
    ) : TextCardLayer() {
        override val nameRes: Int get() = R.string.textcard_layer_background
        override fun copyState(visible: Boolean, locked: Boolean) =
            copy(visible = visible, locked = locked)
    }

    data class Text(
        override val visible: Boolean = true,
        override val locked: Boolean = false,
    ) : TextCardLayer() {
        override val nameRes: Int get() = R.string.textcard_layer_text
        override fun copyState(visible: Boolean, locked: Boolean) =
            copy(visible = visible, locked = locked)
    }

    data class Decoration(
        override val visible: Boolean = true,
        override val locked: Boolean = false,
    ) : TextCardLayer() {
        override val nameRes: Int get() = R.string.textcard_layer_decoration
        override fun copyState(visible: Boolean, locked: Boolean) =
            copy(visible = visible, locked = locked)
    }

    companion object {
        /** 默认 z 序:背景 → 文字 → 装饰 */
        fun defaultOrder(): List<TextCardLayer> = listOf(Background(), Text(), Decoration())
    }
}
