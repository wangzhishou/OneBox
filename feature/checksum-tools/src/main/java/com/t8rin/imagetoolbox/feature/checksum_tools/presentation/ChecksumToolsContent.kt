/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.checksum_tools.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.HashingType
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.FolderCompare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalculate
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompareArrows
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.controls.selection.DataSelector
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.modifier.scaleOnTap
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBarStyle
import com.wanbaohe.core.ui.widget.text.marquee
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.pages.CalculateFromTextPage
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.pages.CalculateFromUriPage
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.pages.CompareWithUriPage
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.components.pages.CompareWithUrisPage
import com.t8rin.imagetoolbox.feature.checksum_tools.presentation.screenLogic.ChecksumToolsComponent

private enum class ChecksumTab { CALCULATE, TEXT, COMPARE, BATCH }

@Composable
fun ChecksumToolsContent(
    component: ChecksumToolsComponent
) {
    val showConfetti: () -> Unit = AppToastHost::showConfetti

    var selectedTab by rememberSaveable {
        mutableStateOf(ChecksumTab.CALCULATE)
    }

    BaseScreen(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.marquee()
            ) {
                Text(
                    text = stringResource(R.string.checksum_tools)
                )
                GlassSurface(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .padding(bottom = 12.dp)
                        .scaleOnTap {
                            showConfetti()
                        },
                    style = GlassStyle.Thin,
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    borderWidth = 0.dp,
                ) {
                    Text(
                        text = HashingType.entries.size.toString(),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        onGoBack = component.onGoBack
    ) {
        DataSelector(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.spaceLarge)
                .padding(top = AppTheme.dimens.spaceSmall),
            value = component.hashingType,
            containerColor = Color.Unspecified,
            selectedItemColor = MaterialTheme.colorScheme.secondaryContainer,
            onValueChange = component::updateChecksumType,
            entries = HashingType.entries,
            title = stringResource(R.string.algorithms),
            titleIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel,
            itemContentText = {
                it.name
            }
        )
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "checksum_tab"
        ) { tab ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.dimens.spaceLarge),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (tab) {
                    ChecksumTab.CALCULATE -> {
                        CalculateFromUriPage(
                            component = component
                        )
                    }

                    ChecksumTab.TEXT -> {
                        CalculateFromTextPage(
                            component = component
                        )
                    }

                    ChecksumTab.COMPARE -> {
                        CompareWithUriPage(
                            component = component
                        )
                    }

                    ChecksumTab.BATCH -> {
                        CompareWithUrisPage(
                            component = component
                        )
                    }
                }
            }
        }
        BottomNavigationBar(
            items = listOf(
                BottomNavItem(
                    id = ChecksumTab.CALCULATE.name,
                    label = stringResource(R.string.calculate),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate
                ),
                BottomNavItem(
                    id = ChecksumTab.TEXT.name,
                    label = stringResource(R.string.text_hash),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText
                ),
                BottomNavItem(
                    id = ChecksumTab.COMPARE.name,
                    label = stringResource(R.string.compare),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompareArrows
                ),
                BottomNavItem(
                    id = ChecksumTab.BATCH.name,
                    label = stringResource(R.string.batch_compare),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.FolderCompare
                )
            ),
            selectedItemId = selectedTab.name,
            onItemClick = { item ->
                selectedTab = ChecksumTab.valueOf(item.id)
            },
            modifier = Modifier.fillMaxWidth(),
            tabTextStyle = MaterialTheme.typography.labelMedium,
            style = BottomNavigationBarStyle(
                tabHorizontalPadding = 8.dp
            )
        )
    }
}
