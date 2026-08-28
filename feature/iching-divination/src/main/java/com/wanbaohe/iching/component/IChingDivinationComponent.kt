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
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.iching.data.IChingHistoryRecord
import com.wanbaohe.iching.data.IChingHistoryRepository
import com.wanbaohe.iching.domain.HexagramGenerator
import com.wanbaohe.iching.domain.IChingInterpretationService
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
    val isGeneratingAI: Boolean = false,
    val aiError: String? = null,
    val currentRecordId: String? = null,
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
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

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
            )
        } ?: IChingUiState()
    )
    val uiState = _uiState.asStateFlow()
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
                    it.copy(stage = CastingStage.Tossing(it.lines.size), aiContent = "", aiError = null)
                }
                delay(TOSS_DURATION_MS)
                val lines = _uiState.value.lines + generator.tossLine()
                _uiState.update { it.copy(lines = lines, stage = CastingStage.Casting(lines.size)) }
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
     * AI 解读入口:先过登录+积分预估闸门(规范见 onebox-doc/AGENTS.md「AI 功能登录与积分」),
     * 未登录弹公共登录页,登录成功后自动续跑;积分不足自动提示。
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
        val estimatedPoints = BaseUtils.tokenToPoints(
            StringUtils.calculateTokens(interpretationService.buildInput(result))
        ) * POINTS_ESTIMATE_MARGIN
        ActionUtils.checkPointsAndDo(point = estimatedPoints, onSuccess = action)
    }

    private fun doGenerateAIInterpretation(result: DivinationResult) {
        val recordId = _uiState.value.currentRecordId
        aiJob?.cancel()
        aiJob = componentScope.launch {
            _uiState.update { it.copy(isGeneratingAI = true, aiError = null) }
            interpretationService.interpret(result).fold(
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
                        it.copy(isGeneratingAI = false, aiError = error.message ?: "AI 解读生成失败")
                    }
                },
            )
        }
    }

    fun navigateToHistory() = onNavigate(Screen.IChingHistory)

    fun reset() {
        castingJob?.cancel()
        aiJob?.cancel()
        returnToPreviousScreen = false
        _uiState.update {
            it.copy(
                page = IChingPage.CAST,
                stage = CastingStage.Idle,
                lines = emptyList(),
                result = null,
                aiContent = "",
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

