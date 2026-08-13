package com.wanbaohe.calendar.bazi.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.calendar.data.BaZiCalculator
import com.wanbaohe.calendar.data.BaZiData
import com.wanbaohe.calendar.data.DaYunItem
import com.wanbaohe.calendar.data.FortuneData
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.ai.StreamAnswerCachePolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 八字排盘子组件状态
 */
@Immutable
data class BaZiViewState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val day: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val baZiData: BaZiData? = null,
    val daYunList: List<DaYunItem> = emptyList(),
    val fortuneData: FortuneData? = null,
    val isDataReady: Boolean = false,
)

/**
 * 八字排盘子组件
 *
 * 职责：四柱展示、大运走势、五行分布、流年详批、AI 解盘
 */
class BaZiComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("initialYear") private val initialYear: Int,
    @Assisted("initialMonth") private val initialMonth: Int,
    @Assisted("initialDay") private val initialDay: Int,
    @Assisted("initialHour") private val initialHour: Int,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val appDatabase: AppDatabase,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(run {
        val cal = Calendar.getInstance()
        val year = if (initialYear > 0) initialYear else cal.get(Calendar.YEAR)
        val month = if (initialMonth > 0) initialMonth else cal.get(Calendar.MONTH) + 1
        val day = if (initialDay > 0) initialDay else cal.get(Calendar.DAY_OF_MONTH)
        val hour = if (initialHour in 0..23) initialHour else cal.get(Calendar.HOUR_OF_DAY)
        BaZiViewState(year = year, month = month, day = day, hour = hour)
    })
    val uiState = _uiState.asStateFlow()

    init {
        refreshBaZiInfo()
    }

    /** 更新八字日期 */
    fun updateDate(year: Int, month: Int, day: Int) {
        _uiState.value = _uiState.value.copy(year = year, month = month, day = day)
        refreshBaZiInfo()
    }

    /** 更新八字时辰 */
    fun updateHour(hour: Int) {
        _uiState.value = _uiState.value.copy(hour = hour.coerceIn(0, 23))
        refreshBaZiInfo()
    }

    /** 打开 AI 解盘 */
    fun openBaZiAI() {
        val state = _uiState.value
        val baZi = state.baZiData ?: return
        componentScope.launch(defaultDispatcher) {
            val systemPrompt = appDatabase.chatPromptDao()
                .getSystemPromptByKey(PromptEntity.SYSTEM_PROMPT_KEY_BAZI)
                ?.prompt
                ?: AppDatabase.SYSTEM_PROMPT_BAZI

            val question = buildBaZiQuestion(state, baZi)
            onNavigate(
                Screen.AIStreamAnswer(
                    systemPrompt = systemPrompt,
                    question = question,
                    label = "八字解盘 ${state.year}年${state.month}月${state.day}日",
                    useStreaming = true,
                    cachePolicy = StreamAnswerCachePolicy.PERMANENT,
                )
            )
        }
    }

    /** 截图分享 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit = {}) {
        componentScope.launch {
            shareProvider.shareImage(
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless
                ),
                image = bitmap,
                onComplete = onComplete
            )
        }
    }

    private fun refreshBaZiInfo() {
        componentScope.launch {
            val state = _uiState.value
            val (baZi, daYun, fortune) = withContext(defaultDispatcher) {
                val b = BaZiCalculator.calculateBaZi(state.year, state.month, state.day, state.hour)
                val d = BaZiCalculator.getDaYun(state.year, state.month)
                val f = BaZiCalculator.getFortuneData(state.year, state.month, state.year)
                Triple(b, d, f)
            }
            _uiState.value = _uiState.value.copy(
                baZiData = baZi,
                daYunList = daYun,
                fortuneData = fortune,
                isDataReady = true,
            )
        }
    }

    private fun buildBaZiQuestion(state: BaZiViewState, baZi: BaZiData): String {
        val wuXingStr = baZi.wuXingDistribution.entries
            .sortedByDescending { it.value }
            .joinToString(")") { "${it.key}(${it.value.toInt()}%)" }
        val daYunCurrent = state.daYunList.find { it.isCurrent }
        return buildString {
            appendLine("请帮我解析以下八字命盘：")
            appendLine()
            appendLine("**出生日期**：${state.year}年${state.month}月${state.day}日 ${state.hour}时")
            appendLine()
            appendLine("**四柱**：")
            appendLine("- 年柱：${baZi.yearPillar.tianGan}${baZi.yearPillar.diZhi}（十神：${baZi.yearPillar.shiShen}）")
            appendLine("- 月柱：${baZi.monthPillar.tianGan}${baZi.monthPillar.diZhi}（十神：${baZi.monthPillar.shiShen}）")
            appendLine("- 日柱：${baZi.dayPillar.tianGan}${baZi.dayPillar.diZhi}（日主）")
            appendLine("- 时柱：${baZi.hourPillar.tianGan}${baZi.hourPillar.diZhi}（十神：${baZi.hourPillar.shiShen}）")
            appendLine()
            appendLine("**日主**：${baZi.dayMaster}，身${baZi.strength}，喜用神：${baZi.favorableElements}")
            appendLine()
            appendLine("**五行分布**：$wuXingStr")
            if (daYunCurrent != null) {
                appendLine()
                appendLine("**当前大运**：${daYunCurrent.ganZhi}（${daYunCurrent.startYear}年起）")
            }
            state.fortuneData?.let { fortune ->
                appendLine()
                appendLine("**当前流年**：${fortune.ganZhiYear}（${fortune.year}年），运势评分 ${fortune.fortuneScore}%")
            }
        }.trim()
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialYear") initialYear: Int = -1,
            @Assisted("initialMonth") initialMonth: Int = -1,
            @Assisted("initialDay") initialDay: Int = -1,
            @Assisted("initialHour") initialHour: Int = -1,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): BaZiComponent
    }
}
