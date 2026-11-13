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

package com.wanbaohe.blog.logic

/**
 * 反馈提交失败的分类。
 *
 * 网关对失败原因只返回统一结构, 界面不能只显示"提交失败" —— 用户已经写好的内容还在,
 * 得让人知道是网络、登录态还是服务端的问题, 以及能不能直接再点一次。
 */
enum class FeedbackError {
    /** 网络不通/超时, 请求没到服务端 */
    Network,

    /** 401/403: 游客 token 或登录态失效 */
    Auth,

    /** 400/413/422: 内容没通过服务端校验 */
    Invalid,

    /** 429: 触发限流 */
    RateLimited,

    /** 5xx: 服务端自己的问题 */
    Server,

    /** 其它未归类失败 */
    Unknown;

    companion object {
        fun fromHttpCode(code: Int): FeedbackError = when (code) {
            401, 403 -> Auth
            400, 413, 422 -> Invalid
            429 -> RateLimited
            in 500..599 -> Server
            else -> Unknown
        }
    }
}
