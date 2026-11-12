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

package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shifenmiao.common.logic.AppComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.navigation.ScreenCallbackResult

@Composable
fun OpenFilePickerActionScreen(
    screen: Screen.OpenFilePicker,
    appComponent: AppComponent
) {
    var completed by remember { mutableStateOf(false) }

    fun finish(result: ScreenCallbackResult) {
        if (completed) return
        completed = true
        screen.onResult?.invoke(result)
        appComponent.onGoBack()
    }

    val singleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        finish(
            if (uri != null) {
                ScreenCallbackResult.opened(
                    url = uri.toString(),
                    extra = mapOf(
                        "uris" to listOf(uri.toString()),
                        "multiple" to false,
                        "mimeTypes" to screen.mimeTypes
                    )
                )
            } else {
                ScreenCallbackResult.cancelled()
            }
        )
    }

    val multipleFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        finish(
            if (uris.isNotEmpty()) {
                val uriStrings = uris.map { it.toString() }
                ScreenCallbackResult.opened(
                    url = uriStrings.firstOrNull(),
                    data = uriStrings.joinToString(separator = "\n"),
                    extra = mapOf(
                        "uris" to uriStrings,
                        "multiple" to true,
                        "mimeTypes" to screen.mimeTypes
                    )
                )
            } else {
                ScreenCallbackResult.cancelled()
            }
        )
    }

    LaunchedEffect(screen.mimeTypes, screen.allowMultiple) {
        if (completed) return@LaunchedEffect
        val mimeTypes = screen.mimeTypes.ifEmpty { listOf("*/*") }.toTypedArray()
        if (screen.allowMultiple) {
            multipleFileLauncher.launch(mimeTypes)
        } else {
            singleFileLauncher.launch(mimeTypes)
        }
    }

    BackHandler(enabled = !completed) {
        finish(ScreenCallbackResult.cancelled())
    }

    Box(modifier = Modifier.fillMaxSize())
}

