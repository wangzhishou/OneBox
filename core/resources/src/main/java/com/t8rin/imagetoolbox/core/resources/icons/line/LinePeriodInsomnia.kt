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

package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LinePeriodInsomnia: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePeriodInsomnia",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes("""M 16.7 5.57 Q 17.03 5.57 18.18 5.57 C 18.4 5.57 18.44 5.88 18.24 5.95 Q 18.2 5.96 18.11 5.96 Q 17.67 5.96 16.54 5.96 Q 16.38 5.96 16.32 5.93 C 16.19 5.86 16.13 5.72 16.19 5.58 Q 16.21 5.52 16.28 5.44 C 16.65 4.98 17.11 4.45 17.71 3.74 A 0.01 0.01 0 0 0 17.71 3.72 Q 17.07 3.72 16.29 3.72 Q 16.19 3.72 16.15 3.71 C 15.99 3.63 15.99 3.42 16.15 3.34 C 16.21 3.32 16.29 3.32 16.36 3.32 Q 17.36 3.32 17.98 3.32 C 18.25 3.33 18.35 3.61 18.18 3.81 Q 17.31 4.82 16.69 5.56 A 0.01 0.01 0 0 0 16.7 5.57 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 15.44 6.96 Q 14.98 6.95 14.4 6.96 C 14.13 6.96 14.13 6.58 14.4 6.58 Q 14.48 6.58 15.74 6.58 Q 15.85 6.58 15.92 6.66 C 16.01 6.76 15.98 6.91 15.9 7.01 Q 15.18 7.83 14.84 8.22 A 0.01 0.01 0 0 0 14.84 8.23 Q 15.59 8.24 15.75 8.23 Q 15.86 8.23 15.9 8.26 C 16.05 8.34 16.03 8.54 15.88 8.6 Q 15.85 8.61 15.77 8.61 Q 15.16 8.61 14.62 8.61 Q 14.49 8.61 14.44 8.59 C 14.34 8.54 14.28 8.41 14.31 8.3 C 14.34 8.22 14.42 8.15 14.5 8.06 Q 14.85 7.65 15.44 6.97 A 0.01 0.01 -68.4 0 0 15.44 6.96 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 11.22 7.13 C 11.37 7.27 11.31 7.53 11.17 7.66 Q 10.91 7.91 10.64 8.23 Q 10.32 8.61 10.05 9.15 C 9.55 10.11 9.42 11.21 9.63 12.27 Q 9.79 13.13 10.17 13.87 Q 10.82 15.12 11.97 15.93 C 13.18 16.78 14.72 17.13 16.14 16.63 Q 16.63 16.46 17.1 16.14 Q 17.21 16.07 17.26 16.06 C 17.55 15.97 17.73 16.22 17.65 16.48 Q 17.63 16.56 17.57 16.66 C 17.04 17.53 16.23 18.22 15.3 18.67 C 13.53 19.52 11.47 19.44 9.73 18.56 Q 8.2 17.79 7.21 16.38 Q 6.4 15.24 6.15 13.82 C 5.96 12.7 6.1 11.54 6.55 10.5 C 7.11 9.21 8.11 8.15 9.38 7.53 Q 10.14 7.17 10.97 7.05 Q 11.11 7.03 11.22 7.13 Z M 10.75 7.5 C 9.76 7.69 8.84 8.21 8.12 8.91 Q 7.55 9.47 7.14 10.2 C 6.34 11.64 6.26 13.38 6.87 14.91 C 7.49 16.48 8.76 17.72 10.31 18.39 C 12.21 19.2 14.45 18.99 16.09 17.71 Q 16.66 17.27 17.08 16.68 A 0.01 0.01 0 0 0 17.06 16.66 Q 17.01 16.67 16.98 16.69 Q 16.52 16.93 16.24 17.02 C 15.44 17.29 14.61 17.33 13.78 17.16 C 12.68 16.93 11.66 16.33 10.88 15.53 C 9.68 14.3 9.02 12.56 9.16 10.84 Q 9.26 9.6 9.94 8.52 Q 10.29 7.97 10.76 7.51 A 0.01 0.01 0 0 0 10.75 7.5 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 13 9.42 Q 12.59 9.41 12.52 9.41 Q 12.38 9.42 12.33 9.4 C 12.16 9.31 12.23 9.05 12.41 9.05 Q 12.97 9.05 13.32 9.05 C 13.54 9.05 13.62 9.27 13.48 9.43 Q 13.06 9.9 12.78 10.22 A 0.01 0.01 0 0 0 12.79 10.23 Q 13.17 10.23 13.42 10.23 C 13.56 10.23 13.64 10.38 13.58 10.5 C 13.53 10.58 13.46 10.6 13.36 10.6 Q 13.08 10.6 12.47 10.6 C 12.27 10.6 12.18 10.36 12.31 10.21 Q 12.45 10.06 13 9.43 A 0.01 0.01 0 0 0 13 9.42 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 14.39 12.86 C 15.44 12.86 16.41 13.32 17.12 14.1 Q 17.25 14.25 17.27 14.33 Q 17.29 14.45 17.18 14.58 C 16.47 15.41 15.48 15.85 14.39 15.86 C 13.29 15.86 12.31 15.42 11.59 14.58 Q 11.48 14.45 11.51 14.33 Q 11.52 14.25 11.65 14.11 C 12.36 13.32 13.33 12.86 14.39 12.86 Z M 14.37 13.31 A 1 0.95 -90.5 0 0 13.42 14.32 A 1 0.95 -90.5 0 0 14.39 15.31 A 1 0.95 -90.5 0 0 15.33 14.3 A 1 0.95 -90.5 0 0 14.37 13.31 Z M 15.15 15.41 A 0.01 0.01 0 0 0 15.15 15.42 Q 16.17 15.17 16.86 14.38 A 0.01 0.01 45.7 0 0 16.86 14.37 Q 16.21 13.63 15.28 13.35 A 0.01 0.01 0 0 0 15.27 13.36 C 15.81 13.9 15.82 14.73 15.29 15.28 C 15.24 15.33 15.19 15.37 15.15 15.41 Z M 13.61 15.4 C 13.57 15.36 13.52 15.32 13.47 15.28 C 12.94 14.73 12.95 13.9 13.48 13.37 A 0.01 0.01 13.7 0 0 13.48 13.36 Q 12.55 13.64 11.91 14.38 A 0.01 0.01 0 0 0 11.91 14.39 Q 12.59 15.17 13.61 15.41 A 0.01 0.01 0 0 0 13.61 15.4 Z"""),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
