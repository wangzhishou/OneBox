/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.iching.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.TaijiBagua

@Preview(showBackground = true, apiLevel = 36)
@Composable
private fun IChingIconsPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            "Qian" to CoinFrontQian,
            "Kun" to CoinFrontKun,
            "Li" to CoinFrontLi,
            "Back" to CoinBackRipple,
            "Taiji" to com.t8rin.imagetoolbox.core.resources.Icons.Outlined.TaijiBagua,
        ).forEach { (label, icon) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(96.dp),
                )
                Text(label)
            }
        }
    }
}
