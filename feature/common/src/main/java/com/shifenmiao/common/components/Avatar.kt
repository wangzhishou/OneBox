package com.shifenmiao.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shifenmiao.base.ui.painter.TextPainter
import com.shifenmiao.base.utils.StringUtils
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAvatarDefault

@Composable
fun Avatar(
    username: String = "",
    avatar: String? = "",
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(50),
    isLogin: Boolean = true
) {
    if (!isLogin) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .glassBackground(color = MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAvatarDefault,
                contentDescription = null,
                modifier = Modifier.size(size * 0.55f),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        return
    }
    // ...existing code below (logged-in user avatar)...
    val avatarTextString = remember {
        if (username.isEmpty()) {
            StringUtils.getRandomCharacterFromAppName()
        } else {
            StringUtils.getFirstCharacter(username)
        }
    }
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    val avatarText = remember {
        TextPainter(
            backgroundColor = containerColor,
            textMeasurer = textMeasurer,
            text = avatarTextString,
            textColor = textColor,
            size = Size(size.value, size.value)
        )
    }
    if (avatar?.isNotEmpty() == true) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(avatar)
                .crossfade(true).build(),
            placeholder = avatarText,
            contentDescription = "avatar image",
            contentScale = ContentScale.FillWidth,
            modifier = modifier
                .size(size)
                .clip(shape)
                .fillMaxWidth()
        )
    } else {
        Image(
            modifier = modifier
                .size(size)
                .clip(shape),
            painter = avatarText,
            contentDescription = null
        )
    }
}