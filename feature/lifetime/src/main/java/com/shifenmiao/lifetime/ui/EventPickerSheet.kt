/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.lifetime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.wanbaohe.base.ui.icon.getIconFromKey
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyType
import com.shifenmiao.lifetime.util.localizedEventUnit
import com.shifenmiao.lifetime.util.localizedPresetEventName
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar

/**
 * 时光频率管理面板。
 *
 * 从底部弹出（[EnhancedModalBottomSheet]），标题 + 列表 + 底部"新增"，
 * 列表里点击切换启用状态，自建事件可删除。
 */
@Composable
fun EventPickerSheet(
    visible: Boolean,
    allEvents: List<FrequencyEvent>,
    onDismiss: () -> Unit,
    onToggleEvent: (FrequencyEvent) -> Unit,
    onDeleteEvent: (FrequencyEvent) -> Unit = {},
    onEditEvent: (FrequencyEvent) -> Unit = {},
    onAddNewEvent: () -> Unit
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        // 从「管理」进入，标题跟卡片分区保持一致，不再用"选择时光频率"
        title = stringResource(R.string.lifetime_frequency_section),
        nestedScrollEnabled = true,
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(R.string.lifetime_event_picker_add),
                onClick = onAddNewEvent,
                leadingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth()
            )
        },
        sheetContent = {
            val customEvents = allEvents.filter { !it.isPreset }
            val recommendedEvents = allEvents.filter { it.isRecommended }
            val otherPresetEvents = allEvents.filter { it.isPreset && !it.isRecommended }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                contentPadding = PaddingValues(
                    start = OneBoxDesignSystem.screenPadding,
                    end = OneBoxDesignSystem.screenPadding,
                    top = OneBoxDesignSystem.compactSpacing,
                    bottom = OneBoxDesignSystem.blockSpacing
                ),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                if (customEvents.isNotEmpty()) {
                    item {
                        OneBoxSectionHeader(
                            title = stringResource(R.string.lifetime_event_picker_custom)
                        )
                    }
                    items(customEvents, key = { it.id }) { event ->
                        EventPickerItem(
                            event = event,
                            canDelete = true,
                            onClick = { onToggleEvent(event) },
                            onDelete = { onDeleteEvent(event) },
                            onEdit = { onEditEvent(event) }
                        )
                    }
                }

                if (recommendedEvents.isNotEmpty()) {
                    item(key = "header_recommended") {
                        if (customEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(OneBoxDesignSystem.microSpacing))
                            OneBoxSectionHeader(
                                title = stringResource(R.string.lifetime_event_picker_recommended)
                            )
                        }
                    }
                    items(recommendedEvents, key = { it.id }) { event ->
                        EventPickerItem(
                            event = event,
                            onClick = { onToggleEvent(event) },
                            onEdit = { onEditEvent(event) }
                        )
                    }
                }

                if (otherPresetEvents.isNotEmpty()) {
                    item(key = "header_preset") {
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        OneBoxSectionHeader(
                            title = stringResource(R.string.lifetime_event_picker_preset)
                        )
                    }
                    items(otherPresetEvents, key = { it.id }) { event ->
                        EventPickerItem(
                            event = event,
                            onClick = { onToggleEvent(event) },
                            onEdit = { onEditEvent(event) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun EventPickerItem(
    event: FrequencyEvent,
    canDelete: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
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
            if (onEdit != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.lifetime_edit_event),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(OneBoxDesignSystem.microSpacing))
            }
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
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (event.isEnabled) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.lifetime_event_added),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
