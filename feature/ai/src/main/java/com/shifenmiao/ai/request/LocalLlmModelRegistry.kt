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

package com.shifenmiao.ai.request

/**
 * 本地模型注册表。
 *
 * google/foss 通过 LocalModelRepository 解析已安装模型，国内渠道使用空实现。
 * 实现按渠道拆分:google/foss 见 src/litertlm，国内渠道见 src/domestic。
 */
interface LocalLlmModelRegistry {
    fun resolve(modelName: String): LocalLlmModelSpec?
}
