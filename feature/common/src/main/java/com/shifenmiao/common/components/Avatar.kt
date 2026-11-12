/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shifenmiao.core.R
import com.wanbaohe.base.ui.painter.TextPainter
import com.shifenmiao.base.utils.StringUtils
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAvatarDefault

@Composable
fun Avatar(
    username: String = "",
    nickname: String? = null,
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
    // 默认字母头像优先取昵称首字符(用户名可能是 openid 等乱码),昵称为空回退用户名
    val avatarTextString = remember(nickname, username) {
        val nameSource = nickname?.takeIf { it.isNotBlank() } ?: username
        if (nameSource.isEmpty()) {
            StringUtils.getRandomCharacterFromAppName()
        } else {
            StringUtils.getFirstCharacter(nameSource)
        }
    }
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    val avatarText = remember(avatarTextString, containerColor, textColor, size) {
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
            contentDescription = stringResource(R.string.profile_user_info_avatar),
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