package com.shifenmiao.common.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

@Composable
internal fun OneBoxMarkdownCodeBlockHeader(
    info: OneBoxMarkdownCodeBlockInfo,
    listener: CodeBlockClickListener,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (listener.isLanguageRunnable(info.language)) {
            IconButton(onClick = { listener.onWidgetButtonClicked(info.language, info.code) }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.markdown_create_widget),
                )
            }
            IconButton(onClick = { listener.onRunButtonClicked(info.language, info.code) }) {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = stringResource(R.string.markdown_run_code),
                )
            }
        }
        if (listener.isLanguageSave(info.language)) {
            IconButton(onClick = { listener.onSaveButtonClicked(info.language, info.code) }) {
                Icon(
                    imageVector = Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.markdown_save_code),
                )
            }
        }
    }
}
