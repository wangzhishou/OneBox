package com.shifenmiao.common.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.base.ui.icon.BaiduBrandIcon
import com.shifenmiao.base.ui.icon.ClaudeBrandIcon
import com.shifenmiao.base.ui.icon.DeepSeekBrandIcon
import com.shifenmiao.base.ui.icon.DoubaoBrandIcon
import com.shifenmiao.base.ui.icon.GeminiBrandIcon
import com.shifenmiao.base.ui.icon.GrokBrandIcon
import com.shifenmiao.base.ui.icon.HunyuanBrandIcon
import com.shifenmiao.base.ui.icon.KimiBrandIcon
import com.shifenmiao.base.ui.icon.MiniMaxBrandIcon
import com.shifenmiao.base.ui.icon.OpenAiBrandIcon
import com.shifenmiao.base.ui.icon.OpenRouterBrandIcon
import com.shifenmiao.base.ui.icon.QwenBrandIcon
import com.shifenmiao.base.ui.icon.XiaomiBrandIcon
import com.shifenmiao.base.ui.icon.ZhipuBrandIcon

/**
 * AI 厂商品牌矢量图标(单色填充,使用处按厂商色 tint)。
 * 图形在 core/ui 的厂商标里,这里按厂商名取用。
 * 未收录的厂商由 [providerBrandIcon] 返回 null,调用方回退到字母 monogram。
 */

/** 按 AiProvider.value 解析品牌图标;未知厂商返回 null(走 monogram 回退) */
fun providerBrandIcon(providerValue: String): ImageVector? =
    when (providerValue.trim().lowercase()) {
        "openai" -> OpenAiBrandIcon
        "gemini" -> GeminiBrandIcon
        "claude" -> ClaudeBrandIcon
        "deepseek" -> DeepSeekBrandIcon
        "grok" -> GrokBrandIcon
        "qwen" -> QwenBrandIcon
        "kimi" -> KimiBrandIcon
        "mimo" -> XiaomiBrandIcon
        "doubao" -> DoubaoBrandIcon
        "tencent" -> HunyuanBrandIcon
        "zhipu" -> ZhipuBrandIcon
        "minmax" -> MiniMaxBrandIcon
        "openrouter" -> OpenRouterBrandIcon
        "baidu" -> BaiduBrandIcon
        else -> null
    }

/** 厂商品牌色;未知厂商返回 null,由调用方决定回退色 */
fun providerAccentColor(providerValue: String): Color? =
    when (providerValue.trim().lowercase()) {
        "doubao" -> Color(0xFF6D5DF6)
        "tencent" -> Color(0xFF4F46E5)
        "deepseek" -> Color(0xFF4A3FF0)
        "qwen" -> Color(0xFF0F766E)
        "kimi" -> Color(0xFF7C3AED)
        "openai" -> Color(0xFF10A37F)
        "gemini" -> Color(0xFF1B72E8)
        "claude" -> Color(0xFFD97757)
        "grok" -> Color(0xFF404145)
        "mimo" -> Color(0xFFFF6900)
        "zhipu" -> Color(0xFF3859FF)
        "minmax" -> Color(0xFFD92D20)
        "openrouter" -> Color(0xFF6566F1)
        "baidu" -> Color(0xFF2932E1)
        else -> null
    }
