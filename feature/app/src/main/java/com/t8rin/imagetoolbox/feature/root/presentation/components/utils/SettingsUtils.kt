/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.root.presentation.components.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiState
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import kotlinx.coroutines.delay

@Composable
internal fun RootComponent.uiSettingsState(): UiSettingsState = settingsState.toUiState(
    randomEmojiKey = childStack.randomEmojiKey()
)

@Composable
private fun Value<ChildStack<Screen, *>>.randomEmojiKey(): Any {
    val randomEmojiKey = remember {
        mutableIntStateOf(0)
    }

    val currentDestination = subscribeAsState().value.items
    LaunchedEffect(currentDestination) {
        delay(200L) // Delay for transition
        randomEmojiKey.intValue++
    }

    return randomEmojiKey.intValue
}