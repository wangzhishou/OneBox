package com.t8rin.imagetoolbox.core.settings.domain.model

import com.shifenmiao.model.theme.ThemeDefaults

/**
 * 主题预设 —— 将颜色方案 + 玻璃效果 + 背景风格 + 透明度基准值 + 日夜模式打包为一个可切换的完整"主题"。
 *
 * @param id                         唯一标识符
 * @param name                       显示名称（中文）
 * @param colorTupleString           四色元组字符串: "primary*secondary*tertiary*surface" (ARGB int)。
 * @param nightMode                  该主题预设绑定的日夜模式
 * @param isDynamicColors            是否使用壁纸动态取色（"千色千面"模式）
 * @param isGlassmorphismEnabled     是否开启玻璃透明（默认关闭：玻璃是重特效，
 *                                   默认给实心 M3 界面保流畅，需要的人在主题设置里主动开）
 * @param isLiquidGlassEnabled       是否开启液态玻璃
 * @param isMeshGradientBackgroundEnabled 是否开启渐变背景
 * @param gradientBackgroundStyle    渐变背景风格
 * @param glassBaseAlpha             玻璃特效透明度基准值 (0f..1f)，
 *                                   所有 GlassStyle 的 backgroundAlpha 按此基准缩放
 * @param glassBorderAlpha           玻璃描边可见度 (0f..1f)，0 隐藏，1 为完整强度，
 *                                   默认 [ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA]
 * @param customBackgroundImageUri   自定义背景图片 URI（可为 null）
 * @param isBuiltin                  是否为内置主题（内置主题不可删除）
 */
data class AppThemePreset(
    val id: String,
    val name: String,
    val colorTupleString: String,
    val nightMode: NightMode = NightMode.System,
    val isDynamicColors: Boolean = false,
    val isGlassmorphismEnabled: Boolean = false,
    val isLiquidGlassEnabled: Boolean = false,
    val isMeshGradientBackgroundEnabled: Boolean = true,
    val gradientBackgroundStyle: GradientBackgroundStyle = GradientBackgroundStyle.Sunset,
    val glassBaseAlpha: Float = 1.0f,
    val customBackgroundImageUri: String? = null,
    val isBuiltin: Boolean = true,
    val glassBorderAlpha: Float = ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA,
) {
    companion object {

        /**
         * "自定义/未保存"哨兵 id —— 当实际生效的主题配置偏离任何已存预设时使用
         * (AI 工具直接改字段、旧版本预览草稿的 "__editing__" 残留)。
         * 选择器匹配不到该 id 时不高亮任何卡片, 语义上诚实。
         */
        const val CUSTOM_ID = "__custom__"

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

        /**
         * 默认主题「千色千面」(壁纸动态取色)。
         * 刻意不带玻璃透明: 首启要的是流畅, 玻璃是用户自己到主题设置里打开的加特效。
         * 其余内置预设保留各自的玻璃观感(卡片上有透明角标提示), 由用户主动点选。
         */
        val Default = AppThemePreset(
            id = "builtin_dynamic",
            name = "千色千面",
            colorTupleString = "",
            isDynamicColors = true,
            isGlassmorphismEnabled = false,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.Classic,
            glassBaseAlpha = 1.0f,
            isBuiltin = true,
        )

        /**
         * 品牌紫 + 汽水青，背景用极光（青绿光幕和紫罗兰），跟这两色是同一族。
         * 磨砂玻璃开、液态玻璃关，日夜跟随系统。
         */
        val LogoTheme = AppThemePreset(
            id = "builtin_logo",
            name = "品牌色",
            colorTupleString = colorTuple(
                primary   = 0xFF9592EA,
                secondary = 0xFF73CDEC,
                tertiary  = 0xFFD2B9EA,
                surface   = 0xFFE5E5E5,
            ),
            nightMode = NightMode.System,
            isDynamicColors = false,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Aurora,
            glassBaseAlpha = 1.0f,
            glassBorderAlpha = 0.24f,
            isBuiltin = true,
        )

        // ── 预制主题 ──
        // 每套只把该开的开关打开，背景风格跟配色走同一色相，不要套一套参数。
        // 浅色：樱花 / 薄荷 / 薰衣草 / 焦糖。暗色：晨雾蓝 / 深海 / 星野。
        // 跟随系统：品牌色 / 烟雾灰 / 简约灰。液态玻璃只给花、紫、海、星野。

        val SakuraPink = AppThemePreset(
            id = "builtin_sakura_pink",
            name = "樱花粉",
            colorTupleString = colorTuple(
                primary   = 0xFFF2BED1,
                secondary = 0xFFFDCEDF,
                tertiary  = 0xFFF8E8EE,
                surface   = 0xFFF9F5F6,
            ),
            nightMode = NightMode.Light,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.SakuraMist,
            glassBaseAlpha = 0.72f,
            glassBorderAlpha = 0.36f,
            isBuiltin = true,
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
            nightMode = NightMode.Light,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.MintBreeze,
            glassBaseAlpha = 0.92f,
            glassBorderAlpha = 0.20f,
            isBuiltin = true,
        )

        /** 暗色 + 磨砂，不叠加液态玻璃。背景是午夜星河，不是极光。 */
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
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.StarryNight,
            glassBaseAlpha = 0.78f,
            glassBorderAlpha = 0.42f,
            isBuiltin = true,
        )

        /** 只有磨砂，没有渐变，用来看一块平的灰底。 */
        val PureGray = AppThemePreset(
            id = "builtin_pure_gray",
            name = "简约灰",
            colorTupleString = colorTuple(
                primary   = 0xFF9E9E9E,
                secondary = 0xFFBDBDBD,
                tertiary  = 0xFFE0E0E0,
                surface   = 0xFFF5F5F5,
            ),
            nightMode = NightMode.System,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.Classic,
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
            nightMode = NightMode.System,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Ethereal,
            glassBaseAlpha = 0.88f,
            glassBorderAlpha = 0.20f,
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
            nightMode = NightMode.Light,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.Lavender,
            glassBaseAlpha = 0.68f,
            glassBorderAlpha = 0.32f,
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
            nightMode = NightMode.Light,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = false,
            isMeshGradientBackgroundEnabled = true,
            gradientBackgroundStyle = GradientBackgroundStyle.WarmGlow,
            glassBaseAlpha = 0.90f,
            glassBorderAlpha = 0.22f,
            isBuiltin = true,
        )

        /** 暗色 + 液态玻璃，背景只有蓝和青绿。 */
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
            glassBaseAlpha = 0.62f,
            glassBorderAlpha = 0.48f,
            isBuiltin = true,
        )

        /**
         * 星野：夜色湖面壁纸，图在 R2 `themes/starfield-night.jpg`。
         * 网格渐变关掉，否则会盖住照片。液态玻璃叠在图上，描边拉高一点，卡片边才站得住。
         * 配色取湖面那道月光金、地平线的青和雪峰的冷灰；surface 仍用浅种子，暗色由 [NightMode.Dark] 生成。
         */
        const val STARFIELD_BACKGROUND_URL =
            "https://images.oneboxable.com/themes/starfield-night.jpg"

        val StarField = AppThemePreset(
            id = "builtin_starfield",
            name = "星野",
            colorTupleString = colorTuple(
                primary   = 0xFFC9A36E,
                secondary = 0xFF6E929C,
                tertiary  = 0xFFA8B7BC,
                surface   = 0xFFE7EBED,
            ),
            nightMode = NightMode.Dark,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.StarryNight,
            glassBaseAlpha = 0.58f,
            glassBorderAlpha = 0.50f,
            customBackgroundImageUri = STARFIELD_BACKGROUND_URL,
            isBuiltin = true,
        )

        /**
         * 朝晖：清晨湖面壁纸，图在 R2 `themes/daybreak.jpg`。
         * 和星野一对，这张给浅色。网格渐变关掉，否则会盖住照片。
         * 配色取日出的暖光、雾蓝的山和很浅的天；照片偏亮，玻璃比星野稍实一点。
         */
        const val DAYBREAK_BACKGROUND_URL =
            "https://images.oneboxable.com/themes/daybreak.jpg"

        val Daybreak = AppThemePreset(
            id = "builtin_daybreak",
            name = "朝晖",
            colorTupleString = colorTuple(
                primary   = 0xFFD4A48A,
                secondary = 0xFF7E9AA3,
                tertiary  = 0xFFC5D0D2,
                surface   = 0xFFF3F6F7,
            ),
            nightMode = NightMode.Light,
            isGlassmorphismEnabled = true,
            isLiquidGlassEnabled = true,
            isMeshGradientBackgroundEnabled = false,
            gradientBackgroundStyle = GradientBackgroundStyle.WarmGlow,
            glassBaseAlpha = 0.72f,
            glassBorderAlpha = 0.36f,
            customBackgroundImageUri = DAYBREAK_BACKGROUND_URL,
            isBuiltin = true,
        )

        /** 所有内置主题（按展示顺序）。第一项千色千面固定不动。 */
        val builtinThemes: List<AppThemePreset> = listOf(
            Default,
            LogoTheme,
            StarField,
            Daybreak,
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
