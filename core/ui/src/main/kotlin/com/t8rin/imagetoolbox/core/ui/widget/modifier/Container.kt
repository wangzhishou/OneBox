/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.modifier

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalContainerShape
import com.t8rin.imagetoolbox.core.ui.utils.provider.SafeLocalContainerColor
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground

fun Modifier.container(
    shape: Shape? = null,
    color: Color = Color.Unspecified,
    resultPadding: Dp = 4.dp,
    borderWidth: Dp = Dp.Unspecified,
    clip: Boolean = true,
) = this.composed {
    val localContainerShape = LocalContainerShape.current
    val resultShape = localContainerShape ?: shape ?: ShapeDefaults.default
    val settingsState = LocalSettingsState.current

    val targetBorderWidth = borderWidth.takeOrElse {
        settingsState.borderWidth
    }

    val containerColor = if (color.isUnspecified) {
        SafeLocalContainerColor
    } else {
        color
    }

    Modifier
        .glassBackground(
            style = GlassStyle.Dense,
            shape = resultShape,
            color = containerColor,
            borderWidth = targetBorderWidth
        )
        .then(if (clip) Modifier.clip(resultShape) else Modifier)
        .then(if (resultPadding > 0.dp) Modifier.padding(resultPadding) else Modifier)
}
