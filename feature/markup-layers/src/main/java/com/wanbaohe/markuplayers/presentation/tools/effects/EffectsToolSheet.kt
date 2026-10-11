package com.wanbaohe.markuplayers.presentation.tools.effects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.filters.presentation.widget.FilterItem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.components.EditorTargetSelector
import com.wanbaohe.markuplayers.presentation.components.LayerIconButton
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp

/** Effect parameters and strength edit the stack, not a flattened preview bitmap. */
@Composable
fun EffectsToolSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onDismiss: () -> Unit,
    onAddEffect: () -> Unit,
) {
    if (!visible) return
    val candidateLayerId = remember { component.selectedLayerId }
    val targetId = component.selectedLayerId
    val enabled = component.selectedLayer?.transform?.locked != true
    val effects = component.targetEffects

    LaunchedEffect(targetId) { component.endEffectsChange() }
    DisposableEffect(component, targetId) {
        onDispose { component.endEffectsChange() }
    }
    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {},
        onDismiss = {
            component.endEffectsChange()
            onDismiss()
        },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.studio_effects),
                onClose = {
                    component.endEffectsChange()
                    onDismiss()
                }
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                EditorTargetSelector(
                    component = component,
                    candidateLayerId = candidateLayerId
                )
                Text(
                    text = stringResource(R.string.studio_effects_order_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (component.rasterizeRequiredLayer != null) {
                    Text(
                        text = stringResource(R.string.studio_effects_vector_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (effects.isEmpty()) {
                    Text(
                        text = stringResource(R.string.studio_effects_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                effects.forEachIndexed { index, effect ->
                    key(targetId, index, effect.filter::class) {
                        var draggingStrength by remember { mutableStateOf(false) }
                        val controlsFilter = remember(effect.filter) {
                            effect.filter.copy(effect.filter.value).also { it.isVisible = true }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                LayerIconButton(
                                    icon = MaterialIcons.Outlined.KeyboardArrowUp,
                                    contentDescription = stringResource(R.string.studio_effects_move_up),
                                    enabled = enabled && index > 0,
                                    onClick = {
                                        component.endEffectsChange()
                                        component.moveEffect(index, -1)
                                    }
                                )
                                LayerIconButton(
                                    icon = MaterialIcons.Outlined.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.studio_effects_move_down),
                                    enabled = enabled && index < effects.lastIndex,
                                    onClick = {
                                        component.endEffectsChange()
                                        component.moveEffect(index, 1)
                                    }
                                )
                            }
                            FilterItem(
                                filter = controlsFilter,
                                showDragHandle = false,
                                onRemove = {
                                    if (enabled) {
                                        component.endEffectsChange()
                                        component.removeEffect(index)
                                    }
                                },
                                previewOnly = !enabled,
                                onFilterChange = { value ->
                                    if (enabled && index in component.targetEffects.indices) {
                                        component.replaceEffect(index, effect.filter.copy(value))
                                    }
                                },
                                onCreateTemplate = null,
                                canHide = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.studio_effects_strength),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.studio_percent,
                                        (effect.strength * 100).roundToInt()
                                    ),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            EnhancedSlider(
                                value = effect.strength,
                                enabled = enabled,
                                valueRange = 0f..1f,
                                onValueChange = {
                                    if (!draggingStrength) {
                                        component.endEffectsChange()
                                        component.beginEffectsChange()
                                        draggingStrength = true
                                    }
                                    if (index in component.targetEffects.indices) {
                                        component.updateEffectStrengthTransient(index, it)
                                    }
                                },
                                onValueChangeFinished = {
                                    draggingStrength = false
                                    component.endEffectsChange()
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            )
                        }
                    }
                }
                ConfirmButton(
                    text = stringResource(R.string.studio_effects_add),
                    enabled = enabled,
                    onClick = {
                        component.endEffectsChange()
                        onAddEffect()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                )
            }
        }
    )
}
