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

val Icons.Outlined.LinePeriodFatigue: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePeriodFatigue",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes("""M 8.26 9.61 C 8.08 9.41 8 9.19 8 8.91 Q 8 7.71 8 6.59 C 8 5.98 8.45 5.51 9.06 5.51 Q 13.54 5.51 14.76 5.51 C 15.07 5.51 15.34 5.62 15.55 5.85 C 15.7 6.02 15.81 6.25 15.82 6.49 Q 15.82 6.57 15.82 6.57 A 0.02 0.02 87.8 0 0 15.83 6.58 C 15.93 6.59 16.13 6.57 16.25 6.6 Q 16.58 6.68 16.69 6.99 Q 16.72 7.09 16.72 7.31 Q 16.72 7.77 16.72 8.21 Q 16.72 8.41 16.69 8.51 C 16.6 8.76 16.38 8.91 16.12 8.91 Q 15.97 8.9 15.83 8.91 A 0.01 0.01 0 0 0 15.82 8.92 Q 15.82 9.28 15.62 9.56 A 0.01 0.01 0 0 0 15.62 9.58 Q 15.89 9.83 16.05 10.06 Q 16.49 10.68 16.5 11.44 Q 16.5 11.63 16.51 11.7 A 0.01 0.01 0 0 0 16.52 11.71 C 17.03 11.76 17.37 12.15 17.38 12.66 Q 17.38 12.97 17.37 13.08 Q 17.35 13.4 17.14 13.64 Q 16.88 13.94 16.43 13.95 A 0.01 0.01 0 0 0 16.42 13.96 C 16.31 14.51 16.1 15.08 15.84 15.57 Q 15.27 16.63 14.35 17.31 C 13.37 18.03 12.11 18.31 10.92 17.96 C 9.1 17.43 7.9 15.76 7.53 13.96 A 0.02 0.02 84.1 0 0 7.51 13.95 C 6.97 13.95 6.58 13.54 6.56 13.01 Q 6.56 12.96 6.56 12.65 Q 6.56 12.47 6.63 12.31 Q 6.82 11.83 7.33 11.72 Q 7.35 11.72 7.43 11.71 A 0.01 0.01 0 0 0 7.44 11.69 Q 7.44 11.58 7.44 11.38 C 7.47 10.69 7.76 10.11 8.26 9.64 A 0.02 0.02 0 0 0 8.26 9.61 Z M 15.45 6.54 A 0.66 0.66 0 0 0 14.79 5.88 L 9.04 5.88 A 0.66 0.66 0 0 0 8.38 6.54 L 8.38 8.94 A 0.66 0.66 0 0 0 9.04 9.6 L 14.79 9.6 A 0.66 0.66 0 0 0 15.45 8.94 L 15.45 6.54 Z M 15.82 6.97 L 15.82 8.52 A 0.02 0.02 0 0 0 15.84 8.54 L 16.12 8.54 A 0.24 0.23 90 0 0 16.35 8.3 L 16.35 7.19 A 0.24 0.23 90 0 0 16.12 6.95 L 15.84 6.95 A 0.02 0.02 0 0 0 15.82 6.97 Z M 15.32 9.83 Q 15.07 9.98 14.73 9.98 Q 11.83 9.97 9.09 9.97 Q 8.79 9.97 8.58 9.87 A 0.02 0.02 37.3 0 0 8.56 9.87 Q 7.94 10.42 7.83 11.22 C 7.8 11.41 7.82 11.67 7.81 11.84 C 7.81 11.96 7.8 12.03 7.7 12.07 Q 7.67 12.09 7.54 12.09 Q 7.42 12.08 7.38 12.09 Q 7.03 12.18 6.95 12.52 C 6.93 12.64 6.94 12.78 6.94 12.9 C 6.93 13.17 7.02 13.41 7.28 13.53 C 7.38 13.58 7.5 13.58 7.62 13.57 C 7.75 13.55 7.84 13.59 7.87 13.73 Q 7.87 13.76 7.89 13.84 Q 8.11 14.94 8.71 15.84 C 9.46 16.95 10.65 17.75 12.02 17.73 C 13.18 17.72 14.24 17.09 14.97 16.2 C 15.54 15.49 15.91 14.65 16.07 13.76 C 16.1 13.62 16.16 13.55 16.31 13.57 C 16.47 13.58 16.6 13.58 16.74 13.49 Q 16.98 13.34 17 13.03 Q 17 13.01 17 12.75 Q 17 12.57 16.97 12.47 C 16.9 12.24 16.71 12.09 16.45 12.08 C 16.37 12.08 16.28 12.1 16.22 12.06 Q 16.13 12.01 16.13 11.91 C 16.13 11.6 16.14 11.41 16.11 11.19 Q 15.99 10.38 15.34 9.83 A 0.02 0.02 49.9 0 0 15.32 9.83 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 10.49 8.64 A 0.43 0.43 0 0 1 10.07 9.07 L 9.35 9.07 A 0.43 0.43 0 0 1 8.92 8.64 L 8.91 6.89 A 0.43 0.43 0 0 1 9.34 6.46 L 10.06 6.46 A 0.43 0.43 0 0 1 10.49 6.88 L 10.49 8.64 Z M 10.13 6.89 A 0.08 0.08 0 0 0 10.05 6.82 L 9.35 6.82 A 0.08 0.08 0 0 0 9.28 6.89 L 9.28 8.63 A 0.08 0.08 0 0 0 9.35 8.71 L 10.05 8.71 A 0.08 0.08 0 0 0 10.13 8.63 L 10.13 6.89 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 9.86 13.12 C 10.17 13.12 10.47 12.98 10.65 12.73 Q 10.7 12.65 10.74 12.64 C 10.9 12.57 11.05 12.7 11 12.87 Q 10.98 12.92 10.89 13.03 C 10.63 13.33 10.26 13.49 9.86 13.49 C 9.46 13.49 9.09 13.33 8.83 13.03 Q 8.74 12.92 8.72 12.87 C 8.67 12.71 8.82 12.58 8.97 12.64 Q 9.01 12.65 9.07 12.73 C 9.24 12.98 9.55 13.12 9.86 13.12 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 14.08 13.49 C 13.68 13.49 13.31 13.33 13.05 13.03 Q 12.96 12.92 12.94 12.87 C 12.89 12.7 13.04 12.57 13.2 12.64 Q 13.24 12.65 13.29 12.73 C 13.47 12.98 13.77 13.12 14.08 13.12 C 14.39 13.12 14.69 12.98 14.87 12.73 Q 14.93 12.65 14.96 12.64 C 15.12 12.58 15.27 12.71 15.22 12.87 Q 15.2 12.92 15.11 13.03 C 14.85 13.33 14.48 13.49 14.08 13.49 Z"""),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
