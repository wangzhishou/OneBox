package com.wanbaohe.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent

/**
 * 全局工具交互宿主。
 *
 * 承担三件事：
 * 1. 消费工具触发的全局导航请求
 * 2. 渲染工具等待用户处理的交互 UI（表单/确认等）
 * 3. 消费 AI 触发的视觉效果（撒花、炸弹等）
 */
@Composable
fun GlobalToolInteractionHost(
    rootComponent: RootComponent,
    appComponent: AppComponent
) {
    val toolUiHost = rootComponent.globalToolUiHost
    val effectHost = rootComponent.effectHost
    val pendingNavigations by toolUiHost.pendingScreenNavigations.collectAsState()

    LaunchedEffect(pendingNavigations.firstOrNull()?.requestId) {
        pendingNavigations.firstOrNull()?.let { request ->
            appComponent.onNavigate(request.screen)
            toolUiHost.acknowledgeScreenNavigation(request.requestId)
        }
    }

    // 消费视觉效果请求
    val effectRequest by effectHost.effectRequest.collectAsState()
    LaunchedEffect(effectRequest) {
        effectRequest?.let { req ->
            when (req.effect) {
                "confetti" -> AppToastHost.showConfetti()
                "bomb" -> {
                    AppToastHost.showToast(req.message ?: "")
                }
                "toast" -> AppToastHost.showToast(req.message ?: "")
            }
            effectHost.consumeEffect()
        }
    }

    AIGlobalActionOverlay(
        toolUiHost = toolUiHost
    )
}
