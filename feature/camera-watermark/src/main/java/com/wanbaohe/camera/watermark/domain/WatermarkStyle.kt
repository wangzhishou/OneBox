package com.wanbaohe.camera.watermark.domain

import androidx.compose.ui.graphics.Color

/**
 * 水印样式配置
 * 定义水印的视觉外观和自定义内容
 */
data class WatermarkStyle(
    val id: Long = 0,
    val name: String = "经典",
    val backgroundColor: Long = 0xFFFFFFFF,     // 背景色
    val primaryTextColor: Long = 0xFF1A1A1A,    // 主文字颜色
    val secondaryTextColor: Long = 0xFF666666,  // 次要文字颜色
    val logoType: LogoType = LogoType.LEICA,    // Logo 类型
    val customLogoPath: String? = null,         // 自定义 Logo 路径（当 logoType 为 CUSTOM 时使用）
    val showDivider: Boolean = true,            // 是否显示分隔线
    val dividerColor: Long = 0xFFE0E0E0,        // 分隔线颜色
    val watermarkHeight: Int = 120,             // 水印区域高度 (dp)
    val paddingHorizontal: Int = 24,            // 水平内边距 (dp)
    val paddingVertical: Int = 16,              // 垂直内边距 (dp)
    val primaryFontSize: Int = 35,              // 主文字大小 (相对于水印高度的百分比)
    val secondaryFontSize: Int = 22,            // 次要文字大小 (相对于水印高度的百分比)
    val createdAt: Long = System.currentTimeMillis(),
    val isPreset: Boolean = false,              // 是否为预置模板（由 Entity 透传，展示层据此本地化名称）

    // ===== 自定义内容 (为 null 时使用 EXIF 元数据) =====
    val customContent: WatermarkContent = WatermarkContent.EMPTY,
) {
    companion object {
        /**
         * 默认样式（徕卡经典白色风格）
         * 用于初始化和兼容旧代码
         */
        val DEFAULT = WatermarkStyle(
            name = "徕卡经典",
            logoType = LogoType.LEICA,
        )

        // 兼容旧代码
        val CLASSIC_WHITE = DEFAULT
    }
}

/**
 * Logo 类型
 * 预置 Logo 和自定义（用户选择图片）
 */
enum class LogoType {
    NONE,           // 无 Logo
    LEICA,          // 徕卡 (R.drawable.leica_logo)
    WANBAOHE,       // 万宝盒 (R.drawable.logo)
    APPLE,          // Apple (R.drawable.apple_logo)
    GOOGLE,         // Google (R.drawable.google_logo)
    HUAWEI,         // 华为 (R.drawable.huawei_logo)
    OPPO,           // OPPO (R.drawable.oppo_logo)
    VIVO,           // vivo (R.drawable.vivo_logo)
    XIAOMI,         // 小米 (R.drawable.xiaomi_logo)

    ONEPLUS,        // 一加 (R.drawable.oneplus_logo)
    CUSTOM,         // 自定义（用户选择的图片）
}

