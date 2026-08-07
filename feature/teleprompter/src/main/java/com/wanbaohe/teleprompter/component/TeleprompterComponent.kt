package com.wanbaohe.teleprompter.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.teleprompter.service.TeleprompterService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 提词器主组件
 *
 * 管理文稿列表（首页）、编辑（子页）、播放（子页）三个状态，
 * 内部通过 [currentPage] 驱动页面切换，对外仅暴露一个 Screen 路由。
 *
 * [type] 非空时支持外部路由（deeplink）直达指定文稿的编辑页或播放页。
 */
class TeleprompterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted private val type: Screen.Teleprompter.Type?,
    dispatchersHolder: DispatchersHolder,
    private val service: TeleprompterService,
) : BaseComponent(dispatchersHolder, componentContext) {

    // ── 内部导航状态 ─────────────────────────────────────────────────────

    enum class Page { LIST, EDITOR, PLAYER }

    private val _currentPage = MutableStateFlow(Page.LIST)
    val currentPage = _currentPage.asStateFlow()

    // ── 文稿列表 ─────────────────────────────────────────────────────────

    private val _listState = MutableStateFlow(TeleprompterListUiState())
    val listState = _listState.asStateFlow()

    // ── 编辑器 ───────────────────────────────────────────────────────────

    private val _editorState = MutableStateFlow(TeleprompterEditorUiState())
    val editorState = _editorState.asStateFlow()

    // ── 播放器 ───────────────────────────────────────────────────────────

    private val _playerState = MutableStateFlow(TeleprompterPlayerUiState())
    val playerState = _playerState.asStateFlow()

    init {
        observeScripts()
        applyInitialRoute(type)
    }

    private fun observeScripts() {
        service.observeScripts()
            .onEach { list ->
                _listState.update { it.copy(scripts = list, isLoading = false) }
            }
            .launchIn(componentScope)
    }

    /** 处理外部路由参数：加载指定文稿后直达编辑页或播放页，失败则停留在列表页 */
    private fun applyInitialRoute(type: Screen.Teleprompter.Type?) {
        type ?: return
        val scriptId = when (type) {
            is Screen.Teleprompter.Type.Edit -> type.scriptId
            is Screen.Teleprompter.Type.Play -> type.scriptId
        }
        if (scriptId.isBlank()) return

        componentScope.launch {
            val script = service.getScript(scriptId).getOrNull() ?: return@launch
            when (type) {
                is Screen.Teleprompter.Type.Edit -> onEditScript(script)
                is Screen.Teleprompter.Type.Play -> onPlayScript(script)
            }
        }
    }

    // ── 列表页操作 ───────────────────────────────────────────────────────

    fun onNewScript() {
        _editorState.value = TeleprompterEditorUiState(
            isNew = true,
            originalTitle = "",
            originalContent = "",
        )
        _currentPage.value = Page.EDITOR
    }

    fun onEditScript(script: TeleprompterScriptEntity) {
        _editorState.value = TeleprompterEditorUiState(
            scriptId = script.id,
            title = script.title,
            content = script.content,
            isNew = false,
            originalTitle = script.title,
            originalContent = script.content,
        )
        _currentPage.value = Page.EDITOR
    }

    fun onDeleteScript(scriptId: String) {
        componentScope.launch {
            service.deleteScript(scriptId, source = LOG_SOURCE)
        }
    }

    fun onPlayScript(script: TeleprompterScriptEntity) {
        _playerState.value = TeleprompterPlayerUiState(
            content = script.content,
            isPlaying = true,
            showControls = false,
        )
        _currentPage.value = Page.PLAYER
    }

    // ── 编辑器操作 ───────────────────────────────────────────────────────

    fun onTitleChange(title: String) {
        _editorState.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _editorState.update { it.copy(content = content) }
    }

    fun onSaveScript(): Boolean {
        val state = _editorState.value
        if (state.title.isBlank()) return false

        _editorState.update { it.copy(isSaving = true) }
        componentScope.launch {
            service.saveScript(
                scriptId = state.scriptId,
                title = state.title,
                content = state.content,
                source = LOG_SOURCE,
            ).onSuccess { saved ->
                _editorState.update {
                    it.copy(
                        isSaving = false,
                        scriptId = saved.id,
                        isNew = false,
                        originalTitle = it.title,
                        originalContent = it.content,
                    )
                }
            }.onFailure {
                _editorState.update { it.copy(isSaving = false) }
            }
        }
        return true
    }

    fun onPlayFromEditor() {
        val state = _editorState.value
        if (state.content.isBlank()) return
        // 先保存
        onSaveScript()
        _playerState.value = TeleprompterPlayerUiState(
            content = state.content,
            isPlaying = true,
            showControls = false,
        )
        _currentPage.value = Page.PLAYER
    }

    fun onEditorBack() {
        _currentPage.value = Page.LIST
    }

    // ── 播放器操作 ───────────────────────────────────────────────────────

    fun onTogglePlay() {
        val current = _playerState.value
        if (!current.isPlaying && current.scrollProgress >= 0.99f) {
            // 播放已结束，从头开始播放
            _playerState.update { it.copy(scrollProgress = 0f, isPlaying = true) }
        } else {
            _playerState.update { it.copy(isPlaying = !it.isPlaying) }
        }
    }

    fun onSetPlaying(playing: Boolean) {
        _playerState.update { it.copy(isPlaying = playing) }
    }

    fun onFontSizeChange(size: Float) {
        _playerState.update {
            it.copy(
                fontSize = size.coerceIn(
                    TeleprompterPlayerUiState.MIN_FONT_SIZE,
                    TeleprompterPlayerUiState.MAX_FONT_SIZE
                )
            )
        }
    }

    fun onSpeedChange(speed: Float) {
        _playerState.update {
            it.copy(
                scrollSpeed = speed.coerceIn(
                    TeleprompterPlayerUiState.MIN_SPEED,
                    TeleprompterPlayerUiState.MAX_SPEED
                )
            )
        }
    }

    fun onToggleMirror() {
        _playerState.update { it.copy(isMirrorMode = !it.isMirrorMode) }
    }

    fun onToggleControls() {
        _playerState.update { it.copy(showControls = !it.showControls) }
    }

    fun onScrollProgressChange(progress: Float) {
        _playerState.update { it.copy(scrollProgress = progress.coerceIn(0f, 1f)) }
    }

    fun onResetPlayer() {
        _playerState.update { it.copy(scrollProgress = 0f, isPlaying = false) }
    }

    fun onPlayerBack() {
        _playerState.update { it.copy(isPlaying = false) }
        _currentPage.value = Page.EDITOR
    }

    fun onExitPlayer() {
        _playerState.update { it.copy(isPlaying = false) }
        _currentPage.value = Page.LIST
    }

    /** 整合的返回逻辑：播放→编辑→列表→退出 */
    fun handleBack() {
        when (_currentPage.value) {
            Page.PLAYER -> onPlayerBack()
            Page.EDITOR -> onEditorBack()
            Page.LIST -> onGoBack()
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            type: Screen.Teleprompter.Type?,
        ): TeleprompterComponent
    }

    private companion object {
        const val LOG_SOURCE = "UI:TeleprompterScreen"
    }
}
