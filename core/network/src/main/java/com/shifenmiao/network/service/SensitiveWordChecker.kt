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

package com.shifenmiao.network.service

import com.shifenmiao.model.moderation.SensitiveWordCheckField
import com.shifenmiao.model.moderation.SensitiveWordCheckRequest
import com.shifenmiao.model.moderation.SensitiveWordCheckResponse
import com.shifenmiao.network.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SensitiveWordCheckOutcome {
    data object Safe : SensitiveWordCheckOutcome

    data class Hit(val message: String, val hits: List<SensitiveWordCheckField>) :
        SensitiveWordCheckOutcome

    data class Failed(val cause: String) : SensitiveWordCheckOutcome
}

@Singleton
class SensitiveWordChecker @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 对 [fields] 中声明的文本进行敏感词检测。
     *
     * - [Safe] 表示未命中;
     * - [Hit] 表示命中, 携带服务端消息以及非 release 模式下命中的字段详情;
     * - [Failed] 表示网络 / 协议异常, 调用方可按业务策略选择放行或拦截。
     */
    suspend fun check(
        scene: String,
        fields: List<SensitiveWordCheckField>
    ): SensitiveWordCheckOutcome {
        if (fields.isEmpty() || fields.all { it.text.isBlank() }) {
            return SensitiveWordCheckOutcome.Safe
        }
        val request = SensitiveWordCheckRequest(scene = scene, fields = fields)
        return runCatching {
            val response = apiService.checkSensitiveWords(request)
            val body: SensitiveWordCheckResponse? = response.body()
            if (!response.isSuccessful || body == null) {
                SensitiveWordCheckOutcome.Failed(
                    "http ${response.code()}: ${response.message().ifBlank { "unknown" }}"
                )
            } else {
                if (body.hit) {
                    SensitiveWordCheckOutcome.Hit(
                        message = body.message.orEmpty(),
                        hits = body.hits.map { SensitiveWordCheckField(it.key, it.words.joinToString()) }
                    )
                } else {
                    SensitiveWordCheckOutcome.Safe
                }
            }
        }.getOrElse { SensitiveWordCheckOutcome.Failed(it.message ?: "unknown error") }
    }
}
