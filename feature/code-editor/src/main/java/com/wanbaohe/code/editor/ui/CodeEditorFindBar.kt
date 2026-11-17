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

@file:Suppress("FunctionNaming")

package com.wanbaohe.code.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropUp
import com.wanbaohe.code.editor.R
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons as MaterialIcons
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons

/** 查找栏：查询词、命中计数、上一个/下一个、关闭。 */
@Composable
internal fun CodeEditorFindBar(state: CodeEditorState) {
    val focusRequester = remember { FocusRequester() }
    val total = state.findMatches.size
    val position = if (total == 0) {
        stringResource(R.string.code_editor_find_empty)
    } else {
        stringResource(
            R.string.code_editor_find_position,
            state.activeMatchIndex + 1,
            total,
        )
    }
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = GlassStyle.Thin,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FindBarQueryField(
                query = state.findQuery,
                onQueryChange = state::updateFindQuery,
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )
            FindBarNavigation(
                position = position,
                hasMatches = total > 0,
                onPrevious = state::findPrevious,
                onNext = state::findNext,
                onClose = state::closeFind,
            )
        }
    }
    LaunchedEffect(state.isFindVisible) {
        if (state.isFindVisible) {
            // 等这一帧布局完成再抢焦点，否则 FocusRequester 还没挂上
            delay(FOCUS_RETRY_DELAY_MS)
            runCatching { focusRequester.requestFocus() }
        }
    }
}

@Composable
private fun FindBarNavigation(
    position: String,
    hasMatches: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = position,
        style = MaterialTheme.typography.labelSmall,
        color = colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
    FindBarButton(
        icon = OneBoxIcons.Outlined.LineArrowDropUp,
        label = stringResource(R.string.code_editor_find_previous),
        enabled = hasMatches,
        onClick = onPrevious,
    )
    FindBarButton(
        icon = OneBoxIcons.Outlined.LineArrowDropDown,
        label = stringResource(R.string.code_editor_find_next),
        enabled = hasMatches,
        onClick = onNext,
    )
    FindBarButton(
        icon = MaterialIcons.Filled.KeyboardArrowDown,
        label = stringResource(R.string.code_editor_find_close),
        tint = colorScheme.onSurfaceVariant,
        onClick = onClose,
    )
}

@Composable
private fun FindBarQueryField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Search,
        ),
        decorationBox = { innerTextField ->
            Box {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.code_editor_find_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun FindBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
        )
    }
}

private const val FOCUS_RETRY_DELAY_MS = 80L
