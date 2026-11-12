/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.settings.domain.model

import com.t8rin.dynamic.theme.ColorSpecVersion
import com.t8rin.dynamic.theme.PaletteStyle

/**
 * 全局色彩系统 —— 调色板风格 / 对比度 / 色彩规范 / Expressive 动效。
 *
 * 与 [AppThemePreset] 的分工: 预设决定"用什么颜色"(色元组 / 动态取色 / 玻璃 / 背景),
 * 色彩系统决定"颜色如何推导"(Material 色彩规范与派生风格), 因此全局唯一、不随预设切换。
 *
 * @param paletteStyle      调色板风格(TonalSpot / Expressive / Vibrant ...)
 * @param contrastLevel     对比度, -1.0(低) ~ 1.0(高), 0.0 为默认
 * @param colorSpec         色彩规范版本(Spec2021 原版 / Spec2025 Material 3 Expressive)
 * @param isExpressiveTheme 是否启用 MaterialExpressiveTheme(表达性动效与形状)
 */
data class AppColorSystem(
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val contrastLevel: Double = 0.0,
    val colorSpec: ColorSpecVersion = ColorSpecVersion.Spec2021,
    val isExpressiveTheme: Boolean = false,
) {
    companion object {
        const val MIN_CONTRAST_LEVEL: Double = -1.0
        const val MAX_CONTRAST_LEVEL: Double = 1.0
    }
}
