/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
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

package com.shifenmiao.common.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle

enum class NativeEditorInsertKind(
    @StringRes val labelRes: Int,
) {
    Link(R.string.native_editor_insert_link),
    Image(R.string.native_editor_insert_image),
    Audio(R.string.native_editor_insert_audio),
    Video(R.string.native_editor_insert_video),
    ;

    val supportsLocalFile: Boolean
        get() = this != Link

    val pickerMimeTypes: Array<String>
        get() = when (this) {
            Link -> arrayOf("*/*")
            Image -> arrayOf("image/*")
            Audio -> arrayOf("audio/*")
            Video -> arrayOf("video/*")
        }

    val fallbackName: String
        get() = when (this) {
            Link -> "link"
            Image -> "image"
            Audio -> "audio"
            Video -> "video"
        }
}

/**
 * 插入链接 / 图片 / 音频 / 视频的浮动层。
 *
 * URL 可以直接粘贴；本地文件走 SAF，由 [EditorMediaStore] 立即复制到 App 私有目录，
 * 因此 Markdown 只保存稳定的 `onebox-media://` 地址，不依赖临时授权。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeEditorInsertSheet(
    kind: NativeEditorInsertKind?,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onInsertUrl: (kind: NativeEditorInsertKind, url: String, linkText: String) -> Unit,
    onPickLocalFile: (NativeEditorInsertKind) -> Unit,
) {
    if (kind == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var url by rememberSaveable(kind) { mutableStateOf("") }
    var linkText by rememberSaveable(kind) { mutableStateOf("") }

    LaunchedEffect(kind) {
        url = ""
        linkText = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = kind.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(
                        R.string.native_editor_insert_sheet_title,
                        stringResource(kind.labelRes),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            GlassOutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.native_editor_insert_url_label)) },
                placeholder = {
                    Text(
                        text = stringResource(
                            R.string.native_editor_insert_url_hint
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                singleLine = true,
                style = GlassStyle.Medium,
            )

            if (kind == NativeEditorInsertKind.Link) {
                GlassOutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(
                                R.string.native_editor_insert_link_text
                            )
                        )
                    },
                    singleLine = true,
                    style = GlassStyle.Medium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (kind.supportsLocalFile) {
                    TextButton(
                        onClick = { onPickLocalFile(kind) },
                        enabled = !isImporting,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                R.string.native_editor_insert_local_file
                            ),
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { onInsertUrl(kind, url.trim(), linkText.trim()) },
                    enabled = url.isNotBlank() && !isImporting,
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = stringResource(
                            R.string.native_editor_insert_action
                        )
                    )
                }
            }

            if (kind.supportsLocalFile) {
                Text(
                    text = stringResource(
                        R.string.native_editor_insert_local_hint
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NativeEditorInsertKind.icon() = when (this) {
    NativeEditorInsertKind.Link -> OneBoxIcons.Outlined.LineLink
    NativeEditorInsertKind.Image -> Icons.Outlined.Image
    NativeEditorInsertKind.Audio -> OneBoxIcons.Outlined.LineAudioFile
    NativeEditorInsertKind.Video -> Icons.Outlined.PlayCircle
}
