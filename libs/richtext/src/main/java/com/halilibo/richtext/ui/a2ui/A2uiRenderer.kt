package com.halilibo.richtext.ui.a2ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * A2UI JSON 渲染提供者接口。
 *
 * 通过 [LocalA2uiRenderer] CompositionLocal 注入，
 * 解耦 `:libs:richtext` 与具体动态 UI 渲染实现之间的依赖关系。
 */
interface A2uiRenderer {

    /**
     * 渲染 a2ui 代码块的 Composable
     *
     * @param json      A2UI JSON 配置字符串
     * @param modifier  修饰符
     * @param onSubmit  表单提交回调（用户点击 submit 按钮时触发）
     */
    @Composable
    fun RenderA2ui(
        json: String,
        modifier: Modifier,
        onSubmit: ((formData: String) -> Unit)? = null,
    )
}

/**
 * CompositionLocal for A2UI JSON rendering.
 *
 * 默认值为 null — 当未提供时，A2uiCodeBlock 会回退到纯代码显示。
 */
val LocalA2uiRenderer = compositionLocalOf<A2uiRenderer?> { null }

/**
 * CompositionLocal 标记当前消息是否处于流式输出状态。
 *
 * 由上层消息渲染组件提供，A2uiCodeBlock 据此决定显示骨架图还是渲染界面。
 */
val LocalIsMessageStreaming = compositionLocalOf { false }
