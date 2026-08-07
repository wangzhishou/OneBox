package com.wanbaohe.visual.automation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.t8rin.logger.makeLog
import com.wanbaohe.visual.automation.VisualAutomationController

/**
 * 视觉自动化触发组件。
 * 使用时将该组件放入页面中，当状态为 RUNNING 时显示遮罩和加载指示器。
 *
 * 使用示例：
 * ```
 * @Composable
 * fun MyScreen(component: MyComponent) {
 *     val controller = remember { component.visualAutomationController }
 *
 *     // 自动化任务触发
 *     Button(onClick = {
 *         val activity = context.findActivity()
 *         activity?.let { controller.startTask(it, "帮我完成这个页面的操作") }
 *     }) {
 *         Text("启动 AI 自动化")
 *     }
 *
 *     // 显示自动化状态遮罩
 *     VisualAutomationTrigger(controller)
 * }
 * ```
 */
@Composable
fun VisualAutomationTrigger(
    controller: VisualAutomationController,
    modifier: Modifier = Modifier
) {
    val state by controller.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        makeLog { "VisualAutomationTrigger state: $state" }
    }

    when (state) {
        VisualAutomationController.AutomationState.CAPTURING,
        VisualAutomationController.AutomationState.WAITING_AI,
        VisualAutomationController.AutomationState.EXECUTING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            // 不显示任何内容
        }
    }
}
