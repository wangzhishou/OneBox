package com.wanbaohe.dsh.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.CloudUiState
import com.wanbaohe.dsh.component.DshPage
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.component.DshUiState
import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.StoredHost
import com.wanbaohe.dsh.ui.DshWordmark
import com.wanbaohe.dsh.wire.WireCompat
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

/** 连接页焦点:LAN 直连 / 远程网关 / 云端中继(分焦点 tab,互不干扰) */
private enum class ConnectTab { Lan, Remote, Cloud }

/**
 * DSH 连接页:分焦点 tab 结构(「连接 DSH」设计方案)。
 * - LAN 直连:手动地址表单(host:port,纯地址条目无令牌)
 * - 远程网关:同一套手动地址表单(公网/远程场景文案)+ 网关配对入口(P6)
 * - 云端中继:扫码认领 / 绑定码 / 我的电脑(P7/P8)
 * tab 下方共用:连接状态徽章(401 重配入口)、就绪信息与主机簿(按形态过滤)。
 */
@Composable
private fun ConnectScreen(component: DshRootComponent, uiState: DshUiState) {
    val phase = uiState.snapshot.phase
    var selectedTab by rememberSaveable { mutableStateOf(ConnectTab.Lan) }

    BaseScreen(
        title = stringResource(R.string.dsh_connect_title),
        onGoBack = component.onGoBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // 品牌区:DSH 官方 wordmark(点缀,小尺寸)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = DshWordmark,
                    contentDescription = null,
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp),
                    tint = AppTheme.colors.getOnInactiveContainerColor()
                )
            }
            ConnectTabRow(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
            Spacer(Modifier.height(20.dp))

            when (selectedTab) {
                ConnectTab.Lan -> ManualAddressTab(
                    uiState = uiState,
                    phase = phase,
                    introRes = R.string.dsh_lan_intro,
                    helperRes = R.string.dsh_lan_address_helper,
                    tips = listOf(
                        R.string.dsh_lan_tip_same_network,
                        R.string.dsh_lan_tip_port
                    ),
                    onAddressChange = component::onAddressChange,
                    onConnect = component::connect,
                    onDisconnect = component::disconnect,
                    onTrouble = { selectedTab = ConnectTab.Cloud }
                )

                ConnectTab.Remote -> {
                    ManualAddressTab(
                        uiState = uiState,
                        phase = phase,
                        introRes = R.string.dsh_remote_intro,
                        helperRes = R.string.dsh_remote_address_helper,
                        tips = listOf(R.string.dsh_remote_tip_reachable),
                        onAddressChange = component::onAddressChange,
                        onConnect = component::connect,
                        onDisconnect = component::disconnect,
                        onTrouble = { selectedTab = ConnectTab.Cloud }
                    )
                    // 网关配对(P6):配对成功后条目进主机簿,凭令牌静默连
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
                }

                ConnectTab.Cloud -> CloudRelaySection(
                    cloud = uiState.cloud,
                    onGenerateCode = component::requestCloudBindCode,
                    onClaim = component::claimCloudPair,
                    onConnect = component::connectCloud
                )
            }

            Spacer(Modifier.height(16.dp))

            // 状态徽章:阶段 + 代际号 + 失败原因(401 附重配入口)
            StatusBadge(uiState = uiState, onReauth = component::reauthenticate)

            // 协议兼容声明:未就绪时也明说「只在这个 Harness 版本上验证过」,
            // 避免用户把版本不匹配误判成网络问题(0.1.5 起主机不再上报版本)
            if (phase != ConnectionPhase.Ready) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.dsh_wire_compat_note,
                        WireCompat.VERIFIED_HARNESS_VERSION
                    ),
                    fontSize = 12.sp,
                    color = AppTheme.colors.getOnInactiveContainerColor()
                )
            }

            // 就绪后:describe 信息 + 进入聊天页入口(自动进过一次后,退回连接页可再进)
            uiState.snapshot.describe?.let { describe ->
                if (phase == ConnectionPhase.Ready) {
                    Spacer(Modifier.height(16.dp))
                    HostInfoResult(describe)
                    Spacer(Modifier.height(12.dp))
                    // primary 玻璃主按钮(玻璃关闭时退化为实色 primary)
                    GlassButton(
                        onClick = component::enterChat,
                        modifier = Modifier.fillMaxWidth(),
                        color = AppTheme.colors.getPrimaryColor(),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerAlpha = 0.7f
                    ) {
                        Text(
                            text = stringResource(R.string.dsh_open_chat),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 主机簿:按当前 tab 形态过滤;点选即按条目类型连接,垃圾桶删除
            val tabHosts = when (selectedTab) {
                ConnectTab.Lan -> uiState.savedHosts.filter { !it.isCloud && !it.isRemote }
                ConnectTab.Remote -> uiState.savedHosts.filter { it.isRemote && !it.isCloud }
                ConnectTab.Cloud -> uiState.savedHosts.filter { it.isCloud }
            }
            if (tabHosts.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SavedHostList(
                    hosts = tabHosts,
                    onSelect = component::onSelectHost,
                    onRemove = component::onRemoveHost
                )
            }
        }
    }
}

/** 分焦点 tab 行:三段等宽。短文案单行;长文案按词均衡拆成两行并居中,避免末词孤行。 */
@Composable
private fun ConnectTabRow(selected: ConnectTab, onSelect: (ConnectTab) -> Unit) {
    val tabs = listOf(
        ConnectTab.Lan to R.string.dsh_lan_section,
        ConnectTab.Remote to R.string.dsh_remote_section,
        ConnectTab.Cloud to R.string.dsh_tab_cloud
    )
    val labels = tabs.map { stringResource(it.second) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 选中态是 SemiBold,按更宽的字重测量,切段后选中也不会再被挤出
    val measureStyle = LocalTextStyle.current.merge(
        TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        val textBudgetPx = with(density) { (maxWidth / tabs.size - 12.dp).roundToPx() } - 2
        val laidOut = labels.map { label ->
            balancedTwoLine(label, textMeasurer, measureStyle, textBudgetPx)
        }
        val wrapped = laidOut.any { '\n' in it }
        // 两行行高 + 上下 padding,多留 4dp,避免字面贴边被裁
        val twoLineHeight = with(density) { 18.sp.toDp() } * 2 + 20.dp
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, (tab, _) ->
                val isSelected = tab == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (wrapped) Modifier.height(twoLineHeight) else Modifier)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                AppTheme.colors.getPrimaryColor().copy(alpha = 0.15f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = laidOut[index],
                        color = if (isSelected) {
                            AppTheme.colors.getPrimaryColor()
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = measureStyle.merge(
                            TextStyle(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                        ),
                    )
                }
            }
        }
    }
}

/**
 * 等宽格子里的长文案:整句放得下就单行;否则在空格处拆成两行,
 * 取两行宽度最接近、且各自都放得下的切法。没有合适切法时保持原文,交给默认换行。
 */
private fun balancedTwoLine(
    text: String,
    measurer: TextMeasurer,
    style: TextStyle,
    maxWidthPx: Int,
): String {
    if (maxWidthPx <= 0) return text
    if (measurer.measure(text, style = style, maxLines = 1, softWrap = false).size.width <= maxWidthPx) {
        return text
    }
    var best: String? = null
    var bestDiff = Int.MAX_VALUE
    text.forEachIndexed { index, c ->
        if (c != ' ' && c != '\u00A0') return@forEachIndexed
        val line1 = text.substring(0, index).trimEnd()
        val line2 = text.substring(index + 1).trimStart()
        if (line1.isEmpty() || line2.isEmpty()) return@forEachIndexed
        val width1 = measurer.measure(line1, style = style, maxLines = 1, softWrap = false).size.width
        val width2 = measurer.measure(line2, style = style, maxLines = 1, softWrap = false).size.width
        if (width1 > maxWidthPx || width2 > maxWidthPx) return@forEachIndexed
        val diff = kotlin.math.abs(width1 - width2)
        if (diff < bestDiff) {
            bestDiff = diff
            best = "$line1\n$line2"
        }
    }
    return best ?: text
}

/**
 * 两个操作按钮:各自按单行内容宽度测量,半宽放得下就等宽并排,否则各占一整行。
 * 文案始终单行,不在按钮里折断。
 */
@Composable
private fun CloudActionButtons(
    scanLabel: String,
    generateLabel: String,
    scanEnabled: Boolean,
    generateEnabled: Boolean,
    onScan: () -> Unit,
    onGenerate: () -> Unit,
) {
    val spacing = 8.dp
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val spacingPx = spacing.roundToPx()
        // 测量单行固有宽度。父级在滚动容器里高度无界,这里收成有限值,避免子组件拒绝无限约束。
        val loose = Constraints(
            minWidth = 0,
            maxWidth = constraints.maxWidth.coerceAtMost(10_000),
            minHeight = 0,
            maxHeight = constraints.maxHeight.coerceAtMost(10_000),
        )
        val scanIntrinsic = subcompose("scan-intrinsic") {
            CloudRelayActionButton(
                icon = Icons.Outlined.QrCodeScanner,
                label = scanLabel,
                enabled = scanEnabled,
                onClick = onScan,
            )
        }.first().measure(loose).width
        val generateIntrinsic = subcompose("generate-intrinsic") {
            CloudRelayActionButton(
                icon = Icons.Outlined.CloudQueue,
                label = generateLabel,
                enabled = generateEnabled,
                onClick = onGenerate,
            )
        }.first().measure(loose).width
        val half = ((constraints.maxWidth - spacingPx) / 2).coerceAtLeast(0)
        val sideBySide = scanIntrinsic <= half && generateIntrinsic <= half
        if (sideBySide) {
            val halfConstraints = Constraints(
                minWidth = half,
                maxWidth = half,
                minHeight = 0,
                maxHeight = constraints.maxHeight.coerceAtMost(10_000),
            )
            val scan = subcompose("scan") {
                CloudRelayActionButton(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = scanLabel,
                    enabled = scanEnabled,
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth(),
                )
            }.first().measure(halfConstraints)
            val generate = subcompose("generate") {
                CloudRelayActionButton(
                    icon = Icons.Outlined.CloudQueue,
                    label = generateLabel,
                    enabled = generateEnabled,
                    onClick = onGenerate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }.first().measure(halfConstraints)
            val height = maxOf(scan.height, generate.height)
            layout(constraints.maxWidth, height) {
                scan.placeRelative(0, (height - scan.height) / 2)
                generate.placeRelative(half + spacingPx, (height - generate.height) / 2)
            }
        } else {
            val full = Constraints(
                minWidth = constraints.maxWidth,
                maxWidth = constraints.maxWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight.coerceAtMost(10_000),
            )
            val scan = subcompose("scan-full") {
                CloudRelayActionButton(
                    icon = Icons.Outlined.QrCodeScanner,
                    label = scanLabel,
                    enabled = scanEnabled,
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth(),
                )
            }.first().measure(full)
            val generate = subcompose("generate-full") {
                CloudRelayActionButton(
                    icon = Icons.Outlined.CloudQueue,
                    label = generateLabel,
                    enabled = generateEnabled,
                    onClick = onGenerate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }.first().measure(full)
            val height = scan.height + spacingPx + generate.height
            layout(constraints.maxWidth, height) {
                scan.placeRelative(0, 0)
                generate.placeRelative(0, scan.height + spacingPx)
            }
        }
    }
}

@Composable
private fun CloudRelayActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 手动地址表单(LAN 直连 / 远程网关共用,连接路径一致):
 * 说明标题 +「主机地址」Glass 输入框(电脑 leadingIcon、清除 trailingIcon、辅助说明)
 * + 大号主按钮 + 连接提示 bullet 列表 +「无法连接?」链接。
 */
@Composable
private fun ManualAddressTab(
    uiState: DshUiState,
    phase: ConnectionPhase,
    introRes: Int,
    helperRes: Int,
    tips: List<Int>,
    onAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTrouble: () -> Unit
) {
    Text(
        text = stringResource(introRes),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(R.string.dsh_host_address_label),
        fontSize = 13.sp,
        color = AppTheme.colors.getOnInactiveContainerColor()
    )
    Spacer(Modifier.height(6.dp))
    // 主机地址输入框(可不带 scheme,客户端默认补 http://)
    GlassOutlinedTextField(
        value = uiState.address,
        onValueChange = onAddressChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.dsh_host_address_hint)) },
        supportingText = { Text(stringResource(helperRes)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Computer,
                contentDescription = null,
                tint = AppTheme.colors.getPrimaryColor()
            )
        },
        trailingIcon = {
            if (uiState.address.isNotEmpty()) {
                IconButton(onClick = { onAddressChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.dsh_clear_input),
                        tint = AppTheme.colors.getOnInactiveContainerColor()
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
    )

    Spacer(Modifier.height(12.dp))

    // 连接 / 断开按钮
    if (phase == ConnectionPhase.Down) {
        EnhancedButton(
            onClick = onConnect,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.address.isNotBlank()
        ) {
            Text(
                text = stringResource(R.string.dsh_connect),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dsh_disconnect))
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(R.string.dsh_connect_tips),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    tips.forEach { tipRes ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                fontSize = 13.sp,
                color = AppTheme.colors.getPrimaryColor()
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(tipRes),
                fontSize = 13.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
    }

    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onTrouble) {
        Text(
            text = stringResource(R.string.dsh_connect_trouble),
            fontSize = 13.sp,
            color = AppTheme.colors.getPrimaryColor()
        )
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
            R.string.dsh_status_down to MaterialTheme.colorScheme.onErrorContainer
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
                color = MaterialTheme.colorScheme.onErrorContainer,
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
                color = MaterialTheme.colorScheme.onErrorContainer,
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
        // 明示兼容范围:版本号是 App 侧的协议锚点(0.1.5 起主机不再上报版本)
        Text(
            text = stringResource(R.string.dsh_wire_compat_note, info.version),
            fontSize = 12.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )
        info.home?.let { home ->
            Text(
                text = home,
                fontSize = 12.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
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
        // 扫码(推荐)与生成绑定码(CLI 兜底):各自单行放得下就并排,否则整行堆叠,避免半宽把文案折断
        CloudActionButtons(
            scanLabel = stringResource(
                if (cloud.claiming) R.string.dsh_cloud_claiming
                else R.string.dsh_cloud_scan_connect
            ),
            generateLabel = stringResource(
                if (cloud.requesting) R.string.dsh_cloud_generating_code
                else R.string.dsh_cloud_generate_code
            ),
            scanEnabled = !cloud.claiming,
            generateEnabled = !cloud.requesting,
            onScan = {
                scanPermissionDenied = false
                scanLauncher.launch(null)
            },
            onGenerate = onGenerateCode,
        )
        if (scanPermissionDenied) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dsh_camera_permission),
                color = MaterialTheme.colorScheme.onErrorContainer,
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
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 13.sp
            )
        }
        // 401(token 失效/游客账号):本地 token 已清,给一个直达登录页的入口
        if (cloud.authExpired) {
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { onNavigate(Screen.Login()) }) {
                Text(stringResource(R.string.dsh_cloud_login_action))
            }
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
                    MaterialTheme.colorScheme.onErrorContainer
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
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
