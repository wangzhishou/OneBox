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

/**
 * 回调结果包装类
 *
 * 统一的工具回调结果类型，支持成功和失败两种状态。
 *
 * @param data 成功时返回的数据，可以是任意类型
 * @param error 失败时的错误信息，为 null 表示成功
 */
data class CallbackResult(
    val data: Any? = null,
    val error: String? = null
) {
    /** 是否成功 */
    val isSuccess: Boolean get() = error == null

    /** 是否失败 */
    val isError: Boolean get() = error != null

    companion object {
        /** 创建成功结果 */
        fun success(data: Any?) = CallbackResult(data = data)

        /** 创建失败结果 */
        fun error(message: String) = CallbackResult(error = message)
    }
}
