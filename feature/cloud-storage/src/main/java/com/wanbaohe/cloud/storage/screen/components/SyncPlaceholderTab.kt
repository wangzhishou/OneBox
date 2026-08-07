package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxGroupDivider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.cloud.storage.R

@Composable
fun SyncPlaceholderTab() {
    val checklist = stringArrayResource(R.array.cloud_storage_sync_checklist)
    Column(verticalArrangement = Arrangement.spacedBy(com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem.blockSpacing)) {
        OneBoxSectionCard {
            Text(
                text = stringResource(R.string.cloud_storage_sync_placeholder_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.cloud_storage_sync_placeholder_desc),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OneBoxSectionCard {
            checklist.forEachIndexed { index, item ->
                Text(text = item, style = MaterialTheme.typography.bodyMedium)
                if (index < checklist.lastIndex) {
                    OneBoxGroupDivider()
                }
            }
        }
    }
}
