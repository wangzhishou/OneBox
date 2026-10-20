package com.wanbaohe.aidetect.component

import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.database.aidetect.entity.AiDetectRecordEntity
import com.shifenmiao.model.aidetect.AiDetectSegmentLabel
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.aidetect.service.AiDetectError
import com.wanbaohe.aidetect.service.AiDetectException
import com.wanbaohe.aidetect.service.AiDetectService
import com.wanbaohe.aidetect.service.ImageDetectResult
import com.wanbaohe.aidetect.service.TextDetectResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 文本检测 tab 状态 */
data class TextDetectUiState(
    val input: String = "",
    val isDetecting: Boolean = false,
    val result: TextDetectResult? = null,
    val isDetailExpanded: Boolean = false,
)

/** 图片检测 tab 状态 */
data class ImageDetectUiState(
    val selectedUri: Uri? = null,
    val isDetecting: Boolean = false,
    val result: ImageDetectResult? = null,
    val isDetailExpanded: Boolean = false,
)

/** 历史详情页状态(由记录还原) */
data class HistoryDetailUiState(
    val record: AiDetectRecordEntity,
    val segments: List<AiDetectSegmentLabel> = emptyList(),
    val labelsRatio: Map<String, Float> = emptyMap(),
)

/**
 * AI 检测助手主组件
 *
 * 对外仅暴露一个 Screen 路由;内部用 [currentTab] 切换文本/图片检测 tab
 * (参考万年历路由组件),用 [currentPage] 在主界面与模块内历史页/详情页之间切换。
 */
class AiDetectComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialType: Screen.AiDetect.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val service: AiDetectService,
) : BaseComponent(dispatchersHolder, componentContext) {

    enum class Page { MAIN, HISTORY, HISTORY_DETAIL }

    private val _currentPage = MutableStateFlow(Page.MAIN)
    val currentPage = _currentPage.asStateFlow()

    private val _currentTab = MutableStateFlow(
        initialType ?: Screen.AiDetect.Type.TextDetect
    )
    val currentTab = _currentTab.asStateFlow()

    private val _textState = MutableStateFlow(TextDetectUiState())
    val textState = _textState.asStateFlow()

    private val _imageState = MutableStateFlow(ImageDetectUiState())
    val imageState = _imageState.asStateFlow()

    private val _history = MutableStateFlow<List<AiDetectRecordEntity>>(emptyList())
    val history = _history.asStateFlow()

    private val _historyDetail = MutableStateFlow<HistoryDetailUiState?>(null)
    val historyDetail = _historyDetail.asStateFlow()

    /** 一次性错误事件(字符串资源 id 由 UI 映射),UI 消费后调用 [consumeError] */
    private val _errorEvent = MutableStateFlow<AiDetectError?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    init {
        service.observeHistory()
            .onEach { _history.value = it }
            .launchIn(componentScope)
    }

    // ── tab 切换 ─────────────────────────────────────────────────────────

    fun switchTo(type: Screen.AiDetect.Type) {
        if (type != _currentTab.value) {
            _currentTab.value = type
        }
    }

    // ── 文本检测 ─────────────────────────────────────────────────────────

    fun onTextInputChange(input: String) {
        _textState.update { it.copy(input = input) }
    }

    fun detectText() {
        val input = _textState.value.input.trim()
        if (input.isEmpty() || _textState.value.isDetecting) return
        withAiGate { doDetectText(input) }
    }

    private fun doDetectText(input: String) {
        _textState.update { it.copy(isDetecting = true) }
        componentScope.launch {
            service.detectText(input)
                .onSuccess { result ->
                    _textState.update {
                        it.copy(isDetecting = false, result = result, isDetailExpanded = false)
                    }
                    chargePoints(DETECT_DESC_TEXT)
                }
                .onFailure { error ->
                    _textState.update { it.copy(isDetecting = false) }
                    _errorEvent.value = error.toDetectError()
                }
        }
    }

    fun resetTextDetect() {
        _textState.update { it.copy(result = null, isDetailExpanded = false) }
    }

    fun toggleTextDetail() {
        _textState.update { it.copy(isDetailExpanded = !it.isDetailExpanded) }
    }

    // ── 图片检测 ─────────────────────────────────────────────────────────

    fun onImageSelected(uri: Uri) {
        _imageState.update {
            it.copy(selectedUri = uri, result = null, isDetailExpanded = false)
        }
    }

    fun detectImage() {
        val uri = _imageState.value.selectedUri ?: return
        if (_imageState.value.isDetecting) return
        withAiGate { doDetectImage(uri) }
    }

    private fun doDetectImage(uri: Uri) {
        _imageState.update { it.copy(isDetecting = true) }
        componentScope.launch {
            service.detectImage(uri)
                .onSuccess { result ->
                    _imageState.update {
                        it.copy(isDetecting = false, result = result, isDetailExpanded = false)
                    }
                    chargePoints(DETECT_DESC_IMAGE)
                }
                .onFailure { error ->
                    _imageState.update { it.copy(isDetecting = false) }
                    _errorEvent.value = error.toDetectError()
                }
        }
    }

    fun resetImageDetect() {
        _imageState.update { it.copy(result = null, isDetailExpanded = false) }
    }

    fun toggleImageDetail() {
        _imageState.update { it.copy(isDetailExpanded = !it.isDetailExpanded) }
    }

    // ── 登录与积分门控 ─────────────────────────────────────────────────────

    /**
     * AI 功能登录与积分规范(见 onebox-doc/AGENTS.md「AI 功能登录与积分规范」):
     * 检测前登录门控 + 积分预估闸门, 未登录先弹登录(成功后重试检测),
     * 积分不足 toast + 弹购买面板, 不发起请求。
     */
    private fun withAiGate(action: () -> Unit) {
        if (!TokenStorage.isLogin()) {
            ActionUtils.showLogin(source = AI_DETECT_SOURCE) { withAiGate(action) }
            return
        }
        ActionUtils.checkPointsAndDo(
            point = DETECT_POINTS,
            onFailure = {
                ActionUtils.showToast(R.string.no_points)
                AppEventBus.emit(
                    MainClickEvent(
                        from = MainClickEventFrom.AI_DETECT,
                        type = MainShowType.BUY_COFFEE,
                    )
                )
            },
            onSuccess = action,
        )
    }

    /** 检测成功后扣减固定积分; 扣减失败静默(与其他一次性 AI 功能一致) */
    private fun chargePoints(desc: String) {
        runCatching {
            BaseUtils.consumePoints(
                degree = DETECT_POINTS,
                desc = desc,
                source = AI_DETECT_SOURCE,
            )
        }
    }

    // ── 历史 ─────────────────────────────────────────────────────────────

    fun openHistory() {
        _currentPage.value = Page.HISTORY
    }

    fun openHistoryDetail(record: AiDetectRecordEntity) {
        val detail = service.parseTextDetail(record.detailJson)
        _historyDetail.value = HistoryDetailUiState(
            record = record,
            segments = detail?.segmentLabels.orEmpty(),
            labelsRatio = detail?.labelsRatio.orEmpty(),
        )
        _currentPage.value = Page.HISTORY_DETAIL
    }

    fun deleteRecord(id: Long) {
        componentScope.launch { service.deleteRecord(id) }
    }

    fun clearHistory() {
        componentScope.launch { service.clearHistory() }
    }

    // ── 导航 ─────────────────────────────────────────────────────────────

    /** 整合的返回逻辑:详情→历史→主界面→退出 */
    fun handleBack() {
        when (_currentPage.value) {
            Page.HISTORY_DETAIL -> _currentPage.value = Page.HISTORY
            Page.HISTORY -> _currentPage.value = Page.MAIN
            Page.MAIN -> onGoBack()
        }
    }

    fun consumeError() {
        _errorEvent.value = null
    }

    private fun Throwable.toDetectError(): AiDetectError =
        (this as? AiDetectException)?.error ?: AiDetectError.NETWORK

    private companion object {
        /** 单次检测固定扣减积分 */
        const val DETECT_POINTS = 50
        const val AI_DETECT_SOURCE = "AiDetect"
        const val DETECT_DESC_TEXT = "AI文本检测"
        const val DETECT_DESC_IMAGE = "AI图片检测"
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialType: Screen.AiDetect.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AiDetectComponent
    }
}
