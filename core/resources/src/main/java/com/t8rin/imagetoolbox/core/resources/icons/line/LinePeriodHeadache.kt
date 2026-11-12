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

val Icons.Outlined.LinePeriodHeadache: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LinePeriodHeadache",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes("""M 7.18 5.34 Q 6.56 4.89 5.96 4.45 C 5.79 4.32 5.89 4.06 6.1 4.09 Q 6.15 4.1 6.23 4.16 Q 7.29 4.93 7.69 5.23 C 7.81 5.31 7.79 5.5 7.65 5.57 Q 7.23 5.77 6.92 5.9 A 0.01 0.01 49.3 0 0 6.92 5.91 Q 7.33 6.16 7.87 6.5 Q 7.97 6.57 8 6.62 C 8.07 6.75 7.96 6.9 7.8 6.89 C 7.75 6.89 7.69 6.84 7.63 6.81 Q 6.67 6.22 6.39 6.03 C 6.3 5.98 6.29 5.83 6.36 5.75 Q 6.39 5.71 6.48 5.67 Q 6.82 5.52 7.18 5.36 A 0.01 0.01 0 0 0 7.18 5.34 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 17.19 5.34 A 0.01 0.01 0 0 0 17.19 5.36 Q 17.55 5.52 17.89 5.67 Q 17.98 5.71 18.01 5.75 C 18.08 5.83 18.07 5.98 17.98 6.03 Q 17.7 6.22 16.73 6.81 C 16.68 6.84 16.62 6.89 16.57 6.89 C 16.41 6.9 16.3 6.75 16.37 6.62 Q 16.39 6.57 16.5 6.5 Q 17.04 6.16 17.44 5.91 A 0.01 0.01 -49.3 0 0 17.44 5.9 Q 17.14 5.77 16.72 5.57 C 16.58 5.5 16.56 5.31 16.68 5.23 Q 17.08 4.93 18.13 4.16 Q 18.22 4.1 18.27 4.09 C 18.48 4.06 18.58 4.32 18.41 4.45 Q 17.81 4.89 17.19 5.34 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 7.39 13.58 C 7.4 13.48 7.36 13.42 7.26 13.4 Q 7.21 13.39 6.93 13.36 Q 6.87 13.35 6.73 13.3 C 6.35 13.15 6.19 12.76 6.36 12.37 Q 6.42 12.25 6.52 12.1 Q 6.89 11.59 7.28 11.05 C 7.43 10.85 7.59 10.63 7.58 10.38 Q 7.56 10.01 7.56 9.96 Q 7.57 9.38 7.74 8.89 Q 8.03 8.02 8.66 7.35 C 8.75 7.27 8.86 7.15 8.95 7.07 Q 9.76 6.38 10.8 6.1 C 11.83 5.82 12.92 5.84 13.93 6.17 Q 14.89 6.49 15.62 7.2 Q 16.4 7.97 16.78 8.97 C 17.29 10.3 17.12 11.87 16.43 13.1 C 16.29 13.34 16.12 13.59 15.91 13.86 Q 15.63 14.23 15.5 14.47 Q 15.22 14.99 15.2 15.52 C 15.2 15.84 15.23 16.18 15.26 16.52 Q 15.3 16.91 15.42 18.19 Q 15.43 18.31 15.41 18.36 C 15.34 18.51 15.13 18.51 15.05 18.37 Q 15.04 18.34 15.03 18.26 Q 14.87 16.61 14.86 16.51 Q 14.81 15.99 14.81 15.6 Q 14.81 14.97 15.08 14.42 Q 15.21 14.15 15.39 13.9 Q 15.73 13.44 15.84 13.28 C 16.51 12.34 16.79 11.16 16.65 10.02 C 16.56 9.33 16.28 8.67 15.87 8.09 Q 14.99 6.87 13.58 6.48 C 12.61 6.2 11.53 6.25 10.57 6.58 C 10.22 6.7 9.84 6.89 9.53 7.11 Q 9.02 7.48 8.65 7.98 C 8.23 8.55 7.97 9.24 7.95 9.94 C 7.95 10.06 7.96 10.22 7.97 10.37 C 7.98 10.69 7.84 10.94 7.66 11.19 Q 7.01 12.1 6.78 12.42 Q 6.75 12.46 6.71 12.56 C 6.64 12.73 6.72 12.87 6.89 12.94 Q 7.01 12.98 7.14 13 C 7.32 13.01 7.48 13.03 7.61 13.13 C 7.84 13.32 7.77 13.68 7.74 13.95 Q 7.73 14.08 7.79 14.2 C 7.88 14.34 7.92 14.49 7.92 14.64 Q 7.92 15.18 7.93 15.33 Q 7.96 15.87 8.42 16.11 Q 8.61 16.2 8.9 16.23 C 9.23 16.26 9.65 16.22 9.98 16.23 C 10.17 16.24 10.41 16.35 10.54 16.49 Q 10.69 16.65 10.75 16.85 Q 10.78 16.97 10.78 17.27 Q 10.78 17.51 10.78 18.22 Q 10.78 18.32 10.76 18.35 Q 10.68 18.5 10.52 18.46 C 10.41 18.43 10.39 18.34 10.39 18.22 Q 10.39 17.32 10.38 17.1 C 10.38 16.81 10.19 16.62 9.9 16.62 C 9.3 16.62 8.67 16.72 8.13 16.39 C 7.68 16.12 7.53 15.66 7.53 15.16 Q 7.54 14.84 7.53 14.63 Q 7.53 14.53 7.46 14.39 Q 7.39 14.27 7.38 14.24 Q 7.34 14.09 7.35 13.98 Q 7.38 13.64 7.39 13.58 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 5.56 8.35 Q 5.72 8.39 6.62 8.62 Q 6.75 8.66 6.78 8.69 C 6.88 8.76 6.88 8.91 6.78 8.99 C 6.72 9.04 6.66 9.04 6.58 9.01 Q 6 8.87 5.11 8.63 C 4.99 8.6 4.94 8.48 4.98 8.37 Q 4.99 8.33 5.06 8.26 Q 5.61 7.76 5.63 7.73 A 0.01 0 -31.7 0 0 5.63 7.73 Q 5.64 7.73 5.64 7.72 A 0.01 0.01 -80.4 0 0 5.63 7.72 Q 4.89 7.53 4.29 7.4 Q 4.16 7.36 4.12 7.33 C 3.99 7.21 4.09 6.95 4.29 6.99 Q 5.27 7.22 6.08 7.43 C 6.2 7.46 6.26 7.58 6.22 7.7 Q 6.2 7.74 6.12 7.81 Q 5.96 7.95 5.56 8.33 A 0.01 0.01 0 0 0 5.56 8.35 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 18.8 8.35 A 0.01 0.01 0 0 0 18.81 8.34 Q 18.4 7.95 18.24 7.81 Q 18.17 7.74 18.15 7.7 C 18.11 7.58 18.17 7.46 18.29 7.43 Q 19.09 7.22 20.08 6.99 C 20.27 6.95 20.38 7.21 20.24 7.33 Q 20.21 7.37 20.07 7.4 Q 19.48 7.53 18.74 7.72 A 0.01 0.01 80.4 0 0 18.73 7.72 Q 18.73 7.73 18.73 7.73 A 0.01 0 31.7 0 0 18.73 7.73 Q 18.76 7.76 19.31 8.27 Q 19.38 8.33 19.39 8.37 C 19.43 8.48 19.38 8.6 19.26 8.63 Q 18.37 8.87 17.79 9.02 C 17.71 9.04 17.65 9.04 17.58 8.99 C 17.49 8.91 17.49 8.76 17.58 8.69 Q 17.62 8.66 17.75 8.62 Q 18.64 8.39 18.8 8.35 Z"""),
            fill = SolidColor(Color.Black),
        )
        addPath(
            pathData = addPathNodes("""M 9.79 11.16 C 10.08 11.16 10.35 11.05 10.57 10.86 C 10.75 10.7 11.01 10.98 10.83 11.15 C 10.56 11.41 10.18 11.55 9.79 11.55 C 9.41 11.55 9.03 11.41 8.76 11.15 C 8.58 10.98 8.84 10.7 9.02 10.86 C 9.23 11.05 9.51 11.16 9.79 11.16 Z"""),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
