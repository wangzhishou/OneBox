package com.shifenmiao.marquee.screenLogic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.marquee.MarqueePresentationMode
import com.shifenmiao.model.marquee.MarqueeSettings
import com.shifenmiao.storage.MarqueeSettingsStore
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MarqueeComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    dispatchersHolder: DispatchersHolder,
    val settingsManager: SettingsManager,
) : BaseComponent(dispatchersHolder, componentContext) {
    private val _settingsState = mutableStateOf(SettingsState.Default)
    val settingsState: SettingsState by _settingsState
    private val _marqueeSettingsState = MutableStateFlow(MarqueeSettings(
        marqueeTextSize = 100f
    ))
    val marqueeSettingsState: StateFlow<MarqueeSettings> = _marqueeSettingsState.asStateFlow()

    private val _marqueeHistory = MutableStateFlow<List<String>>(emptyList())
    val marqueeHistory: StateFlow<List<String>> = _marqueeHistory.asStateFlow()

    init {
        // 使用 Eagerly-started StateFlow 同步读取，避免 runBlocking 阻塞主线程
        _settingsState.value = settingsManager.settingsState.value
        settingsManager.settingsState.onEach {
            _settingsState.value = it
        }.launchIn(componentScope)
        initStateFormLocalStore()
        loadHistory()
    }

    private fun loadHistory() {
        _marqueeHistory.value = MarqueeSettingsStore.getMarqueeHistory()
    }

    private fun initStateFormLocalStore() {
        val localMarqueeSettings = MarqueeSettingsStore.loadLocalMarqueeSettings()
        if (localMarqueeSettings != null) {
            val textSize = if (localMarqueeSettings.marqueeTextSize <= 0f) 100f else localMarqueeSettings.marqueeTextSize
            _marqueeSettingsState.value = localMarqueeSettings.copy(marqueeTextSize = textSize)
        }
    }

    fun saveState() {
        MarqueeSettingsStore.saveLocalMarqueeSettings(_marqueeSettingsState.value)

        val text = _marqueeSettingsState.value.marqueeText.trim()
        if (text.isNotEmpty()) {
            MarqueeSettingsStore.addMarqueeHistory(text)
        }

        loadHistory()
    }

    fun onTextContentChange(string: String) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeText = string
        )
    }

    fun onTextColorChange(textColor: Color) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeTextColor = textColor.toArgb()
        )
    }

    fun onBackgroundColorChange(backColor: Color) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeBackgroundColor = backColor.toArgb()
        )
    }

    fun onTextVelocityChange(textVelocity: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeTextVelocity = textVelocity
        )
    }

    fun onTextSpacingChange(spacing: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeTextSpacing = spacing
        )
    }

    fun onTextSizeChange(size: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeTextSize = size
        )
    }

    fun onLetterSpacingChange(spacing: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            letterSpacing = spacing
        )
    }

    fun onBlinkEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeBlinkEnabled = enabled
        )
    }

    fun onBlinkSpeedChange(speed: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeBlinkSpeed = speed
        )
    }

    fun onBackgroundBlinkEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            backgroundBlinkEnabled = enabled
        )
    }

    fun onMirrorEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeMirrorEnabled = enabled
        )
    }

    fun onBoldEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            marqueeBoldEnabled = enabled
        )
    }

    fun onPresentationModeChange(mode: MarqueePresentationMode) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            presentationMode = mode
        )
    }

    fun onTypewriterCharsPerSecondChange(value: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            typewriterCharsPerSecond = value
        )
    }

    fun onOneCharHoldMillisChange(value: Int) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            oneCharHoldMillis = value
        )
    }

    fun onPagedAutoAdvanceEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            pagedAutoAdvanceEnabled = enabled
        )
    }

    fun onPagedIntervalMillisChange(value: Int) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            pagedIntervalMillis = value
        )
    }

    fun onLockLandscapeInFullscreenChange(locked: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            lockLandscapeInFullscreen = locked
        )
        // Persist immediately so user choice survives activity recreation/next launch
        MarqueeSettingsStore.saveLocalMarqueeSettings(_marqueeSettingsState.value)
    }

    // Clock 时钟模式配置
    fun onClockShowSecondsChange(show: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            clockShowSeconds = show
        )
    }

    fun onClockShowDateChange(show: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            clockShowDate = show
        )
    }

    fun onClockUse24HourChange(use24Hour: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            clockUse24Hour = use24Hour
        )
    }

    // Countdown 倒计时模式配置
    fun onCountdownSecondsChange(seconds: Int) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            countdownSeconds = seconds
        )
    }

    fun onCountdownShowMillisChange(show: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            countdownShowMillis = show
        )
    }

    // Bouncing 弹跳模式配置
    fun onBouncingSpeedChange(speed: Float) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            bouncingSpeed = speed
        )
    }

    // 背景效果
    fun onFireworksEnabledChange(enabled: Boolean) {
        _marqueeSettingsState.value = _marqueeSettingsState.value.copy(
            fireworksEnabled = enabled
        )
    }

    /**
     * 进入全屏等场景时，强制从本地存储刷新一次，避免跨 Activity/retained 组件拿到旧 state。
     */
    fun reloadFromLocalStore() {
        val localMarqueeSettings = MarqueeSettingsStore.loadLocalMarqueeSettings() ?: return
        val textSize = if (localMarqueeSettings.marqueeTextSize <= 0f) 100f else localMarqueeSettings.marqueeTextSize
        // spacing 做下防呆：负值无意义，过大也会让效果像“不生效”
        val spacing = localMarqueeSettings.marqueeTextSpacing.coerceIn(0f, 200f)
        _marqueeSettingsState.value = localMarqueeSettings.copy(
            marqueeTextSize = textSize,
            marqueeTextSpacing = spacing
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): MarqueeComponent
    }
}
