package com.wanbaohe.decisionwheel.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheel
import com.wanbaohe.decisionwheel.component.DecisionWheelRouterComponent

@Composable
fun DecisionWheelListScreen(
    router: DecisionWheelRouterComponent
) {
    val wheels by router.wheels.collectAsState()
    val currentId = router.spinComponent.uiState.collectAsState().value.currentWheel?.id
    var pendingDelete by remember { mutableStateOf<DecisionWheel?>(null) }

    BaseScreen(
        title = stringResource(R.string.my_wheels),
        onGoBack = router::navigateBack,
        actions = {
            IconButton(onClick = router::createWheel) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.create_wheel)
                )
            }
        }
    ) {
        if (wheels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_wheels),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wheels, key = { it.id }) { wheel ->
                    WheelRow(
                        wheel = wheel,
                        isCurrent = wheel.id == currentId,
                        onClick = { router.selectWheel(wheel.id) },
                        onDuplicate = { router.duplicateWheel(wheel.id) },
                        onDelete = { pendingDelete = wheel }
                    )
                }
            }
        }
    }

    pendingDelete?.let { wheel ->
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_wheel_confirm)) },
            confirmButton = {
                GlassTonalButton(
                    onClick = {
                        router.deleteWheel(wheel.id)
                        pendingDelete = null
                    },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { pendingDelete = null },
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WheelRow(
    wheel: DecisionWheel,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.listRowShape,
        containerAlpha = OneBoxDesignSystem.sectionGlassStyle.backgroundAlpha,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wheel.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fallback = MaterialTheme.colorScheme.primary
                    wheel.options.take(6).forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(option.color.takeIf { it != Color.Unspecified } ?: fallback)
                        )
                    }
                    Text(
                        text = stringResource(R.string.options_count, wheel.options.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isCurrent) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = stringResource(R.string.duplicate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDuplicate)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
