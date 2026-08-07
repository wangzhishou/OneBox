package com.shifenmiao.common.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip

const val ENGINE_FILTER_ALL = "__all__"

@Composable
fun EngineFilterChips(
    allEngines: List<AiEngine>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    includeAllOption: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppTheme.dimens.spaceLarge),
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (includeAllOption) {
            item(key = ENGINE_FILTER_ALL) {
                EngineFilterChip(
                    text = stringResource(R.string.ai_filter_all),
                    isSelected = selectedFilter == ENGINE_FILTER_ALL,
                    onClick = { onFilterSelected(ENGINE_FILTER_ALL) }
                )
            }
        }

        items(items = allEngines, key = { it.identityKey() }) { engine ->
            EngineFilterChip(
                text = engine.title,
                isSelected = selectedFilter == engine.identityKey(),
                onClick = { onFilterSelected(engine.identityKey()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassFilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        selectedColor = MaterialTheme.colorScheme.primary,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
        glassSelectedContainerColor = MaterialTheme.colorScheme.primary,
        border = if (!isSelected) {
            BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        } else null,
    )
}

