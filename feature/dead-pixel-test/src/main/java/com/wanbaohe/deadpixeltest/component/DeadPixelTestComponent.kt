package com.wanbaohe.deadpixeltest.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 轮播颜色列表（顺序：白→黑→红→绿→蓝，覆盖所有亮/暗坏点场景） */
val DEAD_PIXEL_COLORS: List<TestColor> = listOf(
    TestColor(Color.White, "white"),
    TestColor(Color.Black, "black"),
    TestColor(Color(0xFFFF0000), "red"),
    TestColor(Color(0xFF00FF00), "green"),
    TestColor(Color(0xFF0000FF), "blue"),
)

/** 单个测试颜色的数据包，携带本地化 key（供 Compose 端查询 stringResource） */
@Immutable
data class TestColor(val color: Color, val nameKey: String)

/** 屏幕坏点检测的 UI 状态（不可变快照，供 Compose 高效重组） */
@Immutable
data class DeadPixelUiState(
    /** 当前测试颜色索引 */
    val colorIndex: Int = 0,
    /** 是否正在自动轮播 */
    val isAutoPlaying: Boolean = false,
    /** 是否显示辅助网格（帮助定位像素位置） */
    val showGrid: Boolean = false,
    /** 是否全屏（隐藏 TopAppBar，减少视觉干扰） */
    val isFullScreen: Boolean = false,
    /** 是否首次进入，展示使用说明对话框 */
    val showGuide: Boolean = true,
) {
    val currentTestColor: TestColor get() = DEAD_PIXEL_COLORS[colorIndex]
    val totalColors: Int get() = DEAD_PIXEL_COLORS.size
}

/**
 * 屏幕坏点检测 Component
 *
 * 职责：
 * 1. 管理当前测试颜色索引及相关 UI 状态
 * 2. 控制自动轮播协程（切换间隔 [AUTO_PLAY_INTERVAL_MS]）
 * 3. 暴露无副作用的状态流给 Compose 层
 */
class DeadPixelTestComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    companion object {
        /** 自动轮播间隔（毫秒） */
        const val AUTO_PLAY_INTERVAL_MS = 2000L
    }

    private val _uiState = MutableStateFlow(DeadPixelUiState())
    val uiState = _uiState.asStateFlow()

    private var autoPlayJob: Job? = null

    // ──────────────────────────────────────────────────────────────────────────
    // 公开操作
    // ──────────────────────────────────────────────────────────────────────────

    /** 切换到下一个颜色（循环） */
    fun nextColor() {
        _uiState.value = _uiState.value.let {
            it.copy(colorIndex = (it.colorIndex + 1) % DEAD_PIXEL_COLORS.size)
        }
    }

    /** 切换到上一个颜色（循环） */
    fun prevColor() {
        _uiState.value = _uiState.value.let {
            it.copy(colorIndex = (it.colorIndex - 1 + DEAD_PIXEL_COLORS.size) % DEAD_PIXEL_COLORS.size)
        }
    }

    /** 直接跳转到指定颜色索引 */
    fun selectColor(index: Int) {
        if (index !in DEAD_PIXEL_COLORS.indices) return
        _uiState.value = _uiState.value.copy(colorIndex = index)
    }

    /** 切换自动轮播开关 */
    fun toggleAutoPlay() {
        val current = _uiState.value
        if (current.isAutoPlaying) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    /** 切换辅助网格显示 */
    fun toggleGrid() {
        _uiState.value = _uiState.value.copy(showGrid = !_uiState.value.showGrid)
    }

    /** 切换全屏模式 */
    fun toggleFullScreen() {
        _uiState.value = _uiState.value.copy(isFullScreen = !_uiState.value.isFullScreen)
    }

    /** 关闭使用说明对话框 */
    fun dismissGuide() {
        _uiState.value = _uiState.value.copy(showGuide = false)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 内部
    // ──────────────────────────────────────────────────────────────────────────

    private fun startAutoPlay() {
        autoPlayJob?.cancel()
        _uiState.value = _uiState.value.copy(isAutoPlaying = true)
        autoPlayJob = componentScope.launch {
            while (true) {
                delay(AUTO_PLAY_INTERVAL_MS)
                nextColor()
            }
        }
    }

    private fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
        _uiState.value = _uiState.value.copy(isAutoPlaying = false)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): DeadPixelTestComponent
    }
}

