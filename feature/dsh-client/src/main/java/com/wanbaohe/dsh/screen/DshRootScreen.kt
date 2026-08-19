package com.wanbaohe.dsh.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.CloudUiState
import com.wanbaohe.dsh.component.DshPage
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.component.DshUiState
import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.StoredHost
import com.wanbaohe.dsh.wire.model.HostInfo
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.delay

/**
 * DSH 根屏:按模块内页面状态分发 连接页 / 聊天页(P2)/ 配对页(P6)。
 */
@Composable
fun DshRootScreen(component: DshRootComponent) {
    val uiState by component.uiState.collectAsState()
    when (uiState.page) {
        DshPage.Chat -> ChatScreen(component)
        DshPage.Connect -> ConnectScreen(component, uiState)
        DshPage.Pairing -> {
            val manager by component.pairingManager.collectAsState()
            manager?.let { PairingScreen(component, it) } ?: ConnectScreen(component, uiState)
        }
    }
}

/**
 * DSH 连接页:LAN 直连(地址输入)+ 远程网关(配对入口,P6)+ 我的电脑(云端中继,P7)
 * + 主机簿(条目区分形态:远程 = 云图标 + 机器名,LAN = 电脑图标 + 纯地址,
 * 云端 = 云端队列图标 + 「我的电脑」)+ 连接状态徽章。
 */
@Composable
private fun ConnectScreen(component: DshRootComponent, uiState: DshUiState) {
    val phase = uiState.snapshot.phase

    BaseScreen(
        title = stringResource(R.string.dsh_connect_title),
        onGoBack = component.onGoBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── LAN 直连(现有形态) ──
            Text(
                text = stringResource(R.string.dsh_lan_section),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
            Spacer(Modifier.height(8.dp))
            // 主机地址输入框(可不带 scheme,客户端默认补 http://)
            OutlinedTextField(
                value = uiState.address,
                onValueChange = component::onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.dsh_host_address_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(16.dp))

            // 连接 / 断开按钮
            if (phase == ConnectionPhase.Down) {
                Button(
                    onClick = component::connect,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.address.isNotBlank()
                ) {
                    Text(stringResource(R.string.dsh_connect))
                }
            } else {
                OutlinedButton(
                    onClick = component::disconnect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dsh_disconnect))
                }
            }

            Spacer(Modifier.height(16.dp))

            // 状态徽章:阶段 + 代际号 + 失败原因(401 附重配入口)
            StatusBadge(uiState = uiState, onReauth = component::reauthenticate)

            Spacer(Modifier.height(24.dp))

            // ── 远程网关(P6 配对形态) ──
            Text(
                text = stringResource(R.string.dsh_remote_section),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { component.openPairing() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.dsh_open_pairing))
            }

            Spacer(Modifier.height(24.dp))

            // ── 我的电脑(云端中继,P7/P8) ──
            CloudRelaySection(
                cloud = uiState.cloud,
                onGenerateCode = component::requestCloudBindCode,
                onClaim = component::claimCloudPair,
                onConnect = component::connectCloud
            )

            Spacer(Modifier.height(24.dp))

            // 就绪后:describe 信息 + 进入聊天页入口(自动进过一次后,退回连接页可再进)
            uiState.snapshot.describe?.let { describe ->
                if (phase == ConnectionPhase.Ready) {
                    HostInfoResult(describe)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = component::enterChat,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dsh_open_chat))
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // 主机簿:点选即按条目类型连接(LAN 直连 / 有令牌静默连),垃圾桶删除
            if (uiState.savedHosts.isNotEmpty()) {
                SavedHostList(
                    hosts = uiState.savedHosts,
                    onSelect = component::onSelectHost,
                    onRemove = component::onRemoveHost
                )
            }
        }
    }
}

/** 连接状态徽章:connecting / ready / down + 代际号 + 失败原因(401 单独提示 + 重配按钮) */
@Composable
private fun StatusBadge(uiState: DshUiState, onReauth: () -> Unit) {
    val snapshot = uiState.snapshot
    val (textRes, color) = when (snapshot.phase) {
        ConnectionPhase.Connecting ->
            R.string.dsh_status_connecting to AppTheme.colors.getOnInactiveContainerColor()
        ConnectionPhase.Ready ->
            R.string.dsh_status_ready to AppTheme.colors.getPrimaryColor()
        ConnectionPhase.Down ->
            R.string.dsh_status_down to MaterialTheme.colorScheme.error
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(textRes),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (snapshot.generation > 0) {
                Text(
                    text = stringResource(R.string.dsh_generation, snapshot.generation),
                    fontSize = 12.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor()
                )
            }
        }
        if (uiState.authBlocked) {
            Text(
                text = stringResource(
                    if (uiState.currentHostIsCloud) R.string.dsh_cloud_auth_blocked
                    else R.string.dsh_auth_blocked
                ),
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            // 云端形态 401 = App 登录过期,重登录后点重试(resume,令牌现取);
            // 网关形态 401 = 设备令牌被拒,拉起配对/登录,令牌原地刷新后 resume
            TextButton(onClick = onReauth) {
                Text(
                    stringResource(
                        if (uiState.currentHostIsCloud) R.string.dsh_retry_action
                        else R.string.dsh_reauth
                    ),
                    fontSize = 13.sp
                )
            }
        } else if (snapshot.phase == ConnectionPhase.Down && snapshot.failureReason != null) {
            Text(
                text = stringResource(R.string.dsh_connect_failed, snapshot.failureReason),
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
    }
}

/** describe 成功的结果展示 */
@Composable
private fun HostInfoResult(info: HostInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.dsh_describe_success, info.version),
            color = AppTheme.colors.getPrimaryColor(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        info.provider?.let {
            Text(
                text = stringResource(R.string.dsh_provider, it),
                fontSize = 14.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
        info.model?.let {
            Text(
                text = stringResource(R.string.dsh_model, it),
                fontSize = 14.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
    }
}

/**
 * 云端中继区块(P7/P8):未登录提示先登录 App(附「去登录」按钮);推荐路径「扫码连接」
 * (扫 Mac 上 onebox-dsh-bridge 插件页面的二维码,认领成功即自动连接),「生成绑定码」为
 * CLI 兜底(6 位码大字 + 复制 + 有效期倒计时 +「连接我的电脑」)。标题右侧「怎么用?」弹指引。
 */
@Composable
private fun CloudRelaySection(
    cloud: CloudUiState,
    onGenerateCode: () -> Unit,
    onClaim: (String) -> Unit,
    onConnect: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    var scanPermissionDenied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val onNavigate = LocalOnNavigate.current

    // 扫码认领(P8):QR 内容 = oneboxdsh://pair?v=1&g=…&p=…&s=…;解析/校验在组件层
    val scanLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> result.content.rawValue?.let(onClaim)
            QRResult.QRMissingPermission -> scanPermissionDenied = true
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dsh_cloud_section),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
            TextButton(onClick = { showHelp = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.dsh_cloud_help_action), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (!cloud.available) {
            Text(
                text = stringResource(R.string.dsh_cloud_login_required),
                fontSize = 13.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
            Spacer(Modifier.height(8.dp))
            // 云端中继为登录用户专属:直接给登录入口
            OutlinedButton(onClick = { onNavigate(Screen.Login()) }) {
                Text(stringResource(R.string.dsh_cloud_login_action))
            }
            return@Column
        }
        // 扫码连接(推荐)与生成绑定码(CLI 兜底)并列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scanPermissionDenied = false
                    scanLauncher.launch(null)
                },
                modifier = Modifier.weight(1f),
                enabled = !cloud.claiming
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(
                        if (cloud.claiming) R.string.dsh_cloud_claiming
                        else R.string.dsh_cloud_scan_connect
                    )
                )
            }
            OutlinedButton(
                onClick = onGenerateCode,
                modifier = Modifier.weight(1f),
                enabled = !cloud.requesting
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudQueue,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(
                        if (cloud.requesting) R.string.dsh_cloud_generating_code
                        else R.string.dsh_cloud_generate_code
                    )
                )
            }
        }
        if (scanPermissionDenied) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dsh_camera_permission),
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
        // 粘贴链接:模拟器/相机不可用场景,从插件页复制的配对链接直接认领
        // (解析校验与扫码同路,失败走既有错误提示)
        TextButton(
            onClick = {
                val text = clipboard.getText()?.text
                if (!text.isNullOrBlank()) onClaim(text.trim())
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentPaste,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.dsh_cloud_paste_link), fontSize = 13.sp)
        }
        cloud.error?.let { error ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
        if (cloud.code.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.dsh_cloud_code_hint),
                fontSize = 13.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayBindCode(cloud.code),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                IconButton(onClick = { clipboard.setText(AnnotatedString(cloud.code)) }) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.dsh_cloud_copy),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // 有效期倒计时:每秒现取剩余秒数,归零提示重新生成
            val remainingSeconds by produceState(
                initialValue = ((cloud.expiresAtEpochMs - System.currentTimeMillis()) / 1000)
                    .toInt().coerceAtLeast(0),
                cloud.expiresAtEpochMs
            ) {
                while (true) {
                    value = ((cloud.expiresAtEpochMs - System.currentTimeMillis()) / 1000)
                        .toInt().coerceAtLeast(0)
                    if (value <= 0) break
                    delay(1000)
                }
            }
            Text(
                text = if (remainingSeconds > 0) {
                    stringResource(R.string.dsh_cloud_code_expires_in, remainingSeconds)
                } else {
                    stringResource(R.string.dsh_cloud_code_expired)
                },
                fontSize = 12.sp,
                color = if (remainingSeconds > 0) {
                    AppTheme.colors.getOnInactiveContainerColor()
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.dsh_cloud_connect))
            }
        }
    }
    if (showHelp) {
        CloudRelayHelpDialog(onDismiss = { showHelp = false })
    }
}

/** 云端中继「怎么用?」三步指引弹窗(静态说明,风格跟随模块内 AlertDialog 惯例) */
@Composable
private fun CloudRelayHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_cloud_help_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.dsh_cloud_help_step1),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.dsh_cloud_help_step2),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.dsh_cloud_help_step3),
                    fontSize = 14.sp
                )
                // 备选路径:生成绑定码 + dsh-connector CLI
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.dsh_cloud_help_fallback),
                        fontSize = 13.sp,
                        color = AppTheme.colors.getOnInactiveContainerColor()
                    )
                    Text(
                        text = stringResource(R.string.dsh_cloud_help_step2_cmd),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AppTheme.colors.getOnInactiveContainerColor()
                    )
                }
                Text(
                    text = stringResource(R.string.dsh_cloud_help_note),
                    fontSize = 12.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cloud_got_it))
            }
        }
    )
}

/** 绑定码展示形态:6 位数字分两组(123-456),其余长度原样 */
private fun displayBindCode(code: String): String =
    if (code.length == 6) "${code.substring(0, 3)}-${code.substring(3)}" else code

/** 主机簿列表:点击按条目类型连接,垃圾桶删除(远程条目删除时 best-effort 吊销令牌) */
@Composable
private fun SavedHostList(
    hosts: List<StoredHost>,
    onSelect: (StoredHost) -> Unit,
    onRemove: (StoredHost) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.dsh_saved_hosts),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
        hosts.forEach { host ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(host) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 形态图标:云端中继 = 云端队列;远程网关(持令牌)= 云;LAN 直连 = 电脑
                Icon(
                    imageVector = when {
                        host.isCloud -> Icons.Outlined.CloudQueue
                        host.isRemote -> Icons.Outlined.Cloud
                        else -> Icons.Outlined.Computer
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AppTheme.colors.getOnInactiveContainerColor()
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                host.isCloud -> stringResource(R.string.dsh_cloud_my_computer)
                                else -> host.hostLabel.ifEmpty { host.baseUri }
                            },
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                when {
                                    host.isCloud -> R.string.dsh_host_kind_cloud
                                    host.isRemote -> R.string.dsh_host_kind_remote
                                    else -> R.string.dsh_host_kind_lan
                                }
                            ),
                            fontSize = 11.sp,
                            color = AppTheme.colors.getOnInactiveContainerColor()
                        )
                    }
                    if (host.hostLabel.isNotEmpty()) {
                        Text(
                            text = host.baseUri,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = AppTheme.colors.getOnInactiveContainerColor()
                        )
                    }
                }
                IconButton(onClick = { onRemove(host) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.dsh_delete_host),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
