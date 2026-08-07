package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudObjectItem

@Composable
fun SearchTabContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    loading: Boolean,
    results: List<CloudObjectItem>,
    onResultClick: (CloudObjectItem) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        OneBoxSectionCard {
            OneBoxOutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.cloud_storage_search_label)) },
                supportingText = { Text(stringResource(R.string.cloud_storage_search_hint)) },
                singleLine = true,
            )
            OnePrimaryButton(
                text = stringResource(R.string.cloud_storage_search_action),
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                enabled = query.isNotBlank() && !loading,
            )
        }
        if (loading) {
            CircularProgressIndicator()
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            items(results, key = { it.key }) { item ->
                OneBoxSectionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResultClick(item) }
                ) {
                    Text(text = item.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(text = item.key, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
