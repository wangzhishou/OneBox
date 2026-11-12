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

package com.shifenmiao.model.editor

data class ShapeSpec(
    val kind: ShapeKind,
    val color: Int,
    val filled: Boolean = true,
    val widthRatio: Float = 0.3f,
    val heightRatio: Float = 0.3f,
    val cornerRadiusRatio: Float = 0f,
    val strokeWidthRatio: Float = 0.01f,
) {
    companion object {
        val DefaultColor: Int = 0xFF4CAF50.toInt()

        fun default(kind: ShapeKind): ShapeSpec = when (kind) {
            ShapeKind.Rectangle -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                cornerRadiusRatio = 0.01f
            )
            ShapeKind.Arrow -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                heightRatio = 0.18f
            )
            ShapeKind.Line -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                filled = false,
                heightRatio = 0.05f,
                strokeWidthRatio = 0.008f
            )
            ShapeKind.Circle, ShapeKind.Triangle, ShapeKind.Star -> ShapeSpec(
                kind = kind,
                color = DefaultColor
            )
        }
    }
}

enum class ShapeKind {
    Rectangle,
    Circle,
    Triangle,
    Arrow,
    Line,
    Star,
}
