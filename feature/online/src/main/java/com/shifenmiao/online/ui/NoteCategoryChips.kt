/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.Check

/**
 * 笔记分类 chip 的唯一实现：保存/设置弹窗从这里取值，
 * 保证 chip 尺寸、内边距、圆角、选中态一致。
 */
internal val NoteCategoryChipShape = RoundedCornerShape(16.dp)
private val NoteCategoryChipPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
private val NoteCategoryBarVerticalPadding = 4.dp
private val NoteCategoryChipSpacing = 8.dp

@Composable
internal fun NoteCategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCheck: Boolean = selected,
) {
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        },
        leadingIcon = if (showCheck) {
            {
                Icon(
                    imageVector = OneBoxIcons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        } else {
            null
        },
        shape = NoteCategoryChipShape,
        contentPadding = NoteCategoryChipPadding,
        style = GlassStyle.Thin,
        // 玻璃关闭时 M3 FilterChip 默认是透明底，这里显式给中性底与强调色，
        // 保证两种主题下 chip 都有完整胶囊形，不出现「只剩文字」的样式。
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    )
}

/** 保存/设置弹窗里的「新增分类」chip。 */
@Composable
internal fun NoteAddCategoryChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassFilterChip(
        selected = false,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = stringResource(R.string.creation_meta_add_category),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
        shape = NoteCategoryChipShape,
        contentPadding = NoteCategoryChipPadding,
        style = GlassStyle.Thin,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    )
}
