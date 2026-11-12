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

package com.t8rin.dynamic.theme

import com.materialkolor.dynamiccolor.ColorSpec

/**
 * Material 色彩规范版本。
 *
 * - [Spec2021] 原 Material You 规范(MaterialKolor 的默认值)。
 * - [Spec2025] Material 3 Expressive 引入的 2025 色彩规范,配色更饱和、更"表达"。
 *
 * 对应 MaterialKolor Builder 里的 `color_spec` 参数,与 [PaletteStyle.Expressive]
 * 组合即为截图中的 "Material 3 Expressive" 配色。
 */
enum class ColorSpecVersion {
    Spec2021,
    Spec2025;

    companion object {
        /** 兼容用 DataStore / MMKV 存的 ordinal。 */
        fun fromOrdinal(value: Int): ColorSpecVersion = entries.getOrNull(value) ?: Spec2021
    }
}

internal fun ColorSpecVersion.toMaterialKolorSpec(): ColorSpec.SpecVersion = when (this) {
    ColorSpecVersion.Spec2021 -> ColorSpec.SpecVersion.SPEC_2021
    ColorSpecVersion.Spec2025 -> ColorSpec.SpecVersion.SPEC_2025
}
