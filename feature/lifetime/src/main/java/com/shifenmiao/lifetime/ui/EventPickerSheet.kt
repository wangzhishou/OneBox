package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.getIconFromKey
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyType
import com.shifenmiao.lifetime.util.localizedEventUnit
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar

@Composable
fun EventPickerSheet(
    visible: Boolean,
    allEvents: List<FrequencyEvent>,
    onDismiss: () -> Unit,
    onToggleEvent: (FrequencyEvent) -> Unit,
    onDeleteEvent: (FrequencyEvent) -> Unit = {},
    onAddNewEvent: () -> Unit
) {
    EnhancedModalTopSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = {
             Text(
                text = stringResource(R.string.lifetime_event_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(R.string.lifetime_event_picker_add),
                onClick = onAddNewEvent,
                leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth()
            )
        },
        maxContentHeight = 450.dp
    ) {
        val customEvents = allEvents.filter { !it.isPreset }
        val recommendedEvents = allEvents.filter { it.isRecommended }
        val otherPresetEvents = allEvents.filter { it.isPreset && !it.isRecommended }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
        ) {
            if (customEvents.isNotEmpty()) {
                item {
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_event_picker_custom)
                    )
                }
                items(customEvents) { event ->
                    EventPickerItem(
                        event = event,
                        canDelete = true,
                        onClick = { onToggleEvent(event) },
                        onDelete = { onDeleteEvent(event) }
                    )
                }
            }

            if (recommendedEvents.isNotEmpty()) {
                item {
                    if (customEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(OneBoxDesignSystem.microSpacing))
                        OneBoxSectionHeader(
                            title = stringResource(R.string.lifetime_event_picker_recommended)
                        )
                    }
                }
                items(recommendedEvents) { event ->
                    EventPickerItem(
                        event = event,
                        onClick = { onToggleEvent(event) }
                    )
                }
            }

            if (otherPresetEvents.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_event_picker_preset)
                    )
                }
                items(otherPresetEvents) { event ->
                    EventPickerItem(
                        event = event,
                        onClick = { onToggleEvent(event) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
            }
        }
    }
}

@Composable
private fun EventPickerItem(
    event: FrequencyEvent,
    canDelete: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val icon = remember(event.iconKey) { getIconFromKey(event.iconKey) }

    OneBoxListItem(
        headlineContent = {
            Text(
                text = localizedPresetEventName(event.name, event.isPreset),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        subtitle = {
            Text(
                text = getFrequencyDescription(event),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingContent = {
            if (canDelete && onDelete != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(OneBoxDesignSystem.compactSpacing))
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (event.isEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (event.isEnabled) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.lifetime_event_added),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        onClick = onClick,
        contained = true
    )
}

@Composable
private fun getFrequencyDescription(event: FrequencyEvent): String {
    val frequencyText = when (event.frequencyType) {
        FrequencyType.ONE_TIME -> stringResource(R.string.lifetime_frequency_one_time)
        FrequencyType.DAILY -> stringResource(R.string.lifetime_frequency_daily)
        FrequencyType.WEEKLY -> stringResource(R.string.lifetime_frequency_weekly)
        FrequencyType.MONTHLY -> stringResource(R.string.lifetime_frequency_monthly)
        FrequencyType.YEARLY -> stringResource(R.string.lifetime_frequency_yearly)
    }
    return "$frequencyText ${event.timesPerPeriod} ${localizedEventUnit(event.unit)}"
}
