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

package com.wanbaohe.textcard.presentation.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.wanbaohe.textcard.domain.CustomTextCardTemplateStore
import com.wanbaohe.textcard.domain.StoredTextCardTemplate
import com.wanbaohe.textcard.domain.TextCardImportService
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
import com.wanbaohe.textcard.domain.model.TextCardSourceAnchor
import com.wanbaohe.textcard.domain.model.TextCardSourceSelection
import com.wanbaohe.textcard.domain.model.TextCardTemplate
import com.wanbaohe.textcard.domain.model.TextStyleSpan
import com.wanbaohe.textcard.domain.model.clearSourceRangeStyles
import com.wanbaohe.textcard.domain.model.normalizedAnchor
import com.wanbaohe.textcard.domain.model.pageFor
import com.wanbaohe.textcard.domain.model.replaceSourceFonts
import com.wanbaohe.textcard.domain.model.replaceSourceStyles
import com.wanbaohe.textcard.domain.model.replaceSourceText
import com.wanbaohe.textcard.domain.model.withAnnotations
import com.wanbaohe.textcard.domain.render.TextCardLayout
import com.wanbaohe.textcard.domain.render.TextCardLayoutEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

enum class EditorPanel {
    Content, Typography, Layout, Annotation
}

enum class TextCardProjectSaveStatus {
    Dirty, Saving, Saved, Failed
}

private data class TextCardHistoryEntry(
    val document: TextCardDocument,
    val anchor: TextCardSourceAnchor,
)

class TextCardComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialText: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val exportRenderer: TextCardExportRenderer,
    private val exportService: TextCardExportService,
    private val layoutEngine: TextCardLayoutEngine,
    private val paperRepository: TextCardPaperRepository,
    private val customCanvasStore: CustomCanvasStore,
    private val customTemplateStore: CustomTextCardTemplateStore,
    private val importService: TextCardImportService,
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

    private val _customTemplates = mutableStateOf<List<TextCardTemplate>>(emptyList())
    val customTemplates: List<TextCardTemplate> by _customTemplates
    val allTemplates: List<TextCardTemplate> get() = TextCardTemplate.all + customTemplates

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
    val isLayoutCurrent: Boolean
        get() = completedLayoutRevision == documentRevision && _canvasTextEditSession.value == null

    private val _pageCountChange = mutableStateOf<Pair<Int, Int>?>(null)
    val pageCountChange: Pair<Int, Int>? by _pageCountChange

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

    val projects = projectStore.projects

    private val _homeInputText = mutableStateOf(initialText)
    val homeInputText: String by _homeInputText

    private val _isProjectSaving = mutableStateOf(false)
    val isProjectSaving: Boolean by _isProjectSaving

    private val _lastSavedAt = mutableStateOf<Long?>(null)
    val lastSavedAt: Long? by _lastSavedAt

    private val _savedRevision = mutableStateOf(-1L)
    private val _savingRevision = mutableStateOf(-1L)
    private val _projectSaveError = mutableStateOf<String?>(null)
    val projectSaveStatus: TextCardProjectSaveStatus
        get() = when {
            _projectSaveError.value != null -> TextCardProjectSaveStatus.Failed
            _savingRevision.value == documentRevision -> TextCardProjectSaveStatus.Saving
            _savedRevision.value != documentRevision -> TextCardProjectSaveStatus.Dirty
            _lastSavedAt.value != null -> TextCardProjectSaveStatus.Saved
            else -> TextCardProjectSaveStatus.Dirty
        }

    private val _exportRequested = mutableStateOf(false)
    val exportRequested: Boolean by _exportRequested

    private val _isLoadingProject = mutableStateOf(false)
    val isLoadingProject: Boolean by _isLoadingProject

    private val _isSaving = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _isPreparingHandoff = mutableStateOf(false)
    val isPreparingHandoff: Boolean by _isPreparingHandoff

    private var projectId: String? = null
    private var projectName: String = ""
    private var documentRevision by mutableStateOf(0L)
    private var currentAnchor = TextCardSourceAnchor()
    private var completedLayoutRevision by mutableStateOf(-1L)
    private var layoutJob: Job? = null
    private var autosaveJob: Job? = null
    private var projectSaveJob: Job? = null
    private val projectWriteMutex = Mutex()
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

    private val history = DocumentHistory<TextCardHistoryEntry> { it }

    internal val annotationEditor = TextCardAnnotationEditor(
        document = { document },
        layout = { layout },
        currentPage = { currentPage },
        defaultText = { appContext.getString(R.string.textcard_new_block_content) },
        closeOtherEditing = {
            endContinuousEdit()
            endCanvasTextEdit()
            endTextEdit()
            _activePanel.value = null
            clearTextSelection()
        },
        snapshot = { history.snapshot(TextCardHistoryEntry(document, currentAnchor)) },
        mutate = { recordHistory, transform -> mutateDocument(recordHistory, transform) },
        beginContinuous = { beginContinuousEdit() },
        endContinuous = { endContinuousEdit() },
    )

    val textBlocks: List<TextBlock> get() = document.textBlocks
    val background: BackgroundSpec get() = document.background
    val backgroundOpacity: Float get() = document.backgroundOpacity
    val hasSelectedTextBlock: Boolean
        get() = selectedRole != null || annotationEditor.selectedTextBlock != null
    val selectedTextBlockId: String?
        get() = annotationEditor.selectedTextBlock?.id ?: selectedRole?.let(document::block)?.id
    val editingTextBlockId: String?
        get() = selectedTextBlockId.takeIf { contentEditSession }
    val longImageRequiredHeight: Int? get() = layout?.longImageRequiredHeight

    init {
        componentScope.launch {
            runCatching { projectStore.refresh() }
                .onFailure { it.makeLog("TextCardProjects") }
        }
        refreshCustomTemplates()
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

    fun updateHomeInputText(text: String) {
        _homeInputText.value = text
    }

    fun fillHomeWithSample() {
        _homeInputText.value = listOf(
            appContext.getString(R.string.textcard_default_title),
            appContext.getString(R.string.textcard_default_body)
        ).joinToString("\n")
    }

    fun importHomeText(uri: String) {
        if (_isLoadingProject.value) return
        componentScope.launch {
            _isLoadingProject.value = true
            runCatching { importService.readText(uri) }
                .onSuccess { _homeInputText.value = it }
                .onFailure(AppToastHost::showFailureToast)
            _isLoadingProject.value = false
        }
    }

    fun updateCanvas(canvas: CanvasSpec) {
        if (document.canvas == canvas) return
        endContinuousEdit()
        endCanvasTextEdit()
        mutateDocument { it.copy(canvas = canvas) }
    }

    fun updateCustomCanvas(width: Int, height: Int) {
        val safeWidth = width.coerceIn(256, 4096)
        val safeHeight = height.coerceIn(256, 4096)
        val custom = CanvasSpec.Custom(safeWidth, safeHeight)
        customCanvasStore.saveLastCustom(safeWidth, safeHeight)
        _lastCustomCanvas.value = custom
        updateCanvas(custom)
    }

    fun startEditing(initialText: String) {
        endContinuousEdit()
        annotationEditor.reset()
        _canvasTextEditSession.value = null
        _layout.value = null
        _layoutError.value = null
        val newDocument = TextCardDocument().updateBlock(TextCardRole.Body) {
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
        currentAnchor = TextCardSourceAnchor()
        documentRevision++
        completedLayoutRevision = -1L
        _pageCountChange.value = null
        _savedRevision.value = -1L
        _savingRevision.value = -1L
        _projectSaveError.value = null
        _lastSavedAt.value = null
        _exportRequested.value = false
        _currentPage.value = 0
        _selectedRole.value = null
        _pageSelection.value = null
        _activePanel.value = null
        _isEditing.value = true
        _contentResetVersion.update { it + 1 }
        registerChangesCleared()
        recomputeLayout(immediate = true)
        scheduleAutosave()
        loadRemotePapers()
    }

    fun openProject(id: String, openExport: Boolean = false) {
        if (_isLoadingProject.value) return
        endContinuousEdit()
        annotationEditor.reset()
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
                    currentAnchor = TextCardSourceAnchor()
                    documentRevision++
                    completedLayoutRevision = -1L
                    _pageCountChange.value = null
                    _savedRevision.value = documentRevision
                    _savingRevision.value = -1L
                    _projectSaveError.value = null
                    _lastSavedAt.value = stored.summary.updatedAt
                    _exportRequested.value = openExport
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

    fun duplicateProject(id: String) {
        val source = projects.value.firstOrNull { it.id == id } ?: return
        componentScope.launch {
            runCatching {
                projectStore.duplicate(
                    id = id,
                    newId = UUID.randomUUID().toString(),
                    newName = appContext.getString(R.string.textcard_project_name_copy, source.name)
                )
            }.onSuccess { projectStore.refresh() }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    fun consumeExportRequest() {
        _exportRequested.value = false
    }

    fun deleteProject(id: String) {
        componentScope.launch {
            runCatching { projectStore.delete(id) }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    fun closeEditor() {
        endContinuousEdit()
        annotationEditor.finish()
        endCanvasTextEdit(recompute = false)
        contentEditSession = false
        _activePanel.value = null
        _selectedRole.value = null
        _pageSelection.value = null
        saveProject {
            _isEditing.value = false
        }
    }

    fun selectRole(role: TextCardRole) {
        if (canvasTextEditSession?.role != role) endCanvasTextEdit()
        layout?.pages?.getOrNull(currentPage)?.segments?.firstOrNull { it.role == role }?.let {
            currentAnchor = TextCardSourceAnchor(role, it.sourceStart)
        }
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

    fun selectedTextBlock(): TextBlock? = annotationEditor.selectedTextBlock ?: selectedRole?.let(document::block)
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
        val safeCursor = cursor.coerceIn(0, segment.block.content.length)
        currentAnchor = TextCardSourceAnchor(
            role = segment.role,
            offset = segment.sourceStart + safeCursor
        )
        _selectedRole.value = segment.role
        _pageSelection.value = null
        _activePanel.value = null
        _currentPage.value = pageIndex.coerceAtLeast(0)
        _canvasTextEditSession.value = CanvasTextEditSession(
            id = ++nextCanvasTextEditSessionId,
            role = segment.role,
            pageIndex = pageIndex,
            sourceStart = segment.sourceStart,
            initialCursor = safeCursor,
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
        currentAnchor = TextCardSourceAnchor(
            role = session.role,
            offset = session.sourceStart + localStart
        )
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

    fun updateContentTextSelection(role: TextCardRole, start: Int) {
        if (!contentEditSession || selectedRole != role) return
        currentAnchor = TextCardSourceAnchor(
            role = role,
            offset = start.coerceIn(0, document.block(role).content.length)
        )
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
            history.snapshot(TextCardHistoryEntry(document, currentAnchor))
            contentSnapshotTaken = true
            refreshHistoryFlags()
        }
        mutateDocument(recordHistory = false) { document ->
            document.updateBlock(role) { it.copy(content = content, styleSpans = spans) }
        }
    }

    fun updateTextBlock(id: String, transform: (TextBlock) -> TextBlock) {
        if (annotationEditor.updateTextBlock(id, transform)) return
        val role = TextCardRole.entries.firstOrNull { document.block(it).id == id } ?: return
        mutateDocument { current -> current.updateBlock(role, transform) }
    }

    fun updateTextBlockTransform(id: String, transformed: TextBlock) {
        updateTextBlock(id) {
            it.copy(
                widthRatio = transformed.widthRatio.coerceIn(0.1f, 1f),
                widthManuallySet = transformed.widthManuallySet,
                offsetX = transformed.offsetX.coerceIn(-1f, 1f),
                offsetY = transformed.offsetY.coerceIn(-1f, 1f),
                scale = transformed.scale.coerceIn(0.1f, 10f),
                rotation = transformed.rotation
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

    fun saveCurrentTemplate(name: String) {
        val normalized = name.trim()
        if (normalized.isEmpty()) return
        val id = "custom_${UUID.randomUUID()}"
        val styleDocument = document.copy(
            title = document.title.copy(content = "", styleSpans = emptyList(), fontSpans = emptyList()),
            body = document.body.copy(content = "", styleSpans = emptyList(), fontSpans = emptyList()),
            author = document.author.copy(content = "", styleSpans = emptyList(), fontSpans = emptyList()),
            source = document.source.copy(content = "", styleSpans = emptyList(), fontSpans = emptyList()),
            templateId = id
        )
        val stored = StoredTextCardTemplate(id = id, name = normalized, document = styleDocument)
        componentScope.launch {
            runCatching { customTemplateStore.save(stored) }
                .onSuccess {
                    refreshCustomTemplates()
                    AppToastHost.showToast(R.string.textcard_template_saved)
                }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    fun deleteCustomTemplate(template: TextCardTemplate) {
        componentScope.launch {
            runCatching { customTemplateStore.delete(template.id) }
                .onSuccess {
                    if (pendingTemplate.id == template.id) {
                        _pendingTemplate.value = TextCardTemplate.all.first()
                    }
                    refreshCustomTemplates()
                }
                .onFailure(AppToastHost::showFailureToast)
        }
    }

    private fun refreshCustomTemplates() {
        componentScope.launch {
            runCatching { customTemplateStore.list() }
                .onSuccess { stored -> _customTemplates.value = stored.map(::toTemplate) }
                .onFailure { it.makeLog("TextCardCustomTemplates") }
        }
    }

    private fun toTemplate(stored: StoredTextCardTemplate): TextCardTemplate {
        fun TextBlock.withStyleOf(source: TextBlock): TextBlock = source.copy(
            id = id,
            content = content,
            styleSpans = styleSpans,
            fontSpans = fontSpans
        )
        return TextCardTemplate(
            id = stored.id,
            customTitle = stored.name,
            customDescription = appContext.getString(R.string.textcard_custom_template_desc),
            applyStyle = {
                copy(
                    canvas = stored.document.canvas,
                    background = stored.document.background,
                    backgroundOpacity = stored.document.backgroundOpacity,
                    title = title.withStyleOf(stored.document.title),
                    body = body.withStyleOf(stored.document.body),
                    author = author.withStyleOf(stored.document.author),
                    source = source.withStyleOf(stored.document.source),
                    templateId = stored.id
                )
            }
        )
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
    ) {
        mutateDocument(recordHistory = false) {
            it.copy(
                exportFormat = format,
                exportQuality = quality.coerceIn(10, 100)
            )
        }
    }

    fun setCurrentPage(index: Int, preservePageSelection: Boolean = false) {
        val currentLayout = layout ?: return
        val count = currentLayout.pages.size
        val target = index.coerceIn(0, (count - 1).coerceAtLeast(0))
        if (target != _currentPage.value) {
            endContinuousEdit()
            val hadCanvasSession = _canvasTextEditSession.value != null
            endCanvasTextEdit(recompute = false)
            if (!preservePageSelection) {
                clearPageSelection()
                currentLayout.pages.getOrNull(target)?.segments?.firstOrNull()?.let { segment ->
                    currentAnchor = TextCardSourceAnchor(
                        role = segment.role,
                        offset = segment.sourceStart
                    )
                }
            } else {
                pageSelection?.let { selection ->
                    currentAnchor = TextCardSourceAnchor(selection.role, selection.start)
                }
            }
            _currentPage.value = target
            if (hadCanvasSession) recomputeLayout(immediate = true)
        }
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
        if (panel != null && panel != EditorPanel.Typography && annotationEditor.isActive) {
            annotationEditor.finish()
        }
        _activePanel.value = panel
    }

    fun undo() {
        endContinuousEdit()
        endCanvasTextEdit(recompute = false)
        endTextEdit()
        annotationEditor.endTextEdit()
        val restored = history.undo(TextCardHistoryEntry(document, currentAnchor)) ?: return
        applyHistory(restored)
    }

    fun redo() {
        endContinuousEdit()
        endCanvasTextEdit(recompute = false)
        endTextEdit()
        annotationEditor.endTextEdit()
        val restored = history.redo(TextCardHistoryEntry(document, currentAnchor)) ?: return
        applyHistory(restored)
    }

    private fun applyHistory(restored: TextCardHistoryEntry) {
        _document.value = restored.document.copy(
            exportFormat = document.exportFormat,
            exportQuality = document.exportQuality
        )
        currentAnchor = document.normalizedAnchor(restored.anchor)
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
            history.snapshot(TextCardHistoryEntry(document, currentAnchor))
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
        val previousPageCount = layout?.pages?.size
        layoutJob = componentScope.launch {
            if (!immediate) delay(120)
            _layoutError.value = null
            _isLayoutComputing.value = true
            val result = runCatching {
                withContext(defaultDispatcher) { layoutEngine.layout(snapshot) }
            }
            if (revision == documentRevision && _canvasTextEditSession.value == null) {
                result.onSuccess { newLayout ->
                    val annotatedLayout = newLayout.withAnnotations(snapshot)
                    _layout.value = annotatedLayout
                    completedLayoutRevision = revision
                    currentAnchor = document.normalizedAnchor(currentAnchor)
                    _currentPage.value = annotatedLayout.pageFor(currentAnchor)
                    _pageCountChange.value = previousPageCount
                        ?.takeIf { it != annotatedLayout.pages.size }
                        ?.let { it to annotatedLayout.pages.size }
                    syncPageSelectionAfterLayout(annotatedLayout)
                }.onFailure { error ->
                    _layout.value = null
                    completedLayoutRevision = -1L
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
        if (!_isEditing.value) return
        autosaveJob = componentScope.launch {
            delay(900)
            autosaveJob = null
            saveProject()
        }
    }

    fun saveProject(onComplete: () -> Unit = {}) {
        if (!_isEditing.value) {
            onComplete()
            return
        }
        autosaveJob?.cancel()
        val id = projectId ?: return
        val snapshot = document
        val revision = documentRevision
        val name = projectName.ifBlank { snapshot.displayName() }
        val cachedLayout = layout?.takeIf { completedLayoutRevision == revision }
        projectSaveJob = componentScope.launch {
            projectWriteMutex.withLock {
                _isProjectSaving.value = true
                _savingRevision.value = revision
                try {
                    val thumbnail = cachedLayout?.pages?.firstOrNull()?.let { page ->
                        try {
                            withContext(defaultDispatcher) { exportRenderer.render(page) }
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (failure: Throwable) {
                            failure.makeLog("TextCardProjectThumbnail")
                            null
                        }
                    }
                    val durable = projectStore.save(
                        id = id,
                        name = name,
                        document = snapshot,
                        pageCount = cachedLayout?.pages?.size,
                        thumbnail = thumbnail
                    )
                    _savedRevision.value = revision
                    _projectSaveError.value = null
                    _lastSavedAt.value = System.currentTimeMillis()
                    if (revision == documentRevision) {
                        _document.value = durable
                        registerSave()
                        onComplete()
                    } else {
                        scheduleAutosave()
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    error.makeLog("TextCardProjectSave")
                    _projectSaveError.value = error.message
                        ?: appContext.getString(R.string.textcard_save_failed)
                    AppToastHost.showFailureToast(error)
                } finally {
                    _savingRevision.value = -1L
                    _isProjectSaving.value = false
                }
            }
        }
    }

    fun saveCard(currentPageOnly: Boolean = false) {
        if (_isSaving.value || !document.hasContent) return
        val snapshot = document
        val pageIndices = snapshot.exportPageIndices(currentPage, currentPageOnly)
        exportJob = componentScope.launch {
            _isSaving.value = true
            runCatching {
                val currentLayout = withContext(defaultDispatcher) {
                    layoutEngine.layout(snapshot).withAnnotations(snapshot)
                }
                exportService.export(snapshot, currentLayout, pageIndices)
            }.onSuccess { result ->
                result.saved.firstOrNull()?.let { first ->
                    runCatching {
                        imageSaveLogger.recordImageSave(
                            screenId = Screen.TextCard().id.toString(),
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
            }.onFailure(AppToastHost::showFailureToast)
            _isSaving.value = false
        }
    }

    fun shareCard(currentPageOnly: Boolean = false) {
        if (_isSaving.value || !document.hasContent) return
        val snapshot = document
        val pageIndices = snapshot.exportPageIndices(currentPage, currentPageOnly)
        exportJob = componentScope.launch {
            _isSaving.value = true
            runCatching {
                val currentLayout = withContext(defaultDispatcher) {
                    layoutEngine.layout(snapshot).withAnnotations(snapshot)
                }
                exportService.share(snapshot, currentLayout, pageIndices)
            }.onFailure {
                AppToastHost.showFailureToast(appContext.getString(R.string.textcard_share_failed))
            }
            _isSaving.value = false
        }
    }

    private fun TextCardDocument.exportPageIndices(
        currentPage: Int,
        currentPageOnly: Boolean,
    ): Set<Int>? = if (currentPageOnly && outputMode == TextCardOutputMode.Pages) {
        setOf(currentPage)
    } else null

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
            initialText: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): TextCardComponent
    }
}
