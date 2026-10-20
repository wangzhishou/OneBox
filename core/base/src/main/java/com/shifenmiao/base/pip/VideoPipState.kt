package com.shifenmiao.base.pip

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 视频播放中的画中画规格。
 *
 * @param width 视频宽（像素），0 表示还没拿到尺寸
 * @param height 视频高（像素）
 */
data class VideoPipSpec(
    val width: Int,
    val height: Int,
    /** 当前是否在播放：暂停时仍然保留动作（小窗里要能点继续），但不自动进小窗 */
    val isPlaying: Boolean = true,
) {
    /**
     * 换算成系统接受的画中画比例。
     *
     * Android 只接受 1:2.39 ~ 2.39:1 之间的比例，超出会直接拒绝进入 PiP；
     * 竖屏视频按原比例，超宽电影裁到 2.39:1，拿不到尺寸时按 9:16 兜底。
     */
    fun aspectRatio(): Pair<Int, Int> {
        if (width <= 0 || height <= 0) return 9 to 16
        return if (width >= height) {
            if (width.toFloat() / height > MAX_RATIO) 239 to 100 else width to height
        } else {
            if (height.toFloat() / width > MAX_RATIO) 100 to 239 else width to height
        }
    }

    private companion object {
        const val MAX_RATIO = 2.39f
    }
}

/**
 * 全 App 唯一一份"现在有没有视频在放、比例多少"的状态。
 *
 * 播放组件写，AppActivity 读并统一设置画中画参数（Android 12+ 自动进入，12 以下在
 * onUserLeaveHint 里手动进入）。
 *
 * 用 [owner] 区分声明者：Decompose 栈里可能同时挂着多个媒体页（旧页面还没销毁），
 * 停掉旧页面时不能把新页面刚登记的画中画状态一起清掉，所以清空只对"当前所有者"生效。
 */
@Singleton
class VideoPipState @Inject constructor() {

    private val _spec = MutableStateFlow<VideoPipSpec?>(null)
    val spec: StateFlow<VideoPipSpec?> = _spec.asStateFlow()

    private var owner: Any? = null

    private val _inPip = MutableStateFlow(false)

    /** Activity 是否正处于画中画窗口；由 AppActivity 在回调里写入，页面直接订阅。 */
    val inPip: StateFlow<Boolean> = _inPip.asStateFlow()

    /** 视频正在播放时登记画面比例，[owner] 通常是播放页组件。 */
    fun update(owner: Any, width: Int, height: Int, isPlaying: Boolean = true) {
        this.owner = owner
        _spec.value = VideoPipSpec(width = width, height = height, isPlaying = isPlaying)
    }

    /** 暂停/退出/切到音频时清空；只有当前所有者能清，避免旧页面误伤。 */
    fun clear(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        if (_spec.value != null) _spec.value = null
    }

    /** AppActivity 的 onPictureInPictureModeChanged 里同步。 */
    fun setInPip(value: Boolean) {
        if (_inPip.value != value) _inPip.value = value
    }

    /** 无条件清空（进程级兜底，例如播放器被整体停止）。 */
    fun clear() {
        owner = null
        if (_spec.value != null) _spec.value = null
    }

    val isActive: Boolean get() = _spec.value != null
}
