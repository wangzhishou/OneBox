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

package com.wanbaohe.file.browser.prefs

import android.content.Context
import android.net.Uri
import com.wanbaohe.file.browser.model.FileBrowserViewMode
import com.wanbaohe.file.browser.screenLogic.FileBrowserComponent

/**
 * Tiny persistence for FileBrowser UI state.
 *
 * Goals:
 * - Zero new dependencies
 * - Safe defaults and easy to remove/replace later
 */
internal class FileBrowserPrefs(
    context: Context
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLastDestination(destination: FileBrowserComponent.NavDestination) {
        prefs.edit().putString(KEY_LAST_DESTINATION, destination.name).apply()
    }

    fun loadLastDestination(): FileBrowserComponent.NavDestination? {
        val raw = prefs.getString(KEY_LAST_DESTINATION, null) ?: return null
        return runCatching { FileBrowserComponent.NavDestination.valueOf(raw) }.getOrNull()
    }

    fun saveLastWorkspaceUri(uri: Uri?) {
        prefs.edit().putString(KEY_LAST_WORKSPACE_URI, uri?.toString()).apply()
    }

    fun loadLastWorkspaceUri(): Uri? {
        val raw = prefs.getString(KEY_LAST_WORKSPACE_URI, null) ?: return null
        return runCatching { Uri.parse(raw) }.getOrNull()
    }

    fun saveViewMode(mode: FileBrowserViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
    }

    fun loadViewMode(default: FileBrowserViewMode = FileBrowserViewMode.LIST): FileBrowserViewMode {
        val raw = prefs.getString(KEY_VIEW_MODE, null) ?: return default
        return runCatching { FileBrowserViewMode.valueOf(raw) }.getOrElse { default }
    }

    private companion object {
        private const val PREFS_NAME = "file_browser_prefs"
        private const val KEY_LAST_DESTINATION = "last_destination"
        private const val KEY_LAST_WORKSPACE_URI = "last_workspace_uri"
        private const val KEY_VIEW_MODE = "view_mode"
    }
}
