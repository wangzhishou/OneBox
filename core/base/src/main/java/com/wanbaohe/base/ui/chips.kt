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

package com.wanbaohe.base.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.search.SuggestionModel
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.resources.icons.Close


@Composable
fun CancelableChip(
    modifier: Modifier = Modifier,
    suggestion: SuggestionModel,
    @DrawableRes drawableRes: Int = -1,
    onClick: ((SuggestionModel) -> Unit)? = null,
    onCancel: ((SuggestionModel) -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    val shape = MaterialTheme.shapes.large
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .glassRegular(
                shape = shape,
                color = backgroundColor,
            )
            .clickable {
                onClick?.run {
                    invoke(suggestion)
                }
            }
            .padding(
                vertical = 12.dp,
                horizontal = AppTheme.dimens.paddingNormal
            )
    ) {

        if (drawableRes != -1) {
            Image(
                painter = painterResource(drawableRes),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
                    .clip(CircleShape),
                contentDescription = null
            )
        }

        Text(
            text = suggestion.tag,
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(end = 12.dp)
                .widthIn(max = 240.dp)
                .wrapContentWidth(),
            color = textColor
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.paddingNormal))
        IconButton(
            onClick = {
                onCancel?.run {
                    invoke(suggestion)
                }
            },
            modifier = Modifier
                .size(16.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                tint = textColor,
                contentDescription = null
            )
        }
    }
}