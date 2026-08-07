package com.shifenmiao.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.image.IconBox
import com.shifenmiao.base.utils.ColorBuilder
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground

@Composable
fun SearchMessageCard(
    id: Int,
    title: String,
    description: String,
    queryValue: String,
    tag: String,
    iconName: String? = null,
    onClick: (() -> Unit)? = null,
) {
    // Existing color calculations remain the same
    val colorTuple = ColorBuilder.getColorContainerById(id, null)
    val textColor = MaterialTheme.colorScheme.contentColorFor(colorTuple.primary)
    val highlightColor = MaterialTheme.colorScheme.onErrorContainer
    val highlightBackgroundColor = MaterialTheme.colorScheme.errorContainer
    val (backgroundColor, tagContainerColor) = remember(id) {
        Triple(
            colorTuple.primary,
            textColor,
            colorTuple.primary
        )
    }

    val highlightedDescription = remember(description, queryValue) {
        StringUtils.getHighlightedDescription(
            description,
            queryValue,
            highlightColor,
            highlightBackgroundColor
        )
    }

    val highlightedTitle = remember(title, queryValue) {
        StringUtils.getHighlightedDescription(
            title,
            queryValue,
            highlightColor,
            highlightBackgroundColor
        )
    }

    val shape = MaterialTheme.shapes.medium

    Column(
        modifier = Modifier
            .clip(shape) // Apply clip before combinedClickable
            .glassBackground(
                style = GlassStyle.Regular,
                shape = shape,
                color = backgroundColor,
            )
            .combinedClickable(
                // No need to specify indication - using default Material3 ripple
                onClick = { onClick?.invoke() }
            )
            .fillMaxWidth()
            .padding(AppTheme.dimens.paddingNormal)
    ) {
        // Rest of the component remains unchanged
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = AppTheme.dimens.spaceLarge,
                    top = 0.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            val firstCharacter = title.firstOrNull() ?: ' '
            IconBox(
                iconName = iconName,
                firstCharacter = firstCharacter.toString(),
                contentColor = textColor,
                containerColor = textColor.copy(0.1f)
            )
        }

        // Remaining component content...
        Text(
            text = highlightedTitle,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
        )
        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
        Text(
            text = highlightedDescription,
            textAlign = TextAlign.Left,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 3,
            minLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
        TagLabel(
            text = tag,
            containerColor = tagContainerColor
        )
    }
}

@Composable
private fun TagLabel(text: String, containerColor: Color) {
    Text(
        modifier = Modifier
            .background(
                color = containerColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusSmall)
            )
            .padding(
                horizontal = AppTheme.dimens.paddingSmall,
                vertical = 2.dp
            ),
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp
        ),
        color = containerColor
    )
}