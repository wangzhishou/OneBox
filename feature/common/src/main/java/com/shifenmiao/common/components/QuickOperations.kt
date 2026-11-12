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

package com.shifenmiao.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.robot.RobotController
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawer


@Composable
fun QuickOperations(
    appComponent: AppComponent
) {
    DisposableEffect(Unit) {
        onDispose { RobotController.clearSearchAnchor() }
    }

    IconButton(
        onClick = {
            appComponent.onNavigate(Screen.Search())
        },
        modifier = Modifier.onGloballyPositioned { coordinates ->
            if (!coordinates.isAttached || coordinates.size.width == 0) return@onGloballyPositioned
            val left = coordinates.positionInRoot().x
            RobotController.updateSearchAnchor(
                leftPx = left,
                rightPx = left + coordinates.size.width,
            )
        },
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
            contentDescription = stringResource(R.string.search),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    IconButton(onClick = {
        if (!RateLimiter.isFastClick()) {
            appComponent.toggleDrawer()
        }
    }) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrawer,
            contentDescription = stringResource(R.string.open_menu),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

