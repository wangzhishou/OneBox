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

package com.wanbaohe.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExitToApp

@Composable
fun MainBackHandler(
    rootComponent: RootComponent,
    drawerState: DrawerState,
    currentScreen: Screen
) {
    var backPressedOnce by rememberSaveable { mutableStateOf(false) }
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
            return@BackHandler
        }
        if (rootComponent.isStackBottom(currentScreen)) {
            if (backPressedOnce) {
                activity?.finishAffinity()
            } else {
                backPressedOnce = true
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExitToApp,
                    message = activity?.getString(R.string.back_prressed_once) ?: "",
                )
                coroutineScope.launch {
                    delay(2000.milliseconds) // Reset the flag after 2 seconds
                    backPressedOnce = false
                }
            }
        } else {
            rootComponent.navigateBack()
        }
    }
}