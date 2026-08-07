package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * 测速历史记录底部弹出层
 *
 * @param visible    是否显示
 * @param records    历史列表
 * @param onClear    清除所有记录回调
 * @param onDismiss  关闭回调
 */
@Composable
fun SpeedHistorySheet(
    visible: Boolean,
    records: List<SpeedTestRecord>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speed_test_history_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.getPrimaryTextColor(),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    colors = AppTheme.colors.iconButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.speed_test_close_desc),
                        tint = AppTheme.colors.getOnInactiveContainerColor()
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // ── 表头 ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassThin(
                        color = AppTheme.colors.getContainerSurfaceColor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speed_test_history_network_col),
                    fontSize = 13.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor(),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.speed_test_history_download_col),
                    fontSize = 13.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor()
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── 历史列表 ─────────────────────────────────────────────────
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .glassThin(
                            color = AppTheme.colors.getContainerSurfaceColor(),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.speed_test_history_empty),
                        fontSize = 14.sp,
                        color = AppTheme.colors.getOnInactiveContainerColor()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(records, key = { it.recordedAt }) { record ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassThin(
                                    color = AppTheme.colors.getContainerSurfaceColor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            SpeedHistoryItem(record = record)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 清除记录按钮 ─────────────────────────────────────────────
            TextButton(
                onClick = onClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = AppTheme.colors.buttonColors()
            ) {
                Text(
                    text = stringResource(R.string.speed_test_clear_records),
                    fontSize = 15.sp,
                    color = AppTheme.colors.getPrimaryColor()
                )
            }
        }
    }
}

private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

@Composable
private fun SpeedHistoryItem(record: SpeedTestRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：网络名称 + 时间
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.networkType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.getPrimaryTextColor()
            )
            Text(
                text = dateFormatter.format(Date(record.recordedAt)),
                fontSize = 12.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }

        // 右：下载速度
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.1f".format(record.downloadMbps),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.getPrimaryTextColor()
            )
            Text(
                text = stringResource(R.string.speed_test_unit_mbps),
                fontSize = 11.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
    }
}
