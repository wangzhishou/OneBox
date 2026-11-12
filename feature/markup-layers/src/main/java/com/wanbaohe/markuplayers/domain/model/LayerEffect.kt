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

package com.wanbaohe.markuplayers.domain.model

import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter

/** An ordered, non-destructive filter. Strength interpolates premultiplied RGBA. */
data class LayerEffect(
    val filter: UiFilter<*>,
    val strength: Float = 1f,
) {
    init {
        require(strength.isFinite() && strength in 0f..1f) {
            "Effect strength must be finite and between 0 and 1"
        }
    }
}
