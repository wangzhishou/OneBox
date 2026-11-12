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

package com.wanbaohe.decisionwheel.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.DecisionWheelRouterComponent
import kotlin.math.roundToInt

@Composable
fun DecisionWheelSettingsScreen(
    router: DecisionWheelRouterComponent,
    bottomInset: Dp = 0.dp
) {
    val settings by router.settings.collectAsState()

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.wheel_settings),
        onGoBack = router::onTabBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))

            OneBoxSectionCard {
                DurationSetting(
                    value = settings.spinDurationMillis,
                    onValueChange = {
                        router.updateSettings(settings.copy(spinDurationMillis = it))
                    }
                )
            }

            OneBoxSectionCard(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.settingRowSpacing)
            ) {
                ToggleSetting(
                    title = stringResource(R.string.settings_sound),
                    subtitle = stringResource(R.string.settings_sound_note),
                    checked = settings.soundEnabled,
                    onCheckedChange = {
                        router.updateSettings(settings.copy(soundEnabled = it))
                    }
                )
                ToggleSetting(
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_note),
                    checked = settings.hapticsEnabled,
                    onCheckedChange = {
                        router.updateSettings(settings.copy(hapticsEnabled = it))
                    }
                )
                ToggleSetting(
                    title = stringResource(R.string.settings_remove_on_spin),
                    subtitle = stringResource(R.string.settings_remove_on_spin_note),
                    checked = settings.removeOnSpin,
                    onCheckedChange = {
                        router.updateSettings(settings.copy(removeOnSpin = it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

/**
 * 旋转时长。拖动中只改本地值，松手才落库 —— 否则每帧都会往 MMKV 写一次。
 */
@Composable
private fun DurationSetting(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var local by remember(value) { mutableFloatStateOf(value.toFloat()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.wheel_settings_duration),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.wheel_settings_duration_value,
                    local.roundToInt()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
        GlassCustomSlider(
            value = local,
            onValueChange = { local = it },
            valueRange = 1500f..8000f,
            steps = 12,
            onValueChangeFinished = { onValueChange(local.roundToInt()) }
        )
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = AppTheme.colors.switchColors()
        )
    }
}
