package com.shifenmiao.base.ui.announcements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.BuildCustomIcon
import com.shifenmiao.model.common.AnnouncementItem
import kotlinx.coroutines.delay

@Composable
fun AnimatedTitleAnnouncements(
    modifier: Modifier = Modifier,
    items: List<AnnouncementItem>,
    onItemClick: (AnnouncementItem) -> Unit,
    displayTimeMs: Long = 5000,
    transitionDurationMs: Int = 800
) {
    // Filter out potentially problematic items
    val validItems = remember(items) {
        items.filter { item ->
            item.title.isNotEmpty() && item.iconName.isNotEmpty()
        }
    }

    if (validItems.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentItem = validItems[currentIndex]

    // Cycle through announcements
    LaunchedEffect(validItems) {
        while (validItems.isNotEmpty()) {
            val displayTime = currentItem.displayTimeMs.takeIf { it > 0 } ?: displayTimeMs
            delay(displayTime)
            currentIndex = (currentIndex + 1) % validItems.size
        }
    }

    // Animated content with cool transitions
    AnimatedContent(
        targetState = currentItem,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn(
                animationSpec = tween(
                    transitionDurationMs
                )
            ) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(transitionDurationMs, easing = EaseOut)
                    ))
                .togetherWith(slideOutVertically { height -> -height } + fadeOut(
                    animationSpec = tween(
                        transitionDurationMs / 2
                    )
                )
                ).using(
                    SizeTransform(clip = false)
                )
        }
    ) { item ->
        Row(
            modifier = modifier
                .clickable { onItemClick(item) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BuildCustomIcon(
                item.iconName,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Text
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}