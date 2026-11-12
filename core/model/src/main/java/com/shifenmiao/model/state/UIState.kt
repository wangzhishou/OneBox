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

package com.shifenmiao.model.state

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.model.BuildConfig
import com.shifenmiao.model.webview.WebViewParams

data class UIState(
    /**
     * 显示AI聊天对话框
     */
    val initAIChatAndShow: Boolean = false,
    /**
     * 打开微信小程序回调
     */
    val launchMiniProResp: Boolean = false,
    /**
     * 是不是Debug
     */
    val debug: Boolean = BuildConfig.DEBUG,

    /**
     * 是否能够分享
     */
    val showShare: Boolean = false,

    /**
     * 显示权限提示会话框
     */
    val showPermissionDialog: Boolean = false,

    /**
     * 显示打赏对话框
     */
    val showBuyCoffeeDialog: Boolean = false,

    /**
     * 显示AI模型对话框
     */
    val showAIModelsModalSheet: Boolean = false,

    /**
     * 显示抽屉
     */
    val showDrawer: Boolean = false,

    /**
     * 默认标题栏开始是折叠的
     */
    val topAppBarStartCollapsed: Boolean = true,

    /**
     * 要显示blog的ID
     */
    val blogId: Int = 0,
    /**
     * 打开webView
     */
    val openWebView: WebViewParams? = null,

    /**
     * 当前是否处于画中画（PiP）模式
     */
    val isInPipMode: Boolean = false,
)


val LocalUIState = compositionLocalOf<UIState> { error("UIState not present") }
