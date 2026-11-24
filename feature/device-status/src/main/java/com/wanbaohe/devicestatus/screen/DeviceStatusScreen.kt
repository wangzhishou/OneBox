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

package com.wanbaohe.devicestatus.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.base.ui.card.TonalCardPalette
import com.wanbaohe.base.ui.card.TonalCardPaletteDefaults
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.devicestatus.R
import com.wanbaohe.devicestatus.component.DeviceStatusComponent
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR

@Composable
fun DeviceStatusScreen(component: DeviceStatusComponent) {
    val context = LocalContext.current
    val status by component.status.collectAsState()
    val networkNode by component.networkNode.collectAsState()
    val formatted = status?.let { current ->
        remember(current, networkNode) {
            PhoneStatusFormatter.format(context, current, networkNode)
        }
    }
    val isGrid = LocalSettingsState.current.groupOptionsByTypes
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()

    BaseScreen(
        title = stringResource(CoreR.string.device_status),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { scope.launch { settingsManager.toggleGroupOptionsByTypes() } },
            ) {
                Icon(
                    imageVector = if (isGrid) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                    },
                    contentDescription = stringResource(
                        if (isGrid) CoreR.string.profile_item_dislay_list
                        else CoreR.string.profile_item_dislay_gird
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = component::refresh) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSync,
                    contentDescription = stringResource(R.string.status_refresh),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        if (formatted == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.status_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (isGrid) {
            StatusGrid(
                formatted = formatted,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            StatusList(
                formatted = formatted,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatusList(
    formatted: FormattedPhoneStatus,
    modifier: Modifier = Modifier,
) {
    val sectionThemeOffset = formatted.addresses.size.coerceAtLeast(1)
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = AppTheme.dimens.pageHorizontal,
            end = AppTheme.dimens.pageHorizontal,
            top = 4.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "address_title") {
            SectionTitle(
                text = stringResource(R.string.section_address),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (formatted.addresses.isEmpty()) {
            item { EmptyAddressCard(themeIndex = 0) }
        } else {
            itemsIndexed(
                formatted.addresses,
                key = { index, row -> "addr:$index:${row.label}:${row.value}" },
            ) { index, row ->
                AddressCard(row = row, themeIndex = index)
            }
        }
        itemsIndexed(
            formatted.sections,
            key = { index, section -> "section:$index:${section.title}" },
        ) { index, section ->
            SectionCard(
                section = section,
                themeIndex = sectionThemeOffset + index,
            )
        }
        item { Footnote(formatted.footnote) }
    }
}

@Composable
private fun StatusGrid(
    formatted: FormattedPhoneStatus,
    modifier: Modifier = Modifier,
) {
    val sectionStartIndices = remember(formatted.sections) {
        var next = 0
        formatted.sections.map { section ->
            val start = next
            next += section.rows.size
            start
        }
    }
    val sectionThemeOffset = formatted.addresses.size.coerceAtLeast(1)
    LazyVerticalGrid(
        // 列数按内容区真实宽度决定,而不是按设备类型写死两列(320dp 会回落到一列)。
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = AppTheme.dimens.pageHorizontal,
            end = AppTheme.dimens.pageHorizontal,
            top = 4.dp,
            bottom = 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(
            key = "address_title",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            SectionTitle(
                text = stringResource(R.string.section_address),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (formatted.addresses.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyAddressCard(themeIndex = 0) }
        } else {
            formatted.addresses.forEachIndexed { index, row ->
                item(
                    key = "addr:$index:${row.label}:${row.value}",
                    // 地址卡是整页的主信息,固定整行,避免短 IPv4 只占半格留下空洞。
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    AddressCard(row = row, themeIndex = index)
                }
            }
        }
        formatted.sections.forEachIndexed { sectionIndex, section ->
            item(
                key = "title:$sectionIndex:${section.title}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                SectionTitle(
                    text = section.title,
                    modifier = Modifier.padding(top = if (sectionIndex == 0) 4.dp else 8.dp),
                )
            }
            section.rows.forEachIndexed { rowIndex, row ->
                item(
                    key = "row:$sectionIndex:$rowIndex:${row.label}",
                    span = { GridItemSpan(if (spansFullWidth(row.value)) maxLineSpan else 1) },
                ) {
                    GridFactCard(
                        row = row,
                        themeIndex = sectionThemeOffset +
                            sectionStartIndices[sectionIndex] +
                            rowIndex,
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { Footnote(formatted.footnote) }
    }
}

private fun spansFullWidth(value: String): Boolean = value.length > 22 || value.contains('\n')

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
private fun AddressCard(
    row: FormattedRow,
    themeIndex: Int,
) {
    val palette = TonalCardPaletteDefaults.forIndex(themeIndex)
    GlassCard(
        onClick = { Clipboard.copy(row.copyText) },
        modifier = Modifier.fillMaxWidth(),
        shape = TonalCardPaletteDefaults.shape,
        style = GlassStyle.Medium,
        colors = tintedGlassCardColors(palette.containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodySmall,
                color = palette.descriptionColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = row.value,
                style = MaterialTheme.typography.titleLarge,
                color = palette.titleColor,
            )
        }
    }
}

@Composable
private fun GridFactCard(row: FormattedRow, themeIndex: Int) {
    val palette = TonalCardPaletteDefaults.forIndex(themeIndex)
    GlassCard(
        onClick = { Clipboard.copy(row.copyText) },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .defaultMinSize(minHeight = 76.dp),
        shape = TonalCardPaletteDefaults.shape,
        colors = tintedGlassCardColors(palette.containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodySmall,
                color = palette.descriptionColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = row.value,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.titleColor,
            )
        }
    }
}

@Composable
private fun SectionCard(section: FormattedSection, themeIndex: Int) {
    val palette = TonalCardPaletteDefaults.forIndex(themeIndex)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = TonalCardPaletteDefaults.shape,
        colors = tintedGlassCardColors(palette.containerColor),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            section.rows.forEach { row ->
                StatusRow(row = row, palette = palette)
            }
        }
    }
}

@Composable
private fun StatusRow(row: FormattedRow, palette: TonalCardPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Clipboard.copy(row.copyText) }
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.descriptionColor,
        )
        Text(
            text = row.value,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.titleColor,
        )
    }
}

@Composable
private fun EmptyAddressCard(themeIndex: Int) {
    val palette = TonalCardPaletteDefaults.forIndex(themeIndex)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = TonalCardPaletteDefaults.shape,
        colors = tintedGlassCardColors(palette.containerColor),
    ) {
        Text(
            text = stringResource(R.string.status_no_ip),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.titleColor,
        )
    }
}

@Composable
private fun Footnote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
