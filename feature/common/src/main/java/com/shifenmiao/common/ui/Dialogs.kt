package com.shifenmiao.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.base.ui.ConfirmContentDialog
import com.shifenmiao.base.ui.MarkdownLazyContent
import com.shifenmiao.base.ui.styleVerticalScrollbar
import com.shifenmiao.common.utils.ShareUtils
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.model.event.AgreePrivacyPolicyEvent
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity

@Composable
fun PrivacyPolicyDialog(
    showPrivacyPolicyDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
    onUrlClick: (String) -> Unit
) {
    if (showPrivacyPolicyDialog.value) {
        val listState = rememberLazyListState()
        val isFirstLaunch = AppSharedStorage.loadPrivacyPolicyVersion() == 0
        val componentActivity = LocalComponentActivity.current

        val markdown = remember(componentActivity) {
            componentActivity.resources.openRawResource(R.raw.privacy_policy)
                .bufferedReader().use { it.readText() }
        }
        ConfirmContentDialog(
            onDismissRequest = {
                // 不允许用户通过点击外部来关闭对话框
            },
            title = if (isFirstLaunch) {
                stringResource(R.string.privacy_title)
            } else {
                stringResource(R.string.privacy_update_title)
            },
            confirmButtonText = stringResource(R.string.privacy_button_agree),
            dismissButtonText = stringResource(R.string.privacy_button_disagree),
            showDialog = showPrivacyPolicyDialog.value,
            onConfirm = {
                showPrivacyPolicyDialog.value = false
                AppSharedStorage.savePrivacyPolicyVersion(Constants.PRIVACY_POLICY_VERSION)
                AppEventBus.emit(AgreePrivacyPolicyEvent(true))
                onConfirm.invoke()
            },
            onDismiss = {
                componentActivity.finishAffinity()
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                MarkdownLazyContent(
                    message = markdown,
                    modifier = Modifier
                        .fillMaxSize()
                        .styleVerticalScrollbar(listState),
                    lazyListState = listState,
                    onLinkClick = onUrlClick,
                    paddingValues = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WeChatConfirmDialog(
    showDialogState: MutableState<Boolean>,
    title: String = stringResource(R.string.dialog_title),
    message: String = stringResource(R.string.profile_item_wechat_tips),
    copValue: String = stringResource(R.string.profile_item_wechat_account),
    confirmButtonText: String = stringResource(id = R.string.button_copy),
    dismissButtonText: String = stringResource(id = R.string.button_share_to_wechat),
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (!Wechat.isEnabled) return
    if (showDialogState.value) {
        ConfirmContentDialog(
            showDialog = showDialogState,
            title = title,
            confirmButtonText = confirmButtonText,
            dismissButtonText = dismissButtonText,
            onConfirm = {
                showDialogState.value = false
                Clipboard.copy(copValue)
                Wechat.launch()
                onConfirm()
            },
            onDismiss = {
                showDialogState.value = false
                onDismiss()
                ShareUtils.shareWechatQRCode()
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.wechat_qrcode),
                    contentDescription = "WeChat QR Code",
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = message,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}