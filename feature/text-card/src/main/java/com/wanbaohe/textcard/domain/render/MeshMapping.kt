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

package com.wanbaohe.textcard.domain.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.wanbaohe.textcard.domain.model.BackgroundSpec

/**
 * Mesh 渐变模型 → core/ui meshGradient 渲染所需的控制点结构。
 * 预览(Modifier.meshGradient)与导出(离屏 drawMeshGradient)共用本映射与分辨率常量,
 * 保证两端同一份网格数据与插值参数。
 */
fun BackgroundSpec.Gradient.toPointPairs(): List<List<Pair<Offset, Color>>> =
    points.map { row ->
        row.map { point ->
            Offset(point.offsetX, point.offsetY) to Color(point.argb)
        }
    }

/** mesh 插值分辨率(双端一致) */
const val MESH_RESOLUTION = 16
