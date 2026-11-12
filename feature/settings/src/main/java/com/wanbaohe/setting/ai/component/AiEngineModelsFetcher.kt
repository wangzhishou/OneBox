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

package com.wanbaohe.setting.ai.component

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.openai.OpenAIModelItem
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import javax.inject.Inject

/**
 * 拉取服务商模型列表（GET `{base}/{version}/models`），新增页与详情页共用。
 *
 * 列表地址从 requestPath 推导版本前缀：`v1/chat/completions` / `v1/messages`
 * -> `{base}/v1/models`，`api/v1/chat/completions` -> `{base}/api/v1/models`；
 * 没有版本段时退回接口所在目录，path 为空退回 `{base}/models`。直接拼
 * `{base}/models` 会丢掉 path 里的版本段，多数兼容站因此 404。
 */
internal class AiEngineModelsFetcher @Inject constructor(
    private val openAICompatibleService: OpenAICompatibleService,
    private val openAIWithApiKeyService: OpenAIWithApiKeyService,
) {

    suspend fun fetch(engine: AiEngine): Result<List<OpenAIModelItem>> = runCatching {
        val url = buildModelsListUrl(engine)
        val response = when (engine.authType) {
            // 少数厂商（如 Anthropic 官方）要求 api-key 头而不是 Bearer
            AuthType.API_KEY -> openAIWithApiKeyService.listModels(
                url = url,
                apiKey = engine.authorizationCode,
            )

            else -> openAICompatibleService.listModels(
                url = url,
                authorization = engine.authorizationCode
                    .takeIf { it.isNotBlank() }
                    ?.let { "Bearer $it" },
            )
        }
        check(response.isSuccessful) { "HTTP ${response.code()}" }
        response.body()?.data.orEmpty()
    }

    private fun buildModelsListUrl(engine: AiEngine): String {
        val baseUrl = engine.requestUrl.trim().trimEnd('/')
        val pathSegments = engine.requestPath.trim().trim('/')
            .split('/')
            .filter { it.isNotBlank() }
        // 截到版本段为止：`api/v1/chat/completions` -> `api/v1`（不能只砍最后一段，
        // 否则会拼成 `/api/v1/chat/models`）。没有版本段时退回接口所在目录。
        val versionIndex = pathSegments.indexOfLast { VERSION_SEGMENT.matches(it) }
        val pathPrefix = if (versionIndex >= 0) {
            pathSegments.take(versionIndex + 1)
        } else {
            pathSegments.dropLast(1)
        }.joinToString("/")
        return if (pathPrefix.isBlank()) "$baseUrl/models" else "$baseUrl/$pathPrefix/models"
    }

    private companion object {
        private val VERSION_SEGMENT = Regex("^v\\d+(?:beta\\d*|alpha\\d*)?$", RegexOption.IGNORE_CASE)
    }
}
