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

package com.t8rin.imagetoolbox.core.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.FlashAuto: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Outlined.FlashAuto",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        group(translationY = 960f) {
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "m280-336 128-184H294l80-280H160v320h120v144ZM200-80v-320H80v-480h400l-80 280h160L200-80Zm80-400H160h120Zm305-40 135-360h64l137 360h-62l-32-92H679l-32 92h-62Zm112-144h110l-53-150h-2l-55 150Z"
                )
            )
        }
    }.build()
}
