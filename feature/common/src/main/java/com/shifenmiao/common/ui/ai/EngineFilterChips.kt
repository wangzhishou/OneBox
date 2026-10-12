package com.shifenmiao.common.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle

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
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
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

        items(items = allEngines.distinctBy { it.identityKey() }, key = { it.identityKey() }) { engine ->
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
                            tint = if (selectedFilter == engine.identityKey()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun EngineFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassFilterChip(
        modifier = modifier.height(34.dp),
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                leadingIcon?.invoke()
                Text(
                    text = text,
                    style = if (isSelected) {
                        MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    color = contentColor,
                )
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )
                }
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = AppTheme.colors.getFilterChipColors(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        style = if (isSelected) GlassStyle.Regular else GlassStyle.Thin,
        glassBorderWidth = if (isSelected) 1.dp else 0.9.dp,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
    )
}

