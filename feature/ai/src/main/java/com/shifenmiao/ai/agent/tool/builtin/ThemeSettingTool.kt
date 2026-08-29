package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ToolDeepLink
import com.shifenmiao.ai.R
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.settings.domain.ThemeSettingService
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import javax.inject.Inject

class ThemeSettingTool @Inject constructor(
    private val themeSettingService: ThemeSettingService,
    private val gson: Gson,
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
        val current = themeSettingService.getCurrentTheme()
        when (val change = buildThemeChange(current, params)) {
            is ThemeChangeResult.Invalid -> return change.result
            is ThemeChangeResult.NoOp -> return errorResult(
                action = "set",
                reasonCode = "no_fields",
                message = textProvider.string(R.string.agent_tool_theme_setting_no_fields),
            )
            is ThemeChangeResult.Ok -> {
                // 修改使配置偏离已存预设: 以自定义哨兵 id 应用,
                // 之后 getCurrentTheme 从实际生效状态重建, 连续 set 不会互相回滚
                themeSettingService.applyThemePreset(change.next.copy(id = AppThemePreset.CUSTOM_ID))
                val updated = themeSettingService.getCurrentTheme()
                return successResult(
                    action = "set",
                    message = textProvider.string(R.string.agent_tool_theme_setting_set_message),
                    theme = updated,
                    extra = mapOf("changed" to change.changedFields),
                )
            }
        }
    }

    private fun buildThemeChange(
        current: AppThemePreset,
        params: ThemeSettingParams,
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

        params.night_mode?.takeIf { it.isNotBlank() }?.let { raw ->
            val nightMode = parseNightMode(raw) ?: return ThemeChangeResult.Invalid(
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
            next = next.copy(nightMode = nightMode)
            changed += "nightMode"
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
        data class Set(val value: Int) : OptionalColor()
        data object Invalid : OptionalColor()
    }

    private sealed class OptionalBool {
        data object Absent : OptionalBool()
        data class Set(val value: Boolean) : OptionalBool()
        data object Invalid : OptionalBool()
    }

    private sealed class OptionalAlpha {
        data object Absent : OptionalAlpha()
        data class Set(val value: Float) : OptionalAlpha()
        data object Invalid : OptionalAlpha()
    }

    private sealed class ThemeChangeResult {
        data class Ok(val next: AppThemePreset, val changedFields: List<String>) : ThemeChangeResult()
        data class Invalid(val result: AgentToolResult) : ThemeChangeResult()
        data object NoOp : ThemeChangeResult()
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

    private fun parseOptionalAlpha(raw: Double?): OptionalAlpha {
        if (raw == null) return OptionalAlpha.Absent
        if (raw.isNaN() || raw !in 0.1..1.0) return OptionalAlpha.Invalid
        return OptionalAlpha.Set(raw.toFloat())
    }

    private fun parseGradientStyle(raw: String?): GradientBackgroundStyle? {
        if (raw.isNullOrBlank()) return null
        return GradientBackgroundStyle.entries2.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }

    private fun parseNightMode(raw: String): NightMode? {
        return NIGHT_MODES.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }

    private fun parseArguments(arguments: String): ThemeSettingParams {
        if (arguments.isBlank()) return ThemeSettingParams()
        return gson.fromJson(arguments, ThemeSettingParams::class.java) ?: ThemeSettingParams()
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
        return AgentToolResult(content = gson.toJson(payload))
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
        return AgentToolResult(content = gson.toJson(payload), isError = true)
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
        )
    }

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
    }

    private companion object {
        val NIGHT_MODES = listOf(NightMode.Light, NightMode.Dark, NightMode.System)
        val NIGHT_MODE_NAMES = NIGHT_MODES.map { it.name }
        val BOOL_ACCEPTED = listOf("true", "false", "1", "0", "yes", "no", "on", "off")
        val COLOR_FORMATS = listOf(
            "#RRGGBB",
            "#AARRGGBB",
            "RRGGBB",
            "AARRGGBB",
            "0xRRGGBB",
            "0xAARRGGBB",
        )
    }
}
