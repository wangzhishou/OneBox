package com.wanbaohe.decisionwheel.component

import android.os.SystemClock
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.decisionwheel.data.DecisionWheelPresetsProvider
import com.wanbaohe.decisionwheel.data.WheelRepository
import com.wanbaohe.decisionwheel.data.WheelSettingsHolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 转盘 Spin 主界面状态
 */
@Immutable
data class DecisionWheelSpinUiState(
    val wheels: List<DecisionWheel> = emptyList(),
    val currentWheelId: String? = null,
    val isSpinning: Boolean = false,
    /** 已停稳的结果扇区下标；null 表示本次还没转过 */
    val settledIndex: Int? = null,
    val showResult: Boolean = false
) {
    val currentWheel: DecisionWheel?
        get() = wheels.firstOrNull { it.id == currentWheelId } ?: wheels.firstOrNull()

    /** 可参与抽取的选项数（用于判定"全部抽完"） */
    val activeCount: Int
        get() = currentWheel?.options?.count { it.enabled && it.weight > 0f } ?: 0
}

/**
 * 转盘主界面组件。
 *
 * 职责边界：
 * - 决定"转到哪个结果"（按权重抽样），并反算出对齐指针所需的角度 —— 结果唯一真相在这里。
 * - UI 只负责把角度补间过去，结束后回调 [onSpinSettled]。
 */
class DecisionWheelSpinComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("onGoBack") private val onGoBack: () -> Unit,
    @Assisted("onOpenWheelList") private val onOpenWheelList: () -> Unit,
    @Assisted private val onNavigate: (SpinDestination) -> Unit,
    private val repository: WheelRepository,
    private val presetsProvider: DecisionWheelPresetsProvider,
    private val settingsHolder: WheelSettingsHolder,
    private val audioPlayer: NetworkAudioPlayer,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    companion object {
        /**
         * 音效托管在 R2(bucket onebox-images 的 audio/decisionwheel/ 路径)，国内海外同地址。
         * 合成脚本与源文件见 onebox-doc/audio/decisionwheel/。
         *
         * 文件名带版本号:远程是 immutable 缓存、本地又有永久缓存,改音色必须升版本号,
         * 否则装过的用户拿不到新文件。
         */
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/decisionwheel"
        private const val SOUND_SPIN = "$SOUND_BASE/spin_v2.ogg"
        private const val SOUND_RESULT = "$SOUND_BASE/result_v2.ogg"
        private val ALL_SOUNDS = listOf(SOUND_SPIN, SOUND_RESULT)

        /** 停稳时把转动声淡出,别"啪"一下掐断 */
        private const val SPIN_FADE_OUT_MS = 420L

        /** 变速斜坡的采样间隔;再密也只是多写几次 PlaybackParams,没必要 */
        private const val SPEED_RAMP_STEP_MS = 50L

        /**
         * 速度映射区间。
         *
         * 1f = 原速(咔哒 ~12.5 记/秒),慢到底 0.42f。再低听起来就不是"盘在减速"
         * 而是"磁带没电了",反而出戏。
         */
        private const val SPEED_MAX = 1f
        private const val SPEED_MIN = 0.42f

        /** 缓动曲线的峰值斜率,用来把瞬时速度归一到 [0,1];取小一点让高速段更饱和 */
        private const val SPEED_PEAK_SLOPE = 1.6f

        /**
         * 旋转补间曲线。
         *
         * 盘面和声音必须共用同一条:声音的减速节奏是从这条曲线的斜率推出来的,
         * 两边各写一份迟早会 drift,转完停稳了声音还在转。
         */
        val SPIN_EASING: Easing = CubicBezierEasing(0.33f, 0f, 0.2f, 1f)
    }

    private val _uiState = MutableStateFlow(DecisionWheelSpinUiState())
    val uiState = _uiState.asStateFlow()

    /** 转动声的减速斜坡;停稳/取消时必须掐掉,否则它还在继续压速度 */
    private var spinSoundJob: Job? = null

    val settings: StateFlow<WheelSettings> = settingsHolder.settings

    /**
     * 盘面当前角度（度，累积值）。
     *
     * 角度属于 UI 的动画状态，但它需要在切 tab 导致界面重组后仍能复原，所以在这里留一份快照。
     * 每次旋转结束、每次拖动结束时由界面回写。
     */
    var rotationDeg: Float = 0f
        private set

    fun setRotation(value: Float) {
        rotationDeg = value
    }

    init {
        initWheel()
        observeWheels()
        observeSettings()
        // 后台预热音效（下载到本地缓存），首次旋转基本已就绪，避免第一下没声音
        componentScope.launch { ALL_SOUNDS.forEach { audioPlayer.warmUp(it) } }
        componentContext.lifecycle.doOnDestroy {
            spinSoundJob?.cancel()
            spinSoundJob = null
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    private fun initWheel() {
        componentScope.launch {
            var wheels = repository.getAllWheels().firstOrNull()
            if (wheels.isNullOrEmpty()) {
                createPresets()
                wheels = repository.getAllWheels().firstOrNull()
            }
            _uiState.update {
                it.copy(
                    wheels = wheels.orEmpty(),
                    currentWheelId = it.currentWheelId ?: defaultWheelId(wheels.orEmpty())
                )
            }
        }
    }

    /**
     * 默认选中哪个转盘：优先预置列表的第一项（"喝酒惩罚"），找不到再退回库里第一条。
     *
     * 老用户库里的预置是按旧顺序建的，纯按插入序取第一项会拿到"吃什么"。
     */
    private fun defaultWheelId(wheels: List<DecisionWheel>): String? {
        val preferred = runCatching { presetsProvider.defaultPresetTitle() }.getOrNull()
        return wheels.firstOrNull { it.title == preferred }?.id ?: wheels.firstOrNull()?.id
    }

    private fun observeWheels() {
        componentScope.launch {
            repository.getAllWheels().collect { wheels ->
                _uiState.update {
                    val stillExists = wheels.any { w -> w.id == it.currentWheelId }
                    it.copy(
                        wheels = wheels,
                        currentWheelId = if (stillExists) it.currentWheelId else defaultWheelId(wheels)
                    )
                }
                restoreRemovedIfDisabled()
            }
        }
    }

    /**
     * 关掉"抽后移除"就把之前摘掉的选项全部放回。
     *
     * 不做这一步会留下一种无解状态：盘面上一堆灰扇区永远抽不到，界面上又没有恢复入口
     * （入口只在开关打开时才显示），用户只能一个个转盘删掉重建。这里顺手把历史遗留的
     * 禁用项也一并清掉，老版本切过开关的用户升级后能自动恢复。
     */
    private fun observeSettings() {
        componentScope.launch {
            settingsHolder.settings.collect { restoreRemovedIfDisabled() }
        }
    }

    private fun restoreRemovedIfDisabled() {
        if (settingsHolder.current().removeOnSpin) return
        // 按"全部转盘"判定而不是当前转盘：只清当前的话，切到别的转盘仍是灰扇区。
        // 判空是必须的 —— 无脑 UPDATE 会再次触发本 Flow，形成死循环。
        val hasRemoved = _uiState.value.wheels.any { w -> w.options.any { !it.enabled } }
        if (hasRemoved) {
            componentScope.launch { repository.resetAllOptionsEnabled() }
        }
    }

    /**
     * 创建预置转盘。
     *
     * 标题/选项名全部来自 `strings.xml`；颜色按"基色 -> 混合色"规则生成并直接落库。
     */
    private suspend fun createPresets() {
        val listColor = listOf(
            Color(0xFFA3F6F6),
            Color(0xFFF5A3F6),
            Color(0xFFD8F6A3),
            Color(0xFFF6D0A3),
            Color(0xFFA3B7F6),
            Color(0xFFB3F2EA)
        )

        val presets = presetsProvider.presets().map { preset ->
            val baseColor = listColor.randomOrNull() ?: Color(AppTheme.colorScheme.primaryContainer.toArgb())
            val backgrounds = ColorGenerator.generateSegmentBackgrounds(
                baseColor = baseColor,
                count = preset.options.size
            )
            DecisionWheel(
                title = preset.title,
                options = preset.options.mapIndexed { index, optionName ->
                    WheelOption(
                        name = optionName,
                        color = backgrounds.getOrNull(index) ?: baseColor
                    )
                }
            )
        }
        presets.forEach { repository.saveWheel(it) }
    }

    // ─── 旋转 ────────────────────────────────────────────────────────────────

    /**
     * 生成一次旋转计划：**先按权重定结果**，再反算让该扇区中心对齐 12 点指针的目标角度。
     *
     * 盘面绘制约定（`DecisionWheelCanvas`）：扇区 i 的起始角 = `i * sector - 90`，整体再 `rotate(rotation)`。
     * 令扇区中心落在 12 点（-90°）可解得 `rotation ≡ -i*sector - sector/2 (mod 360)`。
     *
     * @param currentRotation 当前盘面角度（度，累积值）
     * @return 计划；选项不足或全部被移除时返回 null
     */
    fun planSpin(currentRotation: Float): SpinPlan? {
        val wheel = _uiState.value.currentWheel ?: return null
        val options = wheel.options
        if (options.size < 2) return null

        val winnerIndex = weightedIndex(options)
        if (winnerIndex < 0) return null

        val sector = 360f / options.size
        val desired = (-winnerIndex * sector - sector / 2f).mod(360f)
        val delta = (desired - currentRotation.mod(360f) + 360f).mod(360f)

        val duration = settingsHolder.current().spinDurationMillis
        val turns = (5 + Random.nextInt(4))
        val target = currentRotation + turns * 360f + delta

        return SpinPlan(
            winnerIndex = winnerIndex,
            targetRotation = target,
            durationMillis = duration,
            turns = turns
        )
    }

    /**
     * 按权重抽取下标；全部权重为 0 时退化为等概率。
     */
    private fun weightedIndex(options: List<WheelOption>): Int {
        val candidates = options.mapIndexedNotNull { index, option ->
            if (option.enabled && option.weight > 0f) index to option.weight.toDouble() else null
        }
        if (candidates.isEmpty()) return -1

        val total = candidates.sumOf { it.second }
        if (total <= 0.0) return candidates.random().first

        var r = Random.nextDouble() * total
        candidates.forEach { (index, weight) ->
            r -= weight
            if (r <= 0.0) return index
        }
        return candidates.last().first
    }

    /**
     * 开始旋转（仅切状态，角度由 [planSpin] 决定）。
     *
     * @param durationMillis 本次动画时长，由界面连同甩动力度一起算好传进来 ——
     *                       转动声的减速节奏要跟盘面严格同步，时长必须对齐。
     */
    fun startSpinning(durationMillis: Int) {
        if (_uiState.value.isSpinning) return
        _uiState.update {
            it.copy(isSpinning = true, showResult = false, settledIndex = null)
        }
        if (settingsHolder.current().soundEnabled) {
            startSpinSound(durationMillis)
        }
    }

    /**
     * 起转并让转动声跟着盘面减速。
     *
     * 声音是固定节奏的 2s 循环,不加处理就是"盘快停了咔哒还一样密",很假。
     * 这里按 [SPIN_EASING] 的瞬时斜率推播放速度:斜率大(走得快)速度高,
     * 收尾斜率趋零时压到 [SPEED_MIN],跟盘面一起停下来。
     */
    private fun startSpinSound(durationMillis: Int) {
        spinSoundJob?.cancel()
        spinSoundJob = componentScope.launch(Dispatchers.Main) {
            audioPlayer.playBackground(SOUND_SPIN)
            val startAt = SystemClock.uptimeMillis()
            val total = durationMillis.coerceAtLeast(1)
            while (isActive) {
                val t = ((SystemClock.uptimeMillis() - startAt).toFloat() / total).coerceIn(0f, 1f)
                audioPlayer.setBackgroundSpeed(spinSpeedAt(t))
                if (t >= 1f) break
                delay(SPEED_RAMP_STEP_MS)
            }
        }
    }

    /**
     * 缓动曲线在 [t] 处的瞬时斜率 —— 也就是盘面此刻的角速度(归一化,均值为 1)。
     * 中心差分足够用,解析导数没意义:这条曲线本身就只是个手感。
     */
    private fun spinSpeedAt(t: Float): Float {
        val h = 0.02f
        val lo = (t - h).coerceAtLeast(0f)
        val hi = (t + h).coerceAtMost(1f)
        val slope = if (hi <= lo) 0f else (SPIN_EASING.transform(hi) - SPIN_EASING.transform(lo)) / (hi - lo)
        val norm = (slope / SPEED_PEAK_SLOPE).coerceIn(0f, 1f)
        return SPEED_MIN + (SPEED_MAX - SPEED_MIN) * norm
    }

    /**
     * 动画结束回调：写历史、更新使用记录、按需移除该选项。
     */
    fun onSpinSettled(winnerIndex: Int) {
        componentScope.launch {
            val wheel = _uiState.value.currentWheel ?: return@launch
            val option = wheel.options.getOrNull(winnerIndex) ?: return@launch

            repository.saveHistory(wheel.id, wheel.title, option)
            repository.updateWheelUsage(wheel.id)

            if (settingsHolder.current().removeOnSpin) {
                repository.setOptionEnabled(option.id, false)
            }

            _uiState.update {
                it.copy(isSpinning = false, settledIndex = winnerIndex, showResult = true)
            }

            // 结果已落库并展示 = 用户刚拿到价值,报给应用内评分弹层做门控判断
            // (真正弹不弹由 InAppReviewPrompt 的次数门槛/冷却/Play 配额决定)
            ReviewPromptHost.notifySuccess(ReviewPromptTrigger.DECISION_WHEEL_SUCCESS)

            // 停稳:转动声淡出,同时补一记结果揭示音,两段交叉着走
            spinSoundJob?.cancel()
            spinSoundJob = null
            audioPlayer.fadeOutBackground(SPIN_FADE_OUT_MS)
            if (settingsHolder.current().soundEnabled) playSound(SOUND_RESULT)
        }
    }

    /**
     * 播放音效。播放失败静默吞掉（网络问题不该影响转盘本身能用）。
     */
    private fun playSound(url: String, isBackground: Boolean = false) {
        componentScope.launch {
            runCatching {
                if (isBackground) audioPlayer.playBackground(url) else audioPlayer.playEffect(url)
            }
        }
    }

    /**
     * 界面被销毁（切 tab、退出模块）时调用。
     *
     * 旋转动画的 LaunchedEffect 会随界面一起取消，[onSpinSettled] 也就永远不会执行；
     * 不复位的话 `isSpinning` 会永久卡在 true，切回来转盘按钮消失、再也转不动。
     */
    fun cancelSpin() {
        // 界面被销毁时旋转 BGM 必须跟着停，否则音乐会在后台一直循环
        spinSoundJob?.cancel()
        spinSoundJob = null
        audioPlayer.stopBackground()
        if (_uiState.value.isSpinning) {
            _uiState.update { it.copy(isSpinning = false) }
        }
    }

    /** 关掉结果卡 */
    fun dismissResult() {
        _uiState.update { it.copy(showResult = false) }
    }

    /** 恢复所有被移除的选项 */
    fun restoreRemovedOptions() {
        val wheel = _uiState.value.currentWheel ?: return
        componentScope.launch { repository.resetOptionsEnabled(wheel.id) }
    }

    // ─── 导航与设置 ─────────────────────────────────────────────────────────

    fun switchWheel(wheelId: String) {
        _uiState.update {
            it.copy(currentWheelId = wheelId, showResult = false, settledIndex = null)
        }
    }

    fun updateSettings(settings: WheelSettings) {
        settingsHolder.update(settings)
    }

    fun goBack() = onGoBack()
    fun openWheelList() = onOpenWheelList()
    fun openEditor() = onNavigate(SpinDestination.Editor)
    fun openHistory() = onNavigate(SpinDestination.History)
    fun openSettings() = onNavigate(SpinDestination.Settings)

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("onGoBack") onGoBack: () -> Unit,
            @Assisted("onOpenWheelList") onOpenWheelList: () -> Unit,
            onNavigate: (SpinDestination) -> Unit
        ): DecisionWheelSpinComponent
    }
}
