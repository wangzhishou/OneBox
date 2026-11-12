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

package com.wanbaohe.speedtest.screen

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.component.SpeedTestComponent
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.screen.ui.SpeedHistorySheet
import com.wanbaohe.speedtest.screen.ui.SpeedTestConfigDialog
import com.wanbaohe.speedtest.screen.ui.SpeedTestConfigSheet
import com.wanbaohe.speedtest.screen.ui.SpeedTestDashboard

@Composable
fun SpeedTestScreen(component: SpeedTestComponent) {
    val uiState by component.uiState.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var showConfigSheet by remember { mutableStateOf(false) }
    /** null = 关闭；SpeedTestConfig(id=0) = 新增；id>0 = 编辑 */
    var editingConfig by remember { mutableStateOf<SpeedTestConfig?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, component) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) component.refreshNetwork()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let { snackbarHostState.showSnackbar(it) }
    }

    BaseScreen(
        title = stringResource(R.string.speed_test_screen_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { showConfigSheet = true },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.speed_test_config_icon_desc)
                )
            }
        },
        foreground = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
            ) { data -> Snackbar(snackbarData = data) }
        }
    ) {
        SpeedTestDashboard(
            uiState = uiState,
            onStart = component::startTest,
            onCancel = component::cancelTest,
            onOpenWifi = component::openWifiSettings,
            onShowHistory = { showHistory = true }
        )
    }

    // ── 测速历史 ──────────────────────────────────────────────────────────
    SpeedHistorySheet(
        visible = showHistory,
        records = uiState.history,
        onClear = component::clearHistory,
        onSelectRecord = { record ->
            component.selectHistoryRecord(record)
            showHistory = false
        },
        onDismiss = { showHistory = false }
    )

    // ── 配置管理弹层 ──────────────────────────────────────────────────────
    SpeedTestConfigSheet(
        visible = showConfigSheet,
        configs = uiState.configList,
        activeConfigId = uiState.config.id,
        onSelect = { id ->
            component.selectConfig(id)
            showConfigSheet = false
        },
        onEdit = { config ->
            editingConfig = config
            showConfigSheet = false
        },
        onDelete = component::deleteConfig,
        onAdd = {
            editingConfig = SpeedTestConfig()   // id=0 代表新增
            showConfigSheet = false
        },
        onDismiss = { showConfigSheet = false }
    )

    // ── 新增 / 编辑配置弹窗 ───────────────────────────────────────────────
    SpeedTestConfigDialog(
        visible = editingConfig != null,
        initial = editingConfig,
        onSave = { saved ->
            if (saved.id > 0) component.updateConfig(saved)
            else component.addConfig(
                name = saved.name,
                testUrl = saved.testUrl,
                estimatedMb = saved.estimatedDataMb,
                durationSeconds = saved.durationSeconds
            )
            editingConfig = null
        },
        onDismiss = { editingConfig = null }
    )
}
