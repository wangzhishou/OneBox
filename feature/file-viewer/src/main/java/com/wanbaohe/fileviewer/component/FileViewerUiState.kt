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

package com.wanbaohe.fileviewer.component

import com.wanbaohe.fileviewer.domain.ViewerFormat

/**
 * 预览页的稳定状态：文件信息 + 阅读位置 + 查找框。
 */
data class FileViewerUiState(
    val displayName: String = "",
    val extension: String = "",
    val format: ViewerFormat? = null,
    val sizeBytes: Long = -1L,
    val isLoading: Boolean = true,
    val error: FileViewerError? = null,
    val page: Int = 0,
    val pageCount: Int = 0,
    /** 全局日夜模式的当前生效值；页面与月亮按钮都只看它 */
    val isNight: Boolean = false,
    val search: SearchUiState = SearchUiState(),
) {
    val canRender: Boolean get() = !isLoading && error == null && format != null

    val canSearch: Boolean get() = format?.supportsFind == true

    val canShowPages: Boolean get() = format?.supportsPages == true && pageCount > 0

    val canToggleNight: Boolean get() = format?.supportsNight == true
}

/**
 * 查找框的状态。
 *
 * [at] / [total] 由页面回报；[done] 为 false 表示文档索引还在建，
 * 这时计数显示成省略号而不是"0/0"。
 */
data class SearchUiState(
    val isOpen: Boolean = false,
    val query: String = "",
    val at: Int = 0,
    val total: Int = 0,
    val done: Boolean = true,
)

sealed interface FileViewerError {
    /** 拿不到 URI、文件不存在、或者 provider 拒绝授权 */
    data object CannotOpen : FileViewerError

    /** 整个文件要被读进 JS 内存的格式，超过上限就不硬开 */
    data class TooLarge(val sizeBytes: Long) : FileViewerError
}
