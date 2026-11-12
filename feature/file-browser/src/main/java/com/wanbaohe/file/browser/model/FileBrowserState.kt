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

package com.wanbaohe.file.browser.model

/**
 * Represents the state of the file browser
 */
sealed interface FileBrowserState {
    /**
     * Initial state before any loading
     */
    data object Idle : FileBrowserState

    /**
     * Loading files from directory
     */
    data object Loading : FileBrowserState

    /**
     * Successfully loaded files
     */
    data class Success(
        val files: List<FileItem>,
        val currentPath: String
    ) : FileBrowserState

    /**
     * Error occurred while loading
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : FileBrowserState

    /**
     * No permission to access directory
     */
    data object NoPermission : FileBrowserState

    /**
     * Directory is empty
     */
    data class Empty(
        val currentPath: String
    ) : FileBrowserState

    /**
     * Recents list screen.
     * In Step 4 we store and show only simple entries.
     */
    data class Recents(
        val items: List<RecentItem>
    ) : FileBrowserState

    /**
     * No recents yet.
     */
    data object RecentsEmpty : FileBrowserState

    data class RecentItem(
        val uri: android.net.Uri,
        val title: String,
        val subtitle: String,
        val accessType: String = "file", // "file" or "folder"
    )
}
