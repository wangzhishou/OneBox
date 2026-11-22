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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTableChart
import com.wanbaohe.richtext.editor.command.isListActive
import com.wanbaohe.richtext.editor.command.toggleList
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind
import com.wanbaohe.icons.SolidEditorIcons
import androidx.compose.material.icons.Icons as MaterialIcons
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons

/**
 * 工具坞的「插入」动作：列表/正文/代码块/图表/表格/任务列表/链接/图片/音频/视频。
 * 全部直接平铺在底部工具坞里，随按钮行一起横向滚动，不再收进「+」菜单。
 * 从 NativeMarkdownEditorActionBar 拆出来，避免单个文件函数过多。
 */
@Composable
internal fun insertActions(
    state: NativeMarkdownEditorState,
    onInsert: (NativeEditorInsertKind) -> Unit,
    solid: Boolean,
): List<NativeEditorDockAction> =
    insertContentActions(state = state, solid = solid) +
        insertTextAction(state = state, solid = solid) +
        insertRawBlockActions(state = state, solid = solid) +
        insertMediaActions(onInsert = onInsert, solid = solid)

@Composable
private fun insertTextAction(state: NativeMarkdownEditorState, solid: Boolean): NativeEditorDockAction = NativeEditorDockAction(
    id = "insert_text",
    label = stringResource(R.string.native_editor_insert_text),
    icon = if (solid) SolidEditorIcons.InsertText else MaterialIcons.Outlined.TextFields,
    onClick = { state.insertTextBlock() },
)

@Composable
private fun insertContentActions(state: NativeMarkdownEditorState, solid: Boolean): List<NativeEditorDockAction> {
    val scheme = rememberNativeMarkdownStyleScheme()
    val editor = state.richTextState()
    return listOf(
        NativeEditorDockAction(
            id = "list_bullet",
            label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_bullet_list),
            // "•" 在 19dp 的图标行里只有一个点，和两侧矢量图标完全不是一个量级；
            // 换成带缩进刻度的列表图标，尺寸才跟其它按钮一致。
            icon = if (solid) SolidEditorIcons.ListBulleted else MaterialIcons.AutoMirrored.Outlined.FormatListBulleted,
            selected = editor?.isListActive(scheme) == true,
            enabled = editor != null && state.mode == NativeMarkdownEditorMode.Edit,
            dividerBefore = true,
            onClick = { editor?.toggleList(scheme) },
        ),
    )
}

@Composable
private fun insertRawBlockActions(state: NativeMarkdownEditorState, solid: Boolean): List<NativeEditorDockAction> {
    val codeTemplate = stringResource(R.string.native_editor_template_code)
    val diagramTemplate = stringResource(R.string.native_editor_template_diagram)
    val tableTemplate = stringResource(R.string.native_editor_template_table)
    val taskListTemplate = stringResource(R.string.native_editor_template_task_list)
    return listOf(
        rawBlockAction(
            id = "insert_code",
            label = stringResource(R.string.native_editor_insert_code),
            icon = if (solid) SolidEditorIcons.CodeBlock else MaterialIcons.Outlined.Code,
            kind = MarkdownRawBlockKind.Code,
            template = codeTemplate,
            state = state,
        ),
        rawBlockAction(
            id = "insert_diagram",
            label = stringResource(R.string.native_editor_insert_diagram),
            icon = if (solid) SolidEditorIcons.Diagram else MaterialIcons.Outlined.AccountTree,
            kind = MarkdownRawBlockKind.Diagram,
            template = diagramTemplate,
            state = state,
        ),
        rawBlockAction(
            id = "insert_table",
            label = stringResource(R.string.native_editor_insert_table),
            icon = if (solid) SolidEditorIcons.Table else OneBoxIcons.Outlined.LineTableChart,
            kind = MarkdownRawBlockKind.Table,
            template = tableTemplate,
            state = state,
        ),
        rawBlockAction(
            id = "insert_task_list",
            label = stringResource(R.string.native_editor_insert_task_list),
            icon = if (solid) SolidEditorIcons.Tasks else OneBoxIcons.Outlined.LineCheckBoxBlank,
            kind = MarkdownRawBlockKind.TaskList,
            template = taskListTemplate,
            state = state,
        ),
    )
}

@Composable
private fun insertMediaActions(
    onInsert: (NativeEditorInsertKind) -> Unit,
    solid: Boolean,
): List<NativeEditorDockAction> = listOf(
    NativeEditorDockAction(
        id = "insert_link",
        label = stringResource(R.string.native_editor_insert_link),
        icon = if (solid) SolidEditorIcons.Link else OneBoxIcons.Outlined.LineLink,
        dividerBefore = true,
        onClick = { onInsert(NativeEditorInsertKind.Link) },
    ),
    NativeEditorDockAction(
        id = "insert_image",
        label = stringResource(R.string.native_editor_insert_image),
        icon = if (solid) SolidEditorIcons.Image else MaterialIcons.Outlined.Image,
        onClick = { onInsert(NativeEditorInsertKind.Image) },
    ),
    NativeEditorDockAction(
        id = "insert_audio",
        label = stringResource(R.string.native_editor_insert_audio),
        icon = if (solid) SolidEditorIcons.Audio else OneBoxIcons.Outlined.LineAudioFile,
        onClick = { onInsert(NativeEditorInsertKind.Audio) },
    ),
    NativeEditorDockAction(
        id = "insert_video",
        label = stringResource(R.string.native_editor_insert_video),
        icon = if (solid) SolidEditorIcons.Video else MaterialIcons.Outlined.PlayCircle,
        onClick = { onInsert(NativeEditorInsertKind.Video) },
    ),
)

/** 插入类原始块动作：同一套「插模板」逻辑，只有图标/标题/块类型不同。 */
@Suppress("LongParameterList") // 图标/标题/模板/块类型一一对应，拆对象没有收益
private fun rawBlockAction(
    id: String,
    label: String,
    icon: ImageVector,
    kind: MarkdownRawBlockKind,
    template: String,
    state: NativeMarkdownEditorState,
): NativeEditorDockAction = NativeEditorDockAction(
    id = id,
    label = label,
    icon = icon,
    onClick = { state.insertRawBlock(kind = kind, markdown = template) },
)
