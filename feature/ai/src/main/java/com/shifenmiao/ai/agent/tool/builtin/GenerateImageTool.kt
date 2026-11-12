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

package com.shifenmiao.ai.agent.tool.builtin

import kotlinx.serialization.SerialName
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolLoginChecker
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.imagegeneration.loader.ImageGenerationLoader
import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.service.ImageGenerationManager
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.storage.TokenStorage
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * 文生图工具:调用 core/image-generation 的生成管线,
 * 产物落在 App 工作目录的 generated-images 文件夹,可在文件管理器中查看。
 *
 * 计费遵循全校 AI 规范:代理路由(我方服务器)需登录、成功后扣积分;
 * 用户自备 API Key 的直连配置免费、免登录。
 */
class GenerateImageTool @Inject constructor(
    private val imageGenerationManager: ImageGenerationManager,
    private val imageGenerationLoader: ImageGenerationLoader,
    private val loginChecker: AgentToolLoginChecker,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "generate_image"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_generate_image)

    override val title: String = textProvider.string(R.string.agent_tool_generate_image_title)

    override val summary: String = textProvider.string(R.string.agent_tool_generate_image_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_generate_image_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_generate_image_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    // 文生图通常要十几秒到一分多钟,默认 30s 超时太短,放宽到 3 分钟
    override val executionTimeoutMs: Long = 180_000L

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "file_browser",
            ),
            label = textProvider.string(R.string.agent_tool_generate_image_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_generate_image_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "prompt" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_generate_image_param_prompt),
            ),
            "size" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_generate_image_param_size),
            ),
            "negative_prompt" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_generate_image_param_negative_prompt),
            ),
            "force_refresh" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_generate_image_param_force_refresh),
            ),
        ),
        required = listOf("prompt"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) {
                GenerateImageParams()
            } else {
                AppJson.decodeFromString<GenerateImageParams>(arguments)
            }
            val prompt = params.prompt?.trim().orEmpty()
            if (prompt.isEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_generate_image_missing_prompt),
                    isError = true,
                )
            }

            val config = imageGenerationManager.getActiveConfig()
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_generate_image_no_config),
                    isError = true,
                )

            // 代理路由走我方服务器:执行前校验登录与积分;自备 Key 直连不做门槛
            val isProxyRoute = !config.hasDirectConfig
            val pointsCost = aiImageProcessPointsCost()
            if (isProxyRoute) {
                if (!loginChecker.isLoggedIn()) {
                    return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_generate_image_need_login),
                        isError = true,
                    )
                }
                if (!TokenStorage.canConsumePoints(pointsCost)) {
                    return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_generate_image_no_points),
                        isError = true,
                    )
                }
            }

            imageGenerationLoader.load(
                request = ImageGenerationRequest(
                    prompt = prompt,
                    negativePrompt = params.negative_prompt?.takeIf(String::isNotBlank),
                    outputSize = params.size?.takeIf(String::isNotBlank),
                ),
                forceRefresh = params.force_refresh == true,
            ).fold(
                onSuccess = { image ->
                    // 与 text-card 等页面一致:仅非缓存结果扣积分,失败不扣
                    if (isProxyRoute && !image.fromCache) {
                        BaseUtils.consumePoints(
                            degree = pointsCost,
                            desc = title,
                            source = POINTS_SOURCE,
                            showToast = true,
                        )
                    }
                    AgentToolResult(
                        content = AppJson.encodeToString(
                            GenerateImageResult(
                                filePath = image.file.absolutePath,
                                fileName = image.file.name,
                                fromCache = image.fromCache,
                                cacheKey = image.cacheKey,
                            )
                        )
                    )
                },
                onFailure = { error -> failure(error) },
            )
        } catch (e: Exception) {
            failure(e)
        }
    }

    private fun failure(error: Throwable): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_generate_image_failed,
                error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
            ),
            isError = true,
        )
    }

    /**
     * LLM 参数 DTO(已迁移 kotlinx.serialization):① 嵌套在 Tool 内且以 Params 结尾,
     * 命中 app/proguard-rules.pro 的 `builtin.**$*Params` keep 规则;
     * ② 字段加 @SerializedName/@SerialName 双标注(灰度期共存),
     * 防止 R8 混淆字段名导致 release 下解析不出参数。
     */
    @Serializable
    private data class GenerateImageParams(
        @SerialName("prompt") val prompt: String? = null,
        @SerialName("size") val size: String? = null,
        @SerialName("negative_prompt") val negative_prompt: String? = null,
        @SerialName("force_refresh") val force_refresh: Boolean? = null,
    )

    @Serializable
    private data class GenerateImageResult(
        @SerialName("filePath") val filePath: String,
        @SerialName("fileName") val fileName: String,
        @SerialName("fromCache") val fromCache: Boolean,
        @SerialName("cacheKey") val cacheKey: String,
    )

    private companion object {
        const val POINTS_SOURCE = "agent_generate_image"
    }
}
