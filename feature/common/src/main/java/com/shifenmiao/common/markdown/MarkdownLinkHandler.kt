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

package com.shifenmiao.common.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import com.shifenmiao.common.handle.LocalUrlNavigator

/**
 * 正文类内容（Markdown、消息气泡、引用卡片）里链接的统一点击处理。
 *
 * 一律交给全局 [com.shifenmiao.common.handle.UrlNavigator] 路由：
 * - http(s) → 内置浏览器（与「网址」tab 打开网页同一入口）
 * - onebox://、站内 App Link → 对应页面
 * - tel:/mailto: 等 → 系统能力
 *
 * 不依赖 [LocalUriHandler] 的默认实现：全局 SafeUriHandler 只是兜底，不会唤起外部浏览器。
 */
@Composable
fun rememberInAppLinkHandler(): (String) -> Unit {
    val navigator = LocalUrlNavigator.current
    val uriHandler = LocalUriHandler.current
    return remember(navigator, uriHandler) {
        { url ->
            if (!navigator.navigate(url)) {
                uriHandler.openUri(url)
            }
        }
    }
}
