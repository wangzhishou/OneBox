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

package com.shifenmiao.common.ai

data class AIPromptResult(
    val content: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val engineName: String = "",
    val modelName: String = "",
    /** 接口返回的 totalTokens(无则 0),供调用方按量扣积分 */
    val totalTokens: Int = 0,
    /** 是否走自有代理路由(需登录/计积分);false 为 BYOK 直连,不应扣积分 */
    val isProxyRoute: Boolean = false,
    /** 是否因「未登录 / 积分不足」被门槛拦截(请求未发出),供调用方区分"没网"与"没积分" */
    val isBlocked: Boolean = false,
)
