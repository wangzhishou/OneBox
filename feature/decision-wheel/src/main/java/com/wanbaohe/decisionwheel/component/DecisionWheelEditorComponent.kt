package com.wanbaohe.decisionwheel.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.data.WheelRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 转盘编辑页状态。
 *
 * 全程只改内存草稿，[save] 时一次性落库 —— 之前是"加选项立即写库、标题等保存才写"，
 * 用户中途放弃会留下脏数据。
 */
@Immutable
data class WheelEditorUiState(
    val title: String = "",
    val options: List<WheelOption> = emptyList(),
    val loading: Boolean = true,
    val dirty: Boolean = false,
    val canSave: Boolean = false,
    /** AI 正在生成选项 */
    val aiGenerating: Boolean = false,
    /** AI 返回并解析出来的候选选项；为空表示还没生成或没解析出东西 */
    val aiSuggestions: List<String> = emptyList(),
    /** 上次生成失败的原因；null 表示没失败 */
    val aiError: String? = null
)

class DecisionWheelEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val wheelId: String,
    @Assisted private val onGoBack: () -> Unit,
    @Assisted("onOpenAiAssistant") private val onOpenAiAssistant: (String) -> Unit,
    private val repository: WheelRepository,
    private val aiPromptExecutor: AIPromptExecutor,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(WheelEditorUiState())
    val uiState = _uiState.asStateFlow()

    /** 最近一次删除，供 Snackbar 撤销 */
    private var lastRemoved: Pair<Int, WheelOption>? = null

    private var original: DecisionWheel? = null

    init {
        componentScope.launch {
            val wheel = repository.getWheelById(wheelId)
            original = wheel
            _uiState.update {
                WheelEditorUiState(
                    title = wheel?.title.orEmpty(),
                    options = wheel?.options.orEmpty(),
                    loading = false,
                    dirty = false,
                    canSave = wheel != null && wheel.title.isNotBlank() && wheel.options.size >= 2
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, dirty = true).validated() }
    }

    fun addOption(name: String) {
        if (name.isBlank()) return
        val current = _uiState.value.options
        val baseColor = current.firstOrNull { it.color != Color.Unspecified }?.color
            ?: ColorGenerator.generateSegmentBackgrounds(Color(0xFFA3B7F6), 1).first()
        val palette = ColorGenerator.generateSegmentBackgrounds(
            baseColor = baseColor,
            count = current.size + 1
        )
        val used = current.map { it.color }.toMutableSet()
        val color = palette.firstOrNull { c ->
            used.none { u -> closeEnough(u, c) }
        } ?: palette.getOrNull(current.size) ?: baseColor

        _uiState.update {
            it.copy(
                options = current + WheelOption(name = name.trim(), color = color),
                dirty = true
            ).validated()
        }
    }

    fun renameOption(optionId: String, name: String) {
        if (name.isBlank()) return
        _uiState.update {
            it.copy(
                options = it.options.map { o -> if (o.id == optionId) o.copy(name = name.trim()) else o },
                dirty = true
            )
        }
    }

    fun setWeight(optionId: String, weight: Float) {
        _uiState.update {
            it.copy(
                options = it.options.map { o ->
                    if (o.id == optionId) o.copy(weight = weight.coerceIn(0.25f, 10f)) else o
                },
                dirty = true
            )
        }
    }

    fun setOptionColor(optionId: String, color: Color) {
        _uiState.update {
            it.copy(
                options = it.options.map { o -> if (o.id == optionId) o.copy(color = color) else o },
                dirty = true
            )
        }
    }

    /**
     * 手动放回 / 摘掉单个选项。
     *
     * "抽后移除"抽走的选项会一直保持 disabled，而主页只有"全部恢复"一个入口、
     * 且它只在开关打开时才显示。编辑页是唯一能单独处理某一条的地方，不露出来
     * 用户要么全量恢复、要么 rebuild 转盘。
     */
    fun setOptionEnabled(optionId: String, enabled: Boolean) {
        _uiState.update {
            it.copy(
                options = it.options.map { o ->
                    if (o.id == optionId) o.copy(enabled = enabled) else o
                },
                dirty = true
            )
        }
    }

    /** 放回所有被"抽后移除"摘掉的选项 */
    fun restoreAllOptions() {
        if (_uiState.value.options.none { !it.enabled }) return
        _uiState.update {
            it.copy(
                options = it.options.map { o -> if (o.enabled) o else o.copy(enabled = true) },
                dirty = true
            )
        }
    }

    /** 返回 false 表示已达下限（至少保留 2 项），UI 应提示而不是弹确认框 */
    fun removeOption(optionId: String): Boolean {
        val current = _uiState.value.options
        if (current.size <= 2) return false
        val index = current.indexOfFirst { it.id == optionId }
        if (index < 0) return false
        lastRemoved = index to current[index]
        _uiState.update {
            it.copy(options = current.filterNot { o -> o.id == optionId }, dirty = true).validated()
        }
        return true
    }

    fun undoRemove() {
        val (index, option) = lastRemoved ?: return
        lastRemoved = null
        _uiState.update {
            val restored = it.options.toMutableList().apply {
                add(index.coerceIn(0, size), option)
            }
            it.copy(options = restored, dirty = true).validated()
        }
    }

    fun consumeUndo() {
        lastRemoved = null
    }

    fun moveOption(from: Int, to: Int) {
        val current = _uiState.value.options
        if (from !in current.indices || to !in current.indices) return
        _uiState.update {
            val moved = current.toMutableList().apply { add(to, removeAt(from)) }
            it.copy(options = moved, dirty = true)
        }
    }

    /** 整体换配色：以 baseColor 为基色重新生成一整套扇区色 */
    fun applyPalette(baseColor: Color) {
        val current = _uiState.value.options
        if (current.isEmpty()) return
        val backgrounds = ColorGenerator.generateSegmentBackgrounds(
            baseColor = baseColor,
            count = current.size
        )
        _uiState.update {
            it.copy(
                options = current.mapIndexed { i, o ->
                    o.copy(color = backgrounds.getOrNull(i) ?: o.color)
                },
                dirty = true
            )
        }
    }

    // ─── AI 生成选项 ────────────────────────────────────────────────────────

    /**
     * 让当前引擎的模型按用户描述列出候选选项。
     *
     * 结果只先放进 [WheelEditorUiState.aiSuggestions] 供用户在弹窗里过一眼，
     * 确认后才 [applyAiSuggestions] 落进草稿 —— AI 会胡说，加之前人得看一眼。
     */
    fun requestAiOptions(input: String) {
        val prompt = input.trim()
        if (prompt.isBlank() || _uiState.value.aiGenerating) return
        componentScope.launch {
            _uiState.update {
                it.copy(aiGenerating = true, aiError = null, aiSuggestions = emptyList())
            }
            val result = aiPromptExecutor.execute(
                input = prompt,
                systemPrompt = aiSystemPrompt()
            )
            val names = parseOptionNames(result.content)
            _uiState.update {
                it.copy(
                    aiGenerating = false,
                    aiSuggestions = names,
                    aiError = if (names.isEmpty()) {
                        result.errorMessage?.takeIf { msg -> msg.isNotBlank() }
                            ?: "empty result"
                    } else null
                )
            }
        }
    }

    /** 把 AI 候选一次性加进草稿。 */
    fun applyAiSuggestions() {
        val names = _uiState.value.aiSuggestions
        if (names.isEmpty()) return
        names.forEach { addOption(it) }
        clearAiState()
    }

    /**
     * 带着填充词跳到 AI 助手 Tab。
     *
     * 和弹窗里那次"直接生成"是两条路：弹窗是就地拿一批候选，这里是让人去跟模型多轮聊
     * （比如"再加点便宜的""把烧烤换成火锅"）。填充词带上转盘名和现有选项，
     * 免得用户到了那边还得从头描述一遍自己在干嘛。
     */
    fun openAiAssistant() {
        val title = _uiState.value.title.ifBlank {
            AppContext.getString(R.string.ai_assistant_fallback_topic)
        }
        val existing = _uiState.value.options
            .takeIf { it.isNotEmpty() }
            ?.joinToString("、") { it.name }
            ?: AppContext.getString(R.string.ai_assistant_no_options)
        // AppContext.getString 只带一个占位符，这里两个参数只能自己 format
        val template = AppContext.getString(R.string.ai_assistant_filler)
        onOpenAiAssistant(String.format(java.util.Locale.getDefault(), template, title, existing))
    }

    fun clearAiState() {
        _uiState.update {
            it.copy(aiGenerating = false, aiSuggestions = emptyList(), aiError = null)
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        componentScope.launch {
            val base = original ?: return@launch
            repository.updateWheel(
                base.copy(title = state.title.trim(), options = state.options)
            )
            onGoBack()
        }
    }

    fun goBack() = onGoBack()

    private fun WheelEditorUiState.validated(): WheelEditorUiState = copy(
        canSave = title.isNotBlank() && options.size >= 2
    )

    private fun closeEnough(a: Color, b: Color): Boolean =
        kotlin.math.abs(a.red - b.red) < 0.01f &&
            kotlin.math.abs(a.green - b.green) < 0.01f &&
            kotlin.math.abs(a.blue - b.blue) < 0.01f

    /**
     * 从模型回复里抠出选项名。
     *
     * 不要指望模型乖乖输出 JSON —— 它更爱给你 "1. 火锅\n2. 烧烤" 或者一行逗号串。
     * 所以这里按行拆、削掉常见的列表前缀，单行结果再退一步按逗号拆，
     * 最后去重截断。宁可少收两个，也不要把"好的，以下是"当成选项加进盘面。
     */
    private fun parseOptionNames(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()

        val parsed = linkedSetOf<String>()
        raw.replace("\r", "\n").split("\n").forEach { line ->
            var text = line.trim().trim('`')
            text = text.replaceFirst(LIST_PREFIX, "")
            text = text.trim()
                .trim('"', '“', '”', '「', '」', '\'', ',', '，', '。', '：', ':')
                .take(MAX_OPTION_LENGTH)
            if (text.isNotBlank()) parsed.add(text)
        }

        if (parsed.size == 1) {
            val single = parsed.first()
            if (single.contains("，") || single.contains(",")) {
                parsed.clear()
                single.split("，", ",").forEach { part ->
                    val cleaned = part.trim().trim('、', '。', '"', ' ').take(MAX_OPTION_LENGTH)
                    if (cleaned.isNotBlank()) parsed.add(cleaned)
                }
            }
        }

        return parsed.take(MAX_AI_OPTIONS)
    }

    companion object {
        private const val MAX_AI_OPTIONS = 20
        private const val MAX_OPTION_LENGTH = 24

        /** 匹配 "1." "2、" "- " "• " "* " 这类列表前缀 */
        private val LIST_PREFIX = Regex("^([0-9]{1,2}\\s*[.、)．:]\\s*|[-•*·]\\s*)")

        /** 选项名直接画在转盘上, 跟随应用语言; "8 个字以内" 对外语改为按词计的等价限制。 */
        private fun aiSystemPrompt(): String = """
You generate candidate options for a decision wheel. The user describes a scenario; you only supply the candidate options.
Requirements:
1. One option per line. No numbering, no quotes, no Markdown, no explanations.
2. Give 6-12 options that are clearly distinct from each other, each kept short (at most ~8 characters in Chinese, or 2-4 words in other languages).
3. Output only the options themselves, nothing else.

${AiLanguagePrompt.outputInCurrentLanguage("the options")}
""".trimIndent()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            wheelId: String,
            onGoBack: () -> Unit,
            @Assisted("onOpenAiAssistant") onOpenAiAssistant: (String) -> Unit
        ): DecisionWheelEditorComponent
    }
}
