/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

/**
 * DSH(DeepSeek Harness)鲸鱼 logo:截自官方 wordmark 的鲸鱼 path
 * (在 24x24 viewport 内原坐标已居中,y 3.5~20.5),tint 交给调用方。
 */
val Icons.Rounded.DshWhale: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Rounded.DshWhale",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(23.06f, 4.95f)
            curveTo(22.81f, 4.83f, 22.71f, 5.06f, 22.56f, 5.18f)
            curveTo(22.51f, 5.21f, 22.47f, 5.26f, 22.43f, 5.31f)
            curveTo(22.07f, 5.69f, 21.65f, 5.94f, 21.11f, 5.91f)
            curveTo(20.31f, 5.87f, 19.62f, 6.12f, 19.02f, 6.73f)
            curveTo(18.89f, 5.98f, 18.47f, 5.53f, 17.82f, 5.24f)
            curveTo(17.48f, 5.09f, 17.13f, 4.94f, 16.9f, 4.61f)
            curveTo(16.73f, 4.38f, 16.68f, 4.12f, 16.6f, 3.86f)
            curveTo(16.55f, 3.71f, 16.5f, 3.55f, 16.32f, 3.53f)
            curveTo(16.13f, 3.5f, 16.05f, 3.66f, 15.97f, 3.79f)
            curveTo(15.67f, 4.34f, 15.56f, 4.95f, 15.57f, 5.57f)
            curveTo(15.59f, 6.95f, 16.18f, 8.06f, 17.34f, 8.84f)
            curveTo(17.47f, 8.93f, 17.51f, 9.02f, 17.47f, 9.15f)
            curveTo(17.39f, 9.42f, 17.29f, 9.69f, 17.21f, 9.96f)
            curveTo(17.16f, 10.13f, 17.08f, 10.17f, 16.89f, 10.09f)
            curveTo(16.25f, 9.83f, 15.7f, 9.43f, 15.22f, 8.95f)
            curveTo(14.39f, 8.16f, 13.64f, 7.27f, 12.71f, 6.58f)
            curveTo(12.49f, 6.42f, 12.27f, 6.27f, 12.04f, 6.13f)
            curveTo(11.09f, 5.2f, 12.17f, 4.45f, 12.42f, 4.36f)
            curveTo(12.68f, 4.26f, 12.51f, 3.94f, 11.67f, 3.94f)
            curveTo(10.83f, 3.95f, 10.06f, 4.23f, 9.07f, 4.6f)
            curveTo(8.93f, 4.66f, 8.78f, 4.7f, 8.63f, 4.73f)
            curveTo(7.73f, 4.57f, 6.81f, 4.53f, 5.84f, 4.64f)
            curveTo(4.02f, 4.84f, 2.57f, 5.7f, 1.5f, 7.17f)
            curveTo(0.22f, 8.93f, -0.08f, 10.94f, 0.29f, 13.03f)
            curveTo(0.68f, 15.23f, 1.8f, 17.06f, 3.53f, 18.48f)
            curveTo(5.32f, 19.96f, 7.39f, 20.69f, 9.74f, 20.55f)
            curveTo(11.17f, 20.47f, 12.77f, 20.27f, 14.56f, 18.75f)
            curveTo(15.02f, 18.98f, 15.49f, 19.07f, 16.28f, 19.14f)
            curveTo(16.89f, 19.19f, 17.47f, 19.11f, 17.93f, 19.01f)
            curveTo(18.63f, 18.86f, 18.59f, 18.21f, 18.33f, 18.09f)
            curveTo(16.25f, 17.12f, 16.71f, 17.51f, 16.29f, 17.19f)
            curveTo(17.35f, 15.94f, 18.96f, 13.72f, 19.46f, 10.69f)
            curveTo(19.51f, 10.36f, 19.57f, 9.89f, 19.56f, 9.62f)
            curveTo(19.56f, 9.45f, 19.6f, 9.39f, 19.79f, 9.37f)
            curveTo(20.31f, 9.31f, 20.81f, 9.17f, 21.28f, 8.91f)
            curveTo(22.62f, 8.18f, 23.17f, 6.97f, 23.3f, 5.52f)
            curveTo(23.32f, 5.3f, 23.29f, 5.07f, 23.06f, 4.95f)
            close()
            moveTo(11.32f, 18f)
            curveTo(9.3f, 16.41f, 8.32f, 15.89f, 7.92f, 15.91f)
            curveTo(7.54f, 15.93f, 7.61f, 16.37f, 7.69f, 16.65f)
            curveTo(7.78f, 16.93f, 7.89f, 17.12f, 8.05f, 17.36f)
            curveTo(8.16f, 17.52f, 8.24f, 17.76f, 7.94f, 17.94f)
            curveTo(7.29f, 18.34f, 6.16f, 17.81f, 6.11f, 17.78f)
            curveTo(4.8f, 17.01f, 3.7f, 15.99f, 2.93f, 14.59f)
            curveTo(2.18f, 13.25f, 1.74f, 11.8f, 1.67f, 10.27f)
            curveTo(1.65f, 9.89f, 1.76f, 9.76f, 2.13f, 9.7f)
            curveTo(2.62f, 9.61f, 3.12f, 9.59f, 3.61f, 9.66f)
            curveTo(5.67f, 9.96f, 7.42f, 10.88f, 8.89f, 12.33f)
            curveTo(9.72f, 13.16f, 10.36f, 14.16f, 11.01f, 15.12f)
            curveTo(11.71f, 16.15f, 12.45f, 17.13f, 13.4f, 17.94f)
            curveTo(13.74f, 18.22f, 14.01f, 18.43f, 14.26f, 18.59f)
            curveTo(13.49f, 18.68f, 12.2f, 18.69f, 11.32f, 18f)
            lineTo(11.32f, 18f)
            close()
            moveTo(12.28f, 11.78f)
            curveTo(12.28f, 11.62f, 12.41f, 11.49f, 12.58f, 11.49f)
            curveTo(12.62f, 11.49f, 12.65f, 11.49f, 12.68f, 11.5f)
            curveTo(12.72f, 11.52f, 12.76f, 11.54f, 12.79f, 11.58f)
            curveTo(12.85f, 11.63f, 12.88f, 11.7f, 12.88f, 11.78f)
            curveTo(12.88f, 11.95f, 12.74f, 12.08f, 12.58f, 12.08f)
            curveTo(12.41f, 12.08f, 12.28f, 11.95f, 12.28f, 11.78f)
            close()
            moveTo(15.28f, 13.32f)
            curveTo(15.09f, 13.4f, 14.9f, 13.47f, 14.71f, 13.47f)
            curveTo(14.43f, 13.49f, 14.11f, 13.37f, 13.94f, 13.23f)
            curveTo(13.68f, 13.01f, 13.49f, 12.89f, 13.41f, 12.5f)
            curveTo(13.38f, 12.33f, 13.4f, 12.08f, 13.43f, 11.93f)
            curveTo(13.49f, 11.62f, 13.42f, 11.41f, 13.2f, 11.23f)
            curveTo(13.02f, 11.08f, 12.78f, 11.04f, 12.53f, 11.04f)
            curveTo(12.44f, 11.04f, 12.35f, 11f, 12.29f, 10.96f)
            curveTo(12.18f, 10.91f, 12.09f, 10.78f, 12.18f, 10.62f)
            curveTo(12.2f, 10.57f, 12.33f, 10.44f, 12.36f, 10.42f)
            curveTo(12.71f, 10.22f, 13.1f, 10.28f, 13.47f, 10.43f)
            curveTo(13.81f, 10.57f, 14.06f, 10.82f, 14.43f, 11.18f)
            curveTo(14.81f, 11.62f, 14.88f, 11.74f, 15.09f, 12.07f)
            curveTo(15.26f, 12.32f, 15.42f, 12.59f, 15.52f, 12.89f)
            curveTo(15.59f, 13.07f, 15.51f, 13.23f, 15.28f, 13.32f)
            close()
        }
    }.build()
}
