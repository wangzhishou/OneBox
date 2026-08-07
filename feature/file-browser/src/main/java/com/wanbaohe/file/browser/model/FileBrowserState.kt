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
