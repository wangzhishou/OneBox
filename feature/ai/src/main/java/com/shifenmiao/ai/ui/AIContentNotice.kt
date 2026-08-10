package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.channel.FlavorType

@Composable
fun AIContentNotice(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    // "AI 生成内容"声明是国内合规要求,海外(google)渠道不展示
    val showNotice = isVisible && FlavorType.fromName() != FlavorType.GOOGLE
    AnimatedVisibility(
        modifier = modifier.padding(top = 20.dp, bottom = 30.dp),
        visible = showNotice,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            modifier = modifier.fillMaxWidth(),
            text = stringResource(R.string.ai_content_notice),
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )
    }
}
