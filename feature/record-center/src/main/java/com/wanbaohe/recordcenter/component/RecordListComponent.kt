package com.wanbaohe.recordcenter.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.aiHealthInsightPointsCost
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.database.recordcenter.repo.HealthRecordRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.data.HealthProfile
import com.wanbaohe.recordcenter.data.HealthProfileStore
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.service.HealthInsightService
import com.wanbaohe.recordcenter.service.RecordCenterService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 列表页时间范围筛选 */
enum class RecordRangeFilter { DAYS_7, DAYS_30, ALL }

/** AI 解读卡片状态:手动触发,不落库 */
sealed interface RecordInsightState {
    data object Idle : RecordInsightState
    data object Loading : RecordInsightState
    data class Content(val text: String) : RecordInsightState
    data class Error(val message: String) : RecordInsightState
}

/** 单个图表字段的统计数据(基于当前筛选范围) */
data class RecordFieldStats(
    val fieldKey: String,
    val latest: Float?,
    val average: Float?,
    val max: Float?,
    val min: Float?,
)

/**
 * 记录列表页 Component — 某一记录类型的趋势图、统计与记录明细。
 *
 * 记录列表统一按 happenedAt 倒序暴露,图表由界面层反转取正序。
 */
class RecordListComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("recordType") val recordTypeKey: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    repository: HealthRecordRepository,
    catalog: RecordTypeCatalog,
    private val service: RecordCenterService,
    private val insightService: HealthInsightService,
    private val profileStore: HealthProfileStore,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 未知类型时为 null,界面层展示空态 */
    val definition: RecordTypeDefinition? = catalog.byKey(recordTypeKey)

    /** 基础信息:AI 解读前未设置时引导完善,生成时作为上下文带上 */
    val profile: StateFlow<HealthProfile> = profileStore.profile

    fun saveProfile(profile: HealthProfile) {
        profileStore.save(profile)
    }

    private val _rangeFilter = MutableStateFlow(RecordRangeFilter.DAYS_30)
    val rangeFilter: StateFlow<RecordRangeFilter> = _rangeFilter

    /**
     * 该类型的全部记录(按 happenedAt 倒序),时间范围在内存里过滤,避免同一张表开多个查询。
     * null 表示首次查询尚未返回,界面据此先留白,避免"空态 → 内容"闪一下。
     */
    private val allRecords: StateFlow<List<HealthRecordEntity>?> = repository.observeByType(recordTypeKey)
        .map { list -> list.sortedByDescending { it.happenedAt } }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), null)

    /**
     * 该类型是否已有任意记录(不受时间范围筛选影响)。
     * null = 加载中;false 时列表页不显示筛选 Bar 与 AI 解读,只留空态引导。
     */
    val hasAnyRecords: StateFlow<Boolean?> = allRecords
        .map { it?.isNotEmpty() }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), null)

    /** 当前范围内的记录,按 happenedAt 倒序 */
    val records: StateFlow<List<HealthRecordEntity>> = combine(allRecords, _rangeFilter) { all, filter ->
        val list = all.orEmpty()
        if (filter == RecordRangeFilter.ALL) {
            list
        } else {
            val to = System.currentTimeMillis()
            val days = if (filter == RecordRangeFilter.DAYS_7) 7L else 30L
            val from = to - TimeUnit.DAYS.toMillis(days)
            list.filter { it.happenedAt in from..to }
        }
    }.stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    /** 每个图表字段的最新/平均/最高/最低 */
    val stats: StateFlow<List<RecordFieldStats>> = records
        .map { list ->
            val keys = definition?.chartFieldKeys.orEmpty()
            keys.map { key ->
                // 按时间正序取 latest,与展示无关,只看时间最新的一条
                val valuesByTime = list.sortedBy { it.happenedAt }
                    .mapNotNull { RecordFieldsCodec.decode(it.fieldsJson)[key] }
                RecordFieldStats(
                    fieldKey = key,
                    latest = valuesByTime.lastOrNull(),
                    average = valuesByTime.takeIf { it.isNotEmpty() }
                        ?.let { (it.sum() / it.size).toFloat() },
                    max = valuesByTime.maxOrNull(),
                    min = valuesByTime.minOrNull(),
                )
            }
        }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun setRangeFilter(filter: RecordRangeFilter) {
        _rangeFilter.value = filter
    }

    private val _insightState = MutableStateFlow<RecordInsightState>(RecordInsightState.Idle)
    val insightState: StateFlow<RecordInsightState> = _insightState

    /**
     * 手动生成 AI 解读:快照当前筛选范围内的记录与时间范围,调用解读服务。
     * 无记录或重复点击 Loading 中时直接忽略。
     * 登录与积分预检由调用方(UI 层 ActionUtils.ensureLoginAndCheckPoints)完成;
     * 仅解读成功扣积分,失败不扣。
     */
    fun generateInsight() {
        val typeDefinition = definition ?: return
        val snapshot = records.value
        if (snapshot.isEmpty() || _insightState.value is RecordInsightState.Loading) return
        _insightState.value = RecordInsightState.Loading
        componentScope.launch {
            val result = insightService.interpret(
                definition = typeDefinition,
                records = snapshot,
                rangeLabel = rangeLabel(_rangeFilter.value),
                profile = profile.value.takeIf { it.isSet },
            )
            _insightState.value = when (result) {
                is HealthInsightService.GenerationResult.Success -> {
                    BaseUtils.consumePoints(
                        degree = aiHealthInsightPointsCost(),
                        desc = AppContext.getString(R.string.record_center_ai_insight),
                        source = POINTS_SOURCE,
                        showToast = true,
                    )
                    RecordInsightState.Content(result.content)
                }

                is HealthInsightService.GenerationResult.Failed ->
                    RecordInsightState.Error(
                        result.reason.ifBlank {
                            AppContext.getString(R.string.record_center_ai_insight_error)
                        }
                    )
            }
        }
    }

    private fun rangeLabel(filter: RecordRangeFilter): String = when (filter) {
        RecordRangeFilter.DAYS_7 -> AppContext.getContext().getString(R.string.record_center_range_days, 7)
        RecordRangeFilter.DAYS_30 -> AppContext.getContext().getString(R.string.record_center_range_days, 30)
        RecordRangeFilter.ALL -> AppContext.getString(R.string.record_center_range_all_label)
    }

    fun deleteRecord(id: String) {
        componentScope.launch {
            service.deleteRecord(id, RecordCenterService.ACTOR_USER)
        }
    }

    fun navigateToAddRecord() {
        onNavigate(
            Screen.RecordCenter(Screen.RecordCenter.Type.AddRecord(recordType = recordTypeKey))
        )
    }

    fun navigateToEditRecord(recordId: String) {
        onNavigate(
            Screen.RecordCenter(
                Screen.RecordCenter.Type.AddRecord(
                    recordType = recordTypeKey,
                    editingRecordId = recordId,
                )
            )
        )
    }

    /** 空态引导:跳转 AI 助手,通过对话记录健康数据 */
    fun navigateToAiChat() {
        onNavigate(
            Screen.AITabChatScreen(
                com.shifenmiao.model.ai.Conversation(
                    entryType = com.shifenmiao.model.ai.AIConversationEntryType.ASSISTANT,
                    title = AppContext.getString(R.string.record_center_ai_assist_title),
                    template = AppContext.getString(R.string.record_center_ai_assist_fill_in),
                )
            )
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("recordType") recordType: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): RecordListComponent
    }

    companion object {
        /** AI 解读积分消耗来源标识 */
        const val POINTS_SOURCE = "health_insight"
    }
}
