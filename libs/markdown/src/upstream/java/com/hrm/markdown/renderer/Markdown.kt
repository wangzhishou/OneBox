package com.hrm.markdown.renderer

import com.hrm.markdown.parser.MarkdownParserConfig
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeLayout
import com.hrm.codehigh.renderer.CodeTypography
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.parser.log.HLog
import com.hrm.markdown.runtime.MarkdownDirectivePlugin
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry
import com.hrm.markdown.runtime.MarkdownSourceMap

/**
 * 解析并显示 Markdown 源码。
 *
 * 源码变化时自动更新内容。流式输入应持续传入累计全文，并在输入结束时将 [isStreaming] 设为 `false`。
 * 应用默认外观可通过 [ProvideMarkdownDefaults] 设置，实例参数优先。已有 AST 时可使用接收 [Document] 的重载。
 *
 * @param markdown 要显示的完整 Markdown 源码；流式更新时传入截至当前的累计全文。
 * @param modifier 应用于 Markdown 容器的 [Modifier]，可用于设置尺寸、内边距和布局位置。
 * @param colors 正文、标题、链接、表格等 Markdown 元素的颜色。默认由 [MarkdownDefaults.colors] 提供，
 * 继承作用域设置，未设置时跟随系统明暗主题。代码、公式和图表使用各自的颜色配置。
 * @param typography 正文、`h1`～`h6`、脚注等文字样式，默认由 [MarkdownDefaults.typography] 提供。
 * 正文与标题样式中的显式颜色优先于 [colors]；代码文字通过 [codeTypography] 配置。
 * @param layout 块间距、列表缩进、引用边框和表格内边距等尺寸，默认由 [MarkdownDefaults.layout] 提供。
 * 代码块与行内代码的布局通过 [codeLayout] 配置。
 * @param codeColors 代码块与行内代码的 codehigh 原生配色，包括语法颜色、背景、行号和 Diff 颜色，
 * 默认由 [MarkdownDefaults.codeColors] 提供。
 * @param codeTypography 代码正文、标题、语言与操作文字以及行内代码的字体、字号和行高，
 * 默认由 [MarkdownDefaults.codeTypography] 提供。
 * @param codeLayout 代码块的形状、内边距、行号尺寸，以及行内代码的形状、边框和内边距，
 * 默认由 [MarkdownDefaults.codeLayout] 提供。
 * @param latexConfig LaTeX 渲染器的原生配置；行内公式和块级公式共用其中的字体、主题、渲染选项与交互配置。
 * @param diagramTheme Mermaid、PlantUML、DOT 等图表的原生主题，默认使用 [DiagramTheme.Default]。
 * @param softBreakPolicy 普通源码换行的显示与复制策略，默认使用 [MarkdownSoftBreakPolicy.Space] 转为空格。
 * 使用 [MarkdownSoftBreakPolicy.LineBreak] 可保留换行；硬换行不受此设置影响。
 * @param enableHeadingNumbering 是否为文档顶层标题添加 `1`、`1.1` 等层级编号，默认为 `false`。
 * @param enableScroll 是否启用组件内部的垂直滚动，默认为 `true`，此时父布局应提供有限的高度约束。
 * 外层容器负责滚动时设为 `false`，Markdown 将按完整内容高度布局。
 * @param externalScrollState 外层垂直滚动状态，仅在 enableScroll=false 时用于保存和恢复脚注返回位置，不增加滚动容器。
 * @param enableSelection 是否允许用户选择 Markdown 文本进行复制，默认为 `true`。
 * @param textSearchState 可选正文精确搜索与定位状态，独立于选区；外部滚动容器配合 enableScroll=false 可定位整篇正文。
 * @param isStreaming 是否仍在接收流式内容，默认为 `false`。启用后对追加的源码进行增量解析，并启用流式渲染效果；
 * 输入结束时设为 `false` 以完成解析。若插件输入转换器不支持流式处理，则回退为全文解析。
 * @param state 用于观察滚动、跳转文档块和清除选区的状态，默认通过 [rememberMarkdownState] 创建。
 * 块索引对应顶层 Markdown 块，不包含头尾插槽；[enableScroll] 为 `false` 时滚动请求返回 `false`。
 * @param components 头尾内容、代码块和表格的附加插槽、图片绘制与告示图标等可选组件设置。
 * 代码块和表格插槽通过 [MarkdownBlockSlot] 声明高度；默认使用 [MarkdownComponents.Default]。
 * @param interactions 链接、引用、图片和代码 Token 的点击回调。未提供链接回调时使用平台 URI 处理器打开链接，
 * 其余事件通过对应回调处理。默认使用 [MarkdownInteractions.Default]，回调更新不重启解析或流式会话。
 * @param directivePlugins 指令扩展插件，可提供解析前的输入转换器及块级、行内指令渲染器，默认为空列表。
 * 输入转换器在解析前依次执行；不支持流式处理的转换器会使 [isStreaming] 回退为全文解析。
 * @param parserConfig 方言、Emoji、ASCII 表情、Lint 和流式追加合并阈值等解析选项，
 * 默认使用 [MarkdownParserConfig.Default]。修改解析配置会重新创建解析会话。
 */
@Composable
// 插件的值相等不代表回调实例相同；入口必须检查引用，解析和编译仍按各自依赖复用。
@NonSkippableComposable
fun Markdown(
    markdown: String,
    modifier: Modifier = Modifier,
    colors: MarkdownColors = MarkdownDefaults.colors(),
    typography: MarkdownTypography = MarkdownDefaults.typography(),
    layout: MarkdownLayout = MarkdownDefaults.layout(),
    codeColors: CodeColors = MarkdownDefaults.codeColors(),
    codeTypography: CodeTypography = MarkdownDefaults.codeTypography(),
    codeLayout: CodeLayout = MarkdownDefaults.codeLayout(),
    latexConfig: LatexConfig = LatexConfig(),
    diagramTheme: DiagramTheme = DiagramTheme.Default,
    softBreakPolicy: MarkdownSoftBreakPolicy = MarkdownSoftBreakPolicy.Space,
    enableHeadingNumbering: Boolean = false,
    enableScroll: Boolean = true,
    enableSelection: Boolean = true,
    isStreaming: Boolean = false,
    state: MarkdownState = rememberMarkdownState(),
    components: MarkdownComponents = MarkdownComponents.Default,
    interactions: MarkdownInteractions = MarkdownInteractions.Default,
    directivePlugins: List<MarkdownDirectivePlugin> = emptyList(),
    parserConfig: MarkdownParserConfig = MarkdownParserConfig.Default,
    textSearchState: MarkdownTextSearchState? = null,
    externalScrollState: ScrollState? = null,
) {
    val directiveRegistry = rememberDirectiveRegistry(directivePlugins)
    val runtimePipeline = remember(directiveRegistry.inputPipeline) { directiveRegistry.inputPipeline }
    val effectiveStreaming = isStreaming && runtimePipeline.supportsStreaming
    LaunchedEffect(isStreaming, effectiveStreaming, runtimePipeline) {
        if (isStreaming && !effectiveStreaming) {
            HLog.w("Markdown") { "Streaming requested, but an input transformer does not support streaming; falling back to full parses." }
        }
    }
    val snapshot = rememberStreamingDocument(markdown, effectiveStreaming, parserConfig, runtimePipeline)
    if (snapshot == null) {
        if (!effectiveStreaming) MarkdownLoading(modifier)
    } else {
        MarkdownDocumentRenderer(
            document = snapshot.document,
            modifier = modifier,
            theme = remember(colors, typography, layout, codeColors, codeTypography, codeLayout, diagramTheme) {
                ResolvedMarkdownStyle(colors, typography, layout, codeColors, codeTypography, codeLayout, diagramTheme)
            },
            config = remember(enableHeadingNumbering, latexConfig, softBreakPolicy) {
                MarkdownRenderOptions(enableHeadingNumbering, latexConfig, softBreakPolicy)
            },
            state = state,
            isStreaming = effectiveStreaming,
            enableScroll = enableScroll,
            enableSelection = enableSelection,
            directiveRegistry = directiveRegistry,
            sourceMap = snapshot.sourceMap,
            components = components,
            interactions = interactions,
            textSearchState = textSearchState,
            externalScrollState = externalScrollState,
        )
    }
}

/**
 * 显示调用方提供的 Markdown [Document]。
 *
 * 调用方通过解析器或自定义 Flavour 构建文档，并在提交新文档后触发重组。已发布的 AST 只读，
 * 后处理器在解析提交前完成转换。本重载直接渲染文档，
 * [directivePlugins] 用于提供指令渲染器。应用默认外观可通过 [ProvideMarkdownDefaults] 设置，实例参数优先。
 *
 * @param document 解析器已提交的 Markdown 文档。传入新的提交后需触发重组。
 * @param modifier 应用于 Markdown 容器的 [Modifier]，可用于设置尺寸、内边距和布局位置。
 * @param colors 正文、标题、链接、表格等 Markdown 元素的颜色。默认由 [MarkdownDefaults.colors] 提供，
 * 继承作用域设置，未设置时跟随系统明暗主题。代码、公式和图表使用各自的颜色配置。
 * @param typography 正文、`h1`～`h6`、脚注等文字样式，默认由 [MarkdownDefaults.typography] 提供。
 * 正文与标题样式中的显式颜色优先于 [colors]；代码文字通过 [codeTypography] 配置。
 * @param layout 块间距、列表缩进、引用边框和表格内边距等尺寸，默认由 [MarkdownDefaults.layout] 提供。
 * 代码块与行内代码的布局通过 [codeLayout] 配置。
 * @param codeColors 代码块与行内代码的 codehigh 原生配色，包括语法颜色、背景、行号和 Diff 颜色，
 * 默认由 [MarkdownDefaults.codeColors] 提供。
 * @param codeTypography 代码正文、标题、语言与操作文字以及行内代码的字体、字号和行高，
 * 默认由 [MarkdownDefaults.codeTypography] 提供。
 * @param codeLayout 代码块的形状、内边距、行号尺寸，以及行内代码的形状、边框和内边距，
 * 默认由 [MarkdownDefaults.codeLayout] 提供。
 * @param latexConfig LaTeX 渲染器的原生配置；行内公式和块级公式共用其中的字体、主题、渲染选项与交互配置。
 * @param diagramTheme Mermaid、PlantUML、DOT 等图表的原生主题，默认使用 [DiagramTheme.Default]。
 * @param softBreakPolicy 文档中软换行的显示与复制策略，默认使用 [MarkdownSoftBreakPolicy.Space] 转为空格。
 * 使用 [MarkdownSoftBreakPolicy.LineBreak] 可保留换行；硬换行不受此设置影响。
 * @param enableHeadingNumbering 是否为文档顶层标题添加 `1`、`1.1` 等层级编号，默认为 `false`。
 * @param enableScroll 是否启用组件内部的垂直滚动，默认为 `true`，此时父布局应提供有限的高度约束。
 * 外层容器负责滚动时设为 `false`，Markdown 将按完整内容高度布局。
 * @param externalScrollState 外层垂直滚动状态，仅在 enableScroll=false 时用于保存和恢复脚注返回位置，不增加滚动容器。
 * @param enableSelection 是否允许用户选择 Markdown 文本进行复制，默认为 `true`。
 * @param textSearchState 可选正文精确搜索与定位状态，独立于选区；外部滚动容器配合 enableScroll=false 可定位整篇正文。
 * @param isStreaming 是否仍在更新流式文档，默认为 `false`。启用后显示流式渲染效果，结束时设为 `false`。
 * 增量解析及解析会话由调用方管理。
 * @param state 用于观察滚动、跳转文档块和清除选区的状态，默认通过 [rememberMarkdownState] 创建。
 * 块索引对应顶层 Markdown 块，不包含头尾插槽；[enableScroll] 为 `false` 时滚动请求返回 `false`。
 * @param components 头尾内容、代码块和表格的附加插槽、图片绘制与告示图标等可选组件设置。
 * 代码块和表格插槽通过 [MarkdownBlockSlot] 声明高度；默认使用 [MarkdownComponents.Default]。
 * @param interactions 链接、引用、图片和代码 Token 的点击回调。未提供链接回调时使用平台 URI 处理器打开链接，
 * 其余事件通过对应回调处理。默认使用 [MarkdownInteractions.Default]，回调更新不重新布局文档。
 * @param directivePlugins 为文档中的块级、行内指令提供渲染器的扩展插件，默认为空列表。
 * 本重载不执行插件的输入转换器，输入转换应在构造 [document] 前完成。
 */
@Composable
@NonSkippableComposable
fun Markdown(
    document: Document,
    modifier: Modifier = Modifier,
    colors: MarkdownColors = MarkdownDefaults.colors(),
    typography: MarkdownTypography = MarkdownDefaults.typography(),
    layout: MarkdownLayout = MarkdownDefaults.layout(),
    codeColors: CodeColors = MarkdownDefaults.codeColors(),
    codeTypography: CodeTypography = MarkdownDefaults.codeTypography(),
    codeLayout: CodeLayout = MarkdownDefaults.codeLayout(),
    latexConfig: LatexConfig = LatexConfig(),
    diagramTheme: DiagramTheme = DiagramTheme.Default,
    softBreakPolicy: MarkdownSoftBreakPolicy = MarkdownSoftBreakPolicy.Space,
    enableHeadingNumbering: Boolean = false,
    enableScroll: Boolean = true,
    enableSelection: Boolean = true,
    isStreaming: Boolean = false,
    state: MarkdownState = rememberMarkdownState(),
    components: MarkdownComponents = MarkdownComponents.Default,
    interactions: MarkdownInteractions = MarkdownInteractions.Default,
    directivePlugins: List<MarkdownDirectivePlugin> = emptyList(),
    textSearchState: MarkdownTextSearchState? = null,
    externalScrollState: ScrollState? = null,
) {
    val directiveRegistry = rememberDirectiveRegistry(directivePlugins)
    MarkdownDocumentRenderer(
        document = document,
        modifier = modifier,
        theme = remember(colors, typography, layout, codeColors, codeTypography, codeLayout, diagramTheme) {
            ResolvedMarkdownStyle(colors, typography, layout, codeColors, codeTypography, codeLayout, diagramTheme)
        },
        config = remember(enableHeadingNumbering, latexConfig, softBreakPolicy) {
            MarkdownRenderOptions(enableHeadingNumbering, latexConfig, softBreakPolicy)
        },
        state = state,
        isStreaming = isStreaming,
        enableScroll = enableScroll,
        enableSelection = enableSelection,
        directiveRegistry = directiveRegistry,
        sourceMap = MarkdownSourceMap.Identity,
        components = components,
        interactions = interactions,
        textSearchState = textSearchState,
        externalScrollState = externalScrollState,
    )
}

@Composable
@NonSkippableComposable
private fun rememberDirectiveRegistry(plugins: List<MarkdownDirectivePlugin>): MarkdownDirectiveRegistry =
    remember { DirectiveRegistryMemo() }.resolve(plugins)

/** 按插件实例与顺序缓存；插件未替换时不创建键、包装对象或列表快照。 */
private class DirectiveRegistryMemo {
    private var plugins: List<MarkdownDirectivePlugin> = emptyList()
    private var registry = MarkdownDirectiveRegistry.Empty

    fun resolve(current: List<MarkdownDirectivePlugin>): MarkdownDirectiveRegistry {
        if (current.size == plugins.size) {
            var index = 0
            while (index < current.size && current[index] === plugins[index]) index++
            if (index == current.size) return registry
        }
        // 独立保存引用，避免调用方复用可变列表时改写上次比较的依据。
        val snapshot = current.toList()
        val updated = if (snapshot.isEmpty()) MarkdownDirectiveRegistry.Empty else MarkdownDirectiveRegistry(snapshot)
        plugins = snapshot
        registry = updated
        return registry
    }
}

@Composable
private fun MarkdownLoading(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
