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

package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSSpeechRequest

/**
 * 新增引擎只需实现本接口并通过 Hilt @IntoSet 注册，无需修改中央调度逻辑。
 */
interface TTSProvider {
    val descriptor: TTSProviderDescriptor

    suspend fun synthesize(
        config: TTSConfig,
        request: TTSSpeechRequest,
    ): Result<ByteArray>

    /** 必须与 synthesize 的实际路由选择保持一致。 */
    fun isProxyRoute(config: TTSConfig): Boolean
}
