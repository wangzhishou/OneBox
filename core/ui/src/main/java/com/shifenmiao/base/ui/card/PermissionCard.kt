package com.shifenmiao.base.ui.card

import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.manageAppAllFilesIntent
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineManageSearch

/**
 * Card to request All Files Access Permission on Android 11+.
 */
@Composable
fun AllFilesAccessPermissionCard(
    modifier:Modifier = Modifier,
    onRequestAllFilesAccessSuccess: () -> Unit = { },
    onRequestAllFilesAccessFailed: () -> Unit = { }
) {
    val context = LocalContext.current
    fun hasAllFilesAccessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }
    // google 渠道按 Play 政策移除了 MANAGE_EXTERNAL_STORAGE 声明，
    // 未声明时系统设置页的开关是灰的、永远无法授权，不再展示引导卡片
    fun isAllFilesAccessDeclared(): Boolean = runCatching {
        context.packageManager
            .getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            ?.contains(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) == true
    }.getOrDefault(false)
    // 用于触发权限返回后的 UI 刷新
    var permissionInvalidator by remember { mutableIntStateOf(0) }
    val checkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionInvalidator++
    }
    LaunchedEffect(permissionInvalidator) {
        if (permissionInvalidator > 0) {
            if (hasAllFilesAccessPermission()) {
                onRequestAllFilesAccessSuccess()
            } else {
                onRequestAllFilesAccessFailed()
            }
        }
    }
    val needAllFilesAccessCard =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasAllFilesAccessPermission() &&
                isAllFilesAccessDeclared()
    if (needAllFilesAccessCard) {
        GlassCard(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.paddingNormal),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLarge)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineManageSearch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.all_files_access_permission_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
                    Text(
                        text = stringResource(R.string.all_files_access_permission_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
                    Button(
                        onClick = {
                            checkPermissionLauncher.launch(context.manageAppAllFilesIntent())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.go_to_grant),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
