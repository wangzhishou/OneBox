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

package com.wanbaohe.poem.component

import com.wanbaohe.poem.model.Poem

data class PoemSearchUiState(
    val query: String = "",
    val dynasties: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val selectedDynasty: String? = null,
    val selectedType: String? = null,
    val selectedAuthor: String? = null,
    val results: List<Poem> = emptyList(),
    val hasSearched: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
) {
    /** 朝代/体裁/诗人筛选在结果集上本地过滤(搜索接口只支持关键词) */
    val displayedResults: List<Poem>
        get() = results.filter { poem ->
            (selectedDynasty == null || poem.dynasty == selectedDynasty) &&
                (selectedType == null || poem.type == selectedType) &&
                (selectedAuthor == null || poem.author == selectedAuthor)
        }
}
