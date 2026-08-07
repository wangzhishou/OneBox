package com.shifenmiao.marktodo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight

/**
 * A composable that displays the header section of a category card.
 *
 * This header shows the category icon, title, progress count, and a navigation arrow.
 *
 * @param title The category title to display.
 * @param icon The category icon.
 * @param completedCount Number of completed tasks in this category.
 * @param totalCount Total number of tasks in this category.
 * @param contentColor Text and icon color for the header.
 * @param modifier Optional modifier for styling the component.
 */
@Composable
fun CategoryHeader(
    title: String,
    icon: ImageVector,
    completedCount: Int,
    totalCount: Int,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Icon and title
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }

        // Right side: Progress count and arrow
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.task_progress_format, completedCount, totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.9f)
            )
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

