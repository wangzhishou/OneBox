package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconAvatar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeCountdownDetailComponent
import com.shifenmiao.lifetime.domain.CountdownCalculator
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.ui.NoteSection
import com.shifenmiao.lifetime.util.countdownStatusLabel
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.resources.icons.Delete

@Composable
fun LifeTimeCountdownDetailScreen(
    component: LifeTimeCountdownDetailComponent
) {
    val uiState by component.uiState.collectAsState()
    val item = uiState.countdown

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        ExitWithoutSavingDialog(
            title = stringResource(R.string.lifetime_delete_confirm_title),
            text = stringResource(R.string.lifetime_delete_confirm_message),
            onExit = {
                showDeleteConfirm = false
                component.deleteCountdown()
            },
            onDismiss = { showDeleteConfirm = false },
            visible = showDeleteConfirm
        )
    }

    BaseScreen(
        title = item?.let { localizedPresetEventName(it.name, it.isPreset) }
            ?: stringResource(R.string.lifetime_countdown_detail_title),
        onGoBack = { component.onGoBack() },
        actions = {
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.lifetime_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        if (item == null) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.lifetime_loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val status = remember(item) {
                CountdownCalculator().calculate(item, java.time.LocalDate.now())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(
                        horizontal = OneBoxDesignSystem.screenPadding,
                        vertical = OneBoxDesignSystem.compactSpacing
                    ),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing)
            ) {
                CountdownHeroCard(status = status)

                if (item.isFromHoliday) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OneBoxDesignSystem.sectionCardShape,
                        containerAlpha = 0.22f,
                    ) {
                        Text(
                            text = stringResource(R.string.lifetime_countdown_holiday_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding)
                        )
                    }
                }

                NoteSection(
                    note = item.note,
                    onSave = { component.updateNote(it) },
                    onDelete = { component.updateNote("") },
                    placeholder = stringResource(R.string.lifetime_countdown_note_placeholder),
                )
            }
        }
    }
}

@Composable
private fun CountdownHeroCard(status: CountdownStatus) {
    val item = status.event
    val accent = when {
        status.isToday -> MaterialTheme.colorScheme.secondary
        status.isPast -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = OneBoxDesignSystem.sectionCardShape,
        containerAlpha = 0.22f,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            IconAvatar(
                iconName = item.iconKey,
                fallbackName = item.name,
                size = 56.dp,
                containerColor = accent.copy(alpha = 0.16f),
                tint = accent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(OneBoxDesignSystem.smallRadius),
                iconSizeRatio = 0.6f,
            )
            Text(
                text = localizedPresetEventName(item.name, item.isPreset),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (status.isToday) "0" else status.daysUntil.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = androidx.compose.ui.unit.TextUnit(64f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = accent
                )
                Text(
                    text = stringResource(R.string.lifetime_unit_days),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accent.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = countdownStatusLabel(status),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (status.nextOccurrence != null) {
                val next = status.nextOccurrence
                Text(
                    text = stringResource(R.string.lifetime_date_ymd, next.year, next.monthValue, next.dayOfMonth),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
