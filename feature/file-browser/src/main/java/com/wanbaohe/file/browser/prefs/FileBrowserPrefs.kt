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
