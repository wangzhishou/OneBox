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

val Icons.Outlined.LinePeriodAcne: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePeriodAcne",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes("""M 6.45 10.76 A 0.01 0.01 -9 0 0 6.46 10.75 Q 6.38 10.42 6.32 10.11 C 6.18 9.37 6.18 8.6 6.35 7.85 C 6.68 6.42 7.52 5.14 8.74 4.32 C 9.82 3.6 11.15 3.3 12.46 3.38 C 13.65 3.46 14.75 3.86 15.66 4.61 C 16.75 5.49 17.47 6.78 17.7 8.15 Q 17.83 8.91 17.73 9.69 Q 17.66 10.28 17.53 10.75 A 0.01 0.01 0 0 0 17.54 10.76 Q 17.6 10.77 17.62 10.77 C 17.99 10.8 18.3 11.06 18.41 11.41 Q 18.5 11.7 18.5 11.99 C 18.5 12.34 18.43 12.7 18.29 13.03 C 18.06 13.55 17.63 13.92 17.04 13.89 A 0.01 0.01 0 0 0 17.03 13.89 C 16.89 15.08 16.39 16.22 15.69 17.18 Q 15.06 18.05 14.18 18.68 Q 13.72 19.01 13.2 19.21 Q 12.04 19.67 10.88 19.21 C 10.14 18.92 9.5 18.45 8.95 17.89 C 8.02 16.93 7.35 15.73 7.06 14.43 Q 6.99 14.14 6.97 13.89 A 0.01 0.01 0 0 0 6.96 13.88 C 6.29 13.95 5.83 13.42 5.63 12.84 Q 5.46 12.33 5.5 11.78 C 5.55 11.23 5.86 10.78 6.45 10.76 Z M 17.04 10.91 L 17.18 10.83 A 0.02 0.02 82.8 0 0 17.19 10.81 C 17.38 10.11 17.49 9.38 17.44 8.65 Q 17.41 8.22 17.29 7.75 Q 16.99 6.61 16.27 5.68 Q 16.03 5.37 15.7 5.05 C 15.57 4.93 15.4 4.8 15.23 4.68 C 14.25 3.96 13.04 3.63 11.82 3.68 C 10.66 3.73 9.56 4.09 8.65 4.78 C 7.62 5.56 6.92 6.71 6.65 7.98 C 6.44 8.93 6.54 9.89 6.8 10.81 A 0.02 0.02 -84.7 0 0 6.81 10.82 L 6.98 10.92 A 0 0 0 0 0 6.99 10.92 Q 7.08 10.34 7.44 9.88 C 8.11 9 9.25 8.69 10.29 8.4 C 10.84 8.24 11.41 8.06 11.99 7.82 Q 12.51 7.61 12.94 7.32 C 13.19 7.16 13.44 6.93 13.59 6.67 Q 13.63 6.59 13.67 6.58 C 13.76 6.53 13.87 6.55 13.9 6.65 Q 14.05 7.2 14.43 7.61 Q 14.61 7.81 14.74 7.91 C 14.87 8.02 15.07 8.17 15.26 8.3 Q 15.35 8.36 15.6 8.55 Q 15.88 8.74 16.13 8.99 Q 16.85 9.71 17 10.72 Q 17.01 10.81 17.02 10.9 A 0.01 0.01 0 0 0 17.04 10.91 Z M 13.69 7.07 C 13.41 7.4 13.04 7.66 12.66 7.86 Q 11.89 8.25 11.02 8.51 Q 10.44 8.69 9.93 8.83 Q 9.59 8.92 9.22 9.06 C 8.8 9.22 8.36 9.43 8.01 9.73 Q 7.29 10.36 7.27 11.3 Q 7.27 11.39 7.25 11.43 C 7.18 11.53 7.03 11.53 6.98 11.42 C 6.88 11.2 6.7 11.07 6.46 11.07 C 6.22 11.06 6.01 11.21 5.91 11.42 Q 5.82 11.61 5.81 11.83 Q 5.77 12.41 5.99 12.92 Q 6.1 13.17 6.28 13.34 C 6.42 13.47 6.64 13.59 6.84 13.58 Q 6.94 13.58 6.97 13.55 C 7.08 13.46 7.25 13.48 7.26 13.64 C 7.3 14.19 7.44 14.76 7.64 15.27 Q 8.13 16.54 9.07 17.55 C 9.6 18.12 10.24 18.61 10.95 18.9 C 11.52 19.13 12.13 19.2 12.73 19.04 C 13.34 18.87 13.91 18.53 14.39 18.11 Q 14.82 17.74 15.11 17.39 Q 16.08 16.26 16.51 14.83 Q 16.69 14.25 16.73 13.69 Q 16.74 13.58 16.77 13.55 C 16.84 13.48 16.92 13.48 17.01 13.54 Q 17.09 13.6 17.22 13.58 C 17.53 13.53 17.76 13.35 17.92 13.08 C 18.14 12.7 18.22 12.22 18.18 11.78 C 18.16 11.54 18.05 11.27 17.84 11.15 Q 17.48 10.95 17.16 11.21 Q 17.08 11.27 17.01 11.42 C 16.95 11.55 16.73 11.51 16.73 11.37 C 16.74 10.61 16.53 9.91 16.03 9.35 C 15.79 9.08 15.52 8.86 15.22 8.66 C 15.02 8.52 14.95 8.47 14.75 8.32 Q 14.45 8.1 14.19 7.82 C 13.98 7.59 13.83 7.35 13.71 7.07 A 0.01 0.01 0 0 0 13.69 7.07 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 9.68 12.67 C 9.26 12.67 8.86 12.53 8.56 12.23 Q 8.5 12.17 8.5 12.11 C 8.48 11.97 8.62 11.91 8.74 11.96 A 0.08 0.07 81.5 0 1 8.76 11.98 C 8.99 12.24 9.34 12.36 9.68 12.36 C 10.02 12.36 10.36 12.24 10.59 11.98 A 0.08 0.07 -81.6 0 1 10.61 11.96 C 10.73 11.9 10.87 11.97 10.86 12.11 Q 10.85 12.17 10.79 12.23 C 10.49 12.52 10.1 12.67 9.68 12.67 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 14.32 12.36 C 14.66 12.36 15.01 12.24 15.23 11.98 A 0.08 0.07 -81.5 0 1 15.26 11.96 C 15.38 11.91 15.51 11.97 15.5 12.11 Q 15.5 12.17 15.43 12.23 C 15.13 12.53 14.74 12.67 14.32 12.67 C 13.9 12.67 13.51 12.52 13.21 12.23 Q 13.14 12.17 13.14 12.11 C 13.13 11.97 13.26 11.91 13.38 11.96 A 0.08 0.07 81.6 0 1 13.41 11.98 C 13.64 12.24 13.98 12.36 14.32 12.36 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 11.98 14.38 Q 11.79 14.38 11.65 14.52 A 0.05 0.05 9.5 0 1 11.64 14.53 C 11.45 14.62 11.34 14.39 11.48 14.26 C 11.61 14.14 11.8 14.07 11.98 14.07 C 12.16 14.07 12.34 14.13 12.47 14.26 C 12.62 14.39 12.5 14.61 12.32 14.53 A 0.05 0.05 -9.8 0 1 12.3 14.52 Q 12.16 14.38 11.98 14.38 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 12 16.18 C 12.28 16.18 12.59 16.25 12.82 16.4 Q 12.94 16.47 12.87 16.61 A 0.06 0.06 81.2 0 1 12.86 16.63 C 12.79 16.7 12.71 16.69 12.63 16.64 C 12.45 16.53 12.22 16.49 12 16.49 C 11.77 16.49 11.55 16.53 11.37 16.64 C 11.29 16.69 11.21 16.7 11.14 16.63 A 0.06 0.06 -81.2 0 1 11.13 16.62 Q 11.06 16.47 11.17 16.4 C 11.41 16.25 11.72 16.18 12 16.18 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 12.84 9.48 A 0.3 0.3 0 1 0 13.43 9.48 A 0.3 0.3 0 1 0 12.84 9.48 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 11.33 10.44 A 0.29 0.29 0 1 0 11.91 10.44 A 0.29 0.29 0 1 0 11.33 10.44 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 8.4 14.18 A 0.28 0.28 0 1 0 8.96 14.18 A 0.28 0.28 0 1 0 8.4 14.18 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 15.05 14.26 A 0.29 0.29 0 1 0 15.64 14.26 A 0.29 0.29 0 1 0 15.05 14.26 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 14.36 15.76 A 0.28 0.28 0 1 0 14.91 15.76 A 0.28 0.28 0 1 0 14.36 15.76 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 13.51 17.52 A 0.28 0.28 0 1 0 14.07 17.52 A 0.28 0.28 0 1 0 13.51 17.52 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 10 17.53 A 0.28 0.28 0 1 0 10.56 17.53 A 0.28 0.28 0 1 0 10 17.53 Z"""),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
