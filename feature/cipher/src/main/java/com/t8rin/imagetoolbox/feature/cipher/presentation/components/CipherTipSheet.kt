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

package com.t8rin.imagetoolbox.feature.cipher.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Interface
import com.t8rin.imagetoolbox.core.resources.icons.Puzzle
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFunctions

@Composable
fun CipherTipSheet(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    EnhancedModalBottomSheet(
        sheetContent = {
            Box {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .flatGlassContainer(
                                shape = ShapeDefaults.top
                            )
                            .fillMaxWidth()
                    ) {
                        TitleItem(
                            text = stringResource(R.string.features),
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFunctions
                        )
                        Text(
                            text = stringResource(R.string.features_sub),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .flatGlassContainer(
                                shape = ShapeDefaults.center
                            )
                            .fillMaxWidth()
                    ) {
                        TitleItem(
                            text = stringResource(R.string.implementation),
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Filled.Interface
                        )
                        Text(
                            text = stringResource(id = R.string.implementation_sub),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .flatGlassContainer(
                                shape = ShapeDefaults.center
                            )
                            .fillMaxWidth()
                    ) {
                        TitleItem(
                            text = stringResource(R.string.file_size),
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFile
                        )
                        Text(
                            text = stringResource(id = R.string.file_size_sub),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .flatGlassContainer(
                                shape = ShapeDefaults.bottom
                            )
                            .fillMaxWidth()
                    ) {
                        TitleItem(
                            text = stringResource(R.string.compatibility),
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Puzzle
                        )
                        Text(
                            text = stringResource(id = R.string.compatibility_sub),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        },
        title = {
            TitleItem(
                text = stringResource(R.string.cipher),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSecurity
            )
        },
        confirmButton = {
            GlassTonalButton(
                onClick = onDismiss
                , color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                AutoSizeText(stringResource(R.string.close))
            }
        }
    )
}