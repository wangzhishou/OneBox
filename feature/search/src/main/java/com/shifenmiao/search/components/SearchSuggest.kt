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

package com.shifenmiao.search.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.base.ui.CancelableChip
import com.shifenmiao.core.R
import com.shifenmiao.model.search.SuggestionModel
import com.shifenmiao.storage.SearchHistoryStore
import com.shifenmiao.theme.AppTheme

@Composable
fun SearchSuggest(
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
    onSuggestionClick: ((SuggestionModel) -> Unit)? = null,
    onSuggestionDelete: ((SuggestionModel) -> Unit)? = null
) {
    val searchHistoryData = remember { mutableStateOf(SearchHistoryStore.getHistoryList().reversed()) }
    if (searchHistoryData.value.isNotEmpty()) {
        Text(
            text = stringResource(R.string.search_history),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingSmall)
        )
        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
        Column(
            modifier = modifier
                .padding(vertical = AppTheme.dimens.spaceNormal)
                .fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = searchHistoryData,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxWidth(),
                label = "SearchSuggestAnimation"
            ) { suggestions ->
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal)
                ) {
                    suggestions.value.forEach { suggestionModel ->
                        CancelableChip(
                            suggestion = suggestionModel,
                            onClick = {
                                if (onSuggestionClick != null) {
                                    onSuggestionClick(it)
                                }
                            },
                            onCancel = {
                                SearchHistoryStore.removeHistory(it)
                                if (onSuggestionDelete != null) {
                                    onSuggestionDelete(it)
                                }
                                searchHistoryData.value = SearchHistoryStore.getHistoryList()
                            }
                        )
                    }
                }
            }
        }
    }
}