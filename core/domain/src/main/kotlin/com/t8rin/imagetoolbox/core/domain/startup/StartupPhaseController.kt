package com.t8rin.imagetoolbox.core.domain.startup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 启动阶段中央控制器。
 *
 * 单例，通过 Hilt 注入到需要感知启动阶段的各层。
 * 提供声明式的启动阶段管理，是启动优化的统一挂载点。
 *
 * 使用方式：
 * ```
 * @Inject lateinit var startupPhaseController: StartupPhaseController
 *
 * // 推进阶段
 * startupPhaseController.advanceTo(StartupPhase.SHELL_VISIBLE)
 *
 * // 订阅阶段
 * val phase by startupPhaseController.phase.collectAsState()
 * if (phase.isAtLeast(StartupPhase.CONTENT_HYDRATED)) { ... }
 * ```
 *
 * 与 [com.t8rin.imagetoolbox.core.domain.performance.StartupTrace] 配合，
 * 可以按阶段输出耗时诊断数据。
 */
@Singleton
class StartupPhaseController @Inject constructor() {

    private val _phase = MutableStateFlow(StartupPhase.SPLASH_RENDERING)
    val phase: StateFlow<StartupPhase> = _phase.asStateFlow()

    /**
     * 推进到指定阶段。只能向前推进（ordinal 更大的阶段），
     * 重复调用或回退调用会被忽略。
     */
    fun advanceTo(phase: StartupPhase) {
        if (phase.ordinal > _phase.value.ordinal) {
            _phase.value = phase
        }
    }
}
