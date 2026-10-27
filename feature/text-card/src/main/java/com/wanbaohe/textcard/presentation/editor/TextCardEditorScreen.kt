@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.textcard.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.clearSelectionStyles
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.editor.EditorToolLabel
import com.wanbaohe.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.wanbaohe.core.ui.widget.glass.glassDense
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardSegment
import com.wanbaohe.textcard.domain.model.toTextStyleSpan
import com.wanbaohe.textcard.presentation.editor.panels.BackgroundPanel
import com.wanbaohe.textcard.presentation.editor.panels.ContentPanel
import com.wanbaohe.textcard.presentation.editor.panels.PageSelectionPanel
import com.wanbaohe.textcard.presentation.editor.panels.PagesPanel
import com.wanbaohe.textcard.presentation.editor.panels.TemplatesPanel
import com.wanbaohe.textcard.presentation.editor.panels.TextStylePanel
import com.wanbaohe.textcard.presentation.screenLogic.EditorPanel
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlinx.coroutines.flow.drop

@Composable
fun TextCardEditorScreen(component: TextCardComponent) {
    var showExportSheet by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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
    val refreshContentRole: (TextCardRole) -> Unit = { role ->
        val block = component.document.block(role)
        val state = fieldStates.getValue(role)
        val previousSelection = state.selection
        state.replaceWith(block, previousSelection)
    }
    val finishCanvasEditing = {
        val role = component.canvasTextEditSession?.role
        syncCanvasState()
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
        keyboardController?.hide()
        Unit
    }
    val onClearSelectionStyles = {
        val state = canvasFieldState
        if (state != null) {
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
    fieldStates.forEach { (role, state) ->
        LaunchedEffect(state) {
            snapshotFlow { state.text.toString() to state.modelStyles() }
                .drop(1)
                .collect { syncRole(role) }
        }
    }

    val closePanel = {
        component.endContinuousEdit()
        component.setActivePanel(null)
        if (component.canvasTextEditSession == null) {
            component.endTextEdit()
            component.clearPageSelection()
        }
        keyboardController?.hide()
    }
    val openPanel: (EditorPanel) -> Unit = { panel ->
        if (component.activePanel == panel) {
            closePanel()
        } else {
            component.endContinuousEdit()
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

    BaseScreen(
        title = stringResource(R.string.textcard_title),
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
            component.activePanel == null,
        actions = {
            IconButton(
                onClick = {
                    syncCanvasState()
                    component.undo()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                enabled = component.canUndo
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.textcard_undo)
                )
            }
            IconButton(
                onClick = {
                    syncCanvasState()
                    component.redo()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                enabled = component.canRedo
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.textcard_redo)
                )
            }
        },
        content = {
            BackHandler(
                enabled = component.canvasTextEditSession != null ||
                    component.editingTextBlockId != null ||
                    component.activePanel != null
            ) {
                when {
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
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val pages = component.layout?.pages.orEmpty()
                when {
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
                                    pageIndex = component.currentPage,
                                    onSegmentTap = { segment, cursor ->
                                        onSegmentTap(segment, component.currentPage, cursor)
                                    },
                                    onSegmentEdit = { segment, cursor ->
                                        onSegmentEdit(segment, component.currentPage, cursor)
                                    },
                                    onSegmentDrag = { segment, dx, dy ->
                                        component.moveTextBlock(
                                            component.document.block(segment.role).id,
                                            dx,
                                            dy
                                        )
                                    },
                                    onGestureEnd = component::endContinuousEdit,
                                    onCanvasTap = onCanvasTap,
                                    onBackgroundDrag = component::updateBackgroundImageOffset,
                                    onEditingSync = syncCanvasState,
                                    onTextEditCommit = onTextEditCommit,
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = canvasSession == null,
                                modifier = Modifier.fillMaxWidth()
                            ) { index ->
                                CardCanvasPreview(
                                    state = pages[index],
                                    liveDocument = component.document,
                                    selectedRole = component.selectedRole,
                                    editingSession = canvasSession,
                                    editingState = canvasFieldState,
                                    pageIndex = index,
                                    onSegmentTap = { segment, cursor ->
                                        onSegmentTap(segment, index, cursor)
                                    },
                                    onSegmentEdit = { segment, cursor ->
                                        onSegmentEdit(segment, index, cursor)
                                    },
                                    onSegmentDrag = { segment, dx, dy ->
                                        component.moveTextBlock(
                                            component.document.block(segment.role).id,
                                            dx,
                                            dy
                                        )
                                    },
                                    onGestureEnd = component::endContinuousEdit,
                                    onCanvasTap = onCanvasTap,
                                    onBackgroundDrag = component::updateBackgroundImageOffset,
                                    onEditingSync = syncCanvasState,
                                    onTextEditCommit = onTextEditCommit,
                                    onOpenStylePanel = onOpenStylePanel,
                                    onClearSelectionStyles = onClearSelectionStyles,
                                    selectionToolbarVisible = component.activePanel != EditorPanel.Typography,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                        }
                    }
                }
            }

            AnimatedVisibility(visible = component.activePanel != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassDense(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
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
                        EditorPanel.Templates -> TemplatesPanel(component)
                        EditorPanel.Typography -> {
                            val state = canvasFieldState
                            if (state != null && component.pageSelection != null) {
                                PageSelectionPanel(
                                    component = component,
                                    state = state,
                                    onClose = {
                                        state.edit {
                                            this.selection = TextRange(state.selection.end)
                                        }
                                        component.setActivePanel(null)
                                    },
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
                        EditorPanel.Background -> BackgroundPanel(component)
                        EditorPanel.Pages -> PagesPanel(component)
                        null -> Unit
                    }
                }
            }
            EditorBottomBar(
                component = component,
                onPanelClick = openPanel,
                onExportClick = {
                    finishCanvasEditing()
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showExportSheet = true
                }
            )
        }
    )

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
    onPanelClick: (EditorPanel) -> Unit,
    onExportClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val tabs = listOf(
                EditorPanel.Content,
                EditorPanel.Templates,
                EditorPanel.Background,
                EditorPanel.Pages
            )
            val itemWidth = maxOf(64.dp, maxWidth / tabs.size)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                tabs.forEach { panel ->
                    BottomTab(
                        panel = panel,
                        active = component.activePanel == panel,
                        onClick = { onPanelClick(panel) },
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
    EditorPanel.Templates -> Icons.Outlined.AutoAwesome
    EditorPanel.Typography -> Icons.Outlined.Tune
    EditorPanel.Background -> Icons.Outlined.CropSquare
    EditorPanel.Pages -> Icons.Outlined.Layers
}

private fun EditorPanel.labelRes(): Int = when (this) {
    EditorPanel.Content -> R.string.textcard_tab_content
    EditorPanel.Templates -> R.string.textcard_templates
    EditorPanel.Typography -> R.string.textcard_text_settings
    EditorPanel.Background -> R.string.textcard_tab_background
    EditorPanel.Pages -> R.string.textcard_tab_pages
}
