/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState

@Composable
fun PreferenceItemsGrid(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    isGrid: Boolean = LocalSettingsState.current.groupOptionsByTypes,
    columns: Int = 2,
    spacing: Dp = AppTheme.dimens.spaceSmall
) {
    val isPortrait by isPortraitOrientationAsState()

    if (!isGrid && isPortrait) {
        Column(modifier = modifier) {
            items.forEachIndexed { index, item ->
                item()
                if (index < items.lastIndex) {
                    Spacer(modifier = Modifier.height(spacing))
                }
            }
        }
    } else {
        Column(modifier = modifier) {
            items.chunked(columns).forEachIndexed { rowIndex, rowItems ->
                Row {
                    rowItems.forEachIndexed { colIndex, item ->
                        Box(modifier = Modifier.weight(1f)) {
                            item()
                        }
                        if (colIndex < rowItems.lastIndex) {
                            Spacer(modifier = Modifier.width(spacing))
                        }
                    }
                }
                if (rowIndex < items.chunked(columns).lastIndex) {
                    Spacer(modifier = Modifier.height(spacing))
                }
            }
        }
    }
}

enum class PreferenceItemTheme { PRIMARY, SECONDARY, TERTIARY, SURFACE }

fun preferenceItemThemeForIndex(index: Int): PreferenceItemTheme = when (index % 4) {
    0 -> PreferenceItemTheme.PRIMARY
    1 -> PreferenceItemTheme.SECONDARY
    2 -> PreferenceItemTheme.TERTIARY
    else -> PreferenceItemTheme.SURFACE
}

@Composable
fun preferenceItemContainerColor(theme: PreferenceItemTheme): Color = when (theme) {
    PreferenceItemTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    PreferenceItemTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    PreferenceItemTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = AppTheme.dimens.containerAlpha)
    PreferenceItemTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = AppTheme.dimens.containerAlpha)
}

@Composable
fun preferenceItemContentColor(theme: PreferenceItemTheme): Color = when (theme) {
    PreferenceItemTheme.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    PreferenceItemTheme.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    PreferenceItemTheme.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    PreferenceItemTheme.SURFACE -> MaterialTheme.colorScheme.onSurface
}

@Composable
fun preferenceItemIconColor(theme: PreferenceItemTheme): Color = when (theme) {
    PreferenceItemTheme.PRIMARY -> MaterialTheme.colorScheme.primary
    PreferenceItemTheme.SECONDARY -> MaterialTheme.colorScheme.secondary
    PreferenceItemTheme.TERTIARY -> MaterialTheme.colorScheme.tertiary
    PreferenceItemTheme.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
}

data class TypeSelectionItem(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val icon: ImageVector
)

@Composable
fun TypeSelectionGrid(
    items: List<TypeSelectionItem>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isGrid: Boolean = LocalSettingsState.current.groupOptionsByTypes,
    columns: Int = 2,
    spacing: Dp = 16.dp
) {
    PreferenceItemsGrid(
        isGrid = isGrid,
        columns = columns,
        spacing = spacing,
        modifier = modifier,
        items = items.mapIndexed { index, item ->
            @Composable {
                TypeSelectionCard(
                    title = stringResource(item.title),
                    subtitle = stringResource(item.subtitle),
                    icon = item.icon,
                    index = index,
                    onClick = { onClick(index) }
                )
            }
        }
    )
}

@Composable
fun TypeSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = preferenceItemThemeForIndex(index)
    val containerColor = preferenceItemContainerColor(theme)
    val contentColor = preferenceItemContentColor(theme)
    val iconColor = preferenceItemIconColor(theme)
    val iconBgColor = when (theme) {
        PreferenceItemTheme.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        PreferenceItemTheme.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        PreferenceItemTheme.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
        PreferenceItemTheme.SURFACE -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
    }

    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    minLines = 1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    minLines = 3,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
