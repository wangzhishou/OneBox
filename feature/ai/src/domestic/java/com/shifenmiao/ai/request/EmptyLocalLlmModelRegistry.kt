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

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 国内渠道不注册任何本地模型:resolve 一律 miss,
 * Adapter 走 "Local model not registered" 分支,国内用户不会看到可用入口。
 */
@Singleton
class EmptyLocalLlmModelRegistry @Inject constructor() : LocalLlmModelRegistry {
    override fun resolve(modelName: String): LocalLlmModelSpec? = null
}
