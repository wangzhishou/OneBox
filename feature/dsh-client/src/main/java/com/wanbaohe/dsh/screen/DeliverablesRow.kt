package com.wanbaohe.dsh.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R

/**
 * 轮末产出文件行(对齐 Flutter deliverables_row.dart):
 * 单行 lane,最多 6 个 chip(basename)+ 剩余计数 "+N 个文件";
 * chip 数据 = ChatNode.Deliverables.paths(本轮成功修改调用的 producedPaths,首见去重)。
 * 裁剪:web 的 fitProducedFiles 精确测量布局降级为单行横向滚动;chip 不可点
 * (host.openPath 是 loopback 特权方法,LAN 形态无意义,P6 后再接)。
 */

/** 单行 lane 最大 chip 数(web:至多 6 个,剩余计 '+N 个文件') */
private const val DeliverablesChipCap = 6

@Composable
fun DeliverablesRow(paths: List<String>, modifier: Modifier = Modifier) {
    if (paths.isEmpty()) return
    val shown = paths.take(DeliverablesChipCap)
    val more = paths.size - shown.size
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (path in shown) {
            Text(
                text = pathBasename(path),
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
        }
        if (more > 0) {
            Text(
                text = stringResource(R.string.dsh_deliverables_more, more),
                modifier = Modifier.heightIn(min = 44.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 路径 basename('/' 与 '\\' 都认);无分隔符原样返回 */
private fun pathBasename(path: String): String {
    val i = path.lastIndexOf('/')
    val j = path.lastIndexOf('\\')
    val k = maxOf(i, j)
    return if (k >= 0) path.substring(k + 1) else path
}
