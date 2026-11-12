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

package com.wanbaohe.base.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.loading.DotDanceLoading
import com.wanbaohe.core.ui.skeleton.Skeleton
import com.shifenmiao.theme.AppTheme

@Composable
fun AILoadingRow() {
    Row(
        modifier = Modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingNormal
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
    ) {
        DotDanceLoading()
        Text(
            text = stringResource(id = R.string.ai_loading),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CenterLoadingBox() {
    val isActive by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Row {
            Skeleton.Square(isActive)
            Spacer(modifier = Modifier.padding(4.dp))
            Column {
                Spacer(modifier = Modifier.padding(8.dp))
                Skeleton.RectangleLineLong(isActive)
                Spacer(modifier = Modifier.padding(4.dp))
                Skeleton.RectangleLineShort(isActive)
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        Row {
            Skeleton.Square(isActive)
            Spacer(modifier = Modifier.padding(4.dp))
            Column {
                Spacer(modifier = Modifier.padding(8.dp))
                Skeleton.RectangleLineLong(isActive)
                Spacer(modifier = Modifier.padding(4.dp))
                Skeleton.RectangleLineShort(isActive)
            }
        }
    }
}
