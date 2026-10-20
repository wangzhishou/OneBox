package com.wanbaohe.householditems.component

import androidx.annotation.StringRes
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.household.repo.HouseholdRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.householditems.R
import com.wanbaohe.householditems.model.DefaultLocations
import com.wanbaohe.householditems.model.HouseholdDisplayMode
import com.wanbaohe.householditems.model.HouseholdItemUi
import com.wanbaohe.householditems.model.HouseholdItemsUiState
import com.wanbaohe.householditems.model.HouseholdLocationUi
import com.wanbaohe.householditems.model.HouseholdTab
import com.wanbaohe.householditems.model.buildHouseholdStats
import com.wanbaohe.householditems.model.buildLocationTree
import com.wanbaohe.householditems.model.localizedDefaultLocationName
import com.wanbaohe.householditems.model.toUi
import com.wanbaohe.householditems.service.HouseholdService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 家庭物品主页 Component — 仅做 UI 状态编排。
 *
 * - 写操作全部走 [HouseholdService](AI 工具同样走 Service,共享业务/审计日志)
 * - 表单状态机由 [ItemEditor] 接管
 * - 预置位置在首次进入(表为空)时播种,照 BookkeepingRepository.ensureDefaults 模式
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdItemsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted("initialType") initialType: Screen.HouseholdItems.Type?,
    dispatchersHolder: DispatchersHolder,
    private val repository: HouseholdRepository,
    private val service: HouseholdService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(HouseholdItemsUiState())
    val uiState: StateFlow<HouseholdItemsUiState> = _uiState

    private val itemEditor = ItemEditor()
    private val searchQuery = MutableStateFlow("")
    private val locations = MutableStateFlow<List<HouseholdLocationUi>>(emptyList())

    init {
        seedDefaultLocations()
        observeLocations()
        observeItems()
        observeStats()
        handleInitialType(initialType)
    }

    // ─────────── Tab ───────────

    fun switchTab(tab: HouseholdTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // ─────────── 搜索 / 浏览 ───────────

    fun onSearchQueryChange(value: String) {
        searchQuery.value = value
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun enterLocation(locationId: String) {
        _uiState.update { it.copy(currentLocationId = locationId) }
    }

    fun navigateToBreadcrumb(locationId: String?) {
        _uiState.update { it.copy(currentLocationId = locationId) }
    }

    /** 物品卡片位置 tag 点击:清掉搜索词并把面包屑定位到该位置(等价于逐级点进) */
    fun browseToLocation(locationId: String) {
        searchQuery.value = ""
        _uiState.update { it.copy(searchQuery = "", currentLocationId = locationId) }
    }

    /** 手动切换列表/宫格;覆盖系统默认(不持久化,页面存活期内有效)。 */
    fun setDisplayMode(mode: HouseholdDisplayMode) {
        _uiState.update { it.copy(displayModeOverride = mode) }
    }

    // ─────────── 物品表单 ───────────

    fun startAddItem() {
        _uiState.update { itemEditor.startAdd(it, it.currentLocationId) }
    }

    fun startEditItem(item: HouseholdItemUi) {
        _uiState.update { itemEditor.startEdit(it, item) }
    }

    fun hideItemEditor() {
        _uiState.update { itemEditor.hide(it) }
    }

    fun onEditorNameChange(value: String) = _uiState.update { itemEditor.changeName(it, value) }
    fun onEditorCategoryChange(value: String) = _uiState.update { itemEditor.changeCategory(it, value) }
    fun onEditorLocationChange(locationId: String?) = _uiState.update { itemEditor.changeLocation(it, locationId) }
    fun onEditorExpireDateChange(date: LocalDate?) = _uiState.update { itemEditor.changeExpireDate(it, date) }
    fun onEditorPhotoPicked(uri: String) = _uiState.update { itemEditor.changePhoto(it, uri) }
    fun onEditorPhotoRemoved() = _uiState.update { itemEditor.removePhoto(it) }
    fun onEditorNoteChange(value: String) = _uiState.update { itemEditor.changeNote(it, value) }

    fun submitItem() {
        val state = _uiState.value
        val input = itemEditor.buildInput(state) ?: return
        val editingId = state.editingItemId
        componentScope.launch {
            val result = if (editingId == null) {
                service.addItem(input, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
                    .map { }
            } else {
                service.updateItem(editingId, input, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
            }
            result.onSuccess {
                hideItemEditor()
                // 新建/编辑给不同反馈文案
                showToast(
                    if (editingId == null) R.string.household_item_added
                    else R.string.household_saved
                )
            }
        }
    }

    fun deleteItem(itemId: String) {
        componentScope.launch {
            service.deleteItem(itemId, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
        }
    }

    // ─────────── AI 帮我记 ───────────

    /** 空态引导:跳转 AI 助手 Tab,预填示例文案,Agent 模式根据输入调用工具记物品。 */
    fun navigateToAiAssist() {
        onNavigate(
            Screen.AITabChatScreen(
                Conversation(
                    entryType = AIConversationEntryType.ASSISTANT,
                    title = AppContext.getString(R.string.household_ai_assist_title),
                    template = AppContext.getString(R.string.household_ai_assist_fill_in),
                )
            )
        )
    }

    // ─────────── 位置管理(设置 tab) ───────────

    fun consumeLocationDeleteBlocked() = _uiState.update { it.copy(locationDeleteBlocked = false) }

    fun addLocation(name: String, parentId: String?, iconKey: String? = null) {
        if (name.isBlank()) return
        componentScope.launch {
            service.addLocation(name, parentId, iconKey, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
                .onSuccess { showToast(R.string.household_location_added) }
        }
    }

    fun renameLocation(locationId: String, newName: String, iconKey: String?) {
        if (newName.isBlank()) return
        componentScope.launch {
            service.renameLocation(locationId, newName, iconKey, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
                .onSuccess { showToast(R.string.household_saved) }
        }
    }

    fun deleteLocation(locationId: String) {
        componentScope.launch {
            service.deleteLocation(locationId, HouseholdService.ACTOR_USER, HouseholdService.SOURCE_UI)
                .onFailure {
                    _uiState.update { state -> state.copy(locationDeleteBlocked = true) }
                }
        }
    }

    // ─────────── 内部 ───────────

    private fun showToast(@StringRes resId: Int) {
        AppToastHost.showToast(AppContext.getString(resId))
    }

    /** 外部跳转(EditItem):itemId = null 直接新建,否则加载物品进入编辑态。 */
    private fun handleInitialType(initialType: Screen.HouseholdItems.Type?) {
        if (initialType !is Screen.HouseholdItems.Type.EditItem) return
        componentScope.launch {
            val itemId = initialType.itemId
            if (itemId == null) {
                _uiState.update { itemEditor.startAdd(it, it.currentLocationId) }
            } else {
                val entity = service.getItemById(itemId) ?: return@launch
                _uiState.update { itemEditor.startEdit(it, entity.toUi(locations.value)) }
            }
        }
    }

    /** 预置位置播种(表为空才写入,幂等)。 */
    private fun seedDefaultLocations() {
        componentScope.launch {
            repository.ensureDefaultLocations(DefaultLocations.all())
        }
    }

    private fun observeLocations() {
        repository.observeLocations()
            .onEach { list ->
                // 预置位置按当前 locale 重新解析显示名(兼容旧库种子名与当前语言不一致)
                val ui = list.map {
                    HouseholdLocationUi(
                        id = it.id,
                        name = localizedDefaultLocationName(it.id) ?: it.name,
                        parentId = it.parentId,
                        iconKey = it.iconKey,
                    )
                }
                locations.value = ui
                _uiState.update { state ->
                    state.copy(
                        locations = ui,
                        locationTree = buildLocationTree(ui),
                        // 当前位置被删除时回到根
                        currentLocationId = state.currentLocationId
                            ?.takeIf { id -> ui.any { it.id == id } },
                    )
                }
            }
            .launchIn(componentScope)
    }

    private fun observeItems() {
        combine(
            searchQuery.flatMapLatest { query ->
                if (query.isBlank()) repository.observeItems() else repository.search(query.trim())
            },
            locations,
        ) { items, locs ->
            items.map { it.toUi(locs) }
        }
            .onEach { list ->
                _uiState.update { state ->
                    state.copy(items = list)
                }
            }
            .launchIn(componentScope)
    }

    /** 统计 tab:全量物品(不受搜索词影响)+ 位置列表 → 聚合数据。 */
    private fun observeStats() {
        combine(repository.observeItems(), locations) { items, locs ->
            buildHouseholdStats(items, locs)
        }
            .onEach { stats ->
                _uiState.update { state -> state.copy(stats = stats) }
            }
            .launchIn(componentScope)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            @Assisted("initialType") initialType: Screen.HouseholdItems.Type?,
        ): HouseholdItemsComponent
    }
}
