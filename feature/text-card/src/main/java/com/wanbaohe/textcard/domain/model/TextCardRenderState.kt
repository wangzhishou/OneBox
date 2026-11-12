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

package com.wanbaohe.textcard.domain.model

data class TextCardSegment(
    val block: TextBlock,
    val role: TextCardRole,
    val sourceStart: Int,
    val sourceEnd: Int,
)

data class TextCardSourceSelection(
    val role: TextCardRole,
    val start: Int,
    val end: Int,
)

data class TextCardRenderState(
    val canvas: CanvasSpec,
    val background: BackgroundSpec,
    val backgroundOpacity: Float = 1f,
    val backgroundVisible: Boolean = true,
    val textBlocks: List<TextBlock> = emptyList(),
    val segments: List<TextCardSegment> = emptyList(),
    val annotations: List<TextCardAnnotation> = emptyList(),
)
