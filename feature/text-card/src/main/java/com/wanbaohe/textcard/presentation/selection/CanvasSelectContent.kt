package com.wanbaohe.textcard.presentation.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.base.ui.button.PrimaryButton
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.TextCardProjectSummary
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.model.TextCardTemplate
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.presentation.editor.CardCanvasPreview
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import java.text.DateFormat
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit

@Composable
fun CanvasSelectContent(component: TextCardComponent) {
    var inputText by remember { mutableStateOf("") }
    var showCustomSizeDialog by remember { mutableStateOf(false) }
    var renameProject by remember { mutableStateOf<TextCardProjectSummary?>(null) }
    var deleteProject by remember { mutableStateOf<TextCardProjectSummary?>(null) }

    BaseScreen(
        title = stringResource(R.string.textcard_title),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                if (component.projects.isNotEmpty()) {
                    RecentProjects(
                        projects = component.projects,
                        onOpen = component::openProject,
                        onRename = { renameProject = it },
                        onDelete = { deleteProject = it }
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.textcard_home_hint),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    GlassOutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(stringResource(R.string.textcard_home_input_hint))
                        },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TemplateSection(
                    selected = component.pendingTemplate,
                    onSelect = component::selectPendingTemplate
                )
                CanvasSection(
                    component = component,
                    onCustomClick = { showCustomSizeDialog = true }
                )
            }
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                style = GlassStyle.Dense,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.textcard_create),
                        enable = inputText.isNotBlank(),
                        onClick = { component.startEditing(inputText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )

    if (showCustomSizeDialog) {
        CustomSizeDialog(
            initial = component.lastCustomCanvas,
            onConfirm = { width, height ->
                component.selectCustomCanvas(width, height)
                showCustomSizeDialog = false
            },
            onDismiss = { showCustomSizeDialog = false }
        )
    }
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
private fun RecentProjects(
    projects: List<TextCardProjectSummary>,
    onOpen: (String) -> Unit,
    onRename: (TextCardProjectSummary) -> Unit,
    onDelete: (TextCardProjectSummary) -> Unit,
) {
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    SectionTitle(stringResource(R.string.textcard_recent_projects))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(projects, key = { it.id }) { project ->
            GlassCard(
                onClick = { onOpen(project.id) },
                modifier = Modifier.width(176.dp)
            ) {
                Column {
                    Picture(
                        model = project.thumbnailPath,
                        contentDescription = project.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.textcard_project_meta,
                            project.pageCount,
                            dateFormat.format(Date(project.updatedAt))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { onOpen(project.id) }) {
                            Text(stringResource(R.string.textcard_continue_editing))
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onRename(project) }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.textcard_rename_project),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { onDelete(project) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.textcard_delete_project),
                                modifier = Modifier.size(18.dp)
                            )
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
) {
    SectionTitle(stringResource(R.string.textcard_templates))
    TextCardTemplate.all.chunked(2).forEach { rowTemplates ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            rowTemplates.forEach { template ->
                TemplateCard(
                    template = template,
                    selected = template.id == selected.id,
                    onClick = { onSelect(template) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (rowTemplates.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
internal fun TemplateCard(
    template: TextCardTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    canvas: CanvasSpec? = null,
    document: TextCardDocument? = null,
) {
    GlassCard(
        onClick = onClick,
        colors = if (selected) {
            tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
        } else null,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            val sample = templatePreviewState(template, canvas, document)
            CardCanvasPreview(
                state = sample,
                cornerRadius = 10.dp,
                interactive = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
            )
            Text(
                text = stringResource(template.titleRes),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(template.descRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
    val hasUserContent = document?.textBlocks?.any { it.content.isNotBlank() } == true
    val titleContent = if (hasUserContent) {
        styled.title.content.lineSequence().take(2).joinToString("\n").take(48)
    } else stringResource(R.string.textcard_default_title)
    val bodyContent = if (hasUserContent) {
        styled.body.content.take(140)
    } else stringResource(R.string.textcard_default_body)
    val title = styled.title.copy(
        content = titleContent,
        baseTopRatio = CardLayout.CONTENT_PADDING_RATIO,
        widthManuallySet = true
    )
    val body = styled.body.copy(
        content = bodyContent,
        baseTopRatio = CardLayout.BODY_BASE_TOP_RATIO,
        widthManuallySet = true
    )
    return TextCardRenderState(
        canvas = canvas ?: styled.canvas,
        background = styled.background,
        backgroundOpacity = styled.backgroundOpacity,
        textBlocks = listOf(title, body).filter { it.content.isNotBlank() }
    )
}

@Composable
private fun CanvasSection(
    component: TextCardComponent,
    onCustomClick: () -> Unit,
) {
    SectionTitle(stringResource(R.string.textcard_select_canvas_title))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        CanvasSpec.builtIn.forEach { spec ->
            GlassCard(
                onClick = { component.selectPendingCanvas(spec) },
                colors = if (component.pendingCanvas == spec) {
                    tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
                } else null,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(stringResource(spec.titleRes), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = spec.ratioLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        GlassCard(
            onClick = onCustomClick,
            colors = if (component.pendingCanvas is CanvasSpec.Custom) {
                tintedGlassCardColors(MaterialTheme.colorScheme.primaryContainer)
            } else null,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.textcard_canvas_custom),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = component.lastCustomCanvas?.ratioLabel ?: "+",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun CustomSizeDialog(
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
