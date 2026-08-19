package com.wanbaohe.dsh.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.PhonelinkRing
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.connection.PairOffer
import com.wanbaohe.dsh.connection.PairingError
import com.wanbaohe.dsh.connection.PairingErrorKind
import com.wanbaohe.dsh.connection.PairingManager
import com.wanbaohe.dsh.connection.PairingStage
import com.wanbaohe.dsh.connection.PairingUiState
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

/**
 * 配对页(P6,对齐 Flutter pairing_page.dart):
 * 亮码等待 → offers 列表 → 人工比对主机码点选。扫码模式下主机码已被二维码锚定,
 * 匹配项自动高亮,不匹配项需长按(防误触);被攻击者塞入假 offer 时肉眼可辨。
 * 密码登录为页内兜底入口(密码永不落盘)。
 */
@Composable
fun PairingScreen(component: DshRootComponent, manager: PairingManager) {
    val state by manager.state.collectAsState()
    var scanPermissionDenied by remember { mutableStateOf(false) }

    // 扫码(ADR-0008):QR 内容 = 网关 /pair 落地页 URL(fragment 携带码)
    val scanLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> result.content.rawValue?.let(manager::applyInvite)
            QRResult.QRMissingPermission -> scanPermissionDenied = true
            else -> Unit
        }
    }

    BaseScreen(
        title = stringResource(R.string.dsh_pairing_title),
        onGoBack = component::closePairing
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.stage) {
                PairingStage.Url -> UrlStep(
                    component = component,
                    manager = manager,
                    state = state,
                    scanPermissionDenied = scanPermissionDenied,
                    onScan = {
                        scanPermissionDenied = false
                        scanLauncher.launch(null)
                    }
                )

                PairingStage.Waiting -> WaitingStep(manager, state)
                PairingStage.Offers -> OffersStep(manager, state)
                PairingStage.Confirming -> ConfirmingStep()
                PairingStage.Done -> CircularProgressIndicator()
            }
        }
    }
}

/** 地址页:扫码/粘贴邀请(推荐)+ 手动地址 + 设备名 + 密码登录兜底 */
@Composable
private fun UrlStep(
    component: DshRootComponent,
    manager: PairingManager,
    state: PairingUiState,
    scanPermissionDenied: Boolean,
    onScan: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeviceNameDialog by remember { mutableStateOf(false) }
    val uiState by component.uiState.collectAsState()

    Icon(
        imageVector = Icons.Outlined.PhonelinkRing,
        contentDescription = null,
        modifier = Modifier.size(44.dp),
        tint = AppTheme.colors.getPrimaryColor()
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.dsh_pair_intro),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    Spacer(Modifier.height(20.dp))

    // 方式一(推荐):扫码/粘贴邀请 —— 网关地址由邀请自带,发起时覆盖手填地址
    OutlinedButton(
        onClick = onScan,
        enabled = !state.busy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.dsh_pair_scan))
    }
    TextButton(
        onClick = { manager.applyInvite(clipboard.getText()?.text.orEmpty()) },
        enabled = !state.busy
    ) {
        Icon(
            Icons.Outlined.ContentPaste,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.dsh_pair_paste), fontSize = 13.sp)
    }
    Text(
        text = stringResource(R.string.dsh_pair_scan_hint),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    if (scanPermissionDenied) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.dsh_camera_permission),
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp
        )
    }

    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(R.string.dsh_pair_manual),
        fontSize = 13.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.gatewayAddress,
        onValueChange = manager::onGatewayAddressChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.busy,
        label = { Text(stringResource(R.string.dsh_gateway_address_label)) },
        placeholder = { Text(stringResource(R.string.dsh_gateway_address_hint)) },
        supportingText = { Text(stringResource(R.string.dsh_gateway_address_helper)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
    )

    // 本机名称:配对成功后出现在宿主「已配对设备」表里;配对前就能改
    TextButton(
        onClick = { showDeviceNameDialog = true },
        enabled = !state.busy
    ) {
        Icon(
            Icons.Outlined.Smartphone,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.dsh_device_name, uiState.deviceName),
            fontSize = 13.sp
        )
    }

    PairingErrorText(state.error)

    Button(
        onClick = { manager.start() },
        enabled = !state.busy,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(stringResource(R.string.dsh_generate_code))
        }
    }

    // 密码登录兜底(密码永不落盘)
    TextButton(
        onClick = { showPasswordDialog = true },
        enabled = !state.busy
    ) {
        Text(stringResource(R.string.dsh_password_login), fontSize = 13.sp)
    }

    if (showPasswordDialog) {
        PasswordLoginDialog(
            busy = state.busy,
            onDismiss = { showPasswordDialog = false },
            onLogin = { password ->
                showPasswordDialog = false
                manager.loginWithPassword(password)
            }
        )
    }
    if (showDeviceNameDialog) {
        DeviceNameDialog(
            current = uiState.deviceName,
            onDismiss = { showDeviceNameDialog = false },
            onSet = { name ->
                showDeviceNameDialog = false
                component.setDeviceName(name)
            }
        )
    }
}

/** 亮码等待页:D 大字 + 等待动画 + 取消 */
@Composable
private fun WaitingStep(manager: PairingManager, state: PairingUiState) {
    val session = state.session ?: return
    Text(
        text = if (state.inviteLabel.isEmpty()) {
            stringResource(R.string.dsh_pair_waiting_manual)
        } else {
            stringResource(
                R.string.dsh_pair_waiting_invite,
                state.inviteLabel,
                session.baseUri
            )
        },
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    Spacer(Modifier.height(16.dp))
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = session.displayCode,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.dsh_pair_code_valid),
                fontSize = 12.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
    }
    Spacer(Modifier.height(18.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.dsh_pair_waiting), fontSize = 14.sp)
    }
    PairingErrorText(state.error)
    Spacer(Modifier.height(14.dp))
    TextButton(onClick = manager::cancel) {
        Text(stringResource(R.string.dsh_cancel), fontSize = 13.sp)
    }
}

/** offers 列表:人工比对主机码点选;锚定模式匹配项绿卡高亮、不匹配项须长按 */
@Composable
private fun OffersStep(manager: PairingManager, state: PairingUiState) {
    val session = state.session ?: return
    Text(
        text = stringResource(R.string.dsh_pair_own_code, session.displayCode),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = if (state.anchoredHostCode == null) {
            stringResource(R.string.dsh_pair_offers_title)
        } else {
            stringResource(R.string.dsh_pair_offers_anchored, fmtHostCode(state.anchoredHostCode))
        },
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    if (state.inviteLabel.isNotEmpty()) {
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.dsh_pair_offers_from, state.inviteLabel),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
    }
    Spacer(Modifier.height(10.dp))
    VerifyGuidance()
    Spacer(Modifier.height(12.dp))
    state.offers.forEach { offer ->
        OfferCard(
            offer = offer,
            anchoredHostCode = state.anchoredHostCode,
            onConfirm = { manager.confirm(offer) }
        )
    }
    if (state.anchoredHostCode != null) {
        Text(
            text = stringResource(R.string.dsh_pair_mismatched_hint),
            fontSize = 12.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
        Spacer(Modifier.height(8.dp))
    }
    PairingErrorText(state.error)
}

/** 双向亮码核对指引(ADR-0007 防抢注) */
@Composable
private fun VerifyGuidance() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppTheme.colors.getPrimaryColor()
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.dsh_pair_verify_ok), fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.dsh_pair_verify_warn),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** 单条 offer 卡片:锚定模式匹配项高亮点按即选;不匹配项降灰、须长按 */
@Composable
private fun OfferCard(
    offer: PairOffer,
    anchoredHostCode: String?,
    onConfirm: () -> Unit
) {
    val anchored = anchoredHostCode != null
    val matched = anchored && offer.hostCode == anchoredHostCode
    val disabled = anchored && !matched
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .combinedClickable(
                onClick = { if (!disabled) onConfirm() },
                onLongClick = { if (disabled) onConfirm() }
            ),
        colors = if (matched) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = offer.displayHostCode,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (disabled) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (matched) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = AppTheme.colors.getPrimaryColor(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.dsh_pair_matched),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(
                            offer.hostLabel.ifEmpty {
                                stringResource(R.string.dsh_default_host_label)
                            }
                        )
                        if (offer.upstreamPort > 0) {
                            append(" · ")
                            append(stringResource(R.string.dsh_pair_tunnel, offer.upstreamPort))
                        }
                    },
                    fontSize = 12.sp,
                    color = if (disabled) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        AppTheme.colors.getOnInactiveContainerColor()
                    }
                )
            }
            Icon(
                imageVector = if (matched) Icons.Outlined.Verified else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (disabled) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    AppTheme.colors.getPrimaryColor()
                }
            )
        }
    }
}

/** 确认中 */
@Composable
private fun ConfirmingStep() {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.dsh_pair_confirming), fontSize = 14.sp)
}

/** 配对错误文案:优先按 kind 本地化,否则原样展示网关错误串 */
@Composable
private fun PairingErrorText(error: PairingError?) {
    if (error == null) return
    Spacer(Modifier.height(12.dp))
    Text(
        text = pairingErrorText(error),
        color = MaterialTheme.colorScheme.error,
        fontSize = 13.sp
    )
}

@Composable
private fun pairingErrorText(error: PairingError): String {
    val detail = error.message.orEmpty()
    return when (error.kind) {
        PairingErrorKind.Network -> stringResource(R.string.dsh_err_network, detail)
        PairingErrorKind.BadAddress -> stringResource(R.string.dsh_err_bad_address)
        PairingErrorKind.InvalidInvite -> stringResource(R.string.dsh_err_invalid_invite)
        PairingErrorKind.Expired -> stringResource(R.string.dsh_err_expired)
        PairingErrorKind.CodeUsed -> stringResource(R.string.dsh_err_code_used)
        PairingErrorKind.WrongPassword -> stringResource(R.string.dsh_err_wrong_password)
        PairingErrorKind.RateLimited -> stringResource(R.string.dsh_err_rate_limited)
        PairingErrorKind.PollFailing -> stringResource(R.string.dsh_err_poll, detail)
        PairingErrorKind.MissingToken -> stringResource(R.string.dsh_err_missing_token)
        null -> detail
    }
}

/** 密码登录对话框(密码只在登录调用期内存在,永不落盘) */
@Composable
private fun PasswordLoginDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_password_title)) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.dsh_password_hint)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onLogin(password) },
                enabled = password.isNotEmpty() && !busy
            ) {
                Text(stringResource(R.string.dsh_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 设备名修改对话框(清洗/泛称拒绝由 CredentialsStore 把关) */
@Composable
private fun DeviceNameDialog(
    current: String,
    onDismiss: () -> Unit,
    onSet: (String) -> Unit
) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_device_name_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSet(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

private fun fmtHostCode(code: String): String =
    if (code.length == 6) "${code.substring(0, 3)}-${code.substring(3)}" else code
