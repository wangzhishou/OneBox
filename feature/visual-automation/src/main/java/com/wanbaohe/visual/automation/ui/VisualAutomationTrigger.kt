/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
