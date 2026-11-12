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

package com.wanbaohe.core.ui.widget.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.settings.domain.model.AppThemePreset

/**
 * 内置主题预设显示名 → 多语言字符串资源 的映射。
 *
 * [AppThemePreset.name] 固定为中文原始名, 供 Agent 工具模糊匹配等场景使用;
 * UI 展示一律走 [displayName] / [localizedName], 用户自建主题直接返回其自命名。
 */
fun AppThemePreset.builtinNameResId(): Int? = when (id) {
    AppThemePreset.Default.id -> R.string.theme_preset_name_dynamic
    AppThemePreset.LogoTheme.id -> R.string.theme_preset_name_logo
    AppThemePreset.SakuraPink.id -> R.string.theme_preset_name_sakura_pink
    AppThemePreset.MintFresh.id -> R.string.theme_preset_name_mint_fresh
    AppThemePreset.StarryBlue.id -> R.string.theme_preset_name_starry_blue
    AppThemePreset.PureGray.id -> R.string.theme_preset_name_pure_gray
    AppThemePreset.PureGlassGray.id -> R.string.theme_preset_name_pure_glass_gray
    AppThemePreset.LavenderDream.id -> R.string.theme_preset_name_lavender_dream
    AppThemePreset.SunsetWarm.id -> R.string.theme_preset_name_sunset_warm
    AppThemePreset.OceanDeep.id -> R.string.theme_preset_name_ocean_deep
    AppThemePreset.StarField.id -> R.string.theme_preset_name_starfield
    AppThemePreset.Daybreak.id -> R.string.theme_preset_name_daybreak
    // 旧版本在中文界面保存的用户主题, 名字是内置主题的中文原名:
    // 按内置名反查一次, 让它们在其它语言下也能显示本地化名
    else -> AppThemePreset.builtinThemes
        .firstOrNull { it.name == name }
        ?.builtinNameResId()
}

/** Composable 场景下的本地化显示名 */
@Composable
fun AppThemePreset.displayName(): String {
    val resId = builtinNameResId()
    return if (resId != null) stringResource(resId) else name
}

/** 非 Composable 场景(如 Component 逻辑)下的本地化显示名 */
fun AppThemePreset.localizedName(): String {
    val resId = builtinNameResId()
    return if (resId != null) AppContext.getString(resId) else name
}
