package com.shifenmiao.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.resources.icons.Close

@Composable
fun AIDuelPersonaHistoryPickerBottomSheet(
    visible: Boolean,
    title: String,
    items: List<String>,
    onSelected: (String) -> Unit,
    onDismiss: (Boolean) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { onDismiss(true) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {}
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .heightIn(min = 400.dp)
                .padding(horizontal = 20.dp)
                .padding(vertical = AppTheme.dimens.spaceNormal)
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ai_duel_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = items,
                        key = { index, _ -> index }
                    ) { _, persona ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onSelected(persona)
                                    onDismiss(true)
                                }
                                .padding(
                                    horizontal = AppTheme.dimens.spaceNormal,
                                    vertical = AppTheme.dimens.spaceSmall
                                )
                        ) {
                            Text(
                                text = persona.replace('\n', ' ').trim()
                                    .ifBlank { stringResource(R.string.ai_duel_choose_history_prompt) },
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
