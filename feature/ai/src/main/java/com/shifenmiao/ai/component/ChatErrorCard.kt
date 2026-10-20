package com.shifenmiao.ai.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.error.AiChatError
import com.shifenmiao.ai.error.AiChatErrorKind
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Block
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountWallet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLogin
import com.t8rin.imagetoolbox.core.resources.icons.line.LineServerModels
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifiError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifiOff
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassSurface

/**
 * AI 聊天错误卡片。
 *
 * 三段式：本地化的"是什么"标题 + 后台原文 / 本地化正文 + 可折叠技术详情。
 * 重试永远可用；积分不足时给"获取积分"，服务端/未知错误时给"切换模型"。
 */
@Composable
fun ChatErrorCard(
    error: AiChatError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onGetPoints: (() -> Unit)? = null,
    onSwitchModel: (() -> Unit)? = null,
) {
    val clipboard = LocalClipboardManager.current
    var showDetails by remember(error) { mutableStateOf(false) }
    val detailText = remember(error) { error.detailText() }

    Column(modifier = modifier.fillMaxWidth()) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = error.kind.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(error.kind.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                Spacer(Modifier.height(6.dp))
                SelectionContainer {
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                if (error.hint.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = error.hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.ai_stream_answer_retry),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (error.kind == AiChatErrorKind.POINTS && onGetPoints != null) {
                TextButton(
                    onClick = onGetPoints,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ai_chat_error_action_get_points),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            if (error.kind.switchesModelOnFailure && onSwitchModel != null) {
                TextButton(
                    onClick = onSwitchModel,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ai_chat_error_action_switch_model),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            TextButton(
                onClick = { showDetails = !showDetails },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(34.dp),
            ) {
                Text(
                    text = stringResource(
                        if (showDetails) {
                            R.string.ai_chat_error_details_hide
                        } else {
                            R.string.ai_chat_error_details
                        }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Icon(
                    imageVector = if (showDetails) {
                        Icons.Outlined.LineArrowDropUp
                    } else {
                        Icons.Outlined.LineArrowDropDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        AnimatedVisibility(visible = showDetails) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(10.dp),
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    SelectionContainer {
                        Text(
                            text = detailText,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(detailText))
                            AppToastHost.showToast(R.string.ai_chat_error_copied)
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.ai_chat_error_copy),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun AiChatError.detailText(): String {
    if (technicalDetail.isNotBlank()) return technicalDetail
    return buildString {
        statusCode?.let { append("HTTP ").append(it) }
        if (!code.isNullOrBlank()) {
            if (isNotEmpty()) append(" · ")
            append(code)
        }
    }.ifBlank { message }
}

private val AiChatErrorKind.switchesModelOnFailure: Boolean
    get() = this in setOf(
        AiChatErrorKind.SERVER,
        AiChatErrorKind.UNKNOWN,
        AiChatErrorKind.CLIENT,
        AiChatErrorKind.EMPTY_RESPONSE,
        AiChatErrorKind.CONTENT_FILTERED,
    )

private fun AiChatErrorKind.icon(): ImageVector = when (this) {
    AiChatErrorKind.OFFLINE -> Icons.Outlined.LineWifiOff
    AiChatErrorKind.NETWORK -> Icons.Outlined.LineWifiError
    AiChatErrorKind.TIMEOUT -> Icons.Outlined.LineTimer
    AiChatErrorKind.AUTH -> Icons.Outlined.LineLogin
    AiChatErrorKind.POINTS, AiChatErrorKind.PAYMENT -> Icons.Outlined.LineAccountWallet
    AiChatErrorKind.FORBIDDEN -> Icons.Outlined.LineLock
    AiChatErrorKind.RATE_LIMITED -> Icons.Outlined.LineAccessTime
    AiChatErrorKind.CONTENT_FILTERED -> Icons.Rounded.Block
    AiChatErrorKind.SERVER -> Icons.Outlined.LineServerModels
    AiChatErrorKind.EMPTY_RESPONSE -> Icons.Outlined.LineInfo
    AiChatErrorKind.INTERRUPTED -> Icons.Outlined.LineWifiError
    AiChatErrorKind.CLIENT, AiChatErrorKind.UNKNOWN -> Icons.Outlined.LineError
}
