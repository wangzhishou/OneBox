/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.core.resources.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCopy

/**
 * 复制图标收口到 `Icons.Outlined.LineCopy`（用户提供的双方块形状）。
 *
 * 历史上有 `Icons.Rounded.ContentCopy` / `Icons.Outlined.ContentCopy` 两份描摹的
 * 材质图标，调用点遍布全项目；这里两个名字都指向同一个线框图标，
 * 调用点零改动就能统一外观，新代码请直接用 `Icons.Outlined.LineCopy`。
 */
val com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy: ImageVector
    get() = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCopy

val com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ContentCopy: ImageVector
    get() = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCopy
