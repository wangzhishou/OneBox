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

package com.t8rin.imagetoolbox.feature.compare.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapHoriz
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotateLeft
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotateRight

@Composable
fun CompareScreenTopAppBar(
    imageNotPicked: Boolean,
    onNavigationIconClick: () -> Unit,
    onShareButtonClick: () -> Unit,
    onSwapImagesClick: () -> Unit,
    onRotateImagesClick: () -> Unit,
    isShareButtonVisible: Boolean,
    isImagesRotated: Boolean,
    titleWhenBitmapsPicked: String,
    onToggleLabelsEnabled: (Boolean) -> Unit,
    isLabelsEnabled: Boolean,
    isLabelsButtonVisible: Boolean
) {
    if (imageNotPicked) {
        EnhancedTopAppBar(
            type = EnhancedTopAppBarType.Center,
            navigationIcon = {
                EnhancedIconButton(
                    onClick = onNavigationIconClick
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.exit)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.compare),
                    modifier = Modifier.marquee()
                )
            },
            actions = {
                TopAppBarEmoji()
            },
            isGlassActive = true
        )
    } else {
        EnhancedTopAppBar(
            type = EnhancedTopAppBarType.Center,
            navigationIcon = {
                EnhancedIconButton(
                    onClick = onNavigationIconClick
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.exit)
                    )
                }
            },
            actions = {
                AnimatedVisibility(visible = isShareButtonVisible) {
                    ShareButton(onShare = onShareButtonClick)
                }
                EnhancedIconButton(
                    onClick = onSwapImagesClick
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapHoriz,
                        contentDescription = "Swap"
                    )
                }
                EnhancedIconButton(
                    onClick = onRotateImagesClick
                ) {
                    AnimatedContent(isImagesRotated) { rotated ->
                        Icon(
                            imageVector = if (rotated) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRotateLeft
                            else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRotateRight,
                            contentDescription = "Rotate"
                        )
                    }
                }
                AnimatedVisibility(visible = isLabelsButtonVisible) {
                    EnhancedIconButton(
                        onClick = {
                            onToggleLabelsEnabled(!isLabelsEnabled)
                        },
                        containerColor = animateColorAsState(
                            if (isLabelsEnabled) MaterialTheme.colorScheme.secondary
                            else Color.Transparent
                        ).value
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel,
                            contentDescription = "Label"
                        )
                    }
                }
            },
            title = {
                AnimatedContent(
                    targetState = titleWhenBitmapsPicked,
                    modifier = Modifier.marquee()
                ) { text ->
                    Text(text)
                }
            },
            isGlassActive = true
        )
    }
}