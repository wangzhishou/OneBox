package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.resources.R as CoreR

/**
 * 底部弹层共享头部:居中标题 + 最右关闭按钮(关闭与标题同行垂直居中)。
 * 可配合 `dragHandle = {}` 替代 EnhancedModalBottomSheet 的默认拖动条,也可与默认拖动条共存。
 * 作为 sheetContent 的第一个子项(滚动 Column 之外)使用时保持固定不随内容滚动。
 * [showDragHandle]=true 时在标题上方内嵌紧凑拖动条(间距远小于默认 DragHandle
 * 上下各 22dp 的留白),此时宿主 Sheet 必须传 `dragHandle = {}` 抑制默认条。
 */
@Composable
fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeEnabled: Boolean = true,
    showDragHandle: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showDragHandle) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 2.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp)
            )
            EnhancedIconButton(
                onClick = onClose,
                enabled = closeEnabled,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(CoreR.string.close),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
