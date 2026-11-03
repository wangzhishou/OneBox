package com.wanbaohe.textcard.presentation.selection

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.TextCardProjectSummary
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.TextCardTemplate
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import java.text.DateFormat
import java.util.Date

@Composable
fun CanvasSelectContent(component: TextCardComponent) {
    var renameProject by remember { mutableStateOf<TextCardProjectSummary?>(null) }
    var deleteProject by remember { mutableStateOf<TextCardProjectSummary?>(null) }
    val clipboard = LocalClipboardManager.current
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { component.importHomeText(it.toString()) }
    }
    BaseScreen(
        title = stringResource(R.string.textcard_title),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        content = {
            val projects by component.projects.collectAsState()
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                if (projects.isNotEmpty()) {
                    key(projects) {
                        RecentProjects(
                            projects = projects,
                            onOpen = { component.openProject(it) },
                            onRename = { renameProject = it },
                            onDuplicate = component::duplicateProject,
                            onExport = { component.openProject(it, openExport = true) },
                            onDelete = { deleteProject = it }
                        )
                    }
                }
                GlassCard(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.textcard_home_hint),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.textcard_home_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        GlassOutlinedTextField(
                            value = component.homeInputText,
                            onValueChange = component::updateHomeInputText,
                            placeholder = {
                                Text(stringResource(R.string.textcard_home_input_hint))
                            },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HomeActionButton(
                                text = stringResource(R.string.textcard_try_sample),
                                icon = Icons.Outlined.AutoAwesome,
                                onClick = component::fillHomeWithSample
                            )
                            HomeActionButton(
                                text = stringResource(R.string.textcard_paste_clipboard),
                                icon = Icons.Outlined.ContentPaste,
                                onClick = {
                                    clipboard.getText()?.text?.let(component::updateHomeInputText)
                                }
                            )
                            HomeActionButton(
                                text = stringResource(R.string.textcard_import_text),
                                icon = Icons.Outlined.UploadFile,
                                onClick = {
                                    importLauncher.launch(
                                        arrayOf("text/plain", "text/markdown", "text/x-markdown")
                                    )
                                }
                            )
                        }
                    }
                }
            }
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                style = GlassStyle.Dense,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.textcard_create),
                        enable = component.homeInputText.isNotBlank(),
                        onClick = { component.startEditing(component.homeInputText) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    )

    renameProject?.let { project ->
        var name by remember(project.id) { mutableStateOf(project.name) }
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { renameProject = null },
            title = { Text(stringResource(R.string.textcard_rename_project)) },
            text = {
                GlassOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                ConfirmButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        component.renameProject(project.id, name)
                        renameProject = null
                    }
                )
            },
            dismissButton = { CancelButton(onClick = { renameProject = null }) }
        )
    }
    deleteProject?.let { project ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deleteProject = null },
            title = { Text(stringResource(R.string.textcard_delete_project)) },
            text = { Text(stringResource(R.string.textcard_delete_project_hint, project.name)) },
            confirmButton = {
                ConfirmButton(
                    text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.delete),
                    onClick = {
                        component.deleteProject(project.id)
                        deleteProject = null
                    }
                )
            },
            dismissButton = { CancelButton(onClick = { deleteProject = null }) }
        )
    }
    LoadingDialog(visible = component.isLoadingProject, canCancel = false)
}

@Composable
private fun HomeActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Composable
private fun RecentProjects(
    projects: List<TextCardProjectSummary>,
    onOpen: (String) -> Unit,
    onRename: (TextCardProjectSummary) -> Unit,
    onDuplicate: (String) -> Unit,
    onExport: (String) -> Unit,
    onDelete: (TextCardProjectSummary) -> Unit,
) {
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    Column {
        SectionTitle(stringResource(R.string.textcard_recent_projects))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                var menuExpanded by remember(project.id) { mutableStateOf(false) }
                GlassCard(
                    onClick = { onOpen(project.id) },
                    modifier = Modifier.width(184.dp)
                ) {
                    Column {
                        Picture(
                            model = project.thumbnailPath,
                            contentDescription = project.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        )
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = 4.dp
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.textcard_project_meta,
                                project.pageCount,
                                dateFormat.format(Date(project.updatedAt))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            IconButton(onClick = { onOpen(project.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(
                                        com.t8rin.imagetoolbox.core.resources.R.string.edit
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = stringResource(R.string.textcard_more_actions),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                EnhancedDropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.textcard_export_again)) },
                                        onClick = {
                                            menuExpanded = false
                                            onExport(project.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.textcard_duplicate_project)) },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicate(project.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.textcard_rename_project)) },
                                        onClick = {
                                            menuExpanded = false
                                            onRename(project)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.textcard_delete_project)) },
                                        onClick = {
                                            menuExpanded = false
                                            onDelete(project)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSection(
    selected: TextCardTemplate,
    onSelect: (TextCardTemplate) -> Unit,
    templates: List<TextCardTemplate> = TextCardTemplate.all,
) {
    SectionTitle(stringResource(R.string.textcard_templates))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        items(templates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                selected = template.id == selected.id,
                onClick = { onSelect(template) },
                modifier = Modifier.width(176.dp)
            )
        }
    }
}

@Composable
internal fun textCardTemplateTitle(template: TextCardTemplate): String =
    template.customTitle ?: template.titleRes?.let { stringResource(it) }.orEmpty()

@Composable
private fun textCardTemplateDescription(template: TextCardTemplate): String =
    template.customDescription ?: template.descRes?.let { stringResource(it) }.orEmpty()

@Composable
internal fun TemplateCard(
    template: TextCardTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    canvas: CanvasSpec? = null,
    document: TextCardDocument? = null,
    onDelete: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        GlassCard(
            onClick = onClick,
            colors = if (selected) {
                tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
            } else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                val sample = templatePreviewState(template, canvas, document)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                ) {
                    CardCanvasPreview(
                        state = sample,
                        cornerRadius = 10.dp,
                        interactive = false,
                        modifier = Modifier.height(152.dp)
                    )
                }
                Text(
                    text = textCardTemplateTitle(template),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = textCardTemplateDescription(template),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        onDelete?.let {
            IconButton(
                onClick = it,
                modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.textcard_delete_template),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun templatePreviewState(
    template: TextCardTemplate,
    canvas: CanvasSpec? = null,
    document: TextCardDocument? = null,
): TextCardRenderState {
    val styled = template.applyTo(document ?: TextCardDocument())
    val previewContent = if (document != null) {
        styled.textBlocks.map { it.content.trim() }.filter { it.isNotBlank() }.joinToString("\n\n")
    } else {
        "${stringResource(R.string.textcard_default_title)}\n${stringResource(R.string.textcard_default_body)}"
    }
    val body = styled.body.copy(
        content = previewContent.replace(TextCardDocument.MANUAL_PAGE_BREAK, '\n').take(180),
        baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
        widthManuallySet = true
    )
    return TextCardRenderState(
        canvas = canvas ?: styled.canvas,
        background = styled.background,
        backgroundOpacity = styled.backgroundOpacity,
        textBlocks = listOf(body)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun CustomSizeDialog(
    initial: CanvasSpec.Custom?,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var widthText by remember(initial) {
        mutableStateOf((initial?.customWidth ?: 1080).toString())
    }
    var heightText by remember(initial) {
        mutableStateOf((initial?.customHeight ?: 1440).toString())
    }
    val width = widthText.toIntOrNull()
    val height = heightText.toIntOrNull()
    val valid = width != null && height != null && width > 0 && height > 0
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = { Text(stringResource(R.string.textcard_custom_size_title)) },
        text = {
            Column {
                GlassOutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = it.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(R.string.textcard_canvas_width)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                GlassOutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(R.string.textcard_canvas_height)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Text(
                    text = stringResource(R.string.textcard_custom_size_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            ConfirmButton(
                enabled = valid,
                onClick = {
                    if (width != null && height != null) onConfirm(width, height)
                }
            )
        },
        dismissButton = { CancelButton(onClick = onDismiss) }
    )
}
