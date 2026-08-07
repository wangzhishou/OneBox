package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.data.SpeedTestConfig

import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

/**
 * 新增 / 编辑测速配置弹窗
 *
 * @param visible  是否显示
 * @param initial  null = 新增，非 null = 编辑（带入现有数据）
 * @param onSave   保存回调，返回填写后的配置
 * @param onDismiss 关闭回调
 */
@Composable
fun SpeedTestConfigDialog(
    visible: Boolean,
    initial: SpeedTestConfig?,
    onSave: (SpeedTestConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = initial != null && initial.id > 0

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var url by remember { mutableStateOf(initial?.testUrl ?: "") }
    var estimatedMb by remember {
        mutableStateOf((initial?.estimatedDataMb ?: SpeedTestConfig.DEFAULT_ESTIMATED_MB).toString())
    }
    var duration by remember {
        mutableStateOf((initial?.durationSeconds ?: SpeedTestConfig.DEFAULT_DURATION_SECONDS).toString())
    }

    // 当编辑目标变化时，重置所有输入框状态（修复回填问题）
    LaunchedEffect(initial?.id) {
        name = initial?.name ?: ""
        url = initial?.testUrl ?: ""
        estimatedMb = (initial?.estimatedDataMb ?: SpeedTestConfig.DEFAULT_ESTIMATED_MB).toString()
        duration = (initial?.durationSeconds ?: SpeedTestConfig.DEFAULT_DURATION_SECONDS).toString()
    }

    val nameError = name.isBlank()
    val urlError = url.isBlank()

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isEdit) R.string.speed_test_config_edit_title
                    else R.string.speed_test_config_add_title
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 配置名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.speed_test_config_name_hint)) },
                    singleLine = true,
                    isError = nameError && name.isNotEmpty().not(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )

                // 测速 URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.speed_test_config_url_hint)) },
                    singleLine = true,
                    isError = urlError && url.isNotEmpty().not(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )

                Spacer(Modifier.height(4.dp))

                // 预估流量 & 超时时长（同一行）
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = estimatedMb,
                        onValueChange = { v -> if (v.all { it.isDigit() }) estimatedMb = v },
                        label = { Text(stringResource(R.string.speed_test_config_estimated_mb_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getOutlinedTextFieldColors()
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { v -> if (v.all { it.isDigit() }) duration = v },
                        label = { Text(stringResource(R.string.speed_test_config_duration_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        colors = AppTheme.colors.getOutlinedTextFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || url.isBlank()) return@TextButton
                    onSave(
                        SpeedTestConfig(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            testUrl = url.trim(),
                            estimatedDataMb = estimatedMb.toIntOrNull()
                                ?: SpeedTestConfig.DEFAULT_ESTIMATED_MB,
                            durationSeconds = duration.toIntOrNull()
                                ?: SpeedTestConfig.DEFAULT_DURATION_SECONDS,
                            isPreset = initial?.isPreset ?: false
                        )
                    )
                },
                enabled = name.isNotBlank() && url.isNotBlank(),
                colors = AppTheme.colors.buttonColors()
            ) {
                Text(
                    stringResource(R.string.speed_test_save),
                    color = AppTheme.colors.getPrimaryColor()
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = AppTheme.colors.buttonColors()
            ) { Text(stringResource(R.string.speed_test_cancel)) }
        }
    )
}

