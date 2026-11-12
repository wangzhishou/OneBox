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

package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.SecondarySmallButton
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import kotlinx.coroutines.delay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwitchAccess

@Composable
fun AIContentRecommend(
    isShow: Boolean,
    appComponent: AppComponent,
) {
    var showContent by remember { mutableStateOf(isShow) }

    LaunchedEffect(isShow) {
        if (isShow) {
            delay(3000)
            showContent = true
        }
    }

    AnimatedContent(
        targetState = showContent,
        transitionSpec = {
            fadeIn(animationSpec = tween(600))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        },
        label = "",
    ) { targetShow ->
        if (targetShow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = AppTheme.dimens.paddingSmall
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.ai_content_recommend),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(
                        top = AppTheme.dimens.spaceNormal,
                        bottom = AppTheme.dimens.spaceNormal
                    )
                )
                Spacer(modifier = Modifier.size(AppTheme.dimens.spaceSmall))
                SecondarySmallButton(
                    onClick = {
                        appComponent.showAIModelsModalSheet()
                    },
                    text = stringResource(id = R.string.switch_ai_models),
                    icon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwitchAccess,
                            contentDescription = "AI Models",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                )
            }
        }
    }
}