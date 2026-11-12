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

package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSheetHeader

/**
 * 底部弹层共享头部 — 已统一到 [EnhancedSheetHeader](样式基准:本模块「编辑文字」面板)。
 * 保留本包装以免各调用点立刻改 import;新代码请直接用 [EnhancedSheetHeader]。
 *
 * [showDragHandle] 默认 true:标题上方内嵌紧凑拖动条,宿主 Sheet 必须传
 * `dragHandle = {}` 抑制默认条。
 */
@Composable
fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeEnabled: Boolean = true,
    showDragHandle: Boolean = true,
) {
    EnhancedSheetHeader(
        title = title,
        onClose = onClose,
        modifier = modifier,
        closeEnabled = closeEnabled,
        showDragHandle = showDragHandle
    )
}
