package com.wanbaohe.markdown.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.markdown.OneBoxMarkdownCodeBlockInfo

/**
 * 代码块默认复制按钮。文案走 core/resources 的 string/copy，
 * 已打包的每种语言都有译文，不使用高亮库里只分中/英的 Copy。
 */
@Composable
internal fun OneBoxMarkdownCopyHeader(info: OneBoxMarkdownCodeBlockInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, end = 8.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = stringResource(R.string.copy),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { Clipboard.copy(info.code) }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}
