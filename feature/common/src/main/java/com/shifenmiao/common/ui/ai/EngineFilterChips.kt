package com.shifenmiao.common.ui.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            val brandIcon = providerBrandIcon(engine.name)
            EngineFilterChip(
                text = engine.title,
                isSelected = selectedFilter == engine.identityKey(),
                onClick = { onFilterSelected(engine.identityKey()) },
                leadingIcon = brandIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = providerAccentColor(engine.name)
                                ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
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
    trailingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    GlassFilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                )
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                }
            }
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        selectedColor = MaterialTheme.colorScheme.primaryContainer,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f),
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        border = if (!isSelected) {
            BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        } else null,
    )
}

