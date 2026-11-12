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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.wanbaohe.core.ui.widget.glass.glassMedium
import com.wanbaohe.core.ui.widget.system.OneBoxPillButton

@Composable
internal fun PickColorEmptyContent(
    bottomInset: Dp,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseScreen(
        modifier = modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.pick_color_title),
        onGoBack = onBack,
        isBackHandler = false,
        isShowDefaultActions = false,
        showNavigationBarsPadding = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .glassMedium(
                        shape = RoundedCornerShape(48.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineImage,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(84.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .glassMedium(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.pick_color_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.pick_color_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))
            OneBoxPillButton(
                text = stringResource(R.string.pick_image_alt),
                onClick = onPickImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                textStyle = MaterialTheme.typography.titleMedium,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
            Spacer(Modifier.height(12.dp))
            OneBoxPillButton(
                text = stringResource(R.string.take_photo),
                onClick = onTakePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                textStyle = MaterialTheme.typography.titleMedium,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            )
        }
    }
}
