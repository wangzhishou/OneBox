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

val Icons.Outlined.LinePeriodAppetite: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePeriodAppetite",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes("""M 18.43 4.59 C 18.65 4.6 18.85 4.78 18.86 5 Q 18.86 5.16 18.86 5.4 Q 18.86 17.39 18.86 17.45 C 18.86 17.7 18.86 17.93 18.79 18.16 C 18.68 18.53 18.39 18.81 18 18.89 C 17.66 18.96 17.3 18.87 17.04 18.64 C 16.73 18.38 16.63 17.97 16.65 17.56 Q 16.75 14.87 16.97 12.05 C 16.98 11.92 16.93 11.83 16.81 11.75 C 16.45 11.52 16.2 11.23 16.09 10.82 C 16.02 10.55 16.02 10.31 16.01 9.95 Q 16 9.07 16.11 8.21 Q 16.2 7.49 16.41 6.78 C 16.57 6.24 16.81 5.68 17.13 5.22 Q 17.21 5.11 17.33 5 Q 17.78 4.55 18.43 4.59 Z M 18.45 5.03 A 0.08 0.08 0 0 0 18.39 4.99 C 17.9 4.98 17.59 5.24 17.35 5.63 C 16.93 6.34 16.69 7.13 16.56 7.94 C 16.44 8.71 16.41 9.5 16.43 10.28 C 16.44 10.7 16.54 11.04 16.88 11.3 C 16.98 11.38 17.1 11.45 17.18 11.53 C 17.33 11.68 17.39 11.9 17.37 12.11 C 17.23 13.92 17.13 15.71 17.06 17.38 C 17.05 17.66 17.04 17.94 17.17 18.16 Q 17.3 18.39 17.53 18.47 C 17.81 18.56 18.11 18.49 18.29 18.26 C 18.42 18.1 18.45 17.85 18.45 17.64 Q 18.46 5.06 18.45 5.05 Q 18.45 5.04 18.45 5.03 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 5.54 5.01 A 0.01 0.01 0 0 0 5.52 5.02 Q 5.5 7.84 5.5 8.21 Q 5.49 8.54 5.53 8.7 Q 5.62 9.08 5.9 9.34 C 5.98 9.42 6.11 9.5 6.19 9.58 Q 6.54 9.9 6.57 10.4 Q 6.57 10.48 6.57 10.73 Q 6.44 15.26 6.37 17.58 C 6.37 17.76 6.38 17.94 6.46 18.1 C 6.73 18.66 7.56 18.65 7.79 18.05 C 7.86 17.87 7.85 17.7 7.85 17.45 Q 7.85 17.39 7.83 16.88 Q 7.76 14.21 7.68 10.53 Q 7.66 9.95 8.06 9.58 Q 8.07 9.57 8.25 9.42 Q 8.75 9.02 8.74 8.34 Q 8.73 7.26 8.71 5.03 A 0.02 0.02 -2.7 0 0 8.69 5.01 C 8.46 5.03 8.36 4.78 8.55 4.65 C 8.74 4.51 9.01 4.64 9.09 4.84 Q 9.12 4.92 9.13 5.14 Q 9.15 7.89 9.15 8.38 C 9.16 8.92 8.93 9.42 8.5 9.75 C 8.43 9.8 8.35 9.86 8.3 9.92 Q 8.08 10.17 8.09 10.48 Q 8.21 15.26 8.26 17.57 C 8.27 17.85 8.23 18.14 8.09 18.38 Q 7.88 18.72 7.53 18.85 C 7.16 18.98 6.71 18.92 6.4 18.67 C 6.09 18.41 5.96 18.02 5.97 17.62 Q 6.11 12.51 6.16 10.48 Q 6.17 10.18 5.96 9.92 Q 5.93 9.88 5.75 9.75 C 5.41 9.5 5.19 9.13 5.11 8.73 Q 5.07 8.54 5.08 8.1 Q 5.09 7.9 5.11 5.04 C 5.11 4.79 5.24 4.62 5.49 4.6 C 5.66 4.58 5.84 4.68 5.8 4.87 C 5.77 5 5.65 5.02 5.54 5.01 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 6.74 5.04 L 6.74 7.95 A 0.1 0.09 -39.1 0 1 6.73 7.97 Q 6.69 8.12 6.54 8.13 C 6.39 8.14 6.33 8.02 6.33 7.89 Q 6.33 6.21 6.33 4.94 Q 6.33 4.76 6.44 4.67 C 6.53 4.59 6.68 4.57 6.79 4.63 C 6.96 4.73 6.95 4.95 6.76 5.02 A 0.03 0.03 80.5 0 0 6.74 5.04 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 7.5 5.01 C 7.27 4.93 7.36 4.63 7.57 4.6 Q 7.8 4.57 7.89 4.75 Q 7.92 4.82 7.92 4.98 Q 7.92 5.86 7.92 7.93 C 7.92 8.2 7.51 8.2 7.51 7.9 Q 7.51 5.5 7.51 5.03 A 0.01 0.01 -79.5 0 0 7.5 5.01 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 12.68 5.95 C 12.8 5.95 12.88 6 12.97 6.09 Q 13.67 6.83 14.46 7.65 Q 14.59 7.79 14.61 7.85 C 14.69 8.06 14.54 8.27 14.31 8.27 Q 14.03 8.27 13.67 8.27 A 0.01 0.01 -0.5 0 0 13.66 8.29 Q 13.66 9.94 13.66 10.27 Q 13.66 10.43 13.64 10.49 C 13.58 10.65 13.44 10.77 13.26 10.77 Q 13.22 10.77 12.68 10.77 Q 12.14 10.77 12.09 10.77 C 11.92 10.77 11.77 10.65 11.71 10.49 Q 11.69 10.43 11.69 10.27 Q 11.69 9.94 11.69 8.29 A 0.01 0.01 0.5 0 0 11.68 8.27 Q 11.32 8.27 11.04 8.27 C 10.81 8.27 10.67 8.06 10.74 7.85 Q 10.77 7.79 10.89 7.65 Q 11.68 6.83 12.39 6.09 C 12.47 6 12.55 5.95 12.68 5.95 Z M 14.12 7.88 L 12.68 6.37 A 0.01 0.01 0 0 0 12.67 6.37 L 11.24 7.87 A 0.01 0.01 0 0 0 11.25 7.89 Q 11.57 7.89 11.9 7.89 Q 12 7.89 12.05 7.96 Q 12.09 8.01 12.08 8.13 Q 12.08 8.19 12.08 10.29 Q 12.08 10.34 12.08 10.35 A 0.04 0.04 0 0 0 12.12 10.39 L 13.24 10.39 A 0.03 0.03 0 0 0 13.27 10.37 Q 13.27 10.35 13.27 10.34 Q 13.28 10.07 13.27 8.1 C 13.27 7.93 13.35 7.89 13.52 7.89 Q 13.77 7.89 14.12 7.89 A 0 0 0 0 0 14.12 7.88 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 12.71 12.61 Q 13.24 12.61 13.24 12.61 C 13.47 12.62 13.66 12.8 13.66 13.04 Q 13.66 13.19 13.66 15.16 A 0.02 0.02 0 0 0 13.67 15.17 Q 14.16 15.18 14.25 15.17 Q 14.42 15.17 14.49 15.21 C 14.67 15.31 14.72 15.54 14.58 15.69 Q 13.44 16.91 12.97 17.41 Q 12.87 17.52 12.71 17.52 Q 12.56 17.52 12.46 17.41 Q 11.99 16.91 10.85 15.69 C 10.71 15.54 10.75 15.31 10.93 15.21 Q 11.01 15.17 11.18 15.17 Q 11.27 15.18 11.75 15.17 A 0.02 0.02 0 0 0 11.77 15.16 Q 11.77 13.19 11.77 13.04 C 11.77 12.8 11.95 12.62 12.19 12.61 Q 12.19 12.61 12.71 12.61 Z M 12.72 17.1 Q 12.72 17.1 12.72 17.1 A 0.01 0 4.1 0 0 12.72 17.09 L 14.14 15.57 A 0.01 0.01 0 0 0 14.14 15.56 Q 13.91 15.56 13.46 15.56 Q 13.36 15.56 13.31 15.49 Q 13.28 15.44 13.28 15.32 Q 13.28 14.56 13.28 13.09 C 13.28 13.04 13.29 13 13.23 13 Q 12.96 13 12.71 13 Q 12.46 13 12.2 13 C 12.14 13 12.15 13.04 12.15 13.09 Q 12.15 14.56 12.15 15.32 Q 12.15 15.44 12.12 15.49 Q 12.07 15.56 11.97 15.56 Q 11.52 15.56 11.29 15.56 A 0.01 0.01 0 0 0 11.29 15.58 L 12.71 17.09 A 0.01 0 -4.2 0 0 12.71 17.1 Q 12.71 17.1 12.72 17.1 Z"""),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
