package com.t8rin.imagetoolbox.core.domain.startup

/**
 * 应用启动阶段枚举。
 *
 * 把启动过程建模为明确的阶段管道，便于各层（Activity、Component、Composable）
 * 感知当前阶段并做出响应。阶段只能向前推进，不能回退。
 *
 * Phase 推进时序：
 * 1. [SPLASH_RENDERING] — Application.onCreate 完成，Activity.onCreate 中已调用 setContent
 * 2. [SHELL_VISIBLE]   — SplashScreen 退出动画开始，首帧导航框架已可见
 * 3. [CONTENT_HYDRATED]— 业务内容（childStack 真实 child）已填充
 * 4. [OVERLAY_MOUNTED] — 全局 Overlay（对话框、BottomSheet 等）已挂载
 * 5. [COMPLETE]        — 所有后台初始化完成
 */
enum class StartupPhase {
    SPLASH_RENDERING,
    SHELL_VISIBLE,
    CONTENT_HYDRATED,
    OVERLAY_MOUNTED,
    COMPLETE;

    /**
     * 当前阶段是否已达到或超过 [phase]。
     */
    fun isAtLeast(phase: StartupPhase): Boolean = ordinal >= phase.ordinal
}
