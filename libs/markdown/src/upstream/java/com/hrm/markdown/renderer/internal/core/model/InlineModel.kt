package com.hrm.markdown.renderer.internal.core.model

import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity

data class InlineModel(
    val identity: RenderIdentity,
    val atoms: List<InlineAtom>,
)

sealed interface InlineAtom {
    val identity: RenderIdentity
}

data class TextAtom(
    override val identity: RenderIdentity,
    val text: String,
    val marks: List<SpanMark> = emptyList(),
) : InlineAtom

data class WidgetAtom(
    override val identity: RenderIdentity,
    val widget: InlineWidgetModel,
) : InlineAtom

data class SpanMark(
    val kind: String,
    /** 渲染器解释后的语义数据（如 citation 的 key、abbreviation 的 fullText）。 */
    val payload: Map<String, String> = emptyMap(),
    /**
     * 渲染器不解释、原样透传给事件使用方的元素属性。
     * 库不认识任何具体键名，也不参与样式或布局计算。
     */
    val attributes: Map<String, String> = emptyMap(),
    /** 由原语义容器的末端确定；单个文本元素默认自身结束，不按内容或属性合并范围。 */
    val isRangeEnd: Boolean = true,
)
