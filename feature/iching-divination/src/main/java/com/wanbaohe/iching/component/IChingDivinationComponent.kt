package com.wanbaohe.iching.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.iching.data.IChingHistoryRecord
import com.wanbaohe.iching.data.IChingHistoryRepository
import com.wanbaohe.iching.data.IChingSoundSettings
import com.wanbaohe.iching.domain.HexagramGenerator
import com.wanbaohe.iching.domain.IChingInterpretationService
import com.wanbaohe.iching.domain.IChingTextLibrary
import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramLine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SHAKE_THRESHOLD = 15f
private const val SHAKE_COOLDOWN_MS = 1_000L
private const val TOSS_DURATION_MS = 700L

/** 第六爻落定后,先让铜钱声落地再起磬声,两声叠一起会糊 */
private const val REVEAL_DELAY_MS = 320L

/** 铜钱落桌时摇卦声淡出的时长,与铜钱声交叉着走,不要硬掐断 */
private const val SHAKE_FADE_OUT_MS = 220L

/** AI 解读积分来源标识 */
private const val AI_INTERPRET_SOURCE = "iching_ai_interpretation"

/** 积分预估余量倍数,与 BaseUtils.canConsumePoints 口径一致 */
private const val POINTS_ESTIMATE_MARGIN = 3

enum class IChingPage { CAST, RESULT }

sealed interface CastingStage {
    data object Idle : CastingStage

    /** 铜钱在空中翻转,completed 为已落定爻数 */
    data class Tossing(val completed: Int) : CastingStage

    /** 铜钱落地,显示已出爻,completed 为已出爻数 */
    data class Casting(val completed: Int) : CastingStage
    data class Success(val result: DivinationResult) : CastingStage
    data class Error(val message: String) : CastingStage
}

data class IChingUiState(
    val page: IChingPage = IChingPage.CAST,
    val question: String = "",
    val stage: CastingStage = CastingStage.Idle,
    val lines: List<HexagramLine> = emptyList(),
    val result: DivinationResult? = null,
    val aiContent: String = "",
    /** 深度思考内容快照(仅展示不落库;成功后保留至重新起卦/重新生成) */
    val aiReasoning: String = "",
    val isGeneratingAI: Boolean = false,
    val aiError: String? = null,
    val currentRecordId: String? = null,
    /** AI 解读积分预估(与积分闸门口径一致),供按钮旁展示成本 */
    val aiPointsEstimate: Int = 0,
)

class IChingDivinationComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialRecordId: String?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted private val onNavigate: (Screen) -> Unit,
    @ApplicationContext context: Context,
    private val generator: HexagramGenerator,
    private val interpretationService: IChingInterpretationService,
    private val historyRepository: IChingHistoryRepository,
    private val textLibrary: IChingTextLibrary,
    private val audioPlayer: NetworkAudioPlayer,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    companion object {
        /**
         * 音效托管在 R2(bucket onebox-images 的 audio/iching/ 路径),国内海外同地址。
         * 合成脚本与源文件见 onebox-doc/audio/iching/。
         */
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/iching"
        private const val SOUND_SHAKE = "$SOUND_BASE/shake.ogg"
        private const val SOUND_COIN = "$SOUND_BASE/coin.ogg"
        private const val SOUND_REVEAL = "$SOUND_BASE/reveal.ogg"
        private val ALL_SOUNDS = listOf(SOUND_SHAKE, SOUND_COIN, SOUND_REVEAL)
    }

    private val initialRecord = initialRecordId?.let(historyRepository::find)
    private val initialResult = initialRecord?.let { record ->
        runCatching { record.toResult(generator) }.getOrNull()
    }
    private val _uiState = MutableStateFlow(
        initialResult?.let { result ->
            IChingUiState(
                page = IChingPage.RESULT,
                question = result.question,
                stage = CastingStage.Success(result),
                result = result,
                aiContent = initialRecord?.aiContent.orEmpty(),
                currentRecordId = initialRecord?.id,
                aiPointsEstimate = estimatePoints(result),
            )
        } ?: IChingUiState()
    )
    val uiState = _uiState.asStateFlow()
    private val _soundEnabled = MutableStateFlow(IChingSoundSettings.isEnabled())
    val soundEnabled = _soundEnabled.asStateFlow()
    private var returnToPreviousScreen = initialResult != null
    private var castingJob: Job? = null
    private var aiJob: Job? = null

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeMs = 0L
    private var sensorRegistered = false

    private val shakeListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onSensorChanged(event: SensorEvent) {
            val (x, y, z) = event.values
            val force = kotlin.math.sqrt(x * x + y * y + z * z)
            val now = System.currentTimeMillis()
            if (force > SHAKE_THRESHOLD && now - lastShakeMs > SHAKE_COOLDOWN_MS) {
                lastShakeMs = now
                if (_uiState.value.page == IChingPage.CAST) startCasting()
            }
        }
    }

    init {
        // 后台预热音效(下载到本地缓存),进入页面后第一次摇卦基本已就绪
        componentScope.launch { ALL_SOUNDS.forEach { audioPlayer.warmUp(it) } }
        componentContext.lifecycle.doOnStart {
            if (!sensorRegistered) {
                accelerometer?.let {
                    sensorManager.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_GAME)
                    sensorRegistered = true
                }
            }
        }
        componentContext.lifecycle.doOnStop {
            sensorManager.unregisterListener(shakeListener)
            sensorRegistered = false
        }
        componentContext.lifecycle.doOnDestroy {
            castingJob?.cancel()
            aiJob?.cancel()
            sensorManager.unregisterListener(shakeListener)
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    fun setQuestion(value: String) {
        if (value.length <= 100 && _uiState.value.stage is CastingStage.Idle) {
            _uiState.update { it.copy(question = value) }
        }
    }

    /** 摇一爻:铜钱动画落定后出一爻;满 6 爻自动生成结果进结果页 */
    fun startCasting() {
        val state = _uiState.value
        if (state.stage is CastingStage.Tossing || state.lines.size >= 6) return
        castingJob?.cancel()
        castingJob = componentScope.launch {
            try {
                _uiState.update {
                    it.copy(stage = CastingStage.Tossing(it.lines.size), aiContent = "", aiReasoning = "", aiError = null)
                }
                // 铜钱在手里的沙沙声,贯穿整个摇卦动画
                if (_soundEnabled.value) playSound(SOUND_SHAKE, isBackground = true)
                delay(TOSS_DURATION_MS)
                val lines = _uiState.value.lines + generator.tossLine()
                _uiState.update { it.copy(lines = lines, stage = CastingStage.Casting(lines.size)) }
                // 铜钱落桌就该收,否则摇卦声会一直循环到下一爻
                audioPlayer.fadeOutBackground(SHAKE_FADE_OUT_MS)
                if (_soundEnabled.value) {
                    playSound(SOUND_COIN)
                    if (lines.size == 6) {
                        playSound(SOUND_REVEAL, delayMillis = REVEAL_DELAY_MS)
                    }
                }
                if (lines.size == 6) {
                    val result = generator.create(_uiState.value.question, lines)
                    val record = IChingHistoryRecord.from(result)
                    runCatching { historyRepository.append(record) }
                    _uiState.update {
                        it.copy(
                            page = IChingPage.RESULT,
                            stage = CastingStage.Success(result),
                            result = result,
                            currentRecordId = record.id,
                            aiPointsEstimate = estimatePoints(result),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(stage = CastingStage.Error(throwable.message ?: "起卦失败，请重试")) }
            }
        }
    }

    /**
     * 播放音效。播放失败静默吞掉(网络问题不该影响起卦本身能用)。
     *
     * [delayMillis] 用于把两记声音错开,避免铜钱声和成卦磬声糊在一起。
     */
    private fun playSound(url: String, isBackground: Boolean = false, delayMillis: Long = 0L) {
        componentScope.launch {
            runCatching {
                if (delayMillis > 0L) delay(delayMillis)
                if (isBackground) audioPlayer.playBackground(url) else audioPlayer.playEffect(url)
            }
        }
    }

    /** 音效总开关;关掉时立刻掐掉正在响的声音 */
    fun toggleSound() {
        val enabled = !_soundEnabled.value
        IChingSoundSettings.setEnabled(enabled)
        _soundEnabled.value = enabled
        if (!enabled) {
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    /**
     * AI 解读入口:先过登录+积分预估闸门(规范见 onebox-doc/AGENTS.md「AI 功能登录与积分」),
     * 未登录弹公共登录页,登录成功后自动续跑;积分不足弹打赏浮动层(与 OCR 等模块一致)。
     */
    fun generateAIInterpretation() {
        val result = _uiState.value.result ?: return
        if (_uiState.value.isGeneratingAI) return
        withAiGate(result) { doGenerateAIInterpretation(result) }
    }

    private fun withAiGate(result: DivinationResult, action: () -> Unit) {
        if (!TokenStorage.isLogin()) {
            ActionUtils.showLogin(source = AI_INTERPRET_SOURCE) { withAiGate(result, action) }
            return
        }
        ActionUtils.checkPointsAndDo(
            point = estimatePoints(result),
            onFailure = {
                AppEventBus.emit(
                    MainClickEvent(
                        from = MainClickEventFrom.ICHING,
                        type = MainShowType.BUY_COFFEE,
                    )
                )
            },
            onSuccess = action,
        )
    }

    /** 积分预估:按输入 token 折算再乘余量,与 canConsumePoints 口径一致 */
    private fun estimatePoints(result: DivinationResult): Int =
        BaseUtils.tokenToPoints(
            StringUtils.calculateTokens(interpretationService.buildInput(result))
        ) * POINTS_ESTIMATE_MARGIN

    /** 卦名/卦辞/爻辞按当前 locale 从文本库解析,供界面展示 */
    fun hexagramText(number: Int) = textLibrary.hexagram(number)

    fun trigramName(code: Int) = textLibrary.trigramName(code)

    private fun doGenerateAIInterpretation(result: DivinationResult) {
        val recordId = _uiState.value.currentRecordId
        val previousContent = _uiState.value.aiContent
        aiJob?.cancel()
        aiJob = componentScope.launch {
            _uiState.update {
                it.copy(isGeneratingAI = true, aiError = null, aiContent = "", aiReasoning = "")
            }
            interpretationService.interpret(
                result = result,
                onDelta = { partial ->
                    // 流式进行中切换了卦象则丢弃增量,避免串页
                    if (isCurrentResult(recordId, result)) {
                        _uiState.update { it.copy(aiContent = partial) }
                    }
                },
                onReasoningDelta = { partial ->
                    if (isCurrentResult(recordId, result)) {
                        _uiState.update { it.copy(aiReasoning = partial) }
                    }
                },
            ).fold(
                onSuccess = { content ->
                    if (!isCurrentResult(recordId, result)) return@fold
                    if (recordId != null) runCatching {
                        historyRepository.updateAIContent(recordId, content)
                    }
                    _uiState.update {
                        it.copy(aiContent = content, isGeneratingAI = false)
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    if (!isCurrentResult(recordId, result)) return@fold
                    _uiState.update {
                        it.copy(
                            isGeneratingAI = false,
                            aiError = error.message ?: "AI 解读生成失败",
                            aiContent = previousContent,
                            aiReasoning = "",
                        )
                    }
                },
            )
        }
    }

    fun navigateToHistory() = onNavigate(Screen.IChingHistory)

    fun reset() {
        castingJob?.cancel()
        aiJob?.cancel()
        // 摇卦中被打断时循环音必须跟着停
        audioPlayer.stopBackground()
        returnToPreviousScreen = false
        _uiState.update {
            it.copy(
                page = IChingPage.CAST,
                stage = CastingStage.Idle,
                lines = emptyList(),
                result = null,
                aiContent = "",
                aiReasoning = "",
                isGeneratingAI = false,
                aiError = null,
                currentRecordId = null,
            )
        }
    }

    fun back() {
        when (_uiState.value.page) {
            IChingPage.RESULT -> if (returnToPreviousScreen) onGoBack() else reset()
            IChingPage.CAST -> onGoBack()
        }
    }

    private fun isCurrentResult(recordId: String?, result: DivinationResult): Boolean {
        val state = _uiState.value
        return state.currentRecordId == recordId && state.result == result
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialRecordId: String?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): IChingDivinationComponent
    }
}

