package com.wanbaohe.file.browser.screenLogic

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.content.ContentRouter
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.SettingsState
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.file.browser.model.FileBrowserConfig
import com.wanbaohe.file.browser.model.FileBrowserState
import com.wanbaohe.file.browser.model.FileBrowserViewMode
import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.model.SortConfig
import com.wanbaohe.file.browser.model.SortOrder
import com.wanbaohe.file.browser.model.SortType
import com.shifenmiao.common.recent.RecentAccessRepository
import com.wanbaohe.file.browser.prefs.FileBrowserPrefs
import com.wanbaohe.file.browser.utils.FileHelper
import com.wanbaohe.file.browser.utils.FileSorter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component managing file browser business logic and state
 *
 * @param componentContext The decompose component context
 * @param context Application context
 * @param dispatchersHolder Coroutine dispatchers holder
 * @param fileHelper Helper for file operations
 * @param initialUri Optional initial URI to navigate to
 */
class FileBrowserComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    @Assisted private val initialUri: Uri?,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val fileHelper: FileHelper,
    private val settingsManager: SettingsManager,
    private val contentRouter: ContentRouter,
    private val recentAccessRepository: RecentAccessRepository,
) : BaseComponent(dispatchersHolder, componentContext) {

    private var _settingsState: SettingsState = SettingsState.Default

    private val settingsState get() = _settingsState

    private val _state = MutableStateFlow<FileBrowserState>(FileBrowserState.Idle)
    val state: StateFlow<FileBrowserState> = _state.asStateFlow()

    private val _currentUri = MutableStateFlow<Uri?>(null)
    val currentUri: StateFlow<Uri?> = _currentUri.asStateFlow()

    private val _sortConfig = MutableStateFlow(SortConfig())
    val sortConfig: StateFlow<SortConfig> = _sortConfig.asStateFlow()

    private val _navigationStack = mutableListOf<Uri?>()

    private var highlightFileName: String? = null

    /**
     * Top-level navigation destinations for the file browser.
     * Note: Recents is a placeholder in Step 1; will be implemented in later steps.
     */
    enum class NavDestination { HOME, RECENTS, WORKSPACE }

    private val _navDestination = MutableStateFlow(NavDestination.HOME)
    val navDestination: StateFlow<NavDestination> = _navDestination.asStateFlow()

    private val prefs: FileBrowserPrefs by lazy { FileBrowserPrefs(context) }

    private val _viewMode = MutableStateFlow(prefs.loadViewMode())
    val viewMode: StateFlow<FileBrowserViewMode> = _viewMode.asStateFlow()

    /** Selection mode state. */
    enum class SelectionMode { NONE, SINGLE, MULTI }

    private val _selectionMode = MutableStateFlow(SelectionMode.NONE)
    val selectionMode: StateFlow<SelectionMode> = _selectionMode.asStateFlow()

    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: StateFlow<Set<Uri>> = _selectedUris.asStateFlow()

    /** Convenience: selected FileItems based on current Success state. */
    val selectedItems: StateFlow<List<FileItem>> = combine(state, selectedUris) { s, selected ->
        if (s is FileBrowserState.Success) {
            s.files.filter { it.uri in selected }
        } else {
            emptyList()
        }
    }.let { flow ->
        val out = MutableStateFlow<List<FileItem>>(emptyList())
        componentScope.launch { flow.collect { out.value = it } }
        out.asStateFlow()
    }

    init {
        initialize()
    }

    /**
     * Initializes the component with the initial URI
     */
    private fun initialize() {
        settingsManager
            .settingsState
            .onEach { state ->
                _settingsState = state
            }.launchIn(CoroutineScope(defaultDispatcher))
        componentScope.launch {
            if (initialUri != null) {
                // 直接加载目录：content tree URI 或 file:// 目录
                // 其他情况当作文件，找其父目录并高亮文件名
                val isDirectory = when {
                    initialUri.scheme == "content" && DocumentsContract.isTreeUri(initialUri) -> true
                    initialUri.scheme == "file" -> {
                        val f = initialUri.path?.let { java.io.File(it) }
                        f != null && f.isDirectory
                    }
                    else -> false
                }
                if (isDirectory) {
                    loadDirectory(initialUri)
                } else {
                    val result = fileHelper.findFileDirectory(initialUri)
                    result.fold(
                        onSuccess = { (directoryUri, fileName) ->
                            highlightFileName = fileName
                            recordRecent(initialUri, fileName, RecentAccessRepository.TYPE_FILE)
                            loadDirectory(directoryUri)
                        },
                        onFailure = { error ->
                            ActionUtils.showToast(AppContext.getString(R.string.file_not_found))
                            loadDirectory(null)
                        }
                    )
                }
                return@launch
            }

            // Restore last destination if possible.
            when (prefs.loadLastDestination()) {
                NavDestination.RECENTS -> {
                    openRecents()
                }

                NavDestination.WORKSPACE -> {
                    openWorkspace()
                }

                NavDestination.HOME, null -> {
                    // Load default directory
                    _navDestination.value = NavDestination.HOME
                    loadDirectory(null)
                }
            }
        }
    }

    /**
     * Loads files from the specified directory URI
     */
    fun loadDirectory(uri: Uri?) {
        componentScope.launch {
            _state.value = FileBrowserState.Loading
            _currentUri.value = uri

            val result = fileHelper.loadFilesFromUri(uri)

            result.fold(
                onSuccess = { files ->
                    // Record directory visits (ignore null -> Home).
                    if (uri != null) {
                        recordRecent(uri, fileHelper.getDisplayPath(uri), RecentAccessRepository.TYPE_FOLDER)
                    }

                    val highlightedFiles = if (highlightFileName != null) {
                        files.map { file ->
                            if (file.name == highlightFileName) {
                                file.copy(isHighlighted = true)
                            } else {
                                file
                            }
                        }
                    } else {
                        files
                    }

                    // Clear highlight after first load
                    highlightFileName = null

                    val sortedFiles = FileSorter.sort(highlightedFiles, _sortConfig.value)
                    val currentPath = fileHelper.getDisplayPath(uri)

                    val selected = _selectedUris.value
                    val filesWithSelection = if (selected.isNotEmpty()) {
                        sortedFiles.map { it.copy(isSelected = it.uri in selected) }
                    } else {
                        sortedFiles
                    }

                    _state.value = if (filesWithSelection.isEmpty()) {
                        FileBrowserState.Empty(currentPath)
                    } else {
                        FileBrowserState.Success(filesWithSelection, currentPath)
                    }

                    // Rebuild navigation stack based on current URI
                    // This ensures the stack is always consistent
                    rebuildNavigationStack(uri)
                },
                onFailure = { error ->
                    _state.value = FileBrowserState.Error(
                        message = error.message ?: "Unknown error",
                        throwable = error
                    )
                }
            )
        }
    }

    /**
     * Rebuilds the navigation stack from the root to the current URI
     */
    private fun rebuildNavigationStack(currentUri: Uri?) {
        _navigationStack.clear()

        if (currentUri == null) {
            // At root
            _navigationStack.add(null)
            return
        }

        // Build parent chain from current URI back to root
        val chain = mutableListOf<Uri?>()
        var uri: Uri? = currentUri

        while (uri != null) {
            chain.add(0, uri) // Add to front
            uri = fileHelper.getParentUri(uri)
        }

        // Add root at beginning
        chain.add(0, null)

        // Set as new navigation stack
        _navigationStack.addAll(chain)
    }

    fun enterSelectionMode(multi: Boolean) {
        _selectionMode.value = if (multi) SelectionMode.MULTI else SelectionMode.SINGLE
        // keep current selection
        refreshSelectionInState()
    }

    fun exitSelectionMode() {
        _selectionMode.value = SelectionMode.NONE
        _selectedUris.value = emptySet()
        refreshSelectionInState()
    }

    fun toggleSelect(uri: Uri) {
        val mode = _selectionMode.value
        if (mode == SelectionMode.NONE) {
            // default: enter multi selection
            _selectionMode.value = SelectionMode.MULTI
        }

        _selectedUris.value = when (_selectionMode.value) {
            SelectionMode.SINGLE -> setOf(uri)
            SelectionMode.MULTI -> {
                val current = _selectedUris.value
                if (uri in current) current - uri else current + uri
            }

            SelectionMode.NONE -> emptySet()
        }

        refreshSelectionInState()
    }

    fun clearSelection() {
        _selectedUris.value = emptySet()
        refreshSelectionInState()
    }

    fun selectAll() {
        val currentState = _state.value
        if (currentState is FileBrowserState.Success) {
            _selectionMode.value = SelectionMode.MULTI
            _selectedUris.value = currentState.files.map { it.uri }.toSet()
            refreshSelectionInState()
        }
    }

    private fun refreshSelectionInState() {
        val currentState = _state.value
        if (currentState is FileBrowserState.Success) {
            val selected = _selectedUris.value
            _state.value = currentState.copy(
                files = currentState.files.map { it.copy(isSelected = it.uri in selected) }
            )
        }
    }

    suspend fun deleteSelected(): Result<Unit> {
        val uris = _selectedUris.value.toList()
        if (uris.isEmpty()) return Result.success(Unit)

        return runCatching {
            uris.forEach { uri ->
                fileHelper.delete(uri).getOrThrow()
            }
            exitSelectionMode()
            refresh()
        }
    }

    suspend fun renameSingleSelected(newName: String): Result<Unit> {
        val uri = _selectedUris.value.singleOrNull() ?: return Result.failure(
            IllegalStateException("rename requires exactly one selected item")
        )

        return runCatching {
            val newUri = fileHelper.rename(uri, newName).getOrThrow()
            _selectedUris.value = setOf(newUri)
            refresh()
        }
    }

    suspend fun copySelectedTo(destDir: Uri): Result<Unit> {
        val uris = _selectedUris.value.toList()
        if (uris.isEmpty()) return Result.success(Unit)

        return runCatching {
            uris.forEach { uri ->
                fileHelper.copy(source = uri, destDir = destDir).getOrThrow()
            }
            exitSelectionMode()
            refresh()
        }
    }

    suspend fun moveSelectedTo(destDir: Uri): Result<Unit> {
        val uris = _selectedUris.value.toList()
        if (uris.isEmpty()) return Result.success(Unit)

        return runCatching {
            uris.forEach { uri ->
                fileHelper.move(source = uri, destDir = destDir).getOrThrow()
            }
            exitSelectionMode()
            refresh()
        }
    }

    /** Home: default directory (same behavior as current default). */
    fun openHome() {
        prefs.saveLastDestination(NavDestination.HOME)
        switchRootDestination(NavDestination.HOME, null)
    }

    /**
     * Recents: load a "recently visited" list from database.
     */
    fun openRecents() {
        prefs.saveLastDestination(NavDestination.RECENTS)

        _navDestination.value = NavDestination.RECENTS
        _navigationStack.clear()
        _currentUri.value = null

        componentScope.launch {
            val entities = recentAccessRepository.getAll(RecentAccessRepository.DEFAULT_LIMIT)
            val items = entities.map { entity ->
                FileBrowserState.RecentItem(
                    uri = Uri.parse(entity.uri),
                    title = entity.displayName,
                    subtitle = entity.pathHint ?: fileHelper.getDisplayPath(Uri.parse(entity.uri)),
                    accessType = entity.accessType
                )
            }
            _state.value = if (items.isEmpty()) {
                FileBrowserState.RecentsEmpty
            } else {
                FileBrowserState.Recents(items)
            }
        }
    }

    /** Workspace: configured folder (temporary default via FileBrowserConfig). */
    fun openWorkspace() {
        prefs.saveLastDestination(NavDestination.WORKSPACE)

        val uri = FileBrowserConfig.workspaceRootUriProvider(context)
        prefs.saveLastWorkspaceUri(uri)

        if (uri == null) {
            // Fallback if host app didn't configure workspace yet.
            switchRootDestination(NavDestination.HOME, null)
        } else {
            switchRootDestination(NavDestination.WORKSPACE, uri)
        }
    }

    /**
     * Navigates to the parent directory (synced with breadcrumbs)
     * Skips system paths that are not shown in breadcrumbs
     */
    fun navigateUp() {
        val breadcrumbs = getBreadcrumbs()

        // Find current position in breadcrumbs
        val currentUri = _currentUri.value
        val currentIndex = breadcrumbs.indexOfLast { it.second == currentUri }

        if (currentIndex > 0) {
            // Navigate to the previous breadcrumb
            val parentBreadcrumb = breadcrumbs[currentIndex - 1]
            loadDirectory(parentBreadcrumb.second)
        } else if (currentUri != null) {
            // If current URI is not in breadcrumbs or at first position, go to Home
            loadDirectory(null)
        }
    }

    /**
     * Navigates back in the navigation stack
     */
    fun navigateBack(): Boolean {
        if (_navigationStack.size > 1) {
            _navigationStack.removeAt(_navigationStack.lastIndex)
            val previousUri = _navigationStack.lastOrNull()
            loadDirectory(previousUri)
            return true
        }
        return false
    }

    private fun recordRecent(uri: Uri, displayName: String, accessType: String) {
        componentScope.launch {
            recentAccessRepository.recordAccess(
                uri = uri.toString(),
                displayName = displayName,
                accessType = accessType,
                pathHint = fileHelper.getDisplayPath(uri),
            )
        }
    }

    fun openUri(uri: Uri) {
        componentScope.launch {
            if (fileHelper.isDirectoryUri(uri)) {
                recordRecent(uri, fileHelper.getDisplayPath(uri), RecentAccessRepository.TYPE_FOLDER)
                loadDirectory(uri)
            } else {
                val name = uri.lastPathSegment ?: uri.toString()
                recordRecent(uri, name, RecentAccessRepository.TYPE_FILE)
                contentRouter.route(
                    uri = uri,
                    context = context,
                    onNavigate = onNavigate,
                    allSiblings = emptyList(),
                    fallbackToExternal = true
                )
            }
        }
    }

    fun openRecent(uri: Uri) {
        _navDestination.value = NavDestination.RECENTS
        openUri(uri)
    }

    fun clearRecents() {
        componentScope.launch {
            recentAccessRepository.clearAll()
            // If user is on recents screen, refresh it.
            if (_navDestination.value == NavDestination.RECENTS) {
                openRecents()
            }
        }
    }

    /**
     * Handles file item click
     *
     * 文件打开逻辑统一委托给 [ContentRouter]，由它根据文件类型
     * 智能路由到最合适的查看器（ImageViewer / PdfTools / MarkdownEditor / 系统 chooser）。
     */
    fun onItemClick(item: FileItem) {
        // If we are in selection mode, click toggles selection.
        if (_selectionMode.value != SelectionMode.NONE) {
            toggleSelect(item.uri)
            return
        }

        if (item.isDirectory) {
            recordRecent(item.uri, item.name, RecentAccessRepository.TYPE_FOLDER)
            loadDirectory(item.uri)
        } else {
            recordRecent(item.uri, item.name, RecentAccessRepository.TYPE_FILE)
            val currentFiles = (_state.value as? FileBrowserState.Success)?.files.orEmpty()
            // 预过滤图片 URI：利用 FileItem 已有的 mimeType，避免 ContentRouter 内重复 I/O
            val imageSiblings = if (item.mimeType?.startsWith("image/") == true) {
                currentFiles
                    .filter { it.mimeType?.startsWith("image/") == true }
                    .map { it.uri }
            } else {
                emptyList()
            }
            contentRouter.route(
                uri = item.uri,
                context = context,
                onNavigate = onNavigate,
                allSiblings = imageSiblings,
                fallbackToExternal = true
            )
        }
    }

    /** For long press, always toggle selection (enter multi mode if needed). */
    fun onItemLongClick(item: FileItem) {
        if (_selectionMode.value == SelectionMode.NONE) {
            _selectionMode.value = SelectionMode.MULTI
        }
        toggleSelect(item.uri)
    }

    /**
     * Opens the selected file via [ContentRouter].
     * Only works when exactly one file is selected and it's not a directory.
     */
    fun openSelectedWithSystemChooser() {
        val selected = _selectedUris.value.singleOrNull() ?: return
        val currentState = _state.value
        if (currentState !is FileBrowserState.Success) return

        val fileItem = currentState.files.firstOrNull { it.uri == selected } ?: return
        if (fileItem.isDirectory) return

        // 预过滤图片 URI，避免 ContentRouter 内重复 I/O
        val imageSiblings = if (fileItem.mimeType?.startsWith("image/") == true) {
            currentState.files
                .filter { it.mimeType?.startsWith("image/") == true }
                .map { it.uri }
        } else {
            emptyList()
        }
        contentRouter.route(
            uri = fileItem.uri,
            context = context,
            onNavigate = onNavigate,
            allSiblings = imageSiblings,
            fallbackToExternal = true
        )
    }

    fun isSelectionMode(): Boolean = _selectionMode.value != SelectionMode.NONE

    // Ensure selection is cleared when switching roots.
    private fun switchRootDestination(
        destination: NavDestination,
        rootUri: Uri?
    ) {
        exitSelectionMode()
        _navDestination.value = destination
        _navigationStack.clear()
        _currentUri.value = rootUri
        loadDirectory(rootUri)
    }

    fun getCurrentPath(): String {
        return when (val s = _state.value) {
            is FileBrowserState.Success -> s.currentPath
            is FileBrowserState.Empty -> s.currentPath
            else -> fileHelper.getDisplayPath(_currentUri.value)
        }
    }

    /**
     * Gets breadcrumb path segments with their corresponding URIs for navigation
     * @return List of Pair<displayName, uri> representing each level in the path hierarchy
     */
    fun getBreadcrumbs(): List<Pair<String, Uri?>> {
        val breadcrumbs = mutableListOf<Pair<String, Uri?>>()

        val currentUri = _currentUri.value

        if (currentUri == null) {
            // At root
            breadcrumbs.add(context.getString(com.wanbaohe.file.browser.R.string.root_path) to null)
            return breadcrumbs
        }

        // Build the complete parent chain from current URI
        val parentChain = mutableListOf<Uri?>()
        var uri: Uri? = currentUri

        // Traverse up to build parent chain
        while (uri != null) {
            parentChain.add(0, uri) // Add to front
            uri = fileHelper.getParentUri(uri)
        }

        // Add root at the beginning
        breadcrumbs.add(context.getString(com.wanbaohe.file.browser.R.string.root_path) to null)

        // Android primary storage root path (e.g., /storage/emulated/0)
        val primaryStoragePath = android.os.Environment.getExternalStorageDirectory().absolutePath

        // Convert parent chain to breadcrumbs, filtering out system paths
        for (i in parentChain.indices) {
            val pathUri = parentChain[i]
            val path = fileHelper.getDisplayPath(pathUri)

            // Skip if this is the primary storage root or its parent paths
            // (e.g., skip /storage, /storage/emulated, /storage/emulated/0)
            if (path == primaryStoragePath) {
                continue
            }

            // Check if this path is a parent of primary storage (e.g., /storage, /storage/emulated)
            if (primaryStoragePath.startsWith(path + "/") || path == "/storage" || path == "/storage/emulated") {
                continue
            }

            // Skip root path "/"
            if (path == "/" || path.isEmpty()) {
                continue
            }

            // Get the folder name from the path
            val segments = path.split("/").filter { it.isNotEmpty() }
            if (segments.isEmpty()) {
                continue
            }

            val displayName = segments.lastOrNull() ?: continue

            // Skip empty, blank, or single-digit names that are likely system paths
            if (displayName.isBlank() || (displayName.length == 1 && displayName[0].isDigit())) {
                continue
            }

            breadcrumbs.add(displayName to pathUri)
        }

        return breadcrumbs
    }

    /**
     * Navigates to a specific URI in the path hierarchy
     */
    fun navigateToUri(uri: Uri?) {
        // Simply load the directory - this will rebuild the navigation stack properly
        loadDirectory(uri)
    }

    fun getWorkspaceRootUriOrNull(): Uri? =
        settingsState.saveFolderUri?.toUri() ?: FileBrowserConfig.workspaceRootUriProvider(context)

    fun toggleViewMode() {
        val newMode = if (_viewMode.value == FileBrowserViewMode.LIST) {
            FileBrowserViewMode.GRID
        } else {
            FileBrowserViewMode.LIST
        }
        _viewMode.value = newMode
        prefs.saveViewMode(newMode)
    }

    fun changeSortType(type: SortType) {
        val currentConfig = _sortConfig.value
        _sortConfig.value = if (currentConfig.type == type) {
            // Same type clicked again - toggle order
            currentConfig.toggleOrder()
        } else {
            // Different type - use default ascending order
            currentConfig.copy(type = type, order = SortOrder.ASCENDING)
        }
        refresh()
        // FileBrowserPrefs currently only persists view mode / destinations.
    }

    fun refresh() {
        loadDirectory(_currentUri.value)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            onNavigate: (Screen) -> Unit
        ): FileBrowserComponent
    }
}
