package com.shifenmiao.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.utils.ShareUtils
import com.shifenmiao.core.R
import com.shifenmiao.model.wechat.Wechat
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberRipple
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare

@Composable
fun WeChatQRCodeBox() {
    var showIcon by remember { mutableStateOf(false) }
    val contentColor = MaterialTheme.colorScheme.primary
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            showIcon = true
                            tryAwaitRelease()
                            showIcon = false
                        },
                        onTap = {
                            ShareUtils.shareWechatQRCode()
                        }
                    )
                }
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(R.drawable.wechat_qrcode),
                contentDescription = "WeChat QR Code",
            )
            if (showIcon) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.size(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                    ) {
                        ShareUtils.shareWechatQRCode()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                    modifier = Modifier
                        .size(12.dp),
                    contentDescription = null,
                    tint = contentColor
                )
                Text(
                    text = stringResource(id = R.string.button_share_qr_code),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                    ) {
                        Clipboard.copy(appContext.getString(R.string.profile_item_wechat_account))
                        Wechat.launch()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                    modifier = Modifier
                        .size(12.dp),
                    contentDescription = null,
                    tint = contentColor
                )
                Text(
                    text = stringResource(id = R.string.button_copy_wechat_public_account),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }

        }
    }

}