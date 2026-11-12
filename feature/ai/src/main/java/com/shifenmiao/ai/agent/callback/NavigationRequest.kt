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

package com.shifenmiao.ai.agent.callback

import com.wanbaohe.core.ui.utils.navigation.Screen

/**
 * 导航请求密封类
 *
 * ToolCallbackRouter 发出的导航请求，由 AIChatComponent 观察并处理。
 */
sealed class NavigationRequest {
    /**
     * 子工具调用请求
     *
     * @param callbackId 回调唯一标识
     * @param toolName 目标工具名
     * @param arguments 传递给子工具的参数（JSON 字符串）
     */
    data class ToolCall(
        val callbackId: String,
        val toolName: String,
        val arguments: String
    ) : NavigationRequest()

    /**
     * Screen 导航请求
     *
     * @param callbackId 回调唯一标识
     * @param screen 要导航到的 Screen（已注入 onResult lambda）
     */
    data class ScreenNavigation(
        val callbackId: String,
        val screen: Screen
    ) : NavigationRequest()

    /**
     * 仅打开 Screen，不等待结果回调。
     */
    data class OpenScreen(
        val requestId: String,
        val screen: Screen
    ) : NavigationRequest()
}
