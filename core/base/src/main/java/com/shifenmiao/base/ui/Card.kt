package com.shifenmiao.base.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.shifenmiao.base.ui.shapes.ChatBubbleShape
import com.shifenmiao.base.ui.shapes.LeftChatBubbleShape
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard

@Composable
fun CustomChatCard(
    isHuman: Boolean,
    showAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = cardShapeFor(isHuman, showAvatar)
    GlassCard(
        modifier = modifier.fillMaxWidth().clip(shape).clickable {
            onClick.invoke()
        },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHuman -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainer
            },
            contentColor = when {
                isHuman -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
            }
        ),
        content = content
    )
}

@Composable
fun cardShapeFor(isHuman: Boolean, showAvatar: Boolean): Shape {
    if (showAvatar) {
        return when {
            isHuman -> ChatBubbleShape()
            else -> LeftChatBubbleShape()
        }
    } else {
        val roundedCorners = MaterialTheme.shapes.medium
        return when {
            isHuman -> roundedCorners.copy(bottomEnd = CornerSize(0))
            else -> roundedCorners.copy(bottomStart = CornerSize(0))
        }
    }
}