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

package com.wanbaohe.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.modifier.scaleOnTap

@Composable
fun TextLogo() {
    val layoutDirection = LocalLayoutDirection.current
    val localUrlNavigator = LocalUrlNavigator.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column {
            val titleText = remember {
                BuildConfig.VERSION_NAME
            }
            Row(
                modifier = if (BuildConfig.DEBUG) {
                    Modifier.clickable {
                        localUrlNavigator.navigate(
                            Screen.Demo()
                        )
                    }
                } else {
                    Modifier
                }, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(
                        start = WindowInsets
                            .displayCutout
                            .asPaddingValues()
                            .calculateStartPadding(layoutDirection)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    content = {
                        Text(
                            text = if (BuildConfig.DEBUG) "$titleText-" + BuildConfig.FLAVOR.uppercase(
                                LocalLocale.current.platformLocale
                            ) else titleText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.68f),
                            fontSize = 9.sp
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.68f),
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .padding(bottom = 12.dp)
                        .scaleOnTap(
                            onRelease = {
                                if (BuildConfig.DEBUG) AppToastHost.showConfetti()
                            }
                        )
                )
            }
            Text(
                text = stringResource(R.string.app_slogan),
                modifier = Modifier.padding(
                    start = WindowInsets
                        .displayCutout
                        .asPaddingValues()
                        .calculateStartPadding(layoutDirection)
                ),

                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}