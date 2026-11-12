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

package com.wanbaohe.file.browser.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.file.browser.model.FileBrowserState
import com.wanbaohe.file.browser.model.FileBrowserViewMode

/**
 * 最近访问列表 — 支持列表/Grid 视图切换，对齐文件浏览器的视图体验。
 *
 * @param items 最近访问项列表
 * @param onItemClick 点击回调
 * @param viewMode 视图模式（列表/Grid）
 * @param contentPadding 内容边距
 * @param modifier Modifier
 */
@Composable
fun RecentAccessList(
    items: List<FileBrowserState.RecentItem>,
    onItemClick: (Uri) -> Unit,
    viewMode: FileBrowserViewMode = FileBrowserViewMode.LIST,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    when (viewMode) {
        FileBrowserViewMode.LIST -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = items,
                    key = { it.uri.toString() }
                ) { item ->
                    RecentAccessListItem(
                        item = item,
                        onClick = { onItemClick(item.uri) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        FileBrowserViewMode.GRID -> {
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = items,
                    key = { it.uri.toString() }
                ) { item ->
                    RecentAccessGridItem(
                        item = item,
                        onClick = { onItemClick(item.uri) }
                    )
                }
            }
        }
    }
}
