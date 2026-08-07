package com.wanbaohe.altitude.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.altitude.component.AltitudeComponent
import com.wanbaohe.altitude.component.AltitudeUiState
import com.wanbaohe.altitude.domain.AltitudeRecord
import com.wanbaohe.altitude.ui.ActionButtonsBar
import com.wanbaohe.altitude.ui.AltitudeBottomNavBar
import com.wanbaohe.altitude.ui.AltitudeHistoryDetailSheet
import com.wanbaohe.altitude.ui.ElevationGaugeSection
import com.wanbaohe.altitude.ui.LocationInfoCard
import com.wanbaohe.altitude.ui.ObservationTimeBar
import com.wanbaohe.altitude.ui.SaveNoteDialog
import com.wanbaohe.altitude.ui.SmallDetailCards
import com.wanbaohe.altitude.ui.TrendBarChartSection
import com.wanbaohe.altitude.ui.WeatherCard
import com.wanbaohe.altitude.ui.WeatherDetailsCard
import com.wanbaohe.altitude.ui.altitudeHistoryItems
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOff

/**
 * 海拔仪主屏幕 — Material3 极致扁平化仪表盘设计
 */
@Composable
fun AltitudeScreen(component: AltitudeComponent) {
    val state by component.uiState.collectAsState()
    val activity = LocalComponentActivity.current
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    // ── 定位权限检查 ──────────────────────────────────────────────────────
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    var permissionGranted by remember { mutableStateOf(isPermissionGranted()) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        permissionGranted = granted
        if (granted) {
            component.onLocationPermissionGranted()
        } else {
            isPermanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    LaunchedEffect(Unit) {
        val granted = isPermissionGranted()
        permissionGranted = granted
        if (granted && !state.locationPermissionGranted) {
            component.onLocationPermissionGranted()
        }
    }

    // ── 主屏幕 BaseScreen ────────────────────────────────────────────────
    BaseScreen(
        title = stringResource(CoreR.string.altitude),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        if (!permissionGranted) {
            Box(modifier = Modifier.weight(1f)) {
                LocationPermissionCard(
                    isPermanentlyDenied = isPermanentlyDenied,
                    onRequestPermission = { permissionLauncher.launch(locationPermissions) },
                    onOpenSettings = {
                        activity.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", activity.packageName, null)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else when (state.selectedTab) {
            0 -> InstrumentTab(
                state = state,
                captureController = captureController,
                onSaveRecord = {
                    if (state.currentAltitudeMeters != null) {
                        component.showSaveDialog()
                    }
                },
                onShare = {
                    scope.launch {
                        component.shareBitmap(
                            bitmap = captureController.bitmap(),
                            onComplete = {}
                        )
                    }
                }
            )

            1 -> HistoryTab(
                state = state,
                onClearAll = component::showClearAllDialog,
                onDelete = component::requestDelete,
                onClick = component::selectRecord
            )
        }
        AltitudeBottomNavBar(
            selectedTab = state.selectedTab,
            onTabSelected = { index ->
                component.selectTab(index)
            }
        )
    }

    // ── 对话框 & 弹窗 ────────────────────────────────────────────────────
    AltitudeDialogs(
        state = state,
        component = component
    )
}

// ─── Tab 0: 仪表盘 ──────────────────────────────────────────────────────

/**
 * 仪表盘 Tab — 海拔仪表 + 趋势图 + 位置信息 + 天气卡片 + 操作按钮
 */
@Composable
private fun ColumnScope.InstrumentTab(
    state: AltitudeUiState,
    captureController: CaptureController,
    onSaveRecord: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 可截图区域（卡片 + 观测时间，不含按钮）──────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .capturable(captureController),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 海拔仪表盘
            ElevationGaugeSection(state = state)

            // 2. 趋势柱状图
            if (state.trendPoints.size >= 2) {
                TrendBarChartSection(points = state.trendPoints)
            }

            // 3. 位置信息卡片
            val cityInfo = state.cityInfo
            if (cityInfo != null) {
                LocationInfoCard(cityInfo = cityInfo)
            }

            // 4~6. 天气相关卡片
            val weatherInfo = state.weatherInfo
            if (weatherInfo != null) {
                WeatherCard(weather = weatherInfo)
                WeatherDetailsCard(weather = weatherInfo)
                SmallDetailCards(weather = weatherInfo)
            }

            // 观测/更新时间（参与截图）
            ObservationTimeBar(weather = state.weatherInfo)
        }

        // ── 按钮区（不参与截图，但跟着滚动）──────────────────
        ActionButtonsBar(
            onSaveRecord = onSaveRecord,
            onShare = onShare
        )
    }
}

// ─── Tab 1: 历史记录 ────────────────────────────────────────────────────

/**
 * 历史记录 Tab — 标题栏 + 可滑动删除的历史列表
 */
@Composable
private fun ColumnScope.HistoryTab(
    state: AltitudeUiState,
    onClearAll: () -> Unit,
    onDelete: (Long) -> Unit,
    onClick: (AltitudeRecord) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        HistoryTitleBar(
            count = state.history.size,
            onClearAll = onClearAll
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            altitudeHistoryItems(
                history = state.history,
                unit = state.unit,
                onDelete = onDelete,
                onClick = onClick
            )
        }
    }
}

/**
 * 历史记录标题栏 — 标题 + 记录数量 + 清空按钮
 */
@Composable
private fun HistoryTitleBar(
    count: Int,
    onClearAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(CoreR.string.altitude_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Text(
                text = stringResource(CoreR.string.altitude_history_count, count),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
            )
            TextButton(onClick = onClearAll) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(CoreR.string.altitude_clear_all),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ─── 对话框 & 弹窗 ──────────────────────────────────────────────────────

/**
 * 所有对话框集中管理 — 保存 / 清空确认 / 删除确认 / 历史详情
 */
@Composable
private fun AltitudeDialogs(
    state: AltitudeUiState,
    component: AltitudeComponent,
) {
    if (state.showSaveDialog) {
        SaveNoteDialog(
            currentDisplay = state.displayAltitude,
            unit = state.unit.suffix,
            onConfirm = component::saveRecord,
            onDismiss = component::dismissSaveDialog
        )
    }

    if (state.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = component::dismissClearAllDialog,
            title = { Text(stringResource(CoreR.string.altitude_clear_all)) },
            text = {
                Text(
                    stringResource(
                        CoreR.string.altitude_clear_all_confirm,
                        state.history.size
                    )
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = component::clearAll,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = component::dismissClearAllDialog) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = component::dismissDeleteDialog,
            title = { Text(stringResource(CoreR.string.altitude_record_deleted)) },
            text = { Text(stringResource(CoreR.string.altitude_delete_confirm)) },
            confirmButton = {
                FilledTonalButton(
                    onClick = component::confirmDelete,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = component::dismissDeleteDialog) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    AltitudeHistoryDetailSheet(
        record = state.selectedRecord,
        unit = state.unit,
        onDismiss = component::dismissRecordDetail,
        onShareBitmap = { bitmap ->
            component.shareBitmap(bitmap = bitmap, onComplete = {})
        }
    )
}

// ─── 权限卡片 ───────────────────────────────────────────────────────────

/**
 * 友好的定位权限申请卡片
 */
@Composable
private fun LocationPermissionCard(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isPermanentlyDenied) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocationOff
                else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(CoreR.string.altitude_location_permission_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isPermanentlyDenied)
                        stringResource(CoreR.string.altitude_location_permission_denied)
                    else
                        stringResource(CoreR.string.altitude_location_permission_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = if (isPermanentlyDenied) onOpenSettings else onRequestPermission,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Text(
                        text = if (isPermanentlyDenied)
                            stringResource(CoreR.string.altitude_open_settings)
                        else
                            stringResource(CoreR.string.altitude_grant_permission)
                    )
                }
            }
        }
    }
}
