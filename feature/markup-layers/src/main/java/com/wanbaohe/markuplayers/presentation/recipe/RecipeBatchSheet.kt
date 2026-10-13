package com.wanbaohe.markuplayers.presentation.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.recipe.BatchProgress
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import com.wanbaohe.markuplayers.domain.recipe.BatchReport
import com.wanbaohe.markuplayers.domain.recipe.MarkupRecipe

@Composable
fun RecipeBatchSheet(
    visible: Boolean,
    recipes: List<MarkupRecipe>,
    onDismiss: () -> Unit,
    onSaveCurrentRecipe: (String) -> Unit,
    onApplyRecipe: (MarkupRecipe) -> Unit,
    onPickBatchImages: () -> Unit,
    onRunBatch: (MarkupRecipe) -> Unit,
    onCancelBatch: () -> Unit,
    selectedImageCount: Int = 0,
    progress: BatchProgress? = null,
    report: BatchReport? = null,
    error: String? = null,
    canSaveCurrentRecipe: Boolean = true,
    onDeleteRecipe: ((MarkupRecipe) -> Unit)? = null
) {
    var name by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedRecipe = recipes.firstOrNull { it.id == selectedId }
    val running = progress != null

    EnhancedModalBottomSheet(
        cancelable = !running,
        visible = visible,
        dragHandle = {},
        onDismiss = { if (!running) onDismiss() },
        confirmButton = {},
        title = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = onDismiss,
                enabled = !running,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.studio_recipe_title),
                onClose = { if (!running) onDismiss() },
                closeEnabled = !running
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.studio_recipe_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error != null) {
                    Text(
                        stringResource(R.string.studio_recipe_error, error),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                GlassOutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(MarkupRecipe.MAX_NAME_LENGTH) },
                    label = { Text(stringResource(R.string.studio_recipe_name)) },
                    singleLine = true,
                    enabled = !running && canSaveCurrentRecipe,
                    modifier = Modifier.fillMaxWidth()
                )
                ConfirmButton(
                    text = stringResource(R.string.studio_recipe_save_current),
                    enabled = !running && canSaveCurrentRecipe && name.isNotBlank(),
                    onClick = { onSaveCurrentRecipe(name.trim()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    stringResource(R.string.studio_recipe_library),
                    style = MaterialTheme.typography.titleMedium
                )
                if (recipes.isEmpty()) {
                    Text(
                        stringResource(R.string.studio_recipe_empty),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                recipes.forEach { recipe ->
                    val selected = recipe.id == selectedId
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .glassBackground(
                                style = GlassStyle.Regular,
                                shape = ShapeDefaults.large,
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !running) { selectedId = recipe.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                enabled = !running,
                                onClick = { selectedId = recipe.id }
                            )
                            Column(Modifier.weight(1f)) {
                                Text(recipe.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    stringResource(
                                        if (selected) R.string.studio_recipe_selected
                                        else R.string.studio_recipe_choose
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { onApplyRecipe(recipe) },
                                enabled = !running
                            ) {
                                Text(stringResource(R.string.studio_recipe_apply))
                            }
                            if (onDeleteRecipe != null) {
                                TextButton(
                                    onClick = { onDeleteRecipe(recipe) },
                                    enabled = !running
                                ) {
                                    Text(stringResource(R.string.studio_recipe_delete))
                                }
                            }
                        }
                    }
                }

                Text(
                    stringResource(R.string.studio_recipe_batch_images),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(
                    onClick = onPickBatchImages,
                    enabled = !running,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.studio_recipe_pick_images))
                }
                Text(stringResource(R.string.studio_recipe_images_selected, selectedImageCount))
                Text(
                    stringResource(R.string.studio_recipe_batch_local_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = {
                            if (progress.total > 0) {
                                (progress.completed.toFloat() / progress.total).coerceIn(0f, 1f)
                            } else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        stringResource(
                            R.string.studio_recipe_batch_progress, progress.completed, progress.total
                        )
                    )
                    progress.currentName?.let {
                        Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    CancelButton(
                        text = stringResource(R.string.studio_recipe_cancel_batch),
                        onClick = onCancelBatch,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ConfirmButton(
                        text = stringResource(R.string.studio_recipe_run_batch),
                        enabled = selectedRecipe != null && selectedImageCount > 0,
                        onClick = { selectedRecipe?.let(onRunBatch) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (report != null) {
                    BatchReportContent(report)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    )
}

@Composable
private fun BatchReportContent(report: BatchReport) {
    Text(
        stringResource(
            R.string.studio_recipe_batch_result, report.successes.size, report.failures.size
        ),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        if (report.cancelled) {
            stringResource(
                R.string.studio_recipe_batch_cancelled, report.completed, report.total
            )
        } else {
            stringResource(
                if (report.isComplete) R.string.studio_recipe_batch_done
                else R.string.studio_recipe_batch_partial
            )
        },
        style = MaterialTheme.typography.bodySmall
    )
    if (report.failures.isNotEmpty()) {
        Text(
            stringResource(R.string.studio_recipe_batch_failures),
            style = MaterialTheme.typography.titleSmall
        )
        report.failures.forEach { failure ->
            Text(
                stringResource(R.string.studio_recipe_failure_item, failure.uri, failure.error),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
