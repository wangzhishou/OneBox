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

package com.shifenmiao.model

import kotlinx.serialization.json.Json

object ModelProvider {

    /**
     * 全局共用的 kotlinx.serialization Json 实例(全项目唯一 JSON 入口)。
     * Json 不可变且线程安全,整个进程共用这一个实例。
     *
     * encodeDefaults = true 是对齐 Gson 时代的行为:Gson 序列化时总是写出所有字段
     * (仅省略 null),kotlinx 默认却会跳过等于声明默认值的字段;不打开此项会导致
     * 请求体/持久化 JSON 比历史产物少字段。
     */
    val AppJson: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

}
