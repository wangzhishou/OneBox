package com.wanbaohe.cloud.storage.screen.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.model.RemoteProtocol
import com.wanbaohe.cloud.storage.model.S3Vendor
import java.util.UUID
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibilityOff

/**
 * 协议无关的连接配置表单：
 * 1. 顶部 Tab 切换 RemoteProtocol (S3 / WebDAV / SMB)
 * 2. 各协议自带一组动态字段
 * 3. S3 协议内嵌 S3Vendor 二级选择 + 凭据字段
 */
@Composable
fun ConnectionSheet(
    savedConnections: List<CloudStorageConnection>,
    initial: CloudStorageConnection?,
    onDismiss: () -> Unit,
    onSave: (CloudStorageConnection) -> Unit,
    onTestConnection: (CloudStorageConnection, (Result<Unit>) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val initialProtocol = initial?.protocol ?: RemoteProtocol.S3_COMPAT
    var protocol by remember { mutableStateOf(initialProtocol) }

    // 每个协议各保留一份未保存草稿
    val s3Draft = remember { mutableStateOf(s3DraftFromInitial(initial)) }
    val webDavDraft = remember { mutableStateOf(webDavDraftFromInitial(initial)) }
    val smbDraft = remember { mutableStateOf(smbDraftFromInitial(initial)) }

    var isTesting by remember { mutableStateOf(false) }
    val testConnectionSuccessStr = stringResource(R.string.cloud_storage_test_connection_success)
    val testConnectionFailedStr = stringResource(R.string.cloud_storage_test_connection_failed)

    val currentConnection: CloudStorageConnection
    val isInputValid: Boolean
    // 编辑已有连接时保留原 isDefault；新建时默认 true（首个连接自然成为默认）。
    val effectiveIsDefault: Boolean = initial?.isDefault ?: true
    when (protocol) {
        RemoteProtocol.S3_COMPAT -> {
            val d = s3Draft.value
            currentConnection = d.toConnection(initial?.id, effectiveIsDefault)
            isInputValid = d.endpoint.isNotBlank() && d.accessKeyId.isNotBlank() && d.secretAccessKey.isNotBlank()
        }
        RemoteProtocol.WEB_DAV -> {
            val d = webDavDraft.value
            currentConnection = d.toConnection(initial?.id, effectiveIsDefault)
            // WebDAV 至少要 baseUrl + username；password 允许空（部分服务器空口令放行）
            isInputValid = d.baseUrl.isNotBlank() && d.username.isNotBlank()
        }
        RemoteProtocol.SMB -> {
            val d = smbDraft.value
            currentConnection = d.toConnection(initial?.id, effectiveIsDefault)
            // SMB 只 host 必填。share / domain / username / password 全部可选：
            // - 空 share：创建后用 listRoots 列举可用 share（Adapter 当前返回空，后续接入 NetBIOS browse）
            // - 空 username + password：匿名 / Guest 访问
            // - 空 domain：工作组场景默认 WORKGROUP
            isInputValid = d.host.isNotBlank()
        }
    }

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        dragHandle = { },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cloud_storage_connection_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cloud_storage_cancel),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 协议 Tab
            OneBoxSectionCard {
                Text(
                    text = stringResource(R.string.cloud_storage_protocol_label),
                    style = MaterialTheme.typography.titleMedium,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(RemoteProtocol.entries) { p ->
                        ProtocolTab(
                            label = stringResource(p.titleRes),
                            selected = protocol == p,
                            onClick = { protocol = p },
                        )
                    }
                }
            }

            // 当前协议对应的动态表单
            when (protocol) {
                RemoteProtocol.S3_COMPAT -> S3CompatForm(draft = s3Draft)
                RemoteProtocol.WEB_DAV -> WebDavForm(draft = webDavDraft)
                RemoteProtocol.SMB -> SmbForm(draft = smbDraft)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OneSecondaryButton(
                    text = if (isTesting) "Testing..." else stringResource(R.string.cloud_storage_test_connection),
                    onClick = {
                        isTesting = true
                        onTestConnection(currentConnection) { res ->
                            isTesting = false
                            res.fold(
                                onSuccess = {
                                    Toast.makeText(context, testConnectionSuccessStr, Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    val err = it.message ?: ""
                                    Toast.makeText(context, "$testConnectionFailedStr: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = isInputValid && !isTesting,
                )
                Spacer(modifier = Modifier.width(8.dp))
                OnePrimaryButton(
                    text = stringResource(R.string.cloud_storage_save_connection),
                    onClick = { onSave(currentConnection) },
                    enabled = isInputValid && !isTesting,
                )
            }
        }
    }
}

// ──────────────── 可复用输入框封装 ────────────────

/**
 * 密文输入框：默认 ●●● 掩码，点眼睛切换明文；右侧有 ✕ 清除按钮（仅在有内容时出现）。
 */
@Composable
private fun SecretField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    OneBoxOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cloud_storage_clear_field),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                IconButton(
                    onClick = { visible = !visible },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (visible) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibilityOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                        contentDescription = if (visible) {
                            stringResource(R.string.cloud_storage_hide_secret)
                        } else {
                            stringResource(R.string.cloud_storage_show_secret)
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
    )
}

/**
 * 普通输入框：右侧 ✕ 清除按钮（仅在有内容时出现）。
 */
@Composable
private fun ClearableField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OneBoxOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cloud_storage_clear_field),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun ProtocolTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .height(38.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ───────────────── S3 兼容族表单 ─────────────────

@Composable
private fun S3CompatForm(draft: androidx.compose.runtime.MutableState<S3FormDraft>) {
    val d = draft.value
    OneBoxSectionCard {
        Text(
            text = stringResource(R.string.cloud_storage_vendor_label),
            style = MaterialTheme.typography.titleSmall,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(S3Vendor.entries) { v ->
                ProtocolTab(
                    label = stringResource(v.titleRes),
                    selected = d.vendor == v,
                    onClick = {
                        if (d.vendor != v) {
                            draft.value = d.copy(vendor = v, endpoint = v.defaultEndpointHint)
                        }
                    },
                )
            }
        }
        ClearableField(
            value = d.displayName,
            onValueChange = { draft.value = d.copy(displayName = it) },
            label = stringResource(R.string.cloud_storage_display_name),
        )
        ClearableField(
            value = d.endpoint,
            onValueChange = { draft.value = d.copy(endpoint = it) },
            label = stringResource(R.string.cloud_storage_endpoint),
            placeholder = "d.vendor.defaultEndpointHint",
        )
        ClearableField(
            value = d.region,
            onValueChange = { draft.value = d.copy(region = it) },
            label = stringResource(R.string.cloud_storage_region),
            placeholder = "cn-hangzhou / bj / gz",
        )
        ClearableField(
            value = d.bucket,
            onValueChange = { draft.value = d.copy(bucket = it) },
            label = stringResource(R.string.cloud_storage_bucket_name),
            placeholder = "my-bucket",
        )
        ClearableField(
            value = d.accessKeyId,
            onValueChange = { draft.value = d.copy(accessKeyId = it) },
            label = stringResource(R.string.cloud_storage_access_key),
            placeholder = "AKIDxxxxxxxxxxxx",
        )
        SecretField(
            value = d.secretAccessKey,
            onValueChange = { draft.value = d.copy(secretAccessKey = it) },
            label = stringResource(R.string.cloud_storage_secret_key),
            placeholder = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        )
    }
}

private data class S3FormDraft(
    val displayName: String = "",
    val vendor: S3Vendor = S3Vendor.ALIYUN_OSS,
    val endpoint: String = "",
    val region: String = "",
    val bucket: String = "",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
) {
    fun toConnection(id: String?, isDefault: Boolean): CloudStorageConnection.S3Compat =
        CloudStorageConnection.S3Compat(
            id = id ?: UUID.randomUUID().toString(),
            displayName = displayName.ifBlank { bucket },
            vendor = vendor,
            endpoint = endpoint.trim(),
            region = region.trim(),
            bucket = bucket.trim(),
            accessKeyId = accessKeyId.trim(),
            secretAccessKey = secretAccessKey.trim(),
            isDefault = isDefault,
        )
}

private fun s3DraftFromInitial(initial: CloudStorageConnection?): S3FormDraft {
    initial as? CloudStorageConnection.S3Compat ?: return S3FormDraft(
        vendor = S3Vendor.ALIYUN_OSS,
        endpoint = S3Vendor.ALIYUN_OSS.defaultEndpointHint,
    )
    return S3FormDraft(
        displayName = initial.displayName,
        vendor = initial.vendor,
        endpoint = initial.endpoint,
        region = initial.region,
        bucket = initial.bucket,
        accessKeyId = initial.accessKeyId,
        secretAccessKey = initial.secretAccessKey,
    )
}

// ───────────────── WebDAV 表单 ─────────────────

@Composable
private fun WebDavForm(draft: androidx.compose.runtime.MutableState<WebDavFormDraft>) {
    val d = draft.value
    OneBoxSectionCard {
        ClearableField(
            value = d.displayName,
            onValueChange = { draft.value = d.copy(displayName = it) },
            label = stringResource(R.string.cloud_storage_display_name),
        )
        ClearableField(
            value = d.baseUrl,
            onValueChange = { draft.value = d.copy(baseUrl = it) },
            label = stringResource(R.string.cloud_storage_webdav_base_url),
            placeholder = "https://dav.example.com/remote.php/webdav",
        )
        ClearableField(
            value = d.rootPath,
            onValueChange = { draft.value = d.copy(rootPath = it) },
            label = stringResource(R.string.cloud_storage_webdav_root_path),
            placeholder = "/",
        )
        ClearableField(
            value = d.username,
            onValueChange = { draft.value = d.copy(username = it) },
            label = stringResource(R.string.cloud_storage_webdav_username),
        )
        SecretField(
            value = d.password,
            onValueChange = { draft.value = d.copy(password = it) },
            label = stringResource(R.string.cloud_storage_webdav_password),
        )
    }
}

private data class WebDavFormDraft(
    val displayName: String = "",
    val baseUrl: String = "",
    val rootPath: String = "/",
    val username: String = "",
    val password: String = "",
) {
    fun toConnection(id: String?, isDefault: Boolean): CloudStorageConnection.WebDav =
        CloudStorageConnection.WebDav(
            id = id ?: UUID.randomUUID().toString(),
            displayName = displayName.ifBlank { baseUrl },
            baseUrl = baseUrl.trim(),
            username = username.trim(),
            password = password,
            rootPath = rootPath.trim().ifBlank { "/" },
            isDefault = isDefault,
        )
}

private fun webDavDraftFromInitial(initial: CloudStorageConnection?): WebDavFormDraft {
    initial as? CloudStorageConnection.WebDav ?: return WebDavFormDraft()
    return WebDavFormDraft(
        displayName = initial.displayName,
        baseUrl = initial.baseUrl,
        rootPath = initial.rootPath,
        username = initial.username,
        password = initial.password,
    )
}

// ───────────────── SMB 表单 ─────────────────

@Composable
private fun SmbForm(draft: androidx.compose.runtime.MutableState<SmbFormDraft>) {
    val d = draft.value
    OneBoxSectionCard {
        ClearableField(
            value = d.displayName,
            onValueChange = { draft.value = d.copy(displayName = it) },
            label = stringResource(R.string.cloud_storage_display_name),
        )
        ClearableField(
            value = d.host,
            onValueChange = { draft.value = d.copy(host = it) },
            label = stringResource(R.string.cloud_storage_smb_host),
            placeholder = "192.168.1.10 / nas.local",
        )
        OneBoxOutlinedTextField(
            value = d.port,
            onValueChange = { draft.value = d.copy(port = it.filter(Char::isDigit).take(5)) },
            label = { Text(stringResource(R.string.cloud_storage_smb_port)) },
            placeholder = { Text(stringResource(R.string.cloud_storage_smb_port_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ClearableField(
            value = d.share,
            onValueChange = { draft.value = d.copy(share = it) },
            label = stringResource(R.string.cloud_storage_smb_share),
            placeholder = stringResource(R.string.cloud_storage_smb_share_placeholder),
        )
        ClearableField(
            value = d.domain,
            onValueChange = { draft.value = d.copy(domain = it) },
            label = stringResource(R.string.cloud_storage_smb_domain),
            placeholder = stringResource(R.string.cloud_storage_smb_domain_placeholder),
        )
        ClearableField(
            value = d.username,
            onValueChange = { draft.value = d.copy(username = it) },
            label = stringResource(R.string.cloud_storage_smb_username),
            placeholder = stringResource(R.string.cloud_storage_smb_username_placeholder),
        )
        SecretField(
            value = d.password,
            onValueChange = { draft.value = d.copy(password = it) },
            label = stringResource(R.string.cloud_storage_smb_password),
            placeholder = stringResource(R.string.cloud_storage_smb_password_placeholder),
        )
    }
}

private data class SmbFormDraft(
    val displayName: String = "",
    val host: String = "",
    val port: String = "445",
    val share: String = "",
    val domain: String = "",
    val username: String = "",
    val password: String = "",
) {
    fun toConnection(id: String?, isDefault: Boolean): CloudStorageConnection.Smb =
        CloudStorageConnection.Smb(
            id = id ?: UUID.randomUUID().toString(),
            displayName = displayName.ifBlank { host },
            host = host.trim(),
            port = port.toIntOrNull() ?: 445,
            share = share.trim(),
            domain = domain.trim(),
            username = username.trim(),
            password = password,
            isDefault = isDefault,
        )
}

private fun smbDraftFromInitial(initial: CloudStorageConnection?): SmbFormDraft {
    initial as? CloudStorageConnection.Smb ?: return SmbFormDraft()
    return SmbFormDraft(
        displayName = initial.displayName,
        host = initial.host,
        port = initial.port.toString(),
        share = initial.share,
        domain = initial.domain,
        username = initial.username,
        password = initial.password,
    )
}
