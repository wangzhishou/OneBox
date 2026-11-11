@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.textcard.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.clearSelectionStyles
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkupLayers
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.editor.EditorToolLabel
import com.wanbaohe.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.glassDense
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardAnnotationType
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSegment
import com.wanbaohe.textcard.domain.model.toTextStyleSpan
import com.wanbaohe.textcard.presentation.editor.panels.ContentPanel
import com.wanbaohe.textcard.presentation.editor.panels.ContentPanel
import com.wanbaohe.textcard.presentation.editor.panels.PagePanelSection
import com.wanbaohe.textcard.presentation.editor.panels.PageSelectionPanel
import com.wanbaohe.textcard.presentation.editor.panels.PagesPanel
import com.wanbaohe.textcard.presentation.editor.panels.TextStylePanel
import com.wanbaohe.textcard.presentation.screenLogic.EditorPanel
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import com.wanbaohe.textcard.presentation.screenLogic.TextCardProjectSaveStatus
import kotlinx.coroutines.flow.drop

@Composable
fun TextCardEditorScreen(component: TextCardComponent) {
    var showExportSheet by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(component.exportRequested) {
        if (component.exportRequested) {
            showExportSheet = true
            component.consumeExportRequest()
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboard = LocalClipboardManager.current
    val fieldStates = remember(component.contentResetVersion) {
        TextCardRole.entries.associateWith { role ->
            val block = component.document.block(role)
            TextFieldState(block.content, TextRange(block.content.length)).also { state ->
                block.styleSpans.forEach { span ->
                    span.clamped(block.content.length)?.let { clamped ->
                        state.edit { addStyle(clamped.toSpanStyle(), clamped.start, clamped.end) }
                    }
                }
            }
        }
    }
    val syncRole: (TextCardRole) -> Unit = { role ->
        val state = fieldStates.getValue(role)
        component.syncEditingTextBlock(
            id = component.document.block(role).id,
            content = state.text.toString(),
            spans = state.modelStyles()
        )
    }
    val canvasSession = component.canvasTextEditSession
    val canvasFontFamilies = canvasSession?.block?.fontSpans?.map {
        it.font.toUiFont().fontFamily
    }
    val canvasFieldState = remember(canvasSession?.id) {
        canvasSession?.let { session ->
            TextFieldState(
                initialText = session.block.content,
                initialSelection = TextRange(session.initialCursor)
            ).also { state ->
                session.block.styleSpans.forEach { span ->
                    span.clamped(session.block.content.length)?.let { clamped ->
                        state.edit { addStyle(clamped.toSpanStyle(), clamped.start, clamped.end) }
                    }
                }
                session.block.fontSpans.forEachIndexed { index, span ->
                    span.clamped(session.block.content.length)?.let { clamped ->
                        state.edit {
                            addStyle(
                                SpanStyle(fontFamily = canvasFontFamilies?.get(index)),
                                clamped.start,
                                clamped.end
                            )
                        }
                    }
                }
            }
        }
    }
    val syncCanvasState = {
        val session = component.canvasTextEditSession
        val state = canvasFieldState
        if (session != null && state != null) {
            component.syncCanvasTextEdit(
                sessionId = session.id,
                content = state.text.toString(),
                spans = state.modelStyles()
            )
        }
    }
    val annotationBlock = component.annotationEditor.textEditId?.let { id ->
        component.document.annotations.firstOrNull { it.id == id }
            ?.let { (it.type as? TextCardAnnotationType.Text)?.block }
    }
    val annotationFontFamilies = annotationBlock?.fontSpans?.map { it.font.toUiFont().fontFamily }
    val annotationFieldState = remember(component.annotationEditor.textEditId) {
        annotationBlock?.let { block ->
            TextFieldState(block.content, TextRange(block.content.length)).also { state ->
                block.styleSpans.forEach { span ->
                    span.clamped(block.content.length)?.let { clamped ->
                        state.edit { addStyle(clamped.toSpanStyle(), clamped.start, clamped.end) }
                    }
                }
                block.fontSpans.forEachIndexed { index, span ->
                    span.clamped(block.content.length)?.let { clamped ->
                        state.edit {
                            addStyle(
                                SpanStyle(fontFamily = annotationFontFamilies?.get(index)),
                                clamped.start,
                                clamped.end
                            )
                        }
                    }
                }
            }
        }
    }
    val syncAnnotationState = {
        val id = component.annotationEditor.textEditId
        val state = annotationFieldState
        if (id != null && state != null) {
            component.annotationEditor.syncTextEdit(
                id = id,
                content = state.text.toString(),
                spans = state.modelStyles()
            )
        }
    }
    val refreshContentRole: (TextCardRole) -> Unit = { role ->
        val block = component.document.block(role)
        val state = fieldStates.getValue(role)
        val previousSelection = state.selection
        state.replaceWith(block, previousSelection)
    }
    val finishCanvasEditing = {
        val role = component.canvasTextEditSession?.role
        syncCanvasState()
        syncAnnotationState()
        component.annotationEditor.endTextEdit()
        component.endCanvasTextEdit()
        if (role != null) refreshContentRole(role)
    }
    val onSegmentTap: (TextCardSegment, Int, Int) -> Unit = { segment, pageIndex, cursor ->
        if (component.canvasTextEditSession != null) finishCanvasEditing()
        if (component.selectedRole == segment.role) {
            component.beginCanvasTextEdit(segment, pageIndex, cursor)
        } else {
            component.selectRole(segment.role)
        }
    }
    val onSegmentEdit: (TextCardSegment, Int, Int) -> Unit = { segment, pageIndex, cursor ->
        if (component.canvasTextEditSession != null) finishCanvasEditing()
        component.beginCanvasTextEdit(segment, pageIndex, cursor)
    }
    val onCanvasTap = {
        finishCanvasEditing()
        component.endTextEdit()
        component.clearTextSelection()
        component.annotationEditor.select(null)
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }
    val onTextEditCommit = {
        finishCanvasEditing()
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }
    val onOpenStylePanel = {
        component.setActivePanel(EditorPanel.Typography)
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }
    val onClearSelectionStyles = {
        val annotationState = annotationFieldState
        val state = canvasFieldState
        if (annotationState != null) {
            component.beginContinuousEdit()
            annotationState.clearSelectionStyles(onChanged = syncAnnotationState)
            component.endContinuousEdit()
        } else if (state != null) {
            component.beginContinuousEdit()
            state.clearSelectionStyles(onChanged = syncCanvasState)
            component.clearPageSelectionStyles()
            component.endContinuousEdit()
        }
    }

    LaunchedEffect(canvasFieldState, canvasSession?.id) {
        val state = canvasFieldState ?: return@LaunchedEffect
        snapshotFlow { state.text.toString() to state.modelStyles() }
            .drop(1)
            .collect { syncCanvasState() }
    }
    LaunchedEffect(canvasFieldState, canvasSession?.id) {
        val state = canvasFieldState ?: return@LaunchedEffect
        val sessionId = canvasSession?.id ?: return@LaunchedEffect
        snapshotFlow { state.selection }.collect { selection ->
            component.updateCanvasTextSelection(
                sessionId = sessionId,
                start = selection.start,
                end = selection.end
            )
        }
    }
    LaunchedEffect(annotationFieldState, component.annotationEditor.textEditId) {
        val state = annotationFieldState ?: return@LaunchedEffect
        snapshotFlow { state.text.toString() to state.modelStyles() }
            .drop(1)
            .collect { syncAnnotationState() }
    }
    fieldStates.forEach { (role, state) ->
        LaunchedEffect(state) {
            snapshotFlow { state.text.toString() to state.modelStyles() }
                .drop(1)
                .collect { syncRole(role) }
        }
        LaunchedEffect(state) {
            snapshotFlow { state.selection }.collect { selection ->
                component.updateContentTextSelection(role, selection.start)
            }
        }
    }

    val closePanel = {
        component.endContinuousEdit()
        component.setActivePanel(null)
        if (component.canvasTextEditSession == null) {
            component.endTextEdit()
            component.clearPageSelection()
        }
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    val openPanel: (EditorPanel) -> Unit = { panel ->
        if (component.activePanel == panel) {
            closePanel()
        } else {
            component.endContinuousEdit()
            focusManager.clearFocus()
            keyboardController?.hide()
            if (panel != EditorPanel.Typography) {
                finishCanvasEditing()
                component.clearPageSelection()
            }
            if (panel != EditorPanel.Content && panel != EditorPanel.Typography) {
                component.endTextEdit()
            }
            component.setActivePanel(panel)
        }
    }

    val keyboardBottomPadding = with(androidx.compose.ui.platform.LocalDensity.current) {
        WindowInsets.ime.getBottom(this).toDp()
    }
    BaseScreen(
        modifier = Modifier.fillMaxSize().padding(bottom = keyboardBottomPadding),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.textcard_title))
                val status = component.projectSaveStatus
                Text(
                    text = stringResource(
                        when (status) {
                            TextCardProjectSaveStatus.Dirty -> R.string.textcard_unsaved_changes
                            TextCardProjectSaveStatus.Saving -> R.string.textcard_autosave_saving
                            TextCardProjectSaveStatus.Saved -> R.string.textcard_autosave_saved
                            TextCardProjectSaveStatus.Failed -> R.string.textcard_save_failed_retry
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (status == TextCardProjectSaveStatus.Failed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        },
        onGoBack = {
            finishCanvasEditing()
            component.closeEditor()
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        showNavigationBarsPadding = false,
        isBackHandler = component.canvasTextEditSession == null &&
            component.editingTextBlockId == null &&
            component.activePanel == null &&
            !component.annotationEditor.isActive,
        actions = {
            IconButton(
                onClick = {
                    val drawSession = component.annotationEditor.drawSession
                    if (drawSession != null) {
                        drawSession.undo()
                    } else {
                        syncCanvasState()
                        component.undo()
                    }
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                enabled = component.annotationEditor.drawSession?.canUndo ?: component.canUndo
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.textcard_undo)
                )
            }
            IconButton(
                onClick = {
                    val drawSession = component.annotationEditor.drawSession
                    if (drawSession != null) {
                        drawSession.redo()
                    } else {
                        syncCanvasState()
                        component.redo()
                    }
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                enabled = component.annotationEditor.drawSession?.canRedo ?: component.canRedo
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.textcard_redo)
                )
            }
            if (component.projectSaveStatus == TextCardProjectSaveStatus.Failed &&
                component.document.hasContent
            ) {
                IconButton(
                    onClick = {
                        val source = component.document.textBlocks
                            .map { it.content }
                            .filter { it.isNotBlank() }
                            .joinToString("\n\n")
                        clipboard.setText(AnnotatedString(source))
                        AppToastHost.showToast(R.string.textcard_source_copied)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.textcard_backup_source)
                    )
                }
            }
            IconButton(
                onClick = {
                    finishCanvasEditing()
                    component.saveProject()
                },
                enabled = component.projectSaveStatus != TextCardProjectSaveStatus.Saving
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.textcard_save_now)
                )
            }
        },
        content = {
            BackHandler(
                enabled = component.canvasTextEditSession != null ||
                    component.editingTextBlockId != null ||
                    component.activePanel != null ||
                    component.annotationEditor.isActive
            ) {
                when {
                    component.annotationEditor.panel != null -> component.annotationEditor.dismissPanel()
                    component.annotationEditor.textEditId != null -> onTextEditCommit()
                    component.annotationEditor.drawSession != null -> component.annotationEditor.cancelDrawing()
                    component.annotationEditor.isActive -> component.annotationEditor.finish()
                    component.activePanel != null -> closePanel()
                    component.canvasTextEditSession != null -> onTextEditCommit()
                    else -> {
                        component.endTextEdit()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val pages = component.layout?.pages.orEmpty()
                when {
                    component.document.outputMode == TextCardOutputMode.LongImage &&
                        component.document.annotations.isNotEmpty() -> {
                        if (pages.isEmpty()) {
                            LayoutStatus(component)
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                pages.forEachIndexed { index, page ->
                                    CardCanvasPreview(
                                        state = page,
                                        liveDocument = component.document,
                                        selectedRole = component.selectedRole,
                                        editingSession = canvasSession,
                                        editingState = canvasFieldState,
                                        annotationEditor = component.annotationEditor,
                                        annotationEditingState = annotationFieldState,
                                        annotationDrawSession = component.annotationEditor.drawSession.takeIf {
                                            component.currentPage == index
                                        },
                                        onAnnotationEditingSync = syncAnnotationState,
                                        pageIndex = index,
                                        onSegmentTap = { segment, cursor ->
                                            onSegmentTap(segment, index, cursor)
                                        },
                                        onSegmentEdit = { segment, cursor ->
                                            onSegmentEdit(segment, index, cursor)
                                        },
                                        onSegmentTransform = { segment, transformed ->
                                            component.updateTextBlockTransform(
                                                component.document.block(segment.role).id,
                                                transformed
                                            )
                                        },
                                        onGestureStart = component::beginContinuousEdit,
                                        onGestureEnd = component::endContinuousEdit,
                                        onCanvasTap = onCanvasTap,
                                        onBackgroundDrag = component::updateBackgroundImageOffset,
                                        onEditingSync = syncCanvasState,
                                        onOpenStylePanel = onOpenStylePanel,
                                        onClearSelectionStyles = onClearSelectionStyles,
                                        selectionToolbarVisible = component.activePanel != EditorPanel.Typography,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    component.document.outputMode == TextCardOutputMode.LongImage -> {
                        val state = component.previewState()
                        if (state == null) {
                            LayoutStatus(component)
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                CardCanvasPreview(
                                    state = state,
                                    liveDocument = component.document,
                                    selectedRole = component.selectedRole,
                                    editingSession = canvasSession,
                                    editingState = canvasFieldState,
                                    annotationEditor = component.annotationEditor,
                                    annotationEditingState = annotationFieldState,
                                    annotationDrawSession = component.annotationEditor.drawSession,
                                    onAnnotationEditingSync = syncAnnotationState,
                                    pageIndex = component.currentPage,
                                    onSegmentTap = { segment, cursor ->
                                        onSegmentTap(segment, component.currentPage, cursor)
                                    },
                                    onSegmentEdit = { segment, cursor ->
                                        onSegmentEdit(segment, component.currentPage, cursor)
                                    },
                                    onSegmentTransform = { segment, transformed ->
                                        component.updateTextBlockTransform(
                                            component.document.block(segment.role).id,
                                            transformed
                                        )
                                    },
                                    onGestureStart = component::beginContinuousEdit,
                                    onGestureEnd = component::endContinuousEdit,
                                    onCanvasTap = onCanvasTap,
                                    onBackgroundDrag = component::updateBackgroundImageOffset,
                                    onEditingSync = syncCanvasState,
                                    onOpenStylePanel = onOpenStylePanel,
                                    onClearSelectionStyles = onClearSelectionStyles,
                                    selectionToolbarVisible = component.activePanel != EditorPanel.Typography,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    pages.isEmpty() -> LayoutStatus(component)
                    else -> {
                        val pagerState = rememberPagerState(
                            initialPage = component.currentPage.coerceIn(0, pages.size - 1),
                            pageCount = { pages.size }
                        )
                        LaunchedEffect(pagerState.currentPage) {
                            component.setCurrentPage(pagerState.currentPage)
                        }
                        LaunchedEffect(component.currentPage, pages.size) {
                            if (pagerState.currentPage != component.currentPage) {
                                pagerState.scrollToPage(component.currentPage)
                            }
                        }
                        val previewMaxHeight = maxHeight - if (pages.size > 1) 28.dp else 0.dp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = canvasSession == null &&
                                    !component.annotationEditor.isActive,
                                modifier = Modifier.fillMaxWidth()
                            ) { index ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CardCanvasPreview(
                                        state = pages[index],
                                        liveDocument = component.document,
                                        selectedRole = component.selectedRole,
                                        editingSession = canvasSession,
                                        editingState = canvasFieldState,
                                        annotationEditor = component.annotationEditor,
                                        annotationEditingState = annotationFieldState,
                                        annotationDrawSession = component.annotationEditor.drawSession.takeIf {
                                            component.currentPage == index
                                        },
                                        onAnnotationEditingSync = syncAnnotationState,
                                        pageIndex = index,
                                        onSegmentTap = { segment, cursor ->
                                            onSegmentTap(segment, index, cursor)
                                        },
                                        onSegmentEdit = { segment, cursor ->
                                            onSegmentEdit(segment, index, cursor)
                                        },
                                        onSegmentTransform = { segment, transformed ->
                                            component.updateTextBlockTransform(
                                                component.document.block(segment.role).id,
                                                transformed
                                            )
                                        },
                                        onGestureStart = component::beginContinuousEdit,
                                        onGestureEnd = component::endContinuousEdit,
                                        onCanvasTap = onCanvasTap,
                                        onBackgroundDrag = component::updateBackgroundImageOffset,
                                        onEditingSync = syncCanvasState,
                                        onOpenStylePanel = onOpenStylePanel,
                                        onClearSelectionStyles = onClearSelectionStyles,
                                        selectionToolbarVisible = component.activePanel != EditorPanel.Typography,
                                        modifier = Modifier.heightIn(max = previewMaxHeight)
                                    )
                                }
                            }
                            if (pages.size > 1) {
                                Text(
                                    text = stringResource(
                                        R.string.textcard_page_indicator,
                                        pagerState.currentPage + 1,
                                        pages.size
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            if (component.isLayoutComputing) {
                                Text(
                                    text = stringResource(R.string.textcard_layout_updating),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            component.pageCountChange?.let { (before, after) ->
                                Text(
                                    text = stringResource(
                                        R.string.textcard_page_count_transition,
                                        before,
                                        after
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                if (component.annotationEditor.isActive) {
                    AnnotationToolRail(
                        editor = component.annotationEditor,
                        containerHeightPx = constraints.maxHeight.toFloat(),
                    )
                }
            }

            if (component.annotationEditor.isActive) {
                AnnotationBottomBar(
                    component = component,
                    onDoneText = onTextEditCommit,
                )
            } else {
                EditorBottomBar(
                    component = component,
                    isCanvasEditing = canvasSession != null,
                    onDoneEditing = onTextEditCommit,
                    onPanelClick = openPanel,
                    onExportClick = {
                        finishCanvasEditing()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        showExportSheet = true
                    }
                )
            }
        }
    )

    var pagePanelSection by rememberSaveable { mutableStateOf(PagePanelSection.Canvas) }
    val panelTitleRes = when (component.activePanel) {
        EditorPanel.Content -> R.string.textcard_tab_content
        EditorPanel.Typography -> if (component.pageSelection != null) {
            R.string.textcard_editing_selection
        } else R.string.textcard_text_settings
        EditorPanel.Layout -> R.string.textcard_layout_settings
        EditorPanel.Annotation -> R.string.textcard_tab_annotations
        null -> R.string.textcard_title
    }
    val dismissPanel: () -> Unit = {
        if (component.activePanel == EditorPanel.Typography && component.pageSelection != null) {
            canvasFieldState?.let { state ->
                state.edit { selection = TextRange(state.selection.end) }
            }
        }
        closePanel()
    }
    EnhancedModalBottomSheet(
        visible = component.activePanel != null,
        title = stringResource(panelTitleRes),
        onDismiss = { dismissPanel() },
        onClose = dismissPanel,
        nestedScrollEnabled = true,
        endContent = {
            if (component.activePanel == EditorPanel.Layout &&
                pagePanelSection == PagePanelSection.Background
            ) {
                IconButton(
                    onClick = component::refreshRemotePapers,
                    enabled = !component.remotePapersRefreshing
                ) {
                    if (component.remotePapersRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.textcard_refresh_papers),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 440.dp)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (component.activePanel) {
                EditorPanel.Content -> ContentPanel(
                    states = fieldStates,
                    selectedRole = component.selectedRole,
                    onFocusRole = { role ->
                        component.beginTextEdit(component.document.block(role).id)
                    }
                )
                EditorPanel.Typography -> {
                    val state = canvasFieldState
                    if (state != null && component.pageSelection != null) {
                        PageSelectionPanel(
                            component = component,
                            state = state,
                            onStylesChanged = syncCanvasState
                        )
                    } else {
                        val role = component.selectedRole
                        TextStylePanel(
                            component = component,
                            editingState = if (
                                component.editingTextBlockId != null && role != null
                            ) {
                                fieldStates.getValue(role)
                            } else null,
                            editingBlock = component.selectedTextBlock(),
                            onEditingSync = {
                                if (role != null) syncRole(role)
                            },
                            onWholeBlockStyleApplied = {
                                if (role != null) {
                                    val state = fieldStates.getValue(role)
                                    val spans = component.document.block(role).styleSpans
                                    state.edit {
                                        getSpanStyles(TextRange(0, state.text.length)).forEach {
                                            removeStyle(it)
                                        }
                                        spans.forEach { span ->
                                            span.clamped(state.text.length)?.let { clamped ->
                                                addStyle(
                                                    clamped.toSpanStyle(),
                                                    clamped.start,
                                                    clamped.end
                                                )
                                            }
                                        }
                                    }
                                    syncRole(role)
                                }
                            }
                        )
                    }
                }
                EditorPanel.Layout -> PagesPanel(
                    component = component,
                    section = pagePanelSection,
                    onSectionChange = { pagePanelSection = it },
                )
                EditorPanel.Annotation -> Unit
                null -> Unit
            }
        }
    }

    AnnotationPanels(component)

    TextCardExportSheet(
        visible = showExportSheet,
        component = component,
        onDismiss = { showExportSheet = false }
    )
    LoadingDialog(
        visible = component.isSaving || component.isPreparingHandoff,
        onCancelLoading = component::cancelSaving,
        canCancel = component.isSaving
    )
}

private fun TextFieldState.modelStyles() = textStyles
    .getSpanStyles(TextRange(0, text.length))
    .mapNotNull { it.toTextStyleSpan() }

private fun TextFieldState.replaceWith(block: TextBlock, previousSelection: TextRange) {
    edit {
        replace(0, length, block.content)
        block.styleSpans.forEach { span ->
            span.clamped(block.content.length)?.let { clamped ->
                addStyle(clamped.toSpanStyle(), clamped.start, clamped.end)
            }
        }
        selection = TextRange(
            previousSelection.start.coerceIn(0, block.content.length),
            previousSelection.end.coerceIn(0, block.content.length)
        )
    }
}

@Composable
private fun EditorBottomBar(
    component: TextCardComponent,
    isCanvasEditing: Boolean,
    onDoneEditing: () -> Unit,
    onPanelClick: (EditorPanel) -> Unit,
    onExportClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
    ) {
        if (isCanvasEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.textcard_canvas_editing_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.textcard_draw_done),
                    onClick = onDoneEditing
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val tabs = listOf(
                        EditorPanel.Content,
                        EditorPanel.Typography,
                        EditorPanel.Layout,
                        EditorPanel.Annotation,
                    )
                    val itemWidth = maxOf(56.dp, maxWidth / tabs.size)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        tabs.forEach { panel ->
                            BottomTab(
                                panel = panel,
                                active = if (panel == EditorPanel.Annotation) {
                                    component.annotationEditor.isActive
                                } else {
                                    component.activePanel == panel
                                },
                                onClick = {
                                    if (panel == EditorPanel.Annotation) {
                                        component.annotationEditor.toggle()
                                    } else {
                                        if (panel == EditorPanel.Typography && component.selectedRole == null) {
                                            component.selectRole(TextCardRole.Body)
                                        }
                                        onPanelClick(panel)
                                    }
                                },
                                modifier = Modifier.width(itemWidth)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                PrimaryButton(
                    text = stringResource(R.string.textcard_export_action),
                    onClick = onExportClick,
                    icon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.width(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomTab(
    panel: EditorPanel,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (active) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = panel.icon(),
            contentDescription = stringResource(panel.labelRes()),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(2.dp))
        EditorToolLabel(
            text = stringResource(panel.labelRes()),
            color = contentColor
        )
    }
}

@Composable
private fun LayoutStatus(component: TextCardComponent) {
    if (component.layoutError != null) {
        Text(
            text = stringResource(R.string.textcard_layout_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    } else {
        EnhancedLoadingIndicator()
    }
}

private fun EditorPanel.icon(): ImageVector = when (this) {
    EditorPanel.Content -> Icons.Outlined.TextFields
    EditorPanel.Typography -> Icons.Outlined.Tune
    EditorPanel.Layout -> Icons.Outlined.CropSquare
    EditorPanel.Annotation -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMarkupLayers
}

private fun EditorPanel.labelRes(): Int = when (this) {
    EditorPanel.Content -> R.string.textcard_tab_content
    EditorPanel.Typography -> R.string.textcard_text_settings
    EditorPanel.Layout -> R.string.textcard_tab_layout
    EditorPanel.Annotation -> R.string.textcard_tab_annotations
}
