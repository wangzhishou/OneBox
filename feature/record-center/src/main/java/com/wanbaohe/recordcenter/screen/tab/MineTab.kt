package com.wanbaohe.recordcenter.screen.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.data.HealthProfile
import com.wanbaohe.recordcenter.screen.HealthProfileForm

/**
 * 我的 tab:基础信息(性别/年龄/身高/体重)页内编辑,保存即写 MMKV。
 * 与列表页 AI 解读前的编辑弹层共用 [HealthProfileForm]。
 */
@Composable
fun MineTab(
    profile: HealthProfile,
    onSave: (HealthProfile) -> Unit,
) {
    val savedText = stringResource(R.string.record_center_save_success)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.record_center_profile_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.record_center_profile_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 最外层背景:glassThin + surfaceContainerLow,比 GlassCard 更轻薄
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassThin(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
                .padding(16.dp),
        ) {
            HealthProfileForm(
                initial = profile,
                onSave = {
                    onSave(it)
                    AppToastHost.showToast(savedText)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
