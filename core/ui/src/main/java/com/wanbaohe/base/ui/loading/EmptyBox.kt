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

package com.wanbaohe.base.ui.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme

@Composable
fun EmptyBox(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.load_empty_toast),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onTextClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier.padding(AppTheme.dimens.spaceLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onTextClick.invoke()
                }
            ) {
                Text(
                    text = text,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceLarge)
                )
            }
        }
    }
}