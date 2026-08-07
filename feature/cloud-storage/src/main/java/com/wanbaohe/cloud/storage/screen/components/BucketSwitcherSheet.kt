package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder

@Composable
fun BucketSwitcherSheet(
    buckets: List<CloudBucket>,
    currentBucket: String?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        dragHandle = { },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.screenPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Text(
                text = stringResource(R.string.cloud_storage_bucket_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )

            if (buckets.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = stringResource(R.string.cloud_storage_no_bucket),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    buckets.forEach { bucket ->
                        val isCurrent = bucket.name == currentBucket
                        val tint = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            onClick = {
                                onSelect(bucket.name)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = bucket.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tint,
                                    modifier = Modifier.weight(1f),
                                )

                                Text(
                                    text = if (bucket.name == currentBucket) {
                                        stringResource(R.string.cloud_storage_current_bucket)
                                    } else {
                                        bucket.creationDate.orEmpty()
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (isCurrent) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
