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

package com.shifenmiao.model.canvas

enum class CreationCanvasSpec(
    val width: Int,
    val height: Int,
    val ratioLabel: String,
) {
    SQUARE(width = 1080, height = 1080, ratioLabel = "1:1"),
    PORTRAIT(width = 1080, height = 1440, ratioLabel = "3:4"),
    LANDSCAPE(width = 1440, height = 1080, ratioLabel = "4:3"),
    WIDE(width = 1920, height = 1080, ratioLabel = "16:9"),
    STORY(width = 1080, height = 1920, ratioLabel = "9:16");

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}
