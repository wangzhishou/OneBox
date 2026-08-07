package com.shifenmiao.ai.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEnergyLeaf

@Composable
internal fun AIModelSelectorChip(
    modifier: Modifier = Modifier,
    modelTitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEnergyLeaf,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = modelTitle,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
            contentDescription = stringResource(R.string.create_ai_common_model_selector),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
internal fun EditableMetaSection(
    modifier: Modifier = Modifier,
    title: String?,
    description: String?,
    defaultTitle: String,
    accentColor: Color,
    isEditing: Boolean,
    onToggleEditing: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
) {
    var draftTitle by remember { mutableStateOf(title.orEmpty()) }
    var draftDescription by remember { mutableStateOf(description.orEmpty()) }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            draftTitle = title.orEmpty()
            draftDescription = description.orEmpty()
        }
    }

    if (isEditing) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassOutlinedTextField(
                value = draftTitle,
                onValueChange = { draftTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.create_ai_common_title_label))
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = accentColor,
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )
            GlassOutlinedTextField(
                value = draftDescription,
                onValueChange = { draftDescription = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.create_ai_common_description_label))
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                minLines = 1,
                maxLines = 2,
                shape = MaterialTheme.shapes.small,
            )
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onToggleEditing,
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = {
                        onTitleChange(draftTitle)
                        onDescriptionChange(draftDescription)
                        onToggleEditing()
                    },
                    colors = AppTheme.colors.iconButtonColors(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.create_ai_common_finish_meta_edit),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.takeUnless { it.isNullOrBlank() } ?: defaultTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description.takeUnless { it.isNullOrBlank() }
                        ?: stringResource(R.string.empty_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onToggleEditing,
                colors = AppTheme.colors.iconButtonColors(),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.create_ai_common_enable_meta_edit),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

