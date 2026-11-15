package com.shifenmiao.common.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.wanbaohe.markdown.LocalOneBoxMarkdownCodeStyle
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
internal fun OneBoxMarkdownCodeBlockHeader(
    info: OneBoxMarkdownCodeBlockInfo,
    actions: OneBoxMarkdownCodeActions,
) {
    val style = LocalOneBoxMarkdownCodeStyle.current
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        color = style.surface,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (actions.canRun(info.language)) {
                IconButton(onClick = { actions.createWidget(info.language, info.code) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.markdown_create_widget),
                        tint = style.content,
                    )
                }
                IconButton(onClick = { actions.run(info.language, info.code) }) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = stringResource(R.string.markdown_run_code),
                        tint = style.content,
                    )
                }
            }
            if (actions.canSave(info.language)) {
                IconButton(onClick = { actions.save(info.language, info.code) }) {
                    Icon(
                        imageVector = Icons.Outlined.LineSave,
                        contentDescription = stringResource(R.string.markdown_save_code),
                        tint = style.content,
                    )
                }
            }
        }
    }
}
