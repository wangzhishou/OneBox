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

package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar

/**
 * 星级评分：横向一排星星，点击第 N 颗即评为 N 分（再点当前分值清零），
 * 结果以数字写入数据模型。
 */
class RatingRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Rating"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val max = (context.resolveInt(component.properties["max"]) ?: 5).coerceIn(1, 10)
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val label = context.resolveString(component.properties["label"])
        val value = (context.resolveFloat(valueDynamic)?.toInt() ?: 0).coerceIn(0, max)

        if (!label.isNullOrBlank()) {
            Text(text = if (value > 0) "$label: $value/$max" else label)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (index in 1..max) {
                val selected = index <= value
                Icon(
                    imageVector = if (selected) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Star
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
                    },
                    contentDescription = "$index",
                    tint = if (selected) StarColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(enabled = enabled) {
                            pointerPath?.let {
                                // 再点当前分值视为取消评分
                                context.updateDataModel(it, JsonPrimitive(if (index == value) 0 else index))
                            }
                        },
                )
            }
        }
    }

    private companion object {
        val StarColor = Color(0xFFFFC107)
    }
}
