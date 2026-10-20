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

package com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

@Composable
fun TileModeSelector(
    value: TileMode,
    onValueChange: (TileMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = remember {
        listOf(
            TileMode.Clamp,
            TileMode.Repeated,
            TileMode.Mirror,
            TileMode.Decal
        )
    }
    // 分段行自带玻璃底，这里不再套第二层容器卡 —— 和「日夜间模式」的用法一致：
    // 只有「标题 + 裸分段行」，父级不画背景。
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        Text(
            text = stringResource(id = R.string.tile_mode),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = OneBoxDesignSystem.compactSpacing)
        )
        GlassSegmentedButtonRow(
            options = entries,
            selectedOption = value,
            onOptionSelected = onValueChange,
            label = { mode ->
                Text(
                    text = mode.translatedName,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            },
            buttonHeight = 40.dp,
        )
    }
}

private val TileMode.translatedName: String
    @Composable
    get() = when (this) {
        TileMode.Repeated -> stringResource(id = R.string.tile_mode_repeated)
        TileMode.Mirror -> stringResource(id = R.string.tile_mode_mirror)
        TileMode.Decal -> stringResource(id = R.string.tile_mode_decal)
        else -> stringResource(id = R.string.tile_mode_clamp)
    }
