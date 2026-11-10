package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudSort
import com.wanbaohe.cloud.storage.model.CloudSortMode
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
fun SortSheet(
    sort: CloudSort,
    onSelect: (CloudSort) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        dragHandle = { },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.screenPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            Text(
                text = stringResource(R.string.cloud_storage_sort_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = OneBoxDesignSystem.compactSpacing),
            )
            SortRow(
                label = stringResource(R.string.cloud_storage_sort_name),
                selected = sort.mode == CloudSortMode.NAME,
                onClick = { onSelect(sort.copy(mode = CloudSortMode.NAME)) },
            )
            SortRow(
                label = stringResource(R.string.cloud_storage_sort_date),
                selected = sort.mode == CloudSortMode.DATE,
                onClick = { onSelect(sort.copy(mode = CloudSortMode.DATE)) },
            )
            SortRow(
                label = stringResource(R.string.cloud_storage_sort_size),
                selected = sort.mode == CloudSortMode.SIZE,
                onClick = { onSelect(sort.copy(mode = CloudSortMode.SIZE)) },
            )
            SortRow(
                label = stringResource(R.string.cloud_storage_sort_ascending),
                selected = sort.ascending,
                onClick = { onSelect(sort.copy(ascending = true)) },
            )
            SortRow(
                label = stringResource(R.string.cloud_storage_sort_descending),
                selected = !sort.ascending,
                onClick = { onSelect(sort.copy(ascending = false)) },
            )
            OnePrimaryButton(
                text = stringResource(R.string.cloud_storage_done),
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OneBoxDesignSystem.compactSpacing),
            )
        }
    }
}

@Composable
private fun SortRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    GlassSurface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        style = GlassStyle.Regular,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
