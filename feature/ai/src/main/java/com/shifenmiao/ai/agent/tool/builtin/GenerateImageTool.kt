package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.storage.TokenStorage
import javax.inject.Inject

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
    private val gson: Gson,
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
                gson.fromJson(arguments, GenerateImageParams::class.java)
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
                        content = gson.toJson(
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
     * Gson 反射解析的 DTO 必须:① 嵌套在 Tool 内且以 Params 结尾,
     * 命中 app/proguard-rules.pro 的 `builtin.**$*Params` keep 规则;
     * ② 字段加 @SerializedName,防止 R8 混淆字段名导致 release 下解析不出参数。
     */
    private data class GenerateImageParams(
        @SerializedName("prompt") val prompt: String? = null,
        @SerializedName("size") val size: String? = null,
        @SerializedName("negative_prompt") val negative_prompt: String? = null,
        @SerializedName("force_refresh") val force_refresh: Boolean? = null,
    )

    private data class GenerateImageResult(
        @SerializedName("filePath") val filePath: String,
        @SerializedName("fileName") val fileName: String,
        @SerializedName("fromCache") val fromCache: Boolean,
        @SerializedName("cacheKey") val cacheKey: String,
    )

    private companion object {
        const val POINTS_SOURCE = "agent_generate_image"
    }
}
