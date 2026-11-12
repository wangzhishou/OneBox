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

package com.wanbaohe.markuplayers.presentation.render

import android.os.Build
import androidx.compose.ui.graphics.BlendMode
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode

/**
 * Compose has no source-over Multiply on API 24..28 (Modulate is not equivalent).
 * Such previews must use a bounded bitmap composition from MarkupLayersApplier,
 * with the same processed-content map, rather than drawing this mode as SrcOver.
 */
val LayerBlendMode.requiresSoftwarePreview: Boolean
    get() = this == LayerBlendMode.Multiply && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

fun LayerBlendMode.toComposeBlendMode(): BlendMode = when (this) {
    LayerBlendMode.Normal -> BlendMode.SrcOver
    LayerBlendMode.Multiply -> BlendMode.Multiply
    LayerBlendMode.Screen -> BlendMode.Screen
    LayerBlendMode.Overlay -> BlendMode.Overlay
    LayerBlendMode.Darken -> BlendMode.Darken
    LayerBlendMode.Lighten -> BlendMode.Lighten
}
