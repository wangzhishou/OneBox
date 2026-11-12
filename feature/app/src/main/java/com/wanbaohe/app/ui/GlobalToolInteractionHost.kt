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

package com.wanbaohe.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shifenmiao.common.logic.AppComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
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
