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
 */

package com.wanbaohe.markuplayers.presentation.tools.filter

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.widget.filterStrip.FilterStrip
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.components.layerDisplayName
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

/**
 * 图片创作的滤镜条：公共 [FilterStrip] 的图片创作适配层。
 * 这里只负责把组件状态（作用目标、图层效果、光栅化确认）翻译成回调，
 * 缩略图渲染、选中态与「更多」入口都复用公共实现。
 */
@Composable
fun FilterPanel(
    component: MarkupLayersComponent,
    onOpenFullCatalog: () -> Unit,
    onOpenEffects: () -> Unit,
    onRequestRasterize: (layerId: String, filter: UiFilter<*>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = remember { UiFilter.sortedGroupedEntries.firstOrNull().orEmpty() }
    val targetLayer = component.selectedLayer
    val target = if (targetLayer != null) {
        stringResource(R.string.markup_target_current_layer, layerDisplayName(targetLayer, component.layers))
    } else {
        stringResource(R.string.markup_target_background)
    }

    FilterStrip(
        filters = filters,
        selectedFilter = component.targetFilter,
        activeFilterCount = component.targetEffects.size,
        transformation = { filter -> component.filterTransformation(filter) },
        onFilterSelected = { filter ->
            val vector = component.rasterizeRequiredLayer
            if (vector != null) onRequestRasterize(vector.id, filter)
            else component.selectFilter(filter)
        },
        onClearFilters = { component.selectFilter(null) },
        onOpenFullCatalog = onOpenFullCatalog,
        modifier = modifier,
        title = target,
        action = {
            TextButton(
                onClick = onOpenEffects,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.studio_effects_edit, component.targetEffects.size))
            }
        },
    )
}
