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

package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.NationalDay: ImageVector
    get() {
        val current = _nationalDay
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.NationalDay",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M14.4 6 L14 4 H5 v17 h2 v-7 h5.6 l.4 2 h7 V6z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.4 6
                moveTo(x = 14.4f, y = 6.0f)
                // L 14 4
                lineTo(x = 14.0f, y = 4.0f)
                // H 5
                horizontalLineTo(x = 5.0f)
                // v 17
                verticalLineToRelative(dy = 17.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v -7
                verticalLineToRelative(dy = -7.0f)
                // h 5.6
                horizontalLineToRelative(dx = 5.6f)
                // l 0.4 2
                lineToRelative(dx = 0.4f, dy = 2.0f)
                // h 7
                horizontalLineToRelative(dx = 7.0f)
                // V 6z
                verticalLineTo(y = 6.0f)
                close()
            }
        }.build().also { _nationalDay = it }
    }

@Suppress("ObjectPropertyName")
private var _nationalDay: ImageVector? = null
