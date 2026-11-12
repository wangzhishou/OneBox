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
 * - 这个宿主现在只负责 AI 聊天面板本身
 * - 画中画参数由 AppActivity 统一设置（它同时要照顾视频播放，两个页面各写一份会互相覆盖）
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
