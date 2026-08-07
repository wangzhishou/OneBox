package com.t8rin.imagetoolbox.core.settings.domain.model

/**
 * 主题预设 —— 将颜色方案 + 玻璃效果 + 背景风格 + 透明度基准值 + 日夜模式打包为一个可切换的完整"主题"。
 *
 * @param id                         唯一标识符
 * @param name                       显示名称（中文）
 * @param colorTupleString           四色元组字符串: "primary*secondary*tertiary*surface" (ARGB int)。
 * @param nightMode                  该主题预设绑定的日夜模式
 * @param isDynamicColors            是否使用壁纸动态取色（"千色千面"模式）
 * @param isGlassmorphismEnabled     是否开启玻璃透明
 * @param isLiquidGlassEnabled       是否开启液态玻璃
 * @param isMeshGradientBackgroundEnabled 是否开启渐变背景
 * @param gradientBackgroundStyle    渐变背景风格
 * @param glassBaseAlpha             玻璃特效透明度基准值 (0f..1f)，
 *                                   所有 GlassStyle 的 backgroundAlpha 按此基准缩放
 * @param customBackgroundImageUri   自定义背景图片 URI（可为 null）
 * @param isBuiltin                  是否为内置主题（内置主题不可删除）
 */
data class AppThemePreset(
    val id: String,
    val name: String,
    val colorTupleString: String,
    val nightMode: NightMode = NightMode.System,
    val isDynamicColors: Boolean = false,
    val isGlassmorphismEnabled: Boolean = true,
    val isLiquidGlassEnabled: Boolean = false,
    val isMeshGradientBackgroundEnabled: Boolean = true,
    val gradientBackgroundStyle: GradientBackgroundStyle = GradientBackgroundStyle.Sunset,
    val glassBaseAlpha: Float = 1.0f,
    val customBackgroundImageUri: String? = null,
    val isBuiltin: Boolean = true,
) {
    companion object {

        /** 将 [colorTupleString] 拆分为 ARGB 颜色列表。 */
        fun parseColorTuple(colorTupleString: String): List<Int> {
            if (colorTupleString.isBlank()) return emptyList()
            return colorTupleString.split("*").mapNotNull { it.toIntOrNull() }
        }

        /**
         * 将 4 个 ARGB 十六进制颜色值转换为 colorTupleString。
         * 用法: `colorTuple(0xFFF2BED1, 0xFFFDCEDF, 0xFFF8E8EE, 0xFFF9F5F6)`
         * 比手写 `"-873775*-147233*-460562*-68874"` 直观得多。
         */
        private fun colorTuple(
            primary: Long,
            secondary: Long,
            tertiary: Long,
            surface: Long,
        ): String = listOf(primary.toInt(), secondary.toInt(), tertiary.toInt(), surface.toInt())
            .joinToString("*")

        // ── 内置主题 ──

        /** 默认主题 */
        val Default = AppThemePreset(
            id = "builtin_dynamic",
            name = "千色千面",
            colorTupleString = "",
            isDynamicColors = true,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.Classic,
            glassBaseAlpha = 1.0f,
            isBuiltin = true,
        )

        /** 默认主题 */
        val LogoTheme = AppThemePreset(
            id = "builtin_logo",
            name = "品牌色",
            colorTupleString = colorTuple(
                primary   = 0xFF9592EA,
                secondary = 0xFF73CDEC,
                tertiary  = 0xFFD2B9EA,
                surface   = 0xFFE5E5E5,
            ),
            isDynamicColors = false,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Sunset,
            glassBaseAlpha = 1.0f,
            isBuiltin = true,
        )

        // ── 预制主题 ──

        val SakuraPink = AppThemePreset(
            id = "builtin_sakura_pink",
            name = "樱花粉",
            colorTupleString = colorTuple(
                primary   = 0xFFF2BED1,
                secondary = 0xFFFDCEDF,
                tertiary  = 0xFFF8E8EE,
                surface   = 0xFFF9F5F6,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.SakuraMist,
            glassBaseAlpha = 0.85f,
            isBuiltin = true,
            nightMode = NightMode.Light,
        )

        val MintFresh = AppThemePreset(
            id = "builtin_mint_fresh",
            name = "薄荷苏打",
            colorTupleString = colorTuple(
                primary   = 0xFF4DB6AC,
                secondary = 0xFF80CBC4,
                tertiary  = 0xFFB2DFDB,
                surface   = 0xFFE0F2F1,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.MintBreeze,
            glassBaseAlpha = 0.90f,
            isBuiltin = true,
        )

        val StarryBlue = AppThemePreset(
            id = "builtin_starry_blue",
            name = "晨雾蓝",
            colorTupleString = colorTuple(
                primary   = 0xFF5C7AEA,
                secondary = 0xFF8EA7E9,
                tertiary  = 0xFFB6C8F5,
                surface   = 0xFFEEF2FF,
            ),
            nightMode = NightMode.Dark,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Aurora,
            glassBaseAlpha = 0.85f,
            isBuiltin = true,
        )

        val PureGray = AppThemePreset(
            id = "builtin_pure_gray",
            name = "简约灰",
            colorTupleString = colorTuple(
                primary   = 0xFF9E9E9E,
                secondary = 0xFFBDBDBD,
                tertiary  = 0xFFE0E0E0,
                surface   = 0xFFF5F5F5,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.Ethereal,
            glassBaseAlpha = 1.0f,
            isBuiltin = true,
        )

        val PureGlassGray = AppThemePreset(
            id = "builtin_pure_glass_gray",
            name = "烟雾灰",
            colorTupleString = colorTuple(
                primary   = 0xFF7D8C9B,
                secondary = 0xFFA9B4C2,
                tertiary  = 0xFFD6DCE4,
                surface   = 0xFFF0F3F7,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Ethereal,
            glassBaseAlpha = 0.95f,
            isBuiltin = true,
        )

        val LavenderDream = AppThemePreset(
            id = "builtin_lavender_dream",
            name = "薰衣草田",
            colorTupleString = colorTuple(
                primary   = 0xFF9575CD,
                secondary = 0xFFB39DDB,
                tertiary  = 0xFFD1C4E9,
                surface   = 0xFFF3E5F5,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Lavender,
            glassBaseAlpha = 0.80f,
            isBuiltin = true,
        )

        val SunsetWarm = AppThemePreset(
            id = "builtin_sunset_warm",
            name = "焦糖拿铁",
            colorTupleString = colorTuple(
                primary   = 0xFFD4A373,
                secondary = 0xFFE9C496,
                tertiary  = 0xFFF4E4D0,
                surface   = 0xFFFDF8F2,
            ),
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.WarmGlow,
            glassBaseAlpha = 0.90f,
            isBuiltin = true,
        )

        val OceanDeep = AppThemePreset(
            id = "builtin_ocean_deep",
            name = "深海秘境",
            colorTupleString = colorTuple(
                primary   = 0xFF00695C,
                secondary = 0xFF00838F,
                tertiary  = 0xFF4DD0E1,
                surface   = 0xFFE0F7FA,
            ),
            nightMode = NightMode.Dark,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Ocean,
            glassBaseAlpha = 0.75f,
            isBuiltin = true,
        )

        /** 所有内置主题（按展示顺序） */
        val builtinThemes: List<AppThemePreset> = listOf(
            Default,
            LogoTheme,
            SakuraPink,
            PureGlassGray,
            MintFresh,
            StarryBlue,
            PureGray,
            LavenderDream,
            SunsetWarm,
            OceanDeep,
        )
    }
}
