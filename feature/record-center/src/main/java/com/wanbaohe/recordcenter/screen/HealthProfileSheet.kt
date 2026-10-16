package com.wanbaohe.recordcenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.data.HealthProfile

/**
 * 基础信息编辑弹层:复用 [HealthProfileForm]。
 * imePadding + 导航栏 padding 适配;非法输入由表单内 toast 拦截,不保存。
 */
@Composable
fun HealthProfileSheet(
    visible: Boolean,
    initial: HealthProfile,
    onSave: (HealthProfile) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    EnhancedModalBottomSheet(
        visible = true,
        title = stringResource(R.string.record_center_profile_title),
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.record_center_profile_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HealthProfileForm(
                    initial = initial,
                    onSave = onSave,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        },
    )
}
