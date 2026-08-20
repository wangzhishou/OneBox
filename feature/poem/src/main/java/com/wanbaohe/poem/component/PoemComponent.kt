package com.wanbaohe.poem.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.TokenStorage
import com.shifenmiao.tts.service.TTSService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.makeLog
import com.wanbaohe.poem.R
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.model.isPinyinAligned
import com.wanbaohe.poem.service.PoemInsightService
import com.wanbaohe.poem.service.PoemService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PoemComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("poemId") poemId: Long?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val poemService: PoemService,
    private val insightService: PoemInsightService,
    private val ttsService: TTSService,
    private val networkAudioPlayer: NetworkAudioPlayer,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PoemUiState())
    val uiState = _uiState.asStateFlow()

    /** 当前展示的诗词 id;为空表示尚未选择(空状态) */
    private val currentPoemId = MutableStateFlow(poemId)

    /** 用户主动选择/生成过后,不再用「最新一条历史」自动回填 */
    private var userDrivenSelection = poemId != null

    /** 已尝试过自动生成拼音的诗词 id,避免重复触发 */
    private val pinyinAttempts = mutableSetOf<Long>()

    init {
        // 历史记录:全量供滑动翻页与历史弹层;无 deeplink 且用户未操作时,自动回填最新一首
        componentScope.launch {
            poemService.observeHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
                if (!userDrivenSelection && currentPoemId.value == null) {
                    currentPoemId.value = history.firstOrNull()?.id
                }
            }
        }
        // 当前诗词:响应式观察,拼音/解读/翻译异步写库后自动刷新
        componentScope.launch {
            currentPoemId.filterNotNull().flatMapLatest { id ->
                poemService.observePoem(id)
            }.collect { poem ->
                _uiState.update { it.copy(poem = poem, isLoading = false) }
                maybeGeneratePinyin(poem)
            }
        }
    }

    /** 生成一首:随机取诗 */
    fun refresh() {
        if (uiState.value.isLoading) return
        componentScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, insightError = null) }
            poemService.fetchRandomPoem()
                .onSuccess { poem ->
                    userDrivenSelection = true
                    currentPoemId.value = poem.id
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /** 历史点选:回填到卡片 */
    fun selectPoem(id: Long) {
        userDrivenSelection = true
        _uiState.update { it.copy(error = null, insightError = null, translationError = null) }
        currentPoemId.value = id
    }

    fun generateInsight() {
        val poem = uiState.value.poem ?: return
        if (uiState.value.isGeneratingInsight) return
        withAiGate(source = "poem_insight", poem = poem) {
            componentScope.launch {
                _uiState.update { it.copy(isGeneratingInsight = true, insightError = null) }
                when (val result = insightService.generateInsight(poem)) {
                    is PoemInsightService.GenerationResult.Success -> {
                        _uiState.update {
                            it.copy(
                                poem = it.poem?.copy(aiInsight = result.content),
                                isGeneratingInsight = false,
                            )
                        }
                    }

                    is PoemInsightService.GenerationResult.Failed -> {
                        _uiState.update {
                            it.copy(isGeneratingInsight = false, insightError = result.reason)
                        }
                    }
                }
            }
        }
    }

    fun generateTranslation() {
        val poem = uiState.value.poem ?: return
        if (uiState.value.isGeneratingTranslation) return
        withAiGate(source = "poem_translation", poem = poem) {
            componentScope.launch {
                _uiState.update { it.copy(isGeneratingTranslation = true, translationError = null) }
                when (val result = insightService.generateTranslation(poem)) {
                    is PoemInsightService.GenerationResult.Success -> {
                        _uiState.update {
                            it.copy(
                                poem = it.poem?.copy(translation = result.content),
                                isGeneratingTranslation = false,
                            )
                        }
                    }

                    is PoemInsightService.GenerationResult.Failed -> {
                        _uiState.update {
                            it.copy(isGeneratingTranslation = false, translationError = result.reason)
                        }
                    }
                }
            }
        }
    }

    /** 清空历史(保留收藏);当前展示的诗词被清掉时回到空状态 */
    fun clearHistory() {
        componentScope.launch {
            val currentId = currentPoemId.value
            poemService.clearHistory()
            userDrivenSelection = true
            if (currentId != null && poemService.getPoem(currentId) == null) {
                currentPoemId.value = null
                _uiState.update { it.copy(poem = null) }
            }
        }
    }

    fun navigateToSearch() {
        onNavigate(Screen.PoemSearch())
    }

    /** 手动触发生成拼音(按钮入口):无拼音时生成,已有(含对齐失败的)时强制重新生成 */
    fun generatePinyin() {
        val poem = uiState.value.poem ?: return
        withAiGate(source = "poem_pinyin", poem = poem) {
            maybeGeneratePinyin(poem, force = true)
        }
    }

    /** 诗朗诵:播放中/合成中再点 = 停止;否则过登录+积分门槛后合成并播放(命中 TTS 缓存免费直播) */
    fun toggleRecite() {
        if (uiState.value.isReciting || uiState.value.isSynthesizingSpeech) {
            stopRecite()
            return
        }
        val poem = uiState.value.poem ?: return
        withAiGate(source = "poem_recite", poem = poem) { startRecite(poem) }
    }

    /** 停止朗诵(页面退出时必须调用,避免音频残留播放) */
    fun stopRecite() {
        reciteJob?.cancel()
        reciteJob = null
        networkAudioPlayer.stopEffect()
        _uiState.update { it.copy(isReciting = false, isSynthesizingSpeech = false) }
    }

    private var reciteJob: Job? = null

    private fun startRecite(poem: Poem) {
        reciteJob?.cancel()
        reciteJob = componentScope.launch {
            val text = buildReciteText(poem)
            val cached = ttsService.getAudioByTextAndTag(text, RECITE_TTS_TAG)
            if (cached != null) {
                playReciteFile(File(cached.filePath))
                return@launch
            }
            _uiState.update { it.copy(isSynthesizingSpeech = true) }
            ttsService.synthesize(text = text, tag = RECITE_TTS_TAG)
                .onSuccess { file ->
                    // TTS 接口无 usage 返回,按文本量估扣
                    val tokens = StringUtils.calculateTokens(text)
                    if (tokens > 0) {
                        runCatching {
                            BaseUtils.consumePoints(
                                degree = BaseUtils.tokenToPoints(tokens),
                                desc = "诗朗诵",
                                source = "poem_recite",
                            )
                        }
                    }
                    playReciteFile(file)
                }
                .onFailure { e ->
                    e.makeLog("PoemComponent")
                    _uiState.update { it.copy(isSynthesizingSpeech = false) }
                    AppToastHost.showFailureToast(R.string.poem_load_failed)
                }
        }
    }

    private suspend fun playReciteFile(file: File) {
        _uiState.update { it.copy(isSynthesizingSpeech = false, isReciting = true) }
        networkAudioPlayer.playLocalFile(file) {
            _uiState.update { state -> state.copy(isReciting = false) }
        }
    }

    private fun buildReciteText(poem: Poem): String = buildString {
        append(poem.title).append('。')
        append(poem.author).append('。')
        poem.content.forEach { append(it) }
    }

    /**
     * AI 功能统一门槛(规范见 onebox-doc/AGENTS.md「AI 功能登录与积分」):
     * 未登录 → 公共登录弹窗,登录成功后继续;已登录 → 按内容预估积分闸门,不足提示。
     */
    private fun withAiGate(source: String, poem: Poem, action: () -> Unit) {
        if (!TokenStorage.isLogin()) {
            ActionUtils.showLogin(source = source) { withAiGate(source, poem, action) }
            return
        }
        val estimatedPoints = BaseUtils.tokenToPoints(
            StringUtils.calculateTokens(poem.content.joinToString(""))
        ) * POINTS_ESTIMATE_MARGIN
        ActionUtils.checkPointsAndDo(point = estimatedPoints, onSuccess = action)
    }

    /**
     * 生成拼音:静默失败,不阻塞卡片展示;生成中置位供底部状态提示;失败允许下次重试。
     * 自动流程(force=false)未登录时静默跳过(不弹登录),已有可用拼音时跳过;
     * 拼音存在但对齐失败时视为无拼音,允许重新生成。
     */
    private fun maybeGeneratePinyin(poem: Poem?, force: Boolean = false) {
        if (poem == null || !TokenStorage.isLogin()) return
        if (!force && poem.isPinyinAligned()) return
        if (uiState.value.isGeneratingPinyin) return
        if (!pinyinAttempts.add(poem.id)) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingPinyin = true) }
            val result = insightService.generatePinyin(poem)
            if (result is PoemInsightService.GenerationResult.Failed) {
                pinyinAttempts.remove(poem.id)
            }
            _uiState.update { it.copy(isGeneratingPinyin = false) }
        }
    }

    companion object {
        /** 积分预估余量倍数,与 BaseUtils.canConsumePoints 口径一致 */
        private const val POINTS_ESTIMATE_MARGIN = 3

        /** 诗朗诵 TTS 缓存分类标签 */
        private const val RECITE_TTS_TAG = "poem-recite"
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("poemId") poemId: Long?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PoemComponent
    }
}
