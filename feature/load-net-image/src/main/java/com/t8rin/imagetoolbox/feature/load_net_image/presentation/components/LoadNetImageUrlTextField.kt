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

package com.t8rin.imagetoolbox.feature.load_net_image.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifiError

@Composable
internal fun LoadNetImageUrlTextField(
    component: LoadNetImageComponent
) {
    RoundedTextField(
        modifier = Modifier
            .flatGlassContainer(
                shape = MaterialTheme.shapes.large,
                resultPadding = 8.dp
            ),
        value = component.targetUrl,
        onValueChange = { str ->
            component.updateTargetUrl(
                newUrl = str,
                onFailure = {
                    AppToastHost.showToast(
                        message = it,
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifiError
                    )
                }
            )
        },
        singleLine = false,
        label = {
            Text(stringResource(id = R.string.image_link))
        },
        endIcon = {
            AnimatedVisibility(component.targetUrl.isNotBlank()) {
                EnhancedIconButton(
                    onClick = {
                        component.updateTargetUrl("")
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }
        }
    )
}