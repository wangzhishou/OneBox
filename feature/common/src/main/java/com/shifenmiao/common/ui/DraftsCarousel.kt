package com.shifenmiao.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrafts

@Composable
fun DraftsCarousel(
    drafts: List<DataDraftEntity>,
    activeDraftId: Long?,
    title: String,
    collapseContentDescription: String,
    expandContentDescription: String,
    activeColor: Color,
    onDraftClick: (DataDraftEntity) -> Unit,
    onDeleteRequest: (DataDraftEntity) -> Unit,
    draftStatusText: @Composable (DataDraftEntity) -> String,
    draftStatusColor: @Composable (DataDraftEntity) -> Color,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(true) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDrafts,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = activeColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$title (${drafts.size})",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                    contentDescription = if (expanded) collapseContentDescription else expandContentDescription,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        AnimatedVisibility(visible = expanded) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                items(
                    items = drafts,
                    key = { it.id }
                ) { draft ->
                    DraftCard(
                        draft = draft,
                        statusText = draftStatusText(draft),
                        statusColor = draftStatusColor(draft),
                        isActive = draft.id == activeDraftId,
                        activeColor = activeColor,
                        onClick = { onDraftClick(draft) },
                        onDeleteRequest = { onDeleteRequest(draft) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftCard(
    draft: DataDraftEntity,
    statusText: String,
    statusColor: Color,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier
            .width(170.dp)
            .height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        containerAlpha = if (isActive) GlassStyle.Dense.backgroundAlpha else GlassStyle.Regular.backgroundAlpha,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 12.dp, top = 10.dp, end = 6.dp, bottom = 6.dp)
                .height(110.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(activeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) activeColor else statusColor,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = draft.title.ifEmpty { draft.description.take(30) },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = draft.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

