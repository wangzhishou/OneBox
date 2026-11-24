/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

/**
 * 方框「停止」图标（来源：图标合集第 3 格，icon-vectorize 矢量化）。
 *
 * 24dp 视口、圆角方框、笔画约 0.9 单位。原来的双框（方框套方框）在 24dp 下
 * 线条糊成一坨，这里换成合集里的单框；聊天流式输出的停止按钮也用它。
 */
package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LineStop: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineStop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4.55f, 3.36f)
            curveTo(4.61f, 3.36f, 4.68f, 3.36f, 4.74f, 3.36f)
            curveTo(4.93f, 3.36f, 5.11f, 3.36f, 5.29f, 3.36f)
            curveTo(5.48f, 3.36f, 5.68f, 3.36f, 5.87f, 3.36f)
            curveTo(6.25f, 3.36f, 6.64f, 3.36f, 7.02f, 3.36f)
            curveTo(7.33f, 3.36f, 7.64f, 3.36f, 7.95f, 3.36f)
            curveTo(8f, 3.36f, 8.04f, 3.36f, 8.09f, 3.36f)
            curveTo(8.18f, 3.36f, 8.27f, 3.36f, 8.36f, 3.36f)
            curveTo(9.2f, 3.36f, 10.04f, 3.36f, 10.88f, 3.36f)
            curveTo(11.66f, 3.36f, 12.43f, 3.36f, 13.2f, 3.36f)
            curveTo(13.99f, 3.36f, 14.78f, 3.36f, 15.57f, 3.36f)
            curveTo(16.02f, 3.36f, 16.46f, 3.36f, 16.91f, 3.36f)
            curveTo(17.28f, 3.36f, 17.66f, 3.36f, 18.04f, 3.36f)
            curveTo(18.23f, 3.36f, 18.43f, 3.36f, 18.62f, 3.36f)
            curveTo(18.8f, 3.36f, 18.97f, 3.36f, 19.15f, 3.36f)
            curveTo(19.24f, 3.36f, 19.34f, 3.36f, 19.43f, 3.36f)
            curveTo(20.19f, 3.36f, 20.77f, 3.56f, 21.33f, 4.07f)
            curveTo(21.41f, 4.16f, 21.47f, 4.24f, 21.52f, 4.36f)
            curveTo(21.55f, 4.36f, 21.58f, 4.36f, 21.61f, 4.36f)
            curveTo(21.9f, 4.78f, 21.97f, 5.25f, 21.96f, 5.74f)
            curveTo(21.96f, 5.77f, 21.96f, 5.77f, 21.96f, 5.91f)
            curveTo(21.96f, 6.07f, 21.96f, 6.22f, 21.96f, 6.38f)
            curveTo(21.96f, 6.55f, 21.96f, 6.71f, 21.96f, 6.88f)
            curveTo(21.96f, 7.21f, 21.96f, 7.53f, 21.96f, 7.86f)
            curveTo(21.96f, 8.13f, 21.96f, 8.39f, 21.96f, 8.66f)
            curveTo(21.96f, 8.7f, 21.96f, 8.74f, 21.96f, 8.78f)
            curveTo(21.96f, 8.85f, 21.96f, 8.93f, 21.96f, 9.01f)
            curveTo(21.96f, 9.73f, 21.96f, 10.45f, 21.96f, 11.18f)
            curveTo(21.96f, 11.8f, 21.96f, 12.42f, 21.96f, 13.03f)
            curveTo(21.96f, 13.75f, 21.96f, 14.48f, 21.96f, 15.2f)
            curveTo(21.96f, 15.27f, 21.96f, 15.35f, 21.96f, 15.43f)
            curveTo(21.96f, 15.45f, 21.96f, 15.45f, 21.96f, 15.54f)
            curveTo(21.96f, 15.81f, 21.96f, 16.07f, 21.96f, 16.34f)
            curveTo(21.96f, 16.66f, 21.96f, 16.99f, 21.96f, 17.31f)
            curveTo(21.96f, 17.48f, 21.96f, 17.64f, 21.96f, 17.81f)
            curveTo(21.96f, 17.99f, 21.96f, 18.17f, 21.96f, 18.34f)
            curveTo(21.96f, 18.37f, 21.96f, 18.37f, 21.96f, 18.5f)
            curveTo(21.96f, 18.87f, 21.89f, 19.21f, 21.75f, 19.55f)
            curveTo(21.72f, 19.56f, 21.69f, 19.58f, 21.66f, 19.59f)
            curveTo(21.64f, 19.64f, 21.63f, 19.69f, 21.61f, 19.73f)
            curveTo(21.58f, 19.73f, 21.55f, 19.73f, 21.52f, 19.73f)
            curveTo(21.5f, 19.76f, 21.49f, 19.79f, 21.48f, 19.82f)
            curveTo(21.41f, 19.94f, 21.33f, 20.02f, 21.22f, 20.12f)
            curveTo(21.18f, 20.15f, 21.14f, 20.18f, 21.11f, 20.22f)
            curveTo(21f, 20.29f, 20.94f, 20.32f, 20.81f, 20.34f)
            curveTo(20.83f, 20.39f, 20.84f, 20.44f, 20.86f, 20.48f)
            curveTo(20.85f, 20.48f, 20.85f, 20.48f, 20.78f, 20.46f)
            curveTo(20.67f, 20.44f, 20.67f, 20.44f, 20.59f, 20.48f)
            curveTo(20.21f, 20.66f, 19.66f, 20.58f, 19.25f, 20.58f)
            curveTo(19.19f, 20.58f, 19.12f, 20.58f, 19.06f, 20.58f)
            curveTo(18.88f, 20.58f, 18.7f, 20.58f, 18.52f, 20.58f)
            curveTo(18.32f, 20.58f, 18.13f, 20.58f, 17.93f, 20.58f)
            curveTo(17.55f, 20.58f, 17.18f, 20.58f, 16.8f, 20.58f)
            curveTo(16.49f, 20.58f, 16.18f, 20.58f, 15.87f, 20.58f)
            curveTo(15.83f, 20.58f, 15.78f, 20.58f, 15.74f, 20.58f)
            curveTo(15.65f, 20.58f, 15.56f, 20.58f, 15.47f, 20.58f)
            curveTo(14.63f, 20.58f, 13.79f, 20.58f, 12.96f, 20.58f)
            curveTo(12.24f, 20.58f, 11.52f, 20.58f, 10.8f, 20.58f)
            curveTo(9.97f, 20.58f, 9.13f, 20.58f, 8.3f, 20.58f)
            curveTo(8.21f, 20.58f, 8.12f, 20.58f, 8.03f, 20.58f)
            curveTo(7.99f, 20.58f, 7.94f, 20.58f, 7.9f, 20.58f)
            curveTo(7.59f, 20.58f, 7.28f, 20.58f, 6.97f, 20.58f)
            curveTo(6.6f, 20.58f, 6.22f, 20.58f, 5.85f, 20.58f)
            curveTo(5.65f, 20.58f, 5.46f, 20.58f, 5.27f, 20.58f)
            curveTo(5.1f, 20.58f, 4.92f, 20.58f, 4.74f, 20.58f)
            curveTo(4.65f, 20.58f, 4.56f, 20.58f, 4.47f, 20.58f)
            curveTo(3.52f, 20.58f, 3.52f, 20.58f, 3.38f, 20.44f)
            curveTo(3.25f, 20.41f, 3.25f, 20.41f, 3.14f, 20.39f)
            curveTo(3.14f, 20.36f, 3.14f, 20.33f, 3.14f, 20.3f)
            curveTo(3.12f, 20.29f, 3.12f, 20.29f, 3.04f, 20.28f)
            curveTo(2.88f, 20.24f, 2.82f, 20.19f, 2.71f, 20.07f)
            curveTo(2.68f, 20.04f, 2.65f, 20.01f, 2.62f, 19.97f)
            curveTo(2.39f, 19.72f, 2.39f, 19.72f, 2.39f, 19.55f)
            curveTo(2.34f, 19.53f, 2.3f, 19.52f, 2.25f, 19.5f)
            curveTo(2.2f, 19.39f, 2.2f, 19.39f, 2.15f, 19.25f)
            curveTo(2.13f, 19.2f, 2.12f, 19.16f, 2.1f, 19.11f)
            curveTo(2.09f, 19.07f, 2.08f, 19.03f, 2.06f, 18.98f)
            curveTo(2.05f, 18.96f, 2.05f, 18.96f, 2.02f, 18.84f)
            curveTo(2.01f, 18.77f, 2.01f, 18.69f, 2.01f, 18.62f)
            curveTo(2.01f, 18.57f, 2.01f, 18.52f, 2.01f, 18.47f)
            curveTo(2.01f, 18.42f, 2.01f, 18.37f, 2.01f, 18.32f)
            curveTo(2.01f, 18.29f, 2.01f, 18.29f, 2.01f, 18.15f)
            curveTo(2.01f, 18f, 2.01f, 17.84f, 2.01f, 17.69f)
            curveTo(2.01f, 17.53f, 2.01f, 17.36f, 2.01f, 17.2f)
            curveTo(2.01f, 16.84f, 2.01f, 16.48f, 2.01f, 16.12f)
            curveTo(2.01f, 15.89f, 2.01f, 15.66f, 2.01f, 15.44f)
            curveTo(2.01f, 14.82f, 2f, 14.19f, 2f, 13.57f)
            curveTo(2f, 13.53f, 2f, 13.49f, 2f, 13.45f)
            curveTo(2f, 13.41f, 2f, 13.37f, 2f, 13.32f)
            curveTo(2f, 13.24f, 2f, 13.16f, 2f, 13.08f)
            curveTo(2f, 13.04f, 2f, 13f, 2f, 12.96f)
            curveTo(2f, 12.31f, 2f, 11.66f, 2f, 11.01f)
            curveTo(2f, 10.34f, 2f, 9.67f, 2f, 9f)
            curveTo(2f, 8.62f, 2f, 8.25f, 2f, 7.87f)
            curveTo(2f, 7.55f, 2f, 7.23f, 2f, 6.91f)
            curveTo(2f, 6.75f, 2f, 6.58f, 2f, 6.42f)
            curveTo(1.99f, 5.16f, 1.99f, 5.16f, 2.16f, 4.64f)
            curveTo(2.19f, 4.64f, 2.22f, 4.64f, 2.25f, 4.64f)
            curveTo(2.25f, 4.61f, 2.26f, 4.59f, 2.26f, 4.56f)
            curveTo(2.3f, 4.43f, 2.36f, 4.33f, 2.44f, 4.22f)
            curveTo(2.47f, 4.22f, 2.5f, 4.22f, 2.53f, 4.22f)
            curveTo(2.54f, 4.19f, 2.56f, 4.16f, 2.57f, 4.14f)
            curveTo(2.75f, 3.81f, 3.13f, 3.6f, 3.47f, 3.47f)
            curveTo(3.83f, 3.37f, 4.17f, 3.36f, 4.55f, 3.36f)
            close()
            moveTo(3.33f, 4.64f)
            curveTo(3.33f, 4.67f, 3.33f, 4.7f, 3.33f, 4.73f)
            curveTo(3.28f, 4.75f, 3.24f, 4.77f, 3.19f, 4.78f)
            curveTo(2.94f, 5.08f, 2.92f, 5.38f, 2.92f, 5.75f)
            curveTo(2.92f, 5.8f, 2.92f, 5.86f, 2.92f, 5.91f)
            curveTo(2.92f, 6.09f, 2.92f, 6.27f, 2.92f, 6.45f)
            curveTo(2.92f, 6.58f, 2.92f, 6.71f, 2.92f, 6.84f)
            curveTo(2.92f, 7.15f, 2.92f, 7.46f, 2.92f, 7.78f)
            curveTo(2.93f, 8.14f, 2.93f, 8.51f, 2.93f, 8.87f)
            curveTo(2.93f, 9.52f, 2.93f, 10.17f, 2.93f, 10.83f)
            curveTo(2.93f, 11.46f, 2.93f, 12.09f, 2.93f, 12.72f)
            curveTo(2.93f, 13.41f, 2.93f, 14.1f, 2.93f, 14.79f)
            curveTo(2.93f, 14.82f, 2.93f, 14.86f, 2.93f, 14.9f)
            curveTo(2.93f, 14.97f, 2.93f, 15.04f, 2.93f, 15.12f)
            curveTo(2.93f, 15.37f, 2.93f, 15.63f, 2.93f, 15.88f)
            curveTo(2.93f, 16.19f, 2.93f, 16.5f, 2.93f, 16.81f)
            curveTo(2.93f, 16.97f, 2.93f, 17.13f, 2.93f, 17.28f)
            curveTo(2.93f, 18.61f, 2.93f, 18.61f, 3.09f, 19.03f)
            curveTo(3.12f, 19.05f, 3.16f, 19.06f, 3.19f, 19.08f)
            curveTo(3.2f, 19.12f, 3.22f, 19.16f, 3.24f, 19.21f)
            curveTo(3.36f, 19.42f, 3.48f, 19.5f, 3.7f, 19.59f)
            curveTo(4.12f, 19.66f, 4.55f, 19.65f, 4.97f, 19.64f)
            curveTo(5.03f, 19.64f, 5.09f, 19.64f, 5.16f, 19.64f)
            curveTo(5.29f, 19.64f, 5.43f, 19.64f, 5.57f, 19.64f)
            curveTo(5.94f, 19.64f, 6.31f, 19.64f, 6.69f, 19.64f)
            curveTo(6.71f, 19.64f, 6.71f, 19.64f, 6.81f, 19.64f)
            curveTo(7.66f, 19.64f, 8.51f, 19.64f, 9.36f, 19.65f)
            curveTo(9.56f, 19.65f, 9.75f, 19.65f, 9.95f, 19.66f)
            curveTo(10.19f, 19.66f, 10.43f, 19.66f, 10.66f, 19.66f)
            curveTo(13.17f, 19.68f, 15.68f, 19.7f, 18.18f, 19.67f)
            curveTo(18.29f, 19.67f, 18.39f, 19.67f, 18.49f, 19.67f)
            curveTo(18.72f, 19.66f, 18.95f, 19.66f, 19.18f, 19.66f)
            curveTo(19.27f, 19.66f, 19.35f, 19.66f, 19.43f, 19.66f)
            curveTo(19.54f, 19.65f, 19.65f, 19.65f, 19.77f, 19.65f)
            curveTo(19.83f, 19.65f, 19.89f, 19.65f, 19.95f, 19.65f)
            curveTo(20.09f, 19.64f, 20.21f, 19.63f, 20.34f, 19.59f)
            curveTo(20.34f, 19.56f, 20.34f, 19.53f, 20.34f, 19.5f)
            curveTo(20.41f, 19.48f, 20.47f, 19.47f, 20.53f, 19.45f)
            curveTo(20.53f, 19.42f, 20.53f, 19.39f, 20.53f, 19.36f)
            curveTo(20.58f, 19.34f, 20.62f, 19.33f, 20.67f, 19.31f)
            curveTo(21.02f, 18.97f, 20.95f, 18.41f, 20.96f, 17.96f)
            curveTo(20.96f, 17.62f, 20.96f, 17.28f, 20.96f, 16.94f)
            curveTo(20.96f, 16.84f, 20.96f, 16.74f, 20.96f, 16.64f)
            curveTo(20.96f, 16.22f, 20.96f, 15.8f, 20.96f, 15.37f)
            curveTo(20.96f, 15.23f, 20.96f, 15.08f, 20.96f, 14.93f)
            curveTo(20.96f, 13.81f, 20.96f, 12.7f, 20.95f, 11.58f)
            curveTo(20.95f, 11.54f, 20.95f, 11.5f, 20.95f, 11.46f)
            curveTo(20.95f, 10.47f, 20.95f, 9.49f, 20.94f, 8.5f)
            curveTo(20.94f, 8.47f, 20.94f, 8.43f, 20.94f, 8.4f)
            curveTo(20.94f, 8.36f, 20.94f, 8.32f, 20.94f, 8.29f)
            curveTo(20.94f, 8.04f, 20.94f, 7.79f, 20.94f, 7.54f)
            curveTo(20.94f, 7.24f, 20.93f, 6.94f, 20.93f, 6.63f)
            curveTo(20.93f, 6.48f, 20.93f, 6.33f, 20.93f, 6.17f)
            curveTo(20.93f, 6.01f, 20.93f, 5.84f, 20.93f, 5.67f)
            curveTo(20.93f, 5.62f, 20.93f, 5.57f, 20.93f, 5.52f)
            curveTo(20.92f, 5.27f, 20.91f, 5.09f, 20.77f, 4.88f)
            curveTo(20.73f, 4.88f, 20.7f, 4.88f, 20.67f, 4.88f)
            curveTo(20.67f, 4.86f, 20.67f, 4.86f, 20.63f, 4.79f)
            curveTo(20.58f, 4.69f, 20.53f, 4.62f, 20.44f, 4.55f)
            curveTo(20.38f, 4.53f, 20.31f, 4.51f, 20.25f, 4.5f)
            curveTo(20.23f, 4.47f, 20.22f, 4.44f, 20.2f, 4.41f)
            curveTo(18.11f, 4.4f, 16.01f, 4.4f, 13.92f, 4.39f)
            curveTo(13.67f, 4.39f, 13.42f, 4.39f, 13.17f, 4.39f)
            curveTo(13.12f, 4.39f, 13.08f, 4.39f, 13.02f, 4.39f)
            curveTo(12.23f, 4.39f, 11.43f, 4.39f, 10.63f, 4.39f)
            curveTo(9.81f, 4.39f, 9f, 4.38f, 8.18f, 4.38f)
            curveTo(7.67f, 4.38f, 7.17f, 4.38f, 6.66f, 4.38f)
            curveTo(6.32f, 4.38f, 5.97f, 4.38f, 5.62f, 4.38f)
            curveTo(5.42f, 4.38f, 5.22f, 4.38f, 5.02f, 4.38f)
            curveTo(4.84f, 4.38f, 4.66f, 4.38f, 4.48f, 4.38f)
            curveTo(4.41f, 4.38f, 4.34f, 4.38f, 4.28f, 4.38f)
            curveTo(3.92f, 4.37f, 3.59f, 4.37f, 3.33f, 4.64f)
            close()
        }
    }.build()
}
