package com.wanbaohe.textcard.presentation.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.common.creation.BackgroundHandoffBus
import com.shifenmiao.interfaces.logging.ImageSaveLogger
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.history.DocumentHistory
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.state.update
import com.wanbaohe.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.CustomCanvasStore
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.TextCardExportService
import com.wanbaohe.textcard.domain.TextCardPaperRepository
import com.wanbaohe.textcard.domain.TextCardProjectStore
import com.wanbaohe.textcard.domain.TextCardProjectSummary
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.RemotePaper
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardFontSpan
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSegment
import com.wanbaohe.textcard.domain.model.TextCardSourceSelection
import com.wanbaohe.textcard.domain.model.TextCardTemplate
import com.wanbaohe.textcard.domain.model.TextStyleSpan
import com.wanbaohe.textcard.domain.model.clearSourceRangeStyles
import com.wanbaohe.textcard.domain.model.replaceSourceFonts
import com.wanbaohe.textcard.domain.model.replaceSourceStyles
import com.wanbaohe.textcard.domain.model.replaceSourceText
import com.wanbaohe.textcard.domain.render.TextCardLayout
import com.wanbaohe.textcard.domain.render.TextCardLayoutEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class EditorPanel {
    Content, Templates, Typography, Background, Pages
}

class TextCardComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val exportRenderer: TextCardExportRenderer,
    private val exportService: TextCardExportService,
    private val layoutEngine: TextCardLayoutEngine,
    private val paperRepository: TextCardPaperRepository,
    private val customCanvasStore: CustomCanvasStore,
    private val projectStore: TextCardProjectStore,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val imageSaveLogger: ImageSaveLogger,
    private val backgroundHandoffBus: BackgroundHandoffBus,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _document = mutableStateOf(TextCardDocument())
    val document: TextCardDocument by _document

    private val _isEditing = mutableStateOf(false)
    val isEditing: Boolean by _isEditing

    private val _pendingCanvas = mutableStateOf<CanvasSpec>(CanvasSpec.Portrait)
    val pendingCanvas: CanvasSpec by _pendingCanvas
    private var pendingCanvasExplicitlySelected = false

    private val _pendingTemplate = mutableStateOf(TextCardTemplate.all.first())
    val pendingTemplate: TextCardTemplate by _pendingTemplate

    private val _lastCustomCanvas = mutableStateOf<CanvasSpec.Custom?>(
        customCanvasStore.lastCustom()?.let { (width, height) -> CanvasSpec.Custom(width, height) }
    )
    val lastCustomCanvas: CanvasSpec.Custom? by _lastCustomCanvas

    private val _layout = mutableStateOf<TextCardLayout?>(null)
    val layout: TextCardLayout? by _layout

    private val _layoutError = mutableStateOf<String?>(null)
    val layoutError: String? by _layoutError

    private val _isLayoutComputing = mutableStateOf(false)
    val isLayoutComputing: Boolean by _isLayoutComputing

    private val _currentPage = mutableStateOf(0)
    val currentPage: Int by _currentPage

    private val _activePanel = mutableStateOf<EditorPanel?>(null)
    val activePanel: EditorPanel? by _activePanel

    private val _selectedRole = mutableStateOf<TextCardRole?>(null)
    val selectedRole: TextCardRole? by _selectedRole

    private val _pageSelection = mutableStateOf<TextCardSourceSelection?>(null)
    val pageSelection: TextCardSourceSelection? by _pageSelection

    private val _canvasTextEditSession = mutableStateOf<CanvasTextEditSession?>(null)
    internal val canvasTextEditSession: CanvasTextEditSession? by _canvasTextEditSession

    private val _contentResetVersion = mutableStateOf(0)
    val contentResetVersion: Int by _contentResetVersion

    private val _canUndo = mutableStateOf(false)
    val canUndo: Boolean by _canUndo
    private val _canRedo = mutableStateOf(false)
    val canRedo: Boolean by _canRedo

    private val _projects = mutableStateOf<List<TextCardProjectSummary>>(emptyList())
    val projects: List<TextCardProjectSummary> by _projects

    private val _isProjectSaving = mutableStateOf(false)
    val isProjectSaving: Boolean by _isProjectSaving

    private val _isLoadingProject = mutableStateOf(false)
    val isLoadingProject: Boolean by _isLoadingProject

    private val _isSaving = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _isPreparingHandoff = mutableStateOf(false)
    val isPreparingHandoff: Boolean by _isPreparingHandoff

    private var projectId: String? = null
    private var projectName: String = ""
    private var documentRevision = 0L
    private var layoutJob: Job? = null
    private var autosaveJob: Job? = null
    private var projectSaveJob: Job? = null
    private var handoffJob: Job? = null
    private var activeHandoffId: String? = null
    private var exportJob: Job? by smartJob {
        _isSaving.update { false }
    }
    private var contentEditSession = false
    private var contentSnapshotTaken = false
    private var canvasTextSnapshotTaken = false
    private var nextCanvasTextEditSessionId = 0L
    private var continuousEditActive = false
    private var continuousEditSnapshotTaken = false

    private val history = DocumentHistory<TextCardDocument> { it }

    val textBlocks: List<TextBlock> get() = document.textBlocks
    val background: BackgroundSpec get() = document.background
    val backgroundOpacity: Float get() = document.backgroundOpacity
    val hasSelectedTextBlock: Boolean get() = selectedRole != null
    val selectedTextBlockId: String? get() = selectedRole?.let(document::block)?.id
    val editingTextBlockId: String?
        get() = selectedTextBlockId.takeIf { contentEditSession }
    val longImageRequiredHeight: Int? get() = layout?.longImageRequiredHeight

    init {
        componentScope.launch {
            projectStore.projects.collect { _projects.value = it }
        }
        componentScope.launch {
            runCatching { projectStore.refresh() }
                .onFailure { it.makeLog("TextCardProjects") }
        }
        componentScope.launch {
            backgroundHandoffBus.result.collect { result ->
                val requestId = activeHandoffId
                if (requestId != null && result?.id == requestId) {
                    val uri = backgroundHandoffBus.consumeResult(requestId)
                    activeHandoffId = null
                    if (uri != null) updateBackground(BackgroundSpec.Image(uri))
                }
            }
        }
        componentScope.launch {
            backgroundHandoffBus.request.collect { request ->
                if (request == null && backgroundHandoffBus.result.value == null) {
                    activeHandoffId = null
                }
            }
        }
        recomputeLayout(immediate = true)
        lifecycle.doOnStop {
            if (_isEditing.value) saveProject()
        }
    }

    override fun resetState() = Unit

    fun selectPendingCanvas(spec: CanvasSpec) {
        pendingCanvasExplicitlySelected = true
        _pendingCanvas.value = spec
    }

    fun selectPendingTemplate(template: TextCardTemplate) {
        _pendingTemplate.value = template
        if (!pendingCanvasExplicitlySelected) {
            _pendingCanvas.value = template.applyTo(TextCardDocument()).canvas
        }
    }

    fun selectCustomCanvas(width: Int, height: Int) {
        val safeWidth = width.coerceIn(256, 4096)
        val safeHeight = height.coerceIn(256, 4096)
        val custom = CanvasSpec.Custom(safeWidth, safeHeight)
        pendingCanvasExplicitlySelected = true
        customCanvasStore.saveLastCustom(safeWidth, safeHeight)
        _lastCustomCanvas.value = custom
        _pendingCanvas.value = custom
    }

    fun startEditing(initialText: String) {
        endContinuousEdit()
        _canvasTextEditSession.value = null
        _layout.value = null
        _layoutError.value = null
        val base = pendingTemplate.applyTo(TextCardDocument()).copy(canvas = pendingCanvas)
        val newDocument = base.updateBlock(TextCardRole.Body) {
            it.copy(content = initialText.trim())
        }
        projectId = UUID.randomUUID().toString()
        projectName = newDocument.displayName()
        history.clear()
        refreshHistoryFlags()
        contentEditSession = false
        contentSnapshotTaken = false
        canvasTextSnapshotTaken = false
        _document.value = newDocument
        _currentPage.value = 0
        _selectedRole.value = null
        _pageSelection.value = null
        _activePanel.value = null
        _isEditing.value = true
        _contentResetVersion.update { it + 1 }
        registerChangesCleared()
        recomputeLayout(immediate = true)
        loadRemotePapers()
    }

    fun openProject(id: String) {
        if (_isLoadingProject.value) return
        endContinuousEdit()
        _canvasTextEditSession.value = null
        _layout.value = null
        _layoutError.value = null
        componentScope.launch {
            _isLoadingProject.value = true
            runCatching { projectStore.load(id) }
                .onSuccess { stored ->
                    projectId = id
                    projectName = stored.summary.name
                    _document.value = stored.document
                    _currentPage.value = 0
                    _selectedRole.value = null
                    _pageSelection.value = null
                    _activePanel.value = null
                    _isEditing.value = true
                    _contentResetVersion.update { it + 1 }
                    history.clear()
                    refreshHistoryFlags()
                    contentEditSession = false
                    canvasTextSnapshotTaken = false
                    registerChangesCleared()
                    recomputeLayout(immediate = true)
                    loadRemotePapers()
                }
                .onFailure(AppToastHost::showFailureToast)
            _isLoadingProject.value = false
        }
    }

    fun renameProject(id: String, name: String) {
        val normalized = name.trim()
        if (normalized.isEmpty()) return
        componentScope.launch {
            runCatching { projectStore.rename(id, normalized) }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    fun deleteProject(id: String) {
        componentScope.launch {
            runCatching { projectStore.delete(id) }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    fun closeEditor() {
        endContinuousEdit()
        endCanvasTextEdit(recompute = false)
        contentEditSession = false
        _activePanel.value = null
        _selectedRole.value = null
        _pageSelection.value = null
        if (layoutError != null) {
            _isEditing.value = false
            return
        }
        if (document.hasContent) {
            saveProject {
                _isEditing.value = false
            }
        } else {
            deleteEmptyProject {
                _isEditing.value = false
            }
        }
    }

    fun selectRole(role: TextCardRole) {
        if (canvasTextEditSession?.role != role) endCanvasTextEdit()
        _selectedRole.value = role
        _pageSelection.value = null
    }

    fun clearTextSelection() {
        _selectedRole.value = null
        _pageSelection.value = null
    }

    fun selectTextBlock(id: String) {
        val role = TextCardRole.entries.firstOrNull { document.block(it).id == id } ?: return
        if (contentEditSession) beginTextEdit(id) else selectRole(role)
    }

    fun selectedTextBlock(): TextBlock? = selectedRole?.let(document::block)
    fun selectedFontTarget(): TextBlock? = selectedTextBlock()

    fun pageSelectionBlock(): TextBlock? = pageSelection?.let { selection ->
        canvasTextEditSession?.takeIf { it.role == selection.role }?.block
            ?: document.block(selection.role)
    }

    fun beginCanvasTextEdit(segment: TextCardSegment, pageIndex: Int, cursor: Int) {
        endCanvasTextEdit(recompute = false)
        layoutJob?.cancel()
        contentEditSession = false
        canvasTextSnapshotTaken = false
        _selectedRole.value = segment.role
        _pageSelection.value = null
        _activePanel.value = null
        _currentPage.value = pageIndex.coerceAtLeast(0)
        _canvasTextEditSession.value = CanvasTextEditSession(
            id = ++nextCanvasTextEditSessionId,
            role = segment.role,
            pageIndex = pageIndex,
            sourceStart = segment.sourceStart,
            initialCursor = cursor.coerceIn(0, segment.block.content.length),
            block = segment.block,
            previousText = segment.block.content
        )
    }

    fun endCanvasTextEdit(recompute: Boolean = true) {
        val hadSession = _canvasTextEditSession.value != null
        _canvasTextEditSession.value = null
        _pageSelection.value = null
        canvasTextSnapshotTaken = false
        if (hadSession && recompute) recomputeLayout(immediate = true)
    }

    fun updateCanvasTextSelection(sessionId: Long, start: Int, end: Int) {
        val session = _canvasTextEditSession.value?.takeIf { it.id == sessionId } ?: return
        val localStart = start.coerceIn(0, session.block.content.length)
        val localEnd = end.coerceIn(0, session.block.content.length)
        val from = minOf(localStart, localEnd)
        val to = maxOf(localStart, localEnd)
        _selectedRole.value = session.role
        _pageSelection.value = if (from < to) {
            TextCardSourceSelection(
                role = session.role,
                start = session.sourceStart + from,
                end = session.sourceStart + to
            )
        } else null
    }

    fun clearPageSelection() {
        _pageSelection.value = null
    }

    fun syncCanvasTextEdit(
        sessionId: Long,
        content: String,
        spans: List<TextStyleSpan>,
    ) {
        val session = _canvasTextEditSession.value?.takeIf { it.id == sessionId } ?: return
        if (session.previousText == content && session.block.styleSpans == spans) return

        val textChanged = session.previousText != content
        val prefix = session.previousText.commonPrefixWith(content).length
        val suffixCapacity = minOf(session.previousText.length, content.length) - prefix
        val suffix = if (textChanged) {
            session.previousText.commonSuffixWith(content).length.coerceAtMost(suffixCapacity)
        } else 0
        val oldLocalEnd = session.previousText.length - suffix
        val newLocalEnd = content.length - suffix
        val replacement = content.substring(prefix, newLocalEnd)
        val updatedLocalBlock = if (textChanged) {
            session.block.replaceSourceText(
                start = prefix,
                end = oldLocalEnd,
                replacement = replacement,
                insertedStyles = emptyList(),
                insertedFonts = replacementFonts(session.block, prefix, oldLocalEnd, replacement.length)
            )
        } else session.block
        val styledLocalBlock = updatedLocalBlock.replaceSourceStyles(0, content.length, spans)
        val sourceEnd = session.sourceStart + content.length

        mutateDocument(recordHistory = !textChanged || !canvasTextSnapshotTaken) { current ->
            current.updateBlock(session.role) { whole ->
                val updatedWhole = if (textChanged) {
                    val sourceChangeStart = session.sourceStart + prefix
                    val sourceChangeEnd = session.sourceStart + oldLocalEnd
                    whole.replaceSourceText(
                        start = sourceChangeStart,
                        end = sourceChangeEnd,
                        replacement = replacement,
                        insertedStyles = emptyList(),
                        insertedFonts = replacementFonts(
                            whole,
                            sourceChangeStart,
                            sourceChangeEnd,
                            replacement.length
                        )
                    )
                } else whole
                updatedWhole.replaceSourceStyles(
                    start = session.sourceStart,
                    end = sourceEnd,
                    inserted = spans.map { span ->
                        span.copy(
                            start = span.start + session.sourceStart,
                            end = span.end + session.sourceStart
                        )
                    }
                )
            }
        }
        if (textChanged) canvasTextSnapshotTaken = true
        _canvasTextEditSession.value = session.copy(
            block = styledLocalBlock,
            previousText = content
        )
    }

    private fun replacementFonts(
        block: TextBlock,
        start: Int,
        end: Int,
        replacementLength: Int,
    ): List<TextCardFontSpan> {
        if (replacementLength == 0) return emptyList()
        val override = if (start == end) {
            block.fontSpans.lastOrNull { start >= it.start && start < it.end }
        } else {
            block.fontSpans.lastOrNull { it.start < end && it.end > start }
        }
        return override?.let {
            listOf(TextCardFontSpan(start, start + replacementLength, it.font))
        }.orEmpty()
    }

    fun applyPageSelectionFont(font: FontType?) {
        val selection = _pageSelection.value ?: return
        mutateDocument { current ->
            current.updateBlock(selection.role) {
                it.replaceSourceFonts(
                    start = selection.start,
                    end = selection.end,
                    inserted = listOf(TextCardFontSpan(selection.start, selection.end, font))
                )
            }
        }
        _canvasTextEditSession.update { session ->
            session?.takeIf { it.role == selection.role }?.let { active ->
                val start = selection.start - active.sourceStart
                val end = selection.end - active.sourceStart
                active.copy(
                    block = active.block.replaceSourceFonts(
                        start = start,
                        end = end,
                        inserted = listOf(TextCardFontSpan(start, end, font))
                    )
                )
            }
        }
    }

    fun clearPageSelectionStyles() {
        val selection = _pageSelection.value ?: return
        mutateDocument { current ->
            current.updateBlock(selection.role) {
                it.clearSourceRangeStyles(selection.start, selection.end)
            }
        }
        _canvasTextEditSession.update { session ->
            session?.takeIf { it.role == selection.role }?.let { active ->
                active.copy(
                    block = active.block.clearSourceRangeStyles(
                        selection.start - active.sourceStart,
                        selection.end - active.sourceStart
                    )
                )
            }
        }
    }

    fun beginTextEdit(id: String) {
        val role = TextCardRole.entries.firstOrNull { document.block(it).id == id } ?: return
        if (_canvasTextEditSession.value != null) endCanvasTextEdit()
        if (contentEditSession && selectedRole != role) endTextEdit()
        _selectedRole.value = role
        _pageSelection.value = null
        if (!contentEditSession) {
            contentEditSession = true
            contentSnapshotTaken = false
        }
    }

    fun endTextEdit() {
        contentEditSession = false
    }

    fun syncEditingTextBlock(
        id: String,
        content: String,
        spans: List<TextStyleSpan>,
    ) {
        val role = TextCardRole.entries.firstOrNull { document.block(it).id == id } ?: return
        val current = document.block(role)
        if (current.content == content && current.styleSpans == spans) return
        if (!contentSnapshotTaken) {
            history.snapshot(document)
            contentSnapshotTaken = true
            refreshHistoryFlags()
        }
        mutateDocument(recordHistory = false) { document ->
            document.updateBlock(role) { it.copy(content = content, styleSpans = spans) }
        }
    }

    fun updateTextBlock(id: String, transform: (TextBlock) -> TextBlock) {
        val role = TextCardRole.entries.firstOrNull { document.block(it).id == id } ?: return
        mutateDocument { current -> current.updateBlock(role, transform) }
    }

    fun moveTextBlock(id: String, dx: Float, dy: Float) {
        beginContinuousEdit()
        updateTextBlock(id) {
            it.copy(
                offsetX = (it.offsetX + dx).coerceIn(-1f, 1f),
                offsetY = (it.offsetY + dy).coerceIn(-1f, 1f)
            )
        }
    }

    fun applyFont(font: FontType?) {
        selectedTextBlockId?.let { id -> updateTextBlock(id) { it.copy(font = font) } }
    }

    fun beginContinuousEdit() {
        if (!continuousEditActive) {
            continuousEditActive = true
            continuousEditSnapshotTaken = false
        }
    }

    fun endContinuousEdit() {
        continuousEditActive = false
        continuousEditSnapshotTaken = false
    }

    fun applyTemplate(template: TextCardTemplate) {
        endContinuousEdit()
        endCanvasTextEdit()
        val canvas = document.canvas
        mutateDocument { template.applyTo(it).copy(canvas = canvas) }
        _pendingTemplate.value = template
        _pendingCanvas.value = canvas
    }

    fun updateBackground(spec: BackgroundSpec) {
        endContinuousEdit()
        mutateDocument { it.copy(background = spec) }
    }

    fun updateBackgroundOpacity(opacity: Float) {
        beginContinuousEdit()
        mutateDocument { it.copy(backgroundOpacity = opacity.coerceIn(0f, 1f)) }
    }

    fun updateBackgroundImageOffset(dx: Float, dy: Float) {
        val image = document.background as? BackgroundSpec.Image ?: return
        beginContinuousEdit()
        mutateDocument { current ->
            current.copy(
                background = image.copy(
                    offsetX = (image.offsetX + dx).coerceIn(-0.075f, 0.075f),
                    offsetY = (image.offsetY + dy).coerceIn(-0.075f, 0.075f)
                )
            )
        }
    }

    fun updateOutputMode(mode: TextCardOutputMode) {
        endContinuousEdit()
        endCanvasTextEdit()
        mutateDocument { it.copy(outputMode = mode) }
    }

    fun updateShowPageNumber(show: Boolean) {
        mutateDocument { it.copy(showPageNumber = show) }
    }

    fun updateExportSettings(
        format: com.wanbaohe.textcard.domain.model.TextCardExportFormat = document.exportFormat,
        quality: Int = document.exportQuality,
        shareAfterSave: Boolean = document.shareAfterSave,
    ) {
        mutateDocument(recordHistory = false) {
            it.copy(
                exportFormat = format,
                exportQuality = quality.coerceIn(10, 100),
                shareAfterSave = shareAfterSave
            )
        }
    }

    fun setCurrentPage(index: Int, preservePageSelection: Boolean = false) {
        val count = layout?.pages?.size ?: return
        val target = index.coerceIn(0, (count - 1).coerceAtLeast(0))
        if (target != _currentPage.value) {
            endContinuousEdit()
            endCanvasTextEdit()
            if (!preservePageSelection) clearPageSelection()
        }
        _currentPage.value = target
    }

    fun previewState(): TextCardRenderState? {
        val currentLayout = layout ?: return null
        return if (document.outputMode == TextCardOutputMode.LongImage) {
            currentLayout.longImage ?: currentLayout.pages.getOrNull(currentPage)
        } else {
            currentLayout.pages.getOrNull(currentPage) ?: currentLayout.pages.firstOrNull()
        }
    }

    fun togglePanel(panel: EditorPanel) {
        _activePanel.update { if (it == panel) null else panel }
    }

    fun setActivePanel(panel: EditorPanel?) {
        _activePanel.value = panel
    }

    fun undo() {
        endContinuousEdit()
        endCanvasTextEdit(recompute = false)
        endTextEdit()
        val restored = history.undo(document) ?: return
        applyHistory(restored)
    }

    fun redo() {
        endContinuousEdit()
        endCanvasTextEdit(recompute = false)
        endTextEdit()
        val restored = history.redo(document) ?: return
        applyHistory(restored)
    }

    private fun applyHistory(restored: TextCardDocument) {
        _document.value = restored.copy(
            exportFormat = document.exportFormat,
            exportQuality = document.exportQuality,
            shareAfterSave = document.shareAfterSave
        )
        _pageSelection.value = null
        _contentResetVersion.update { it + 1 }
        documentRevision++
        refreshHistoryFlags()
        registerChanges()
        recomputeLayout(immediate = true)
        scheduleAutosave()
    }

    private fun mutateDocument(
        recordHistory: Boolean = true,
        transform: (TextCardDocument) -> TextCardDocument,
    ) {
        val updated = transform(document)
        if (updated == document) return
        val shouldSnapshot = when {
            !recordHistory -> false
            continuousEditActive -> !continuousEditSnapshotTaken
            else -> true
        }
        if (shouldSnapshot) {
            history.snapshot(document)
            if (continuousEditActive) continuousEditSnapshotTaken = true
        }
        _document.value = updated
        documentRevision++
        refreshHistoryFlags()
        registerChanges()
        if (_canvasTextEditSession.value == null) recomputeLayout()
        scheduleAutosave()
    }

    private fun refreshHistoryFlags() {
        _canUndo.value = history.canUndo
        _canRedo.value = history.canRedo
    }

    private fun recomputeLayout(immediate: Boolean = false) {
        layoutJob?.cancel()
        if (_canvasTextEditSession.value != null) return
        val snapshot = document
        val revision = documentRevision
        layoutJob = componentScope.launch {
            if (!immediate) delay(120)
            _layoutError.value = null
            _isLayoutComputing.value = true
            val result = runCatching {
                withContext(defaultDispatcher) { layoutEngine.layout(snapshot) }
            }
            if (revision == documentRevision && _canvasTextEditSession.value == null) {
                result.onSuccess { newLayout ->
                    _layout.value = newLayout
                    val lastPage = (newLayout.pages.size - 1).coerceAtLeast(0)
                    _currentPage.value = _currentPage.value.coerceIn(0, lastPage)
                    syncPageSelectionAfterLayout(newLayout)
                }.onFailure { error ->
                    _layout.value = null
                    _layoutError.value = error.message
                    error.makeLog("TextCardLayout")
                    AppToastHost.showFailureToast(error)
                }
                _isLayoutComputing.value = false
            }
        }
    }

    private fun syncPageSelectionAfterLayout(newLayout: TextCardLayout) {
        if (_canvasTextEditSession.value != null) return
        val selection = _pageSelection.value ?: return
        val index = newLayout.pages.indexOfFirst { state ->
            state.segments.any { segment ->
                segment.role == selection.role &&
                    segment.sourceStart < selection.end &&
                    segment.sourceEnd > selection.start
            }
        }
        if (index >= 0) {
            setCurrentPage(index, preservePageSelection = true)
        } else {
            clearPageSelection()
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        projectSaveJob?.cancel()
        if (!_isEditing.value) return
        autosaveJob = componentScope.launch {
            delay(900)
            autosaveJob = null
            if (document.hasContent) saveProject() else deleteEmptyProject()
        }
    }

    fun saveProject(onComplete: () -> Unit = {}) {
        if (!_isEditing.value) {
            onComplete()
            return
        }
        if (!document.hasContent) {
            deleteEmptyProject(onComplete)
            return
        }
        autosaveJob?.cancel()
        projectSaveJob?.cancel()
        val id = projectId ?: return
        val snapshot = document
        val revision = documentRevision
        val name = projectName.ifBlank { snapshot.displayName() }
        projectSaveJob = componentScope.launch {
            _isProjectSaving.value = true
            try {
                runCatching {
                    val layout = withContext(defaultDispatcher) { layoutEngine.layout(snapshot) }
                    val thumbnail = withContext(defaultDispatcher) {
                        exportRenderer.render(layout.pages.first())
                    }
                    projectStore.save(
                        id = id,
                        name = name,
                        document = snapshot,
                        pageCount = layout.pages.size,
                        thumbnail = thumbnail
                    )
                }.onSuccess { durable ->
                    if (revision == documentRevision) {
                        _document.value = durable
                        registerSave()
                    }
                    onComplete()
                }.onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    error.makeLog("TextCardProjectSave")
                    AppToastHost.showFailureToast(error)
                }
            } finally {
                _isProjectSaving.value = false
            }
        }
    }

    private fun deleteEmptyProject(onComplete: () -> Unit = {}) {
        if (document.hasContent) {
            saveProject(onComplete)
            return
        }
        val id = projectId ?: run {
            onComplete()
            return
        }
        autosaveJob?.cancel()
        projectSaveJob?.cancel()
        projectSaveJob = componentScope.launch {
            _isProjectSaving.value = true
            try {
                runCatching { projectStore.delete(id) }
                    .onSuccess { onComplete() }
                    .onFailure { error ->
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        error.makeLog("TextCardProjectDelete")
                        AppToastHost.showFailureToast(error)
                    }
            } finally {
                _isProjectSaving.value = false
            }
        }
    }

    fun saveCard() {
        if (_isSaving.value || !document.hasContent) return
        exportJob = componentScope.launch {
            _isSaving.value = true
            runCatching {
                val currentLayout = withContext(defaultDispatcher) {
                    layoutEngine.layout(document)
                }
                exportService.export(document, currentLayout)
            }.onSuccess { result ->
                result.saved.firstOrNull()?.let { first ->
                    runCatching {
                        imageSaveLogger.recordImageSave(
                            screenId = Screen.TextCard.id.toString(),
                            screenName = appContext.getString(
                                com.shifenmiao.core.R.string.text_card
                            ),
                            description = "Text card export: ${result.saved.size} page(s)",
                            fileUri = first.uri,
                            fileName = first.fileName,
                            savePath = first.savingPath
                        )
                    }
                }
                if (result.saved.isNotEmpty()) {
                    val first = result.saved.first()
                    AppToastHost.showFileSuccessToast(
                        uri = first.uri.takeIf(String::isNotBlank)?.toUri(),
                        message = if (result.saved.size == 1) {
                            SaveSuccessNotifier.successMessage(
                                fileName = first.fileName,
                                savingPath = first.savingPath
                            )
                        } else {
                            appContext.getString(
                                R.string.textcard_export_pages_success,
                                result.saved.size
                            )
                        }
                    )
                    AppToastHost.showConfetti()
                    registerSave()
                }
                if (result.failedPages.isNotEmpty()) {
                    AppToastHost.showToast(
                        message = appContext.getString(
                            R.string.textcard_export_partial_failure,
                            result.failedPages.size
                        ),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                        duration = ToastDuration.Long
                    )
                } else if (result.saved.isEmpty()) {
                    AppToastHost.showToast(
                        message = appContext.getString(R.string.textcard_save_failed),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                        duration = ToastDuration.Short
                    )
                }
                if (result.shareFailed) {
                    AppToastHost.showToast(
                        message = appContext.getString(
                            com.shifenmiao.core.R.string.webview_share_failed
                        ),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                        duration = ToastDuration.Long
                    )
                }
            }.onFailure(AppToastHost::showFailureToast)
            _isSaving.value = false
        }
    }

    fun cancelSaving() {
        exportJob = null
    }

    fun startBackgroundHandoff() {
        if (_isPreparingHandoff.value || activeHandoffId != null) return
        handoffJob?.cancel()
        handoffJob = componentScope.launch {
            _isPreparingHandoff.value = true
            runCatching {
                val state = withContext(defaultDispatcher) {
                    layoutEngine.layout(document).pages.first().copy(textBlocks = emptyList())
                }
                val rendered = withContext(defaultDispatcher) { exportRenderer.render(state) }
                shareProvider.cacheImage(
                    image = rendered,
                    imageInfo = ImageInfo(
                        width = rendered.width,
                        height = rendered.height,
                        imageFormat = com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat.Png.Lossless
                    )
                ) ?: error("Could not cache the background")
            }.onSuccess { uri ->
                val request = BackgroundHandoffBus.Request(
                    id = UUID.randomUUID().toString(),
                    width = document.canvas.width,
                    height = document.canvas.height
                )
                activeHandoffId = request.id
                backgroundHandoffBus.begin(request)
                onNavigate(Screen.MarkupLayers(uri = uri.toUri(), handoffId = request.id))
            }.onFailure {
                activeHandoffId = null
                AppToastHost.showFailureToast(it)
            }
            _isPreparingHandoff.value = false
        }
    }

    private val _remotePapers = mutableStateOf<List<RemotePaper>>(emptyList())
    val remotePapers: List<RemotePaper> by _remotePapers
    private var papersJob: Job? by smartJob()
    private val _remotePapersRefreshing = mutableStateOf(false)
    val remotePapersRefreshing: Boolean by _remotePapersRefreshing

    fun loadRemotePapers() {
        if (papersJob != null || _remotePapers.value.isNotEmpty()) return
        papersJob = componentScope.launch {
            _remotePapers.value = paperRepository.loadLocalPapers()
        }
    }

    fun refreshRemotePapers() {
        if (papersJob != null) return
        papersJob = componentScope.launch {
            _remotePapersRefreshing.value = true
            val papers = paperRepository.loadLocalPapers()
            if (papers.isNotEmpty()) _remotePapers.value = papers
            _remotePapersRefreshing.value = false
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): TextCardComponent
    }
}
