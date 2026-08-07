/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.auth.AuthCodeError
import com.shifenmiao.model.auth.AuthCodeMode
import com.shifenmiao.model.auth.AuthCodeSetupStep
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * 全局授权码锁屏界面。
 *
 * 复用 [LockScreenBase] 实现解锁与首次设置授权码。
 *
 * 行为约定:
 * - 数字键盘第 4 行布局: [回退] [0] [动作键],三键均匀分布
 *   - 动作键在输入未满时显示 ✕ (取消) 图标,满 6 位时切换为 ✓ (确认) 图标
 *   - 动作键点击行为随图标切换:取消 → 调 [onCancel];确认 → 调 [onSubmit] (无自动提交)
 * - 生物识别按钮 (若启用) 单独一行水平居中,放在键盘下方
 * - 返回键 (系统 Back) 触发取消
 * - 首次设置 (Setup) 模式下额外显示 "请牢记" 提示
 *
 * @param mode 解锁模式或设置模式
 * @param setupStep 设置模式下的步骤,仅在 [mode] 为 Setup 时使用
 * @param onSubmit 用户在确认键 (满 6 位时) 点击时回调
 * @param onCancel 取消键点击 / 系统返回时回调
 * @param error 错误类型,由 UI 翻译为对应语言的字符串
 * @param modifier 外部修饰符
 */
@Composable
fun AuthorizationCodeScreen(
    mode: AuthCodeMode,
    setupStep: AuthCodeSetupStep = AuthCodeSetupStep.Enter,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    error: AuthCodeError? = null,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onCancel)

    val title = when {
        mode == AuthCodeMode.Unlock ->
            stringResource(R.string.auth_code_title)
        setupStep == AuthCodeSetupStep.Enter ->
            stringResource(R.string.auth_code_title_setup)
        else ->
            stringResource(R.string.auth_code_title_confirm)
    }

    val defaultStatus = when {
        mode == AuthCodeMode.Unlock ->
            stringResource(R.string.auth_code_status_unlock)
        setupStep == AuthCodeSetupStep.Enter ->
            stringResource(R.string.auth_code_status_setup)
        else ->
            stringResource(R.string.auth_code_status_confirm)
    }

    val errorMessage = error?.let {
        stringResource(
            when (it) {
                AuthCodeError.WrongCode -> R.string.auth_code_wrong
                AuthCodeError.Mismatch -> R.string.auth_code_mismatch
            }
        )
    }

    val pinLength = 6

    when (mode) {
        AuthCodeMode.Unlock ->
            UnlockContent(
                pinLength = pinLength,
                title = title,
                defaultStatus = defaultStatus,
                errorMessage = errorMessage,
                onSubmit = onSubmit,
                onCancel = onCancel,
                modifier = modifier,
            )

        AuthCodeMode.Setup ->
            SetupContent(
                pinLength = pinLength,
                title = title,
                defaultStatus = defaultStatus,
                errorMessage = errorMessage,
                setupStep = setupStep,
                onSubmit = onSubmit,
                onCancel = onCancel,
                modifier = modifier,
            )
    }
}

@Composable
private fun UnlockContent(
    pinLength: Int,
    title: String,
    defaultStatus: String,
    errorMessage: String?,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember { mutableStateOf("") }
    val isShowingError = errorMessage != null && input.isEmpty()
    val status = if (isShowingError) LockScreenStatus.Error else LockScreenStatus.Awaiting
    val statusText = if (isShowingError) errorMessage.orEmpty() else defaultStatus
    val canConfirm = input.length == pinLength

    LockScreenBase(
        pinLength = pinLength,
        filledLength = input.length,
        actions = LockScreenActions(
            onDigit = { digit ->
                if (input.length < pinLength) {
                    input += digit
                }
            },
            onBackspace = { input = input.dropLast(1) },
            onClear = { input = "" },
            onAction = {
                if (canConfirm) onSubmit(input) else onCancel()
            },
            onEmergency = onCancel,
        ),
        showBiometric = false,
        title = title,
        statusText = statusText,
        status = status,
        pinLabel = stringResource(R.string.auth_code_pin_label),
        actionKeyIcon = if (canConfirm) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check else com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
        actionKeyTint = if (canConfirm) MaterialTheme.colorScheme.primary else null,
        modifier = modifier,
    )
}

@Composable
private fun SetupContent(
    pinLength: Int,
    title: String,
    defaultStatus: String,
    errorMessage: String?,
    setupStep: AuthCodeSetupStep,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(setupStep) { mutableStateOf("") }
    val isShowingError = errorMessage != null && input.isEmpty()
    val status = if (isShowingError) LockScreenStatus.Error else LockScreenStatus.Awaiting
    val statusText = if (isShowingError) errorMessage.orEmpty() else defaultStatus
    val canConfirm = input.length == pinLength

    LockScreenBase(
        pinLength = pinLength,
        filledLength = input.length,
        actions = LockScreenActions(
            onDigit = { digit ->
                if (input.length < pinLength) {
                    input += digit
                }
            },
            onBackspace = { input = input.dropLast(1) },
            onClear = { input = "" },
            onAction = {
                if (canConfirm) onSubmit(input) else onCancel()
            },
            onEmergency = onCancel,
        ),
        showBiometric = false,
        title = title,
        statusText = statusText,
        status = status,
        pinLabel = stringResource(R.string.auth_code_pin_label),
        actionKeyIcon = if (canConfirm) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check else com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
        actionKeyTint = if (canConfirm) MaterialTheme.colorScheme.primary else null,
        header = { SetupHint() },
        modifier = modifier
    )
}

@Composable
private fun SetupHint() {
    Text(
        text = stringResource(R.string.auth_code_setup_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}
