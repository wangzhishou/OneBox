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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.devicestatus.R
import com.wanbaohe.devicestatus.component.DeviceStatusComponent
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR

@Composable
fun DeviceStatusScreen(component: DeviceStatusComponent) {
    val context = LocalContext.current
    val status by component.status.collectAsState()
    val formatted = status?.let { current ->
        remember(current) { PhoneStatusFormatter.format(context, current) }
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (formatted.addresses.isEmpty()) {
            item { EmptyAddressCard() }
        } else {
            itemsIndexed(
                formatted.addresses,
                key = { index, row -> "addr:$index:${row.label}:${row.value}" },
            ) { _, row ->
                AddressCard(row)
            }
        }
        itemsIndexed(
            formatted.sections,
            key = { index, section -> "section:$index:${section.title}" },
        ) { _, section ->
            SectionCard(section)
        }
        item { Footnote(formatted.footnote) }
    }
}

@Composable
private fun StatusGrid(
    formatted: FormattedPhoneStatus,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (formatted.addresses.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyAddressCard() }
        } else {
            formatted.addresses.forEachIndexed { index, row ->
                item(
                    key = "addr:$index:${row.label}:${row.value}",
                    span = { GridItemSpan(if (spansFullWidth(row.value)) maxLineSpan else 1) },
                ) {
                    AddressCard(row, fillHeight = true)
                }
            }
        }
        formatted.sections.forEachIndexed { sectionIndex, section ->
            item(
                key = "title:$sectionIndex:${section.title}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = section.title,
                    modifier = Modifier.padding(top = if (sectionIndex == 0) 4.dp else 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            section.rows.forEachIndexed { rowIndex, row ->
                item(
                    key = "row:$sectionIndex:$rowIndex:${row.label}",
                    span = { GridItemSpan(if (spansFullWidth(row.value)) maxLineSpan else 1) },
                ) {
                    GridFactCard(row)
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { Footnote(formatted.footnote) }
    }
}

private fun spansFullWidth(value: String): Boolean = value.length > 22 || value.contains('\n')

@Composable
private fun AddressCard(row: FormattedRow, fillHeight: Boolean = false) {
    GlassCard(
        onClick = { Clipboard.copy(row.copyText) },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier),
        style = GlassStyle.Thick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = row.value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun GridFactCard(row: FormattedRow) {
    GlassCard(
        onClick = { Clipboard.copy(row.copyText) },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .defaultMinSize(minHeight = 76.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = row.value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SectionCard(section: FormattedSection) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            section.rows.forEach { row ->
                StatusRow(row)
            }
        }
    }
}

@Composable
private fun StatusRow(row: FormattedRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Clipboard.copy(row.copyText) }
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = row.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyAddressCard() {
    GlassCard(modifier = Modifier.fillMaxWidth(), style = GlassStyle.Thick) {
        Text(
            text = stringResource(R.string.status_no_ip),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
