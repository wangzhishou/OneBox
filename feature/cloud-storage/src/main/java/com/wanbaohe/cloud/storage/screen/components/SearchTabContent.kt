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
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
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
