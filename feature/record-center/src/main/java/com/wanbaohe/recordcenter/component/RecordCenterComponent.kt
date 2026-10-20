package com.wanbaohe.recordcenter.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.database.recordcenter.repo.HealthRecordRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.recordcenter.data.HealthProfile
import com.wanbaohe.recordcenter.data.HealthProfileStore
import com.wanbaohe.recordcenter.model.RecordTrendSummary
import com.wanbaohe.recordcenter.model.buildRecordTrendSummaries
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 聚合页底部 tab:记录宫格 / 趋势图表 / 我的(基础信息) */
enum class RecordCenterTab { RECORDS, TRENDS, MINE }

/**
 * 记录中心聚合页 Component — 底部三 tab:
 * 记录(各类型最新记录宫格)、趋势(近 7 天图表 + 较上周增量)、我的(基础信息)。
 *
 * 只读,写操作在列表页/录入页走 RecordCenterService。
 */
class RecordCenterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    repository: HealthRecordRepository,
    catalog: RecordTypeCatalog,
    private val profileStore: HealthProfileStore,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 全部记录类型定义,按 sortOrder 升序 */
    val recordTypes: List<RecordTypeDefinition> = catalog.all()

    /** 基础信息(性别/年龄/身高/体重),"我的" tab 展示与编辑 */
    val profile: StateFlow<HealthProfile> = profileStore.profile

    fun saveProfile(profile: HealthProfile) {
        profileStore.save(profile)
    }

    /** 各类型最新一条记录,type → entity */
    val latestByType: StateFlow<Map<String, HealthRecordEntity>> = repository
        .observeLatestPerType()
        .map { list -> list.associateBy { it.type } }
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), emptyMap())

    private val _currentTab = MutableStateFlow(RecordCenterTab.RECORDS)
    val currentTab: StateFlow<RecordCenterTab> = _currentTab.asStateFlow()

    fun switchTab(tab: RecordCenterTab) {
        _currentTab.value = tab
    }

    /**
     * 趋势 tab 聚合数据:近 14 天记录(本周图表 + 上周对比)+ 全量最新值。
     * 窗口起点在组件创建时按自然日确定,记录变更经 Flow 自动重算;
     * 聚合在默认调度器执行,UI 仅在趋势 tab 可见时订阅(见 RecordCenterScreen)。
     */
    val trendSummaries: StateFlow<List<RecordTrendSummary>> = combine(
        repository.observeSinceAllTypes(trendWindowFrom()),
        repository.observeLatestPerType(),
    ) { recent, latest ->
        buildRecordTrendSummaries(
            definitions = recordTypes,
            recentRecords = recent,
            latestByType = latest.associateBy { it.type },
        )
    }.flowOn(defaultDispatcher)
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun navigateToRecordList(recordType: String) {
        onNavigate(
            Screen.RecordCenter(Screen.RecordCenter.Type.RecordList(recordType = recordType))
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): RecordCenterComponent
    }
}

/** 趋势窗口起点:13 天前的自然日 0 点(覆盖本周 7 天 + 上周 7 天) */
private fun trendWindowFrom(): Long =
    LocalDate.now().minusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
