package com.shifenmiao.common.components.comments

import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.common.upload.UploadingImage
import com.shifenmiao.database.AppDatabase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.model.comment.Comment
import com.shifenmiao.network.model.comment.CommentEnvelope
import com.shifenmiao.network.model.comment.CreateCommentRequest
import com.shifenmiao.network.model.comment.UpdateCommentRequest
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/** 评论列表分页大小, 与 UI 层分页触发逻辑共同使用. */
const val COMMENT_PAGE_SIZE = 20

/** 评论单条最多附图数, 与 go-proxy 端无显式上限, 这里 UI 兜底. */
const val COMMENT_MAX_IMAGES = 3

/**
 * 一个 sheet 一份 component, 每次点评论图标新建一份, 关掉后被 GC → 协程取消 → 状态清空.
 *
 * 持有 [BaseComponent.componentScope] 这个 lifecycle-aware scope, 关闭后所有正在
 * "飞"的网络请求会随 lifecycle DESTROY 取消, 不会再写脏 state (修复了之前 sheet 自
 * 带 launch 协程但没有 lifecycle 挂钩的问题).
 *
 * State 全是 [StateFlow], UI 层只 collectAsState(), 自己不持任何状态.
 *
 * Action 全是普通方法, 由 [CommentsSheetContent] 直接调用, 不需要回调注册.
 *
 * 继承 [CommonComponent] 以复用 [CommonComponent.uploadImages]; 注入 SettingsManager /
 * AppDatabase / FileController 仅为满足父类构造签名, 评论场景不直接使用.
 */
class CommentsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("documentId") val documentId: String,
    @Assisted("itemTitle") val itemTitle: String,
    @Assisted("uid") val uid: String,
    @Assisted("onClose") val onClose: () -> Unit,
    @Assisted("onCommentCountChanged") val onCommentCountChanged: (delta: Int) -> Unit,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val apiService: ApiService,
    fileController: FileController,
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController,
) {

    // ──────────────── State ────────────────

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    /** 管理员屏蔽/删除操作进行中. */
    private val _isMutating = MutableStateFlow(false)
    val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _page = MutableStateFlow(1)
    val page: StateFlow<Int> = _page.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _replyTarget = MutableStateFlow<Comment?>(null)
    val replyTarget: StateFlow<Comment?> = _replyTarget.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    /** 待发送的附图 (本地 uri + 上传状态 + 远端 fileId). 草稿语义: sheet 关闭即丢弃. */
    private val _pendingImages = MutableStateFlow<List<UploadingImage>>(emptyList())
    val pendingImages: StateFlow<List<UploadingImage>> = _pendingImages.asStateFlow()

    /** 当前用户 — 每次 isSending 切换时重读 (用户可能在弹窗里登录/登出). */
    private val _currentUser = MutableStateFlow<CommentUserSnapshot?>(null)
    val currentUser: StateFlow<CommentUserSnapshot?> = _currentUser.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = _currentUser
        .map { it?.loggedIn == true }
        .stateIn(componentScope, SharingStarted.Eagerly, _currentUser.value?.loggedIn == true)

    /** vipLevel >= 10 即视为管理员 (项目内"管理员"约定). */
    val isAdmin: StateFlow<Boolean> = _currentUser
        .map { (it?.vipLevel ?: 0) >= 10 }
        .stateIn(componentScope, SharingStarted.Eagerly, (_currentUser.value?.vipLevel ?: 0) >= 10)

    /** 加载互斥, 防止快速滚动/重复触发导致重复请求. */
    private val loadMutex = Mutex()

    init {
        refreshCurrentUser()
        loadFirstPage()
    }

    // ──────────────── Actions ────────────────

    fun refreshCurrentUser() {
        val login = TokenStorage.getLoginInfo()
        _currentUser.value = login?.user?.let {
            CommentUserSnapshot(
                id = it.id,
                nickname = it.nickname.orEmpty(),
                avatar = it.avatar.orEmpty(),
                vipLevel = it.vipLevel ?: 0,
                loggedIn = it.id > 0,
            )
        }
    }

    fun setInputText(text: String) {
        if (text.length <= COMMENT_CONTENT_MAX_LEN) {
            _inputText.value = text
        } else {
            showToast(
                AppContext.getContext().getString(
                    com.shifenmiao.core.R.string.comment_too_long_format,
                    COMMENT_CONTENT_MAX_LEN,
                )
            )
        }
    }

    fun setReplyTarget(comment: Comment?) { _replyTarget.value = comment }
    fun clearReplyTarget() { _replyTarget.value = null }

    /**
     * 添加待上传图片; 立即占位 [UploadingImage] (本地 uri 状态),
     * 通过父类 [CommonComponent.uploadImages] 上传到 Strapi media,
     * 成功回调里把 file.id 写回并标记 isUploaded=true.
     */
    fun addImages(uris: List<Uri>) {
        val current = _pendingImages.value
        val remaining = COMMENT_MAX_IMAGES - current.size
        if (remaining <= 0) {
            showToast(
                AppContext.getContext().getString(
                    com.shifenmiao.core.R.string.comment_max_images_format,
                    COMMENT_MAX_IMAGES,
                )
            )
            return
        }
        val toUpload = uris.take(remaining)
        val startIndex = current.size
        val placeholders = toUpload.map { uri -> UploadingImage(localUri = uri.toString()) }
        _pendingImages.value = current + placeholders

        uploadImages(
            imageUris = toUpload,
            onUploadFailure = { index, msg ->
                val list = _pendingImages.value.toMutableList()
                val target = startIndex + index
                if (target in list.indices) {
                    list[target] = list[target].copy(isError = true, progress = 0f)
                    _pendingImages.value = list
                }
                // 闭环反馈 (review): 用户必须知道为什么图没了. msg 已经是
                // CommonComponent.formatModerationMessage 翻好的中文.
                if (msg.isNotBlank()) {
                    showToast(msg)
                }
            },
            onUploadSuccess = { index, strapiImages ->
                val list = _pendingImages.value.toMutableList()
                val target = startIndex + index
                if (target in list.indices) {
                    val first = strapiImages.firstOrNull()
                    if (first == null) {
                        // 上传 HTTP 200 但没拿到 StrapiImage (服务端返回空列表),
                        // 不能算成功 — 标记失败, 避免后续发包时 mapNotNull 把 id
                        // 全过滤掉, 用户看到 "已上传" 但实际图没发出去.
                        list[target] = list[target].copy(
                            isError = true,
                            isUploaded = false,
                            progress = 0f,
                        )
                    } else {
                        list[target] = list[target].copy(
                            id = first.id,
                            strapiImage = first,
                            progress = 1f,
                            isUploaded = true,
                            isError = false,
                        )
                    }
                    _pendingImages.value = list
                }
            },
            onProgressUpdate = { index, progress ->
                val list = _pendingImages.value.toMutableList()
                val target = startIndex + index
                if (target in list.indices) {
                    list[target] = list[target].copy(progress = progress)
                    _pendingImages.value = list
                }
            }
        )
    }

    /** 从待发送列表移除一张图片 (本地状态移除, OSS 上文件保留为孤儿, 由 OSS 周期清理). */
    fun removeImage(item: UploadingImage) {
        _pendingImages.value = _pendingImages.value.filterNot { it === item }
    }

    fun loadFirstPage() {
        componentScope.launch {
            if (!loadMutex.tryLock()) return@launch
            try {
                _isLoading.value = true
                _errorMessage.value = null
                runCatching {
                    apiService.listComments(documentId = documentId, uid = uid, page = 1, pageSize = COMMENT_PAGE_SIZE)
                }.onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val list = resp.body()?.data.orEmpty()
                        _comments.value = list
                        _page.value = 1
                        _hasMore.value = list.size >= COMMENT_PAGE_SIZE
                        _total.value = resp.body()?.meta?.pagination?.total ?: list.size
                    } else {
                        _errorMessage.value = loadFailedFormat(resp.code())
                        _hasMore.value = false
                    }
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: networkError()
                    _hasMore.value = false
                }
            } finally {
                _isLoading.value = false
                loadMutex.unlock()
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        val nextPage = _page.value + 1
        componentScope.launch {
            if (!loadMutex.tryLock()) return@launch
            try {
                _isLoading.value = true
                runCatching {
                    apiService.listComments(documentId = documentId, uid = uid, page = nextPage, pageSize = COMMENT_PAGE_SIZE)
                }.onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val list = resp.body()?.data.orEmpty()
                        _comments.value = _comments.value + list
                        _page.value = nextPage
                        _hasMore.value = list.size >= COMMENT_PAGE_SIZE
                    } else {
                        _errorMessage.value = loadFailedFormat(resp.code())
                        _hasMore.value = false
                    }
                }.onFailure { e ->
                    _errorMessage.value = e.message ?: networkError()
                    _hasMore.value = false
                }
            } finally {
                _isLoading.value = false
                loadMutex.unlock()
            }
        }
    }

    fun sendComment() {
        refreshCurrentUser()
        val user = _currentUser.value
        if (user?.loggedIn != true) {
            showToast(AppContext.getString(com.shifenmiao.core.R.string.login_to_comment))
            return
        }
        val content = _inputText.value.trim()
        val pending = _pendingImages.value
        if (content.isEmpty() && pending.isEmpty()) return
        if (content.length > COMMENT_CONTENT_MAX_LEN) {
            showToast(
                AppContext.getContext().getString(
                    com.shifenmiao.core.R.string.comment_too_long_format,
                    COMMENT_CONTENT_MAX_LEN,
                )
            )
            return
        }
        if (pending.any { !it.isUploaded }) {
            showToast(AppContext.getString(com.shifenmiao.core.R.string.comment_send_with_images_pending))
            return
        }
        val imageIds = pending.mapNotNull { it.id }
        val body = CreateCommentRequest(content = content, images = imageIds)
        val target = _replyTarget.value
        _isSending.value = true
        componentScope.launch {
            try {
                val resp = if (target == null) {
                    apiService.createComment(documentId, uid, body)
                } else {
                    apiService.replyComment(documentId, target.id, uid, body)
                }
                if (resp.isSuccessful) {
                    _inputText.value = ""
                    _replyTarget.value = null
                    _pendingImages.value = emptyList()
                    // 重拉首页拿到最新数据.
                    loadFirstPage()
                    // 通知父组件本地评论数 +1, 列表角标立即刷新.
                    onCommentCountChanged(1)
                } else {
                    // 闭环反馈: 解析服务端结构化错误, 区分"文本违规"和"网络/服务异常".
                    val errMsg = parseCommentErrorMessage(resp.errorBody()?.string())
                    if (errMsg != null) {
                        showToast(errMsg)
                    } else {
                        showToast(AppContext.getString(com.shifenmiao.core.R.string.comment_send_failed))
                    }
                }
            } catch (e: Exception) {
                showToast(e.message ?: networkError())
            } finally {
                _isSending.value = false
            }
        }
    }

    fun deleteComment(commentId: Int) {
        if (_isMutating.value) return
        _isMutating.value = true
        componentScope.launch {
            try {
                val resp = apiService.adminDeleteComment(commentId)
                if (resp.isSuccessful) {
                    _comments.value = _comments.value.filterNot { it.id == commentId }
                    _total.value = (_total.value - 1).coerceAtLeast(0)
                    showToast(AppContext.getString(com.shifenmiao.core.R.string.comment_delete_success))
                    // 通知父组件本地评论数 -1, 列表角标立即刷新.
                    onCommentCountChanged(-1)
                } else {
                    showToast(loadFailedFormat(resp.code()))
                }
            } catch (e: Exception) {
                showToast(e.message ?: networkError())
            } finally {
                _isMutating.value = false
            }
        }
    }

    fun toggleBlock(comment: Comment) {
        if (_isMutating.value) return
        val target = !comment.blocked
        _isMutating.value = true
        componentScope.launch {
            try {
                val resp = apiService.adminUpdateComment(
                    commentId = comment.id,
                    body = UpdateCommentRequest(
                        blocked = target,
                        blockReason = if (target) "admin_block" else null,
                    ),
                )
                if (resp.isSuccessful) {
                    val updated = resp.body()?.data
                    if (updated != null) {
                        _comments.value = _comments.value.map { if (it.id == comment.id) updated else it }
                    } else {
                        // 兜底: 直接本地标记.
                        _comments.value = _comments.value.map {
                            if (it.id == comment.id) it.copy(blocked = target) else it
                        }
                    }
                } else {
                    showToast(loadFailedFormat(resp.code()))
                }
            } catch (e: Exception) {
                showToast(e.message ?: networkError())
            } finally {
                _isMutating.value = false
            }
        }
    }

    /** 用户点 close 按钮 / 外部要求销毁. */
    fun close() {
        onClose()
    }

    // ──────────────── Helpers ────────────────

    private fun showToast(msg: String) {
        com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost.showToast(msg)
    }

    private fun loadFailedFormat(code: Int): String =
        AppContext.getContext().getString(
            com.shifenmiao.core.R.string.comment_load_failed_format,
            code,
        )

    private fun networkError(): String =
        AppContext.getString(com.shifenmiao.core.R.string.comment_network_error)

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("documentId") documentId: String,
            @Assisted("itemTitle") itemTitle: String,
            @Assisted("uid") uid: String,
            @Assisted("onClose") onClose: () -> Unit,
            @Assisted("onCommentCountChanged") onCommentCountChanged: (delta: Int) -> Unit,
        ): CommentsComponent
    }
}

/**
 * 评论场景下的用户快照 — 把 [com.shifenmiao.model.user.User] 的相关字段抽出,
 * 避免 UI 层直接依赖 TokenStorage / MMKV, 也方便未来切换 reactive 来源.
 */
data class CommentUserSnapshot(
    val id: Int,
    val nickname: String,
    val avatar: String,
    val vipLevel: Int,
    val loggedIn: Boolean,
)

/**
 * 服务端评论错误响应结构 — 与 go-proxy controllers/comments.go
 * formatBlockedMessage / StrapiErrorResponse 输出对齐.
 */
private data class CommentErrorBody(
    val error: String? = null,
    val code: String? = null,
    val message: String? = null,
)

/**
 * 解析服务端 4xx 响应体, 拿到给用户看的中文 message.
 * - 文本审核拦截: code=TEXT_BLOCKED → 服务端已经填好 message, 直接用
 * - 其他 4xx: 尝试用 message 字段; 解析失败返 null (让调用方走通用 toast).
 */
private fun parseCommentErrorMessage(errorBody: String?): String? {
    if (errorBody.isNullOrBlank()) return null
    return try {
        val parsed = Gson().fromJson(errorBody, CommentErrorBody::class.java)
        // 优先用服务端 message (已经是面向用户的中文); 兜底用 error 字段.
        parsed?.message?.takeIf { it.isNotBlank() } ?: parsed?.error
    } catch (e: JsonSyntaxException) {
        null
    }
}