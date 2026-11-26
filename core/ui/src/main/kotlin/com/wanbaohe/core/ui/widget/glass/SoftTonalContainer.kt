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
 * along with this program. If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.core.ui.widget.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * 按钮底色用的「摊薄」强调色。
 *
 * tertiaryContainer / secondaryContainer 这类实色角色直接塞进玻璃按钮,浅色主题下会被
 * 色字配对算法(GlassMaterial.chromaticContrastAlpha)一路抬到接近实色,一枚胶囊就把整页
 * 压重了,和旁边的浅卡片不在一个量级。往 surfaceContainer 摊薄后色相还在、底色轻很多;
 * 描边与高光仍由玻璃管线补,所以这里只调色,不烘 alpha。
 *
 * @param amount 向卡片底色靠拢的比例,0 = 原色,1 = 完全变卡片底色
 */
@Composable
fun softTonalContainer(
    color: Color,
    amount: Float = 0.40f,
): Color = lerp(
    start = color,
    stop = MaterialTheme.colorScheme.surfaceContainer,
    fraction = amount.coerceIn(0f, 1f),
)
