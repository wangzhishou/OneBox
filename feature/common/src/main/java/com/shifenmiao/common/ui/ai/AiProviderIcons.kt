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

package com.shifenmiao.common.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.base.ui.icon.BaiduBrandIcon
import com.wanbaohe.base.ui.icon.ClaudeBrandIcon
import com.wanbaohe.base.ui.icon.DeepSeekBrandIcon
import com.wanbaohe.base.ui.icon.DoubaoBrandIcon
import com.wanbaohe.base.ui.icon.GeminiBrandIcon
import com.wanbaohe.base.ui.icon.GrokBrandIcon
import com.wanbaohe.base.ui.icon.HunyuanBrandIcon
import com.wanbaohe.base.ui.icon.KimiBrandIcon
import com.wanbaohe.base.ui.icon.MiniMaxBrandIcon
import com.wanbaohe.base.ui.icon.OpenAiBrandIcon
import com.wanbaohe.base.ui.icon.OpenRouterBrandIcon
import com.wanbaohe.base.ui.icon.QwenBrandIcon
import com.wanbaohe.base.ui.icon.XiaomiBrandIcon
import com.wanbaohe.base.ui.icon.ZhipuBrandIcon

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
