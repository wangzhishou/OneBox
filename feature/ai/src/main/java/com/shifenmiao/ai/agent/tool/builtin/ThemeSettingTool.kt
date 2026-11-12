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

import android.net.Uri
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolLoginChecker
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.ai.R
import com.shifenmiao.model.jsonStringOf
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
import com.t8rin.dynamic.theme.ColorSpecVersion
import com.t8rin.dynamic.theme.PaletteStyle
import com.t8rin.imagetoolbox.core.settings.domain.ThemeSettingService
import com.t8rin.imagetoolbox.core.settings.domain.model.AppColorSystem
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ThemeSettingTool @Inject constructor(
    private val themeSettingService: ThemeSettingService,
    private val imageGenerationManager: ImageGenerationManager,
    private val imageGenerationLoader: ImageGenerationLoader,
    private val loginChecker: AgentToolLoginChecker,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "theme_setting"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_theme_setting)

    override val title: String = textProvider.string(R.string.agent_tool_theme_setting_title)

    override val summary: String = textProvider.string(R.string.agent_tool_theme_setting_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_theme_setting_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_theme_setting_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    // background_prompt 触发文生图,通常要十几秒到一分多钟,超时放宽到 3 分钟
    override val executionTimeoutMs: Long = 180_000L

    override val sortOrder: Int = -72

    override val deepLinks: List<ToolDeepLink> = listOf(
        ToolDeepLink(
            uri = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "theme_settings",
            ),
            label = textProvider.string(R.string.agent_tool_theme_setting_deeplink_label),
            guidance = textProvider.string(R.string.agent_tool_theme_setting_deeplink_guidance),
            primary = true,
        )
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_action),
                enum = listOf("list", "set", "apply"),
            ),
            "preset_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_preset_id),
            ),
            "preset_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_preset_name),
            ),
            "night_mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_night_mode),
                enum = listOf("Light", "Dark", "System"),
            ),
            "primary" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_primary),
            ),
            "secondary" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_secondary),
            ),
            "tertiary" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_tertiary),
            ),
            "surface" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_surface),
            ),
            "glass" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_glass),
            ),
            "liquid_glass" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_liquid_glass),
            ),
            "mesh_gradient" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_mesh_gradient),
            ),
            "gradient_style" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_gradient_style),
                enum = GradientBackgroundStyle.entries2.map { it.name },
            ),
            "glass_alpha" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_glass_alpha),
            ),
            "glass_border_alpha" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_glass_border_alpha),
            ),
            "palette_style" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_palette_style),
                enum = PALETTE_STYLE_NAMES,
            ),
            "color_spec" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_color_spec),
                enum = COLOR_SPEC_NAMES,
            ),
            "expressive_motion" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_expressive_motion),
            ),
            "contrast" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_contrast),
            ),
            "background_image" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_background_image),
                enum = listOf("clear"),
            ),
            "background_prompt" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_theme_setting_param_background_prompt),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            when (params.action?.lowercase()) {
                "list", "get" -> handleList()
                "set", "update" -> handleSet(params)
                "apply", "switch" -> handleApply(params)
                null, "" -> errorResult(
                    action = "unknown",
                    reasonCode = "missing_action",
                    message = textProvider.string(R.string.agent_tool_theme_setting_missing_action),
                )
                else -> errorResult(
                    action = params.action,
                    reasonCode = "unknown_action",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_unknown_action,
                        params.action,
                    ),
                )
            }
        }.getOrElse { error ->
            errorResult(
                action = "unknown",
                reasonCode = "exception",
                message = textProvider.string(
                    R.string.agent_tool_theme_setting_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
            )
        }
    }

    private suspend fun handleList(): AgentToolResult {
        val current = themeSettingService.getCurrentTheme()
        val presets = themeSettingService.listThemes()
        return successResult(
            action = "list",
            message = textProvider.string(R.string.agent_tool_theme_setting_list_message),
            theme = current,
            extra = mapOf(
                "gradientStyles" to GradientBackgroundStyle.entries2.map { it.name },
                "nightModes" to NIGHT_MODE_NAMES,
                "paletteStyles" to PALETTE_STYLE_NAMES,
                "colorSpecs" to COLOR_SPEC_NAMES,
                "colorSystem" to colorSystemSummary(themeSettingService.getColorSystem()),
                "builtinPresets" to presets.map { mapOf("id" to it.id, "name" to it.name) },
            ),
        )
    }

    private suspend fun handleApply(params: ThemeSettingParams): AgentToolResult {
        if (params.preset_id.isNullOrBlank() && params.preset_name.isNullOrBlank()) {
            return errorResult(
                action = "apply",
                reasonCode = "missing_preset_arg",
                message = textProvider.string(R.string.agent_tool_theme_setting_no_preset_arg),
            )
        }
        val all = themeSettingService.listThemes()
        val matched = resolvePreset(params, all)
        if (matched == null) {
            return errorResult(
                action = "apply",
                reasonCode = "unknown_preset",
                message = textProvider.string(
                    R.string.agent_tool_theme_setting_unknown_preset,
                    params.preset_name ?: params.preset_id.orEmpty(),
                    all.joinToString { "${it.id}(${it.name})" },
                ),
                validOptions = mapOf(
                    "builtinPresets" to all.map { mapOf("id" to it.id, "name" to it.name) },
                ),
            )
        }
        val switched = themeSettingService.switchTheme(matched.id)
            ?: return errorResult(
                action = "apply",
                reasonCode = "switch_failed",
                message = textProvider.string(
                    R.string.agent_tool_theme_setting_failed,
                    matched.id,
                ),
            )
        return successResult(
            action = "apply",
            message = textProvider.string(
                R.string.agent_tool_theme_setting_applied_preset,
                switched.name,
            ),
            theme = switched,
        )
    }

    private suspend fun handleSet(params: ThemeSettingParams): AgentToolResult {
        if (params.isEmpty()) {
            return errorResult(
                action = "set",
                reasonCode = "no_fields",
                message = textProvider.string(R.string.agent_tool_theme_setting_no_fields),
            )
        }
        // 全局色彩系统(调色板风格 / 色彩规范 / 对比度 / Expressive 动效)不属于主题预设:
        // 先只做校验(失败立即返回), 待预设字段也校验通过后再一起落盘, 避免半途生效
        val colorSystem = when (val resolved = resolveGlobalSettings(params)) {
            is GlobalSettingChange.Invalid -> return resolved.result
            is GlobalSettingChange.NoOp -> null
            is GlobalSettingChange.Ok -> resolved
        }
        // 背景图先单独解析:background_prompt 需要挂起调用文生图管线,
        // 产物 file:// URI 注入 buildThemeChange 统一应用
        val background = when (val resolved = resolveBackground(params)) {
            is BackgroundResolution.Failed -> return resolved.result
            is BackgroundResolution.Resolved -> resolved
        }
        val current = themeSettingService.getCurrentTheme()
        val presetChange = buildThemeChange(current, params, background)
        if (presetChange is ThemeChangeResult.Invalid) return presetChange.result

        val colorSystemFields = colorSystem?.changedFields.orEmpty()
        val presetFields = (presetChange as? ThemeChangeResult.Ok)?.changedFields.orEmpty()
        if (colorSystemFields.isEmpty() && presetFields.isEmpty()) {
            return errorResult(
                action = "set",
                reasonCode = "no_fields",
                message = textProvider.string(R.string.agent_tool_theme_setting_no_fields),
            )
        }

        colorSystem?.let {
            themeSettingService.setColorSystem(it.next)
            it.nightMode?.let { nightMode -> themeSettingService.setNightMode(nightMode) }
        }
        if (presetChange is ThemeChangeResult.Ok) {
            // 修改使配置偏离已存预设: 以自定义哨兵 id 应用,
            // 之后 getCurrentTheme 从实际生效状态重建, 连续 set 不会互相回滚
            themeSettingService.applyThemePreset(presetChange.next.copy(id = AppThemePreset.CUSTOM_ID))
        }
        return successResult(
            action = "set",
            message = textProvider.string(R.string.agent_tool_theme_setting_set_message),
            theme = themeSettingService.getCurrentTheme(),
            extra = mapOf(
                "changed" to (colorSystemFields + presetFields),
                "colorSystem" to colorSystemSummary(themeSettingService.getColorSystem()),
            ),
        )
    }

    /**
     * 解析全局设置字段(色彩系统 + 日夜模式, 全部可选)。任一项非法即整体失败,
     * 避免"风格改了、规范没改"这种半生效状态。
     *
     * 这些字段都不属于主题预设: 日夜模式自 2026-09 起彻底与预设解耦,
     * 主题切换再也不会顺手改掉用户的深浅色偏好。
     */
    private suspend fun resolveGlobalSettings(params: ThemeSettingParams): GlobalSettingChange {
        val touched = !params.palette_style.isNullOrBlank() ||
            !params.color_spec.isNullOrBlank() ||
            !params.expressive_motion.isNullOrBlank() ||
            params.contrast != null ||
            !params.night_mode.isNullOrBlank()
        if (!touched) return GlobalSettingChange.NoOp

        var next = themeSettingService.getColorSystem()
        var nextNightMode: NightMode? = null
        val changed = mutableListOf<String>()

        params.night_mode?.takeIf { it.isNotBlank() }?.let { raw ->
            val nightMode = parseNightMode(raw) ?: return GlobalSettingChange.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "invalid_night_mode",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_invalid_night_mode,
                        raw,
                    ),
                    validOptions = mapOf("nightModes" to NIGHT_MODE_NAMES),
                )
            )
            nextNightMode = nightMode
            changed += "nightMode"
        }

        params.palette_style?.takeIf { it.isNotBlank() }?.let { raw ->
            val style = parsePaletteStyle(raw) ?: return GlobalSettingChange.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "unknown_palette_style",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_unknown_palette_style,
                        raw,
                    ),
                    validOptions = mapOf("paletteStyles" to PALETTE_STYLE_NAMES),
                )
            )
            next = next.copy(paletteStyle = style)
            changed += "paletteStyle"
        }

        params.color_spec?.takeIf { it.isNotBlank() }?.let { raw ->
            val spec = parseColorSpec(raw) ?: return GlobalSettingChange.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "unknown_color_spec",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_unknown_color_spec,
                        raw,
                    ),
                    validOptions = mapOf("colorSpecs" to COLOR_SPEC_NAMES),
                )
            )
            next = next.copy(colorSpec = spec)
            changed += "colorSpec"
        }

        params.expressive_motion?.takeIf { it.isNotBlank() }?.let { raw ->
            when (val parsed = parseOptionalBool(raw)) {
                is OptionalBool.Set -> {
                    next = next.copy(isExpressiveTheme = parsed.value)
                    changed += "expressiveMotion"
                }
                OptionalBool.Invalid -> return GlobalSettingChange.Invalid(
                    errorResult(
                        action = "set",
                        reasonCode = "invalid_expressive_motion",
                        message = textProvider.string(
                            R.string.agent_tool_theme_setting_failed,
                            "expressive_motion=$raw",
                        ),
                        validOptions = mapOf("acceptedValues" to BOOL_ACCEPTED),
                    )
                )
                OptionalBool.Absent -> Unit
            }
        }

        when (val parsed = parseOptionalContrast(params.contrast)) {
            is OptionalContrast.Set -> {
                next = next.copy(contrastLevel = parsed.value)
                changed += "contrast"
            }
            OptionalContrast.Invalid -> return GlobalSettingChange.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "contrast_out_of_range",
                    message = textProvider.string(R.string.agent_tool_theme_setting_contrast_out_of_range),
                    validOptions = mapOf("contrastRange" to "-1.0..1.0"),
                )
            )
            OptionalContrast.Absent -> Unit
        }

        if (changed.isEmpty()) return GlobalSettingChange.NoOp
        return GlobalSettingChange.Ok(next, nextNightMode, changed)
    }

    /**
     * 解析背景图字段(在 buildThemeChange 之前调用):
     * - background_image="clear" → 清除背景;
     * - background_prompt 非空 → 文生图(计费门控同 generate_image 工具:
     *   代理路由需登录+积分,自备 Key 直连免费),产物为本地 file:// URI;
     * - 两者同传时 prompt 优先。
     */
    private suspend fun resolveBackground(params: ThemeSettingParams): BackgroundResolution {
        val prompt = params.background_prompt?.trim().orEmpty()
        if (prompt.isNotEmpty()) {
            val config = imageGenerationManager.getActiveConfig()
                ?: return BackgroundResolution.Failed(
                    errorResult(
                        action = "set",
                        reasonCode = "no_image_config",
                        message = textProvider.string(R.string.agent_tool_generate_image_no_config),
                    )
                )
            val isProxyRoute = !config.hasDirectConfig
            val pointsCost = aiImageProcessPointsCost()
            if (isProxyRoute) {
                if (!loginChecker.isLoggedIn()) {
                    return BackgroundResolution.Failed(
                        errorResult(
                            action = "set",
                            reasonCode = "need_login",
                            message = textProvider.string(R.string.agent_tool_generate_image_need_login),
                        )
                    )
                }
                if (!TokenStorage.canConsumePoints(pointsCost)) {
                    return BackgroundResolution.Failed(
                        errorResult(
                            action = "set",
                            reasonCode = "no_points",
                            message = textProvider.string(R.string.agent_tool_generate_image_no_points),
                        )
                    )
                }
            }
            return imageGenerationLoader.load(
                ImageGenerationRequest(prompt = prompt, outputSize = BACKGROUND_OUTPUT_SIZE)
            ).fold(
                onSuccess = { image ->
                    // 与 generate_image / text-card 一致:仅非缓存结果扣积分,失败不扣
                    if (isProxyRoute && !image.fromCache) {
                        BaseUtils.consumePoints(
                            degree = pointsCost,
                            desc = title,
                            source = POINTS_SOURCE,
                            showToast = true,
                        )
                    }
                    BackgroundResolution.Resolved(imageUri = Uri.fromFile(image.file).toString())
                },
                onFailure = { error ->
                    BackgroundResolution.Failed(
                        errorResult(
                            action = "set",
                            reasonCode = "background_generate_failed",
                            message = textProvider.string(
                                R.string.agent_tool_generate_image_failed,
                                error.message
                                    ?: textProvider.string(R.string.agent_tool_unknown_error),
                            ),
                        )
                    )
                },
            )
        }
        if (params.background_image?.trim()?.lowercase() == "clear") {
            return BackgroundResolution.Resolved(cleared = true)
        }
        return BackgroundResolution.Resolved()
    }

    private sealed interface BackgroundResolution {
        @Serializable
        data class Resolved(
            val imageUri: String? = null,
            val cleared: Boolean = false,
        ) : BackgroundResolution

        data class Failed(val result: AgentToolResult) : BackgroundResolution
    }

    private fun buildThemeChange(
        current: AppThemePreset,
        params: ThemeSettingParams,
        background: BackgroundResolution.Resolved,
    ): ThemeChangeResult {
        var next = current
        val changed = mutableListOf<String>()

        val colorInputs = listOf(
            "primary" to params.primary,
            "secondary" to params.secondary,
            "tertiary" to params.tertiary,
            "surface" to params.surface,
        )
        val colorUpdates = mutableMapOf<Int, Int>()
        for ((idx, pair) in colorInputs.withIndex()) {
            val (field, raw) = pair
            when (val parsed = parseOptionalColor(raw)) {
                is OptionalColor.Set -> colorUpdates[idx] = parsed.value
                OptionalColor.Invalid -> return ThemeChangeResult.Invalid(invalidColorError(field, raw))
                OptionalColor.Absent -> Unit
            }
        }
        if (colorUpdates.isNotEmpty()) {
            // 动态取色主题无基线色元组, 以品牌色为基线, 避免必然失败
            val baseline = AppThemePreset.parseColorTuple(current.colorTupleString)
                .ifEmpty { AppThemePreset.parseColorTuple(AppThemePreset.LogoTheme.colorTupleString) }
            val newColors = (0..3).map { idx -> colorUpdates[idx] ?: baseline.getOrNull(idx) }
            if (newColors.any { it == null }) {
                return ThemeChangeResult.Invalid(
                    errorResult(
                        action = "set",
                        reasonCode = "missing_baseline_colors",
                        message = textProvider.string(
                            R.string.agent_tool_theme_setting_failed,
                            "no baseline color tuple to apply against",
                        ),
                    )
                )
            }
            next = next.copy(
                colorTupleString = newColors.joinToString("*"),
                isDynamicColors = false,
            )
            changed += "colors"
        }

        when (val parsed = parseOptionalBool(params.glass)) {
            is OptionalBool.Set -> { next = next.copy(isGlassmorphismEnabled = parsed.value); changed += "glass" }
            OptionalBool.Invalid -> return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "invalid_glass",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_failed,
                        "glass=${params.glass}",
                    ),
                    validOptions = mapOf("acceptedValues" to BOOL_ACCEPTED),
                )
            )
            OptionalBool.Absent -> Unit
        }
        when (val parsed = parseOptionalBool(params.liquid_glass)) {
            is OptionalBool.Set -> { next = next.copy(isLiquidGlassEnabled = parsed.value); changed += "liquidGlass" }
            OptionalBool.Invalid -> return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "invalid_liquid_glass",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_failed,
                        "liquid_glass=${params.liquid_glass}",
                    ),
                    validOptions = mapOf("acceptedValues" to BOOL_ACCEPTED),
                )
            )
            OptionalBool.Absent -> Unit
        }
        when (val parsed = parseOptionalBool(params.mesh_gradient)) {
            is OptionalBool.Set -> { next = next.copy(isMeshGradientBackgroundEnabled = parsed.value); changed += "meshGradient" }
            OptionalBool.Invalid -> return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "invalid_mesh_gradient",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_failed,
                        "mesh_gradient=${params.mesh_gradient}",
                    ),
                    validOptions = mapOf("acceptedValues" to BOOL_ACCEPTED),
                )
            )
            OptionalBool.Absent -> Unit
        }

        params.gradient_style?.takeIf { it.isNotBlank() }?.let { raw ->
            val style = parseGradientStyle(raw) ?: return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "unknown_style",
                    message = textProvider.string(
                        R.string.agent_tool_theme_setting_unknown_style,
                        raw,
                    ),
                    validOptions = mapOf(
                        "gradientStyles" to GradientBackgroundStyle.entries2.map { it.name },
                    ),
                )
            )
            next = next.copy(gradientBackgroundStyle = style)
            changed += "gradientStyle"
        }

        when (val parsed = parseOptionalAlpha(params.glass_alpha)) {
            is OptionalAlpha.Set -> { next = next.copy(glassBaseAlpha = parsed.value); changed += "glassAlpha" }
            OptionalAlpha.Invalid -> return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "alpha_out_of_range",
                    message = textProvider.string(R.string.agent_tool_theme_setting_alpha_out_of_range),
                    validOptions = mapOf("alphaRange" to "0.1..1.0"),
                )
            )
            OptionalAlpha.Absent -> Unit
        }

        when (val parsed = parseOptionalAlpha(params.glass_border_alpha)) {
            is OptionalAlpha.Set -> { next = next.copy(glassBorderAlpha = parsed.value); changed += "glassBorderAlpha" }
            OptionalAlpha.Invalid -> return ThemeChangeResult.Invalid(
                errorResult(
                    action = "set",
                    reasonCode = "border_alpha_out_of_range",
                    message = textProvider.string(R.string.agent_tool_theme_setting_alpha_out_of_range),
                    validOptions = mapOf("alphaRange" to "0.0..1.0"),
                )
            )
            OptionalAlpha.Absent -> Unit
        }

        if (background.cleared) {
            next = next.copy(customBackgroundImageUri = null)
            changed += "backgroundImage"
        }
        background.imageUri?.let { uri ->
            next = next.copy(customBackgroundImageUri = uri)
            changed += "backgroundImage"
        }

        if (changed.isEmpty()) return ThemeChangeResult.NoOp
        return ThemeChangeResult.Ok(next, changed)
    }

    private fun resolvePreset(
        params: ThemeSettingParams,
        all: List<AppThemePreset>,
    ): AppThemePreset? {
        params.preset_id?.takeIf { it.isNotBlank() }?.let { rawId ->
            all.firstOrNull { it.id.equals(rawId, ignoreCase = true) }?.let { return it }
        }
        val rawName = params.preset_name?.takeIf { it.isNotBlank() } ?: return null
        val needle = rawName.trim()
        all.firstOrNull { it.id.equals(needle, ignoreCase = true) }?.let { return it }
        all.firstOrNull { it.name.equals(needle, ignoreCase = true) }?.let { return it }
        return all.firstOrNull {
            it.name.contains(needle, ignoreCase = true) || it.id.contains(needle, ignoreCase = true)
        }
    }

    private fun invalidColorError(field: String, raw: String?): AgentToolResult {
        return errorResult(
            action = "set",
            reasonCode = "invalid_$field",
            message = textProvider.string(
                R.string.agent_tool_theme_setting_invalid_color,
                field,
                raw.orEmpty(),
            ),
            validOptions = mapOf("formats" to COLOR_FORMATS),
        )
    }

    private fun parseColorOrNull(raw: String): Int? {
        val normalized = raw.trim()
            .removePrefix("#")
            .removePrefix("0x")
            .removePrefix("0X")
        val argb = when (normalized.length) {
            6 -> "FF$normalized"
            8 -> normalized
            else -> return null
        }
        if (argb.any { !it.isDigit() && it.lowercaseChar() !in 'a'..'f' }) return null
        return argb.toLongOrNull(16)?.toInt()
    }

    private sealed class OptionalColor {
        data object Absent : OptionalColor()
        @Serializable
        data class Set(val value: Int) : OptionalColor()
        data object Invalid : OptionalColor()
    }

    private sealed class OptionalBool {
        data object Absent : OptionalBool()
        @Serializable
        data class Set(val value: Boolean) : OptionalBool()
        data object Invalid : OptionalBool()
    }

    private sealed class OptionalAlpha {
        data object Absent : OptionalAlpha()
        @Serializable
        data class Set(val value: Float) : OptionalAlpha()
        data object Invalid : OptionalAlpha()
    }

    private sealed class OptionalContrast {
        data object Absent : OptionalContrast()
        @Serializable
        data class Set(val value: Double) : OptionalContrast()
        data object Invalid : OptionalContrast()
    }

    private sealed class ThemeChangeResult {
        data class Ok(val next: AppThemePreset, val changedFields: List<String>) : ThemeChangeResult()
        data class Invalid(val result: AgentToolResult) : ThemeChangeResult()
        data object NoOp : ThemeChangeResult()
    }

    private sealed class GlobalSettingChange {
        data class Ok(
            val next: AppColorSystem,
            val nightMode: NightMode?,
            val changedFields: List<String>,
        ) : GlobalSettingChange()
        data class Invalid(val result: AgentToolResult) : GlobalSettingChange()
        data object NoOp : GlobalSettingChange()
    }

    private fun parseOptionalColor(raw: String?): OptionalColor {
        if (raw.isNullOrBlank()) return OptionalColor.Absent
        val parsed = parseColorOrNull(raw)
        return if (parsed != null) OptionalColor.Set(parsed) else OptionalColor.Invalid
    }

    private fun parseOptionalBool(raw: String?): OptionalBool = when (raw?.lowercase()?.trim().orEmpty()) {
        "" -> OptionalBool.Absent
        "true", "1", "yes", "on" -> OptionalBool.Set(true)
        "false", "0", "no", "off" -> OptionalBool.Set(false)
        else -> OptionalBool.Invalid
    }

    /** glass_alpha 与 glass_border_alpha 共用同一区间 [0.0, 1.0]，与主题设置页滑块一致 */
    private fun parseOptionalAlpha(raw: Double?): OptionalAlpha {
        if (raw == null) return OptionalAlpha.Absent
        if (raw.isNaN() || raw < MIN_ALPHA || raw > MAX_ALPHA) return OptionalAlpha.Invalid
        return OptionalAlpha.Set(raw.toFloat())
    }

    private fun parseGradientStyle(raw: String?): GradientBackgroundStyle? {
        if (raw.isNullOrBlank()) return null
        return GradientBackgroundStyle.entries2.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }

    private fun parseNightMode(raw: String): NightMode? {
        return NIGHT_MODES.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }

    private fun parsePaletteStyle(raw: String?): PaletteStyle? {
        if (raw.isNullOrBlank()) return null
        val needle = raw.trim()
        return PaletteStyle.entries.firstOrNull { it.name.equals(needle, ignoreCase = true) }
    }

    /** 同时接受枚举名(Spec2025)、年份("2025")、带下划线写法(SPEC_2025) */
    private fun parseColorSpec(raw: String?): ColorSpecVersion? {
        if (raw.isNullOrBlank()) return null
        val needle = raw.trim().lowercase()
            .removePrefix("spec")
            .removePrefix("_")
            .removePrefix("-")
        return when (needle) {
            "2021" -> ColorSpecVersion.Spec2021
            "2025" -> ColorSpecVersion.Spec2025
            else -> ColorSpecVersion.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
        }
    }

    /** 对比度与主题设置页滑块一致: -1.0(低) ~ 1.0(高), 0.0 为默认 */
    private fun parseOptionalContrast(raw: Double?): OptionalContrast {
        if (raw == null) return OptionalContrast.Absent
        if (raw.isNaN() ||
            raw < AppColorSystem.MIN_CONTRAST_LEVEL ||
            raw > AppColorSystem.MAX_CONTRAST_LEVEL
        ) {
            return OptionalContrast.Invalid
        }
        return OptionalContrast.Set(raw)
    }

    private fun parseArguments(arguments: String): ThemeSettingParams {
        if (arguments.isBlank()) return ThemeSettingParams()
        return runCatching { AppJson.decodeFromString<ThemeSettingParams>(arguments) }.getOrNull() ?: ThemeSettingParams()
    }

    private fun successResult(
        action: String,
        message: String,
        theme: AppThemePreset? = null,
        extra: Map<String, Any?> = emptyMap(),
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "ok",
            "action" to action,
            "message" to message,
            "theme" to theme?.let(::themeSummary),
        )
        payload.putAll(extra)
        return AgentToolResult(content = jsonStringOf(payload))
    }

    private fun errorResult(
        action: String,
        reasonCode: String,
        message: String,
        validOptions: Map<String, Any?>? = null,
    ): AgentToolResult {
        val payload = linkedMapOf<String, Any?>(
            "status" to "error",
            "action" to action,
            "reasonCode" to reasonCode,
            "message" to message,
        )
        if (validOptions != null) payload["validOptions"] = validOptions
        return AgentToolResult(content = jsonStringOf(payload), isError = true)
    }

    private fun themeSummary(theme: AppThemePreset): Map<String, Any?> {
        val colors = AppThemePreset.parseColorTuple(theme.colorTupleString)
        return mapOf(
            "id" to theme.id,
            "name" to theme.name,
            "primaryHex" to colors.getOrNull(0)?.let { String.format("#%06X", 0xFFFFFF and it) },
            "secondaryHex" to colors.getOrNull(1)?.let { String.format("#%06X", 0xFFFFFF and it) },
            "tertiaryHex" to colors.getOrNull(2)?.let { String.format("#%06X", 0xFFFFFF and it) },
            "surfaceHex" to colors.getOrNull(3)?.let { String.format("#%06X", 0xFFFFFF and it) },
            "nightMode" to theme.nightMode.name,
            "glass" to theme.isGlassmorphismEnabled,
            "liquidGlass" to theme.isLiquidGlassEnabled,
            "meshGradient" to theme.isMeshGradientBackgroundEnabled,
            "gradientStyle" to theme.gradientBackgroundStyle.name,
            "glassAlpha" to theme.glassBaseAlpha,
            "glassBorderAlpha" to theme.glassBorderAlpha,
            "backgroundImage" to theme.customBackgroundImageUri,
        )
    }

    private fun colorSystemSummary(system: AppColorSystem): Map<String, Any?> = mapOf(
        "paletteStyle" to system.paletteStyle.name,
        "contrast" to system.contrastLevel,
        "colorSpec" to system.colorSpec.name,
        "expressiveMotion" to system.isExpressiveTheme,
    )

    @Serializable
    private data class ThemeSettingParams(
        val action: String? = null,
        val preset_id: String? = null,
        val preset_name: String? = null,
        val night_mode: String? = null,
        val primary: String? = null,
        val secondary: String? = null,
        val tertiary: String? = null,
        val surface: String? = null,
        val glass: String? = null,
        val liquid_glass: String? = null,
        val mesh_gradient: String? = null,
        val gradient_style: String? = null,
        val glass_alpha: Double? = null,
        val glass_border_alpha: Double? = null,
        val palette_style: String? = null,
        val color_spec: String? = null,
        val expressive_motion: String? = null,
        val contrast: Double? = null,
        val background_image: String? = null,
        val background_prompt: String? = null,
    ) {
        fun isEmpty(): Boolean = preset_id.isNullOrBlank()
            && preset_name.isNullOrBlank()
            && night_mode.isNullOrBlank()
            && primary.isNullOrBlank()
            && secondary.isNullOrBlank()
            && tertiary.isNullOrBlank()
            && surface.isNullOrBlank()
            && glass.isNullOrBlank()
            && liquid_glass.isNullOrBlank()
            && mesh_gradient.isNullOrBlank()
            && gradient_style.isNullOrBlank()
            && glass_alpha == null
            && glass_border_alpha == null
            && palette_style.isNullOrBlank()
            && color_spec.isNullOrBlank()
            && expressive_motion.isNullOrBlank()
            && contrast == null
            && background_image.isNullOrBlank()
            && background_prompt.isNullOrBlank()
    }

    private companion object {
        val NIGHT_MODES = listOf(NightMode.Light, NightMode.Dark, NightMode.System)
        val NIGHT_MODE_NAMES = NIGHT_MODES.map { it.name }
        val PALETTE_STYLE_NAMES = PaletteStyle.entries.map { it.name }
        val COLOR_SPEC_NAMES = ColorSpecVersion.entries.map { it.name }
        val BOOL_ACCEPTED = listOf("true", "false", "1", "0", "yes", "no", "on", "off")
        val COLOR_FORMATS = listOf(
            "#RRGGBB",
            "#AARRGGBB",
            "RRGGBB",
            "AARRGGBB",
            "0xRRGGBB",
            "0xAARRGGBB",
        )

        /** 玻璃透明度 / 描边可见度的统一取值范围，与主题设置页滑块一致 */
        const val MIN_ALPHA = 0.0
        const val MAX_ALPHA = 1.0

        /** AI 生成背景积分消耗来源标识 */
        const val POINTS_SOURCE = "agent_theme_background"

        /** AI 生成背景输出尺寸(竖屏,符合服务商 512–2048 限制) */
        const val BACKGROUND_OUTPUT_SIZE = "1080*1920"
    }
}
