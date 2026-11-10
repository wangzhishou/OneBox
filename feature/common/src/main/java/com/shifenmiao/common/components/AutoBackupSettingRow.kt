package com.shifenmiao.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.R
import com.shifenmiao.common.backup.BackupAutoSettings
import com.shifenmiao.common.backup.BackupCloudStatus
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineToggle
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxThemedIconBadge
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 「自动备份」设置行：开关放在设置列表里，点整行进去调间隔与云盘同步。
 *
 * 备份中心页只负责一次性操作（立即备份 / 恢复 / 导出），定时策略属于设置，
 * 所以开关和「备份与恢复」一起放进「数据」分组。
 */
@Composable
fun AutoBackupSettingRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val onNavigate = LocalOnNavigate.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            BackupSettingsEntryPoint::class.java,
        )
    }
    val settings = remember { entryPoint.autoBackupSettings() }
    val cloudStatus = remember { entryPoint.cloudStatus() }

    var enabled by remember { mutableStateOf(settings.isEnabled) }
    var intervalDays by remember { mutableStateOf(settings.intervalDays) }
    var uploadToCloud by remember { mutableStateOf(settings.uploadsToCloud) }
    var lastRunAt by remember { mutableStateOf(settings.lastRunAt) }
    var cloudLabel by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(showSheet) {
        lastRunAt = settings.lastRunAt
        cloudLabel = cloudStatus.label()
    }

    OneBoxListItem(
        modifier = modifier,
        onClick = { showSheet = true },
        headlineContent = {
            Text(
                text = stringResource(R.string.backup_auto_title),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = {
            Text(
                text = autoBackupSubtitle(enabled, intervalDays, uploadToCloud, lastRunAt),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            OneBoxThemedIconBadge(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineToggle,
                themeIndex = 1,
            )
        },
        trailingContent = {
            GlassSwitch(
                checked = enabled,
                onCheckedChange = { checked ->
                    enabled = checked
                    settings.isEnabled = checked
                    lastRunAt = settings.lastRunAt
                },
                thumbContent = {
                    if (enabled) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
                colors = AppTheme.colors.switchColors(),
            )
        },
    )

    if (showSheet) {
        AutoBackupSheet(
            enabled = enabled,
            intervalDays = intervalDays,
            uploadToCloud = uploadToCloud,
            cloudLabel = cloudLabel,
            onEnabledChange = {
                enabled = it
                settings.isEnabled = it
            },
            onIntervalChange = {
                intervalDays = it
                settings.intervalDays = it
            },
            onUploadToCloudChange = {
                uploadToCloud = it
                settings.uploadsToCloud = it
            },
            onConfigureCloud = {
                showSheet = false
                onNavigate(Screen.CloudStorage())
            },
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun autoBackupSubtitle(
    enabled: Boolean,
    intervalDays: Int,
    uploadToCloud: Boolean,
    lastRunAt: Long,
): String {
    if (!enabled) return stringResource(R.string.backup_auto_off)
    val parts = buildList {
        add(intervalLabel(intervalDays))
        if (uploadToCloud) add(stringResource(R.string.backup_auto_cloud_on))
        add(
            if (lastRunAt <= 0L) {
                stringResource(R.string.backup_auto_never)
            } else {
                stringResource(R.string.backup_auto_last_run, formatTimestamp(lastRunAt))
            }
        )
    }
    return parts.joinToString(" · ")
}

@Composable
private fun intervalLabel(days: Int): String = when (days) {
    1 -> stringResource(R.string.backup_auto_interval_daily)
    3 -> stringResource(R.string.backup_auto_interval_3days)
    else -> stringResource(R.string.backup_auto_interval_weekly)
}

@Composable
private fun AutoBackupSheet(
    enabled: Boolean,
    intervalDays: Int,
    uploadToCloud: Boolean,
    cloudLabel: String?,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onUploadToCloudChange: (Boolean) -> Unit,
    onConfigureCloud: () -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.backup_auto_title),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.backup_auto_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                ToggleRow(
                    title = stringResource(R.string.backup_auto_enable),
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )

                BackupAutoSettings.INTERVAL_OPTIONS.forEach { days ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = intervalDays == days,
                            onClick = { onIntervalChange(days) },
                            enabled = enabled,
                        )
                        Text(
                            text = intervalLabel(days),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }

                if (cloudLabel != null) {
                    ToggleRow(
                        title = stringResource(R.string.backup_auto_upload_cloud),
                        subtitle = cloudLabel,
                        checked = uploadToCloud,
                        enabled = enabled,
                        onCheckedChange = onUploadToCloudChange,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.backup_auto_upload_cloud),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = stringResource(R.string.backup_save_to_cloud_unavailable),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = onConfigureCloud) {
                            Text(stringResource(R.string.backup_auto_cloud_configure))
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        GlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = AppTheme.colors.switchColors(),
        )
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

/**
 * 设置行只需要「自动备份偏好 + 云盘是否就绪」两样东西，
 * 用 EntryPoint 取，避免把整个备份目标集合传进 UI。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupSettingsEntryPoint {

    fun autoBackupSettings(): BackupAutoSettings

    fun cloudStatus(): BackupCloudStatus
}
