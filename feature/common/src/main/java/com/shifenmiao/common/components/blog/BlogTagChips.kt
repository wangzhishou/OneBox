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

package com.shifenmiao.common.components.blog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.blog.Tag
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle

/**
 * 博客标签。没有标签时不占位。
 * 玩法卡片上传入 [onTagClick] 时，点 chip 与点卡片一样进入详情。
 */
@Composable
fun BlogTagChips(
    tags: List<Tag>,
    modifier: Modifier = Modifier,
    onTagClick: ((Tag) -> Unit)? = null,
) {
    val visible = tags.filter { it.name.isNotBlank() }
    if (visible.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        visible.forEach { tag ->
            BlogTagChip(
                name = tag.name,
                onClick = { onTagClick?.invoke(tag) },
            )
        }
    }
}

@Composable
private fun BlogTagChip(
    name: String,
    onClick: () -> Unit,
) {
    val container = MaterialTheme.colorScheme.primaryContainer
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    GlassFilterChip(
        selected = false,
        onClick = onClick,
        style = GlassStyle.Thin,
        glassContainerColor = container,
        glassBorderWidth = 0.dp,
        modifier = Modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = container,
            labelColor = content,
        ),
        label = {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = content,
                maxLines = 1,
            )
        },
    )
}
