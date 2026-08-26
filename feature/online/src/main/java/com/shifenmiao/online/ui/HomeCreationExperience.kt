package com.shifenmiao.online.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin

@Composable
fun HomeEmptyState(
    listType: ListItemType,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    isFiltered: Boolean = false,
    onClearFilter: () -> Unit = {},
) {
    val typeName = listTypeDisplayName(listType)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .glassThin(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isFiltered) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
                } else listTypeIcon(listType),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(
                if (isFiltered) R.string.home_filter_empty_title else R.string.home_empty_title,
                typeName,
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (isFiltered) R.string.home_filter_empty_description
                else R.string.home_empty_description,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        if (isFiltered) {
            CreationActionCard(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                title = stringResource(R.string.home_clear_filter),
                description = stringResource(R.string.home_clear_filter_description),
                onClick = onClearFilter,
            )
        } else {
            CreationActionCard(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.home_create_manual),
                description = stringResource(R.string.home_create_manual_description, typeName),
                onClick = onManualCreate,
            )
            Spacer(Modifier.height(12.dp))
            CreationActionCard(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                title = stringResource(R.string.home_create_with_ai),
                description = stringResource(R.string.home_create_with_ai_description, typeName),
                onClick = onAiCreate,
                emphasized = true,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.home_created_content_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun CreateChoiceCard(
    listType: ListItemType,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeName = listTypeDisplayName(listType)
    val shape = MaterialTheme.shapes.extraLarge
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .glassThin(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            .clip(shape)
            .padding(12.dp),
    ) {
        val showActionIcons = maxWidth >= 160.dp
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.home_create_new_title, typeName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_create_new_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            CreationActionCard(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.home_create_manual),
                description = stringResource(R.string.home_create_manual_description, typeName),
                onClick = onManualCreate,
                compact = true,
                showIcon = showActionIcons,
            )
            Spacer(Modifier.height(8.dp))
            CreationActionCard(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                title = stringResource(R.string.home_create_with_ai),
                description = stringResource(R.string.home_create_with_ai_description, typeName),
                onClick = onAiCreate,
                emphasized = true,
                compact = true,
                showIcon = showActionIcons,
            )
        }
    }
}


@Composable
private fun CreationActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
    compact: Boolean = false,
    showIcon: Boolean = true,
) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 60.dp else 76.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .glassThin(
                shape = shape,
                color = if (emphasized) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 8.dp else 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon) {
            CreationIcon(
                icon = icon,
                emphasized = emphasized,
                modifier = Modifier.size(if (compact) 42.dp else 52.dp),
            )
            Spacer(Modifier.width(if (compact) 10.dp else 14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!compact) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CreationIcon(
    icon: ImageVector,
    emphasized: Boolean,
    modifier: Modifier = Modifier.size(52.dp),
) {
    Box(
        modifier = modifier.glassThin(
            shape = CircleShape,
            color = if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun listTypeDisplayName(listType: ListItemType): String = stringResource(
    when (listType) {
        ListItemType.NOTE -> R.string.record_tab_title
        ListItemType.NORMAL -> R.string.type_default
        ListItemType.AGENT -> R.string.type_agent
        ListItemType.PROMPT -> R.string.type_prompt
        ListItemType.HTML -> R.string.home_tab_web_title
        else -> R.string.placeholder_empty_title
    }
)

private fun listTypeIcon(listType: ListItemType): ImageVector = when (listType) {
    ListItemType.NOTE -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
    ListItemType.AGENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    ListItemType.PROMPT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt
    ListItemType.HTML -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor
    else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoAwesomeMosaic
}


