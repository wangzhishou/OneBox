package com.wanbaohe.cloud.storage.screenLogic

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.model.CloudBrowserState
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CloudStorageComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: CloudStorageRepository,
    private val connectionHolder: CloudAgentToolConnectionHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    enum class Tab { FILES, SEARCH, SYNC }

    private val _connections = MutableStateFlow<List<CloudStorageConnection>>(emptyList())
    val connections: StateFlow<List<CloudStorageConnection>> = _connections.asStateFlow()

    private val _currentConnection = MutableStateFlow<CloudStorageConnection?>(null)
    val currentConnection: StateFlow<CloudStorageConnection?> = _currentConnection.asStateFlow()

    private val _buckets = MutableStateFlow<List<CloudBucket>>(emptyList())
    val buckets: StateFlow<List<CloudBucket>> = _buckets.asStateFlow()

    private val _currentBucket = MutableStateFlow<String?>(null)
    val currentBucket: StateFlow<String?> = _currentBucket.asStateFlow()

    private val _currentPrefix = MutableStateFlow("")
    val currentPrefix: StateFlow<String> = _currentPrefix.asStateFlow()

    private val _browserState = MutableStateFlow<CloudBrowserState>(CloudBrowserState.Idle)
    val browserState: StateFlow<CloudBrowserState> = _browserState.asStateFlow()

    private val _selectedObject = MutableStateFlow<CloudObjectItem?>(null)
    val selectedObject: StateFlow<CloudObjectItem?> = _selectedObject.asStateFlow()

    private val _selectedTab = MutableStateFlow(Tab.FILES)
    val selectedTab: StateFlow<Tab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow(repository.getLastSearchQuery())
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CloudObjectItem>>(emptyList())
    val searchResults: StateFlow<List<CloudObjectItem>> = _searchResults.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isGridMode = MutableStateFlow(true)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    init {
        componentScope.launch(ioDispatcher) {
            restoreState()
        }
    }

    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        persistUiState()
    }

    fun toggleViewMode() {
        _isGridMode.value = !_isGridMode.value
        repository.saveGridMode(_isGridMode.value)
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun showObject(item: CloudObjectItem) {
        _selectedObject.value = item
    }

    fun hideObject() {
        _selectedObject.value = null
    }

    fun updateConnection(connection: CloudStorageConnection) {
        repository.saveConnection(connection)
        _connections.value = repository.getConnections()
        connectionHolder.update(_connections.value)
        switchConnection(connection.id)
    }

    fun removeConnection(connectionId: String) {
        repository.deleteConnection(connectionId)
        _connections.value = repository.getConnections()
        connectionHolder.update(_connections.value)
        val newCurrent = _connections.value.firstOrNull { it.isDefault } ?: _connections.value.firstOrNull()
        _currentConnection.value = newCurrent
        componentScope.launch(ioDispatcher) { loadBucketsAndObjects() }
    }

    fun switchConnection(connectionId: String) {
        _currentConnection.value = _connections.value.firstOrNull { it.id == connectionId }
        _currentPrefix.value = ""
        componentScope.launch(ioDispatcher) { loadBucketsAndObjects() }
    }

    fun switchBucket(bucket: String) {
        _currentBucket.value = bucket
        _currentPrefix.value = ""
        componentScope.launch(ioDispatcher) { refreshObjects() }
    }

    fun testConnection(connection: CloudStorageConnection, onResult: (Result<Unit>) -> Unit) {
        componentScope.launch(ioDispatcher) {
            val result = repository.testConnection(connection)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun openPrefix(prefix: String) {
        _currentPrefix.value = ObjectStoragePathResolver.normalizePrefix(prefix)
        componentScope.launch(ioDispatcher) { refreshObjects() }
    }

    fun navigateUp() {
        _currentPrefix.value = ObjectStoragePathResolver.parentPrefix(_currentPrefix.value)
        componentScope.launch(ioDispatcher) { refreshObjects() }
    }

    fun refresh() {
        componentScope.launch(ioDispatcher) { refreshObjects() }
    }

    fun search() {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        componentScope.launch(ioDispatcher) {
            _isSearchLoading.value = true
            val result = repository.searchObjects(connection, bucket, _searchQuery.value.trim())
            result.fold(
                onSuccess = { _searchResults.value = it },
                onFailure = { _message.value = it.message ?: context.getString(R.string.cloud_storage_error_generic) }
            )
            _isSearchLoading.value = false
            persistUiState()
        }
    }

    fun openSearchResult(item: CloudObjectItem) {
        _selectedTab.value = Tab.FILES
        _currentPrefix.value = item.prefix
        componentScope.launch(ioDispatcher) {
            refreshObjects()
            _selectedObject.value = item
        }
    }

    fun createFolder(name: String) {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        componentScope.launch(ioDispatcher) {
            mutate {
                repository.createFolder(
                    connection = connection,
                    bucket = bucket,
                    prefix = ObjectStoragePathResolver.childPrefix(_currentPrefix.value, name.trim()),
                )
            }
        }
    }

    fun uploadFromUri(uri: Uri) {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        componentScope.launch(ioDispatcher) {
            _busy.value = true
            _uploadProgress.value = 0f
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException(context.getString(R.string.cloud_storage_error_read_file))
                val fileName = resolveDisplayName(uri)
                val key = "${_currentPrefix.value}$fileName"
                val contentType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                repository.uploadObject(connection, bucket, key, bytes, contentType) { progress ->
                    _uploadProgress.value = progress
                }
            }.fold(
                onSuccess = {
                    _uploadProgress.value = null
                    hideObject()
                    refreshObjects()
                },
                onFailure = {
                    _uploadProgress.value = null
                    _message.value = it.message ?: context.getString(R.string.cloud_storage_error_generic)
                }
            )
            _busy.value = false
        }
    }

    fun renameSelected(newName: String) {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        val selected = _selectedObject.value ?: return
        val targetKey = if (selected.isDirectory) {
            "${selected.prefix}${newName.trim()}/"
        } else {
            "${selected.prefix.takeIf { it.isNotBlank() }?.let { "$it/" }.orEmpty()}${newName.trim()}"
        }.replace("//", "/")
        componentScope.launch(ioDispatcher) {
            mutate { repository.renameObject(connection, bucket, selected.key, targetKey) }
        }
    }

    fun moveSelected(targetPrefix: String) {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        val selected = _selectedObject.value ?: return
        val normalizedTarget = ObjectStoragePathResolver.normalizePrefix(targetPrefix)
        val targetKey = "$normalizedTarget${selected.displayName}${if (selected.isDirectory) "/" else ""}"
        componentScope.launch(ioDispatcher) {
            mutate { repository.moveObject(connection, bucket, selected.key, targetKey) }
        }
    }

    fun deleteSelected() {
        val selected = _selectedObject.value ?: return
        deleteObjects(listOf(selected))
    }

    fun deleteObjects(items: List<CloudObjectItem>) {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        val targets = items.distinctBy { it.key }.takeIf { it.isNotEmpty() } ?: return
        componentScope.launch(ioDispatcher) {
            mutate {
                targets.forEach { item ->
                    repository.deleteObject(connection, bucket, item.key, item.isDirectory)
                        .getOrElse { return@mutate Result.failure(it) }
                }
                Result.success(Unit)
            }
        }
    }

    fun signedUrlFor(item: CloudObjectItem): String? {
        val connection = _currentConnection.value ?: return null
        val bucket = _currentBucket.value ?: return null
        return repository.buildSignedGetUrl(connection, bucket, item.key)
    }

    private suspend fun restoreState() {
        _connections.value = repository.getConnections()
        connectionHolder.update(_connections.value)
        _isGridMode.value = repository.isGridMode()
        val current = _connections.value.firstOrNull { it.id == repository.getLastConnectionId() }
            ?: _connections.value.firstOrNull { it.isDefault }
            ?: _connections.value.firstOrNull()
        _currentConnection.value = current
        _currentBucket.value = repository.getLastBucket()
        _currentPrefix.value = repository.getLastPrefix()
        loadBucketsAndObjects()
    }

    private suspend fun loadBucketsAndObjects() {
        val connection = _currentConnection.value ?: run {
            _buckets.value = emptyList()
            _browserState.value = CloudBrowserState.Idle
            return
        }
        val defaultRoot = defaultRootName(connection)
        repository.listBuckets(connection).fold(
            onSuccess = { buckets ->
                _buckets.value = buckets
                _currentBucket.value = _currentBucket.value
                    ?.takeIf { current -> buckets.any { it.name == current } }
                    ?: buckets.firstOrNull { it.name == defaultRoot }?.name
                    ?: buckets.firstOrNull()?.name
            },
            onFailure = {
                if (!defaultRoot.isNullOrBlank()) {
                    _buckets.value = listOf(CloudBucket(defaultRoot))
                    _currentBucket.value = defaultRoot
                } else {
                    _message.value = it.message ?: context.getString(R.string.cloud_storage_error_generic)
                    _buckets.value = emptyList()
                    _currentBucket.value = null
                }
            },
        )
        refreshObjects()
    }

    private fun defaultRootName(connection: CloudStorageConnection): String? = when (connection) {
        is CloudStorageConnection.S3Compat -> connection.bucket.takeIf { it.isNotBlank() }
        is CloudStorageConnection.WebDav -> connection.rootPath.takeIf { it.isNotBlank() } ?: "/"
        is CloudStorageConnection.Smb -> connection.share.takeIf { it.isNotBlank() }
    }

    private suspend fun refreshObjects() {
        val connection = _currentConnection.value ?: return
        val bucket = _currentBucket.value ?: return
        _browserState.value = CloudBrowserState.Loading
        repository.listObjects(connection, bucket, _currentPrefix.value).fold(
            onSuccess = { items ->
                val filtered = items.filterNot { it.key == _currentPrefix.value }
                val breadcrumbs = ObjectStoragePathResolver.splitBreadcrumbs(_currentPrefix.value)
                _browserState.value = if (filtered.isEmpty()) {
                    CloudBrowserState.Empty(_currentPrefix.value)
                } else {
                    CloudBrowserState.Success(filtered, _currentPrefix.value, breadcrumbs)
                }
                persistUiState()
            },
            onFailure = {
                _browserState.value = CloudBrowserState.Error(
                    message = it.message ?: context.getString(R.string.cloud_storage_error_generic),
                    throwable = it,
                )
            }
        )
    }

    private suspend fun mutate(action: suspend () -> Result<Unit>) {
        _busy.value = true
        action().fold(
            onSuccess = {
                hideObject()
                refreshObjects()
            },
            onFailure = {
                _message.value = it.message ?: context.getString(R.string.cloud_storage_error_generic)
            }
        )
        _busy.value = false
    }

    private fun persistUiState() {
        repository.saveUiState(
            connectionId = _currentConnection.value?.id,
            bucket = _currentBucket.value,
            prefix = _currentPrefix.value,
            searchQuery = _searchQuery.value,
        )
    }

    private suspend fun resolveDisplayName(uri: Uri): String = withContext(ioDispatcher) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor.useDisplayName() ?: "upload-${System.currentTimeMillis()}"
    }

    private fun Cursor?.useDisplayName(): String? = this?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }

    override fun resetState() = Unit

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(componentContext: ComponentContext): CloudStorageComponent
    }
}
