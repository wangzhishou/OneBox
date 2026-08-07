package com.wanbaohe.app.ui

import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent

/**
 * 全局 AI 宿主。
 *
 * 这里把 App 顶层与 AI 相关的 UI/状态订阅收拢到一个独立重组函数里，目的有两个：
 * 1. 让 [com.wanbaohe.app.AppContent] 保持干净，AI 逻辑不再散落在启动主函数中
 * 2. 只有真的进入 AI 使用场景（AI 底部浮层 / AITabChatScreen）时才挂载，降低冷启动期开销
 *
 * 注意：
 * - 这个宿主现在只负责 AI 聊天面板与 PiP 相关能力
 * - 工具确认、动态表单、工具导航等全局交互已迁移到 GlobalToolInteractionHost
 */
@Composable
fun GlobalAIHost(
    rootComponent: RootComponent,
    appComponent: AppComponent,
    showAIChatOverlay: Boolean,
    isPortrait: Boolean,
) {
    val aiChatComponent = rootComponent.globalAIChatComponent

    AttachGlobalAIPip(aiChatComponent = aiChatComponent)

    LaunchedEffect(isPortrait, showAIChatOverlay) {
        if (!isPortrait && showAIChatOverlay) {
            appComponent.hideAIChat()
        }
    }

    if (isPortrait && showAIChatOverlay) {
        AIChatModalBottomSheet(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent
        )
    }
}

@Composable
private fun AttachGlobalAIPip(
    aiChatComponent: AIChatComponent
) {
    val activity = LocalActivity.current ?: return
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val chatActive = chatUIState.chatActive

    LaunchedEffect(chatActive) {
        val paramsBuilder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(9, 16))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            paramsBuilder.setAutoEnterEnabled(chatActive)
        }
        activity.setPictureInPictureParams(paramsBuilder.build())
    }
}
