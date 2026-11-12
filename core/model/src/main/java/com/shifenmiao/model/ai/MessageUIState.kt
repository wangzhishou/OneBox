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

package com.shifenmiao.model.ai

/**
 * 消息UI状态枚举
 */
enum class MessageUIState(val value: Int) {
    LOADING(-1),         // 输入后正在等待后台返回数据
    ERROR(-2),           // 错误消息
    STREAMING(0),        // 流式输出中的消息
    NORMAL(1);           // 正常保存的消息

    companion object {
        fun fromValue(value: Int): MessageUIState {
            return when(value) {
                LOADING.value -> LOADING
                ERROR.value -> ERROR
                STREAMING.value -> STREAMING
                else -> NORMAL
            }
        }
    }
}
