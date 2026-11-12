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

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.channel.OnDeviceAiCapabilities
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

internal const val ON_DEVICE_LLM_UNSUPPORTED_ERROR =
    "Local on-device models are not supported in this channel"

data class OnDeviceLlmRequest(
    val sessionId: String,
    val modelId: String,
    val messages: List<LlmMessage>,
    val temperature: Double = 0.7,
    val topP: Double = 0.9,
    val maxTokens: Int = 1024,
    val stop: List<String>? = null,
)

interface OnDeviceLlmService {
    val loadingModelName: StateFlow<String?>

    fun isModelInstalled(modelId: String): Boolean

    suspend fun prepare(modelId: String): LocalLlmPrepareResult

    fun generate(request: OnDeviceLlmRequest): Flow<LocalLlmGenerateEvent>

    fun maybeWarmUp(engine: AiEngine)

    fun scheduleRelease()

    suspend fun cancel(sessionId: String)

    suspend fun releaseAll()
}

@Singleton
class DefaultOnDeviceLlmService @Inject constructor(
    private val runtime: LocalLlmRuntime,
    private val modelRegistry: LocalLlmModelRegistry,
    private val sessionManager: LocalLlmSessionManager,
) : OnDeviceLlmService {
    private val capabilities = OnDeviceAiCapabilities.fromFlavor()

    override val loadingModelName: StateFlow<String?> = sessionManager.loadingModelName

    override fun isModelInstalled(modelId: String): Boolean =
        capabilities.isLocalModelManagementSupported && modelRegistry.resolve(modelId) != null

    override suspend fun prepare(modelId: String): LocalLlmPrepareResult {
        if (!capabilities.isLocalModelManagementSupported) {
            return LocalLlmPrepareResult.Failure(
                LocalLlmError.Unknown(ON_DEVICE_LLM_UNSUPPORTED_ERROR)
            )
        }
        val spec = modelRegistry.resolve(modelId)
            ?: return LocalLlmPrepareResult.Failure(
                LocalLlmError.Unknown("Local model not registered: $modelId")
            )
        return sessionManager.prepare(spec)
    }

    override fun generate(request: OnDeviceLlmRequest): Flow<LocalLlmGenerateEvent> = flow {
        if (!capabilities.isLocalModelManagementSupported) {
            emit(LocalLlmGenerateEvent.Failed(ON_DEVICE_LLM_UNSUPPORTED_ERROR))
            return@flow
        }
        val spec = modelRegistry.resolve(request.modelId)
        if (spec == null) {
            emit(LocalLlmGenerateEvent.Failed("Local model not registered: ${request.modelId}"))
            return@flow
        }
        val maxTokens = minOf(spec.maxOutputTokens, request.maxTokens)
            .takeIf { it > 0 }
            ?: spec.maxOutputTokens
        val runtimeRequest = LocalLlmGenerateRequest(
            sessionId = request.sessionId,
            model = spec,
            messages = request.messages,
            temperature = request.temperature,
            topP = request.topP,
            maxTokens = maxTokens,
            stop = request.stop ?: spec.chatTemplate.stopTokens,
        )
        runtime.generate(runtimeRequest).collect { emit(it) }
    }

    override fun maybeWarmUp(engine: AiEngine) {
        if (capabilities.isLocalChatSupported) sessionManager.maybeWarmUp(engine)
    }

    override fun scheduleRelease() = sessionManager.scheduleRelease()

    override suspend fun cancel(sessionId: String) = runtime.cancel(sessionId)

    override suspend fun releaseAll() = sessionManager.releaseAll()
}
