package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.decisionwheel.R

/**
 * Decision Wheel settings sheet.
 *
 * Contract:
 * - **Inputs**: current [durationMillis] and [speedMultiplier]
 * - **Outputs**: callbacks with updated values when user taps apply/cancel
 * - **UX**: ultra-flat (no shadows), colors reusing [AppTheme] where possible
 *
 * Edge cases handled:
 * - When [enabled] is false (e.g. wheel is spinning), controls are disabled.
 * - Values are clamped by the caller on apply.
 */
@Composable
fun DecisionWheelSettingsSheet(
    visible: Boolean,
    durationMillis: Int,
    speedMultiplier: Float,
    enabled: Boolean = true,
    onDismiss: () -> Unit,
    onApply: (durationMillis: Int, speedMultiplier: Float) -> Unit
) {
    val durationState: MutableFloatState = remember(durationMillis) {
        mutableFloatStateOf(durationMillis.toFloat())
    }
    val speedState: MutableFloatState = remember(speedMultiplier) {
        mutableFloatStateOf(speedMultiplier)
    }

    // Live preview model:
    // - Default spin duration is 4000ms in DecisionWheelScreen.
    // - Base rotations come from Random(5..9) => mid value 7.
    // - We scale estimated rotations by (duration / 4000) and by speedMultiplier.
    // This keeps the preview responsive to both sliders without trying to exactly
    // replicate randomness.
    val estimatedRotations = remember(durationState.floatValue, speedState.floatValue) {
        val durationFactor = (durationState.floatValue / 4000f).coerceIn(0.5f, 2.0f)
        val speedFactor = speedState.floatValue.coerceIn(0.6f, 1.6f)
        7.0f * durationFactor * speedFactor
    }

    EnhancedModalTopSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        cancelable = enabled,
        title = {
            Text(
                text = stringResource(R.string.wheel_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier.padding(
                    start = AppTheme.dimens.paddingNormal,
                    bottom = AppTheme.dimens.paddingNormal,
                    end = AppTheme.dimens.paddingNormal
                ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal, Alignment.Top)
            ) {
                // Live preview (informational only)
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.wheel_settings_preview),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.wheel_settings_preview_rotations,
                                estimatedRotations
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.wheel_settings_preview_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wheel_settings_duration),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.wheel_settings_duration_value,
                            durationState.floatValue.toInt()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GlassCustomSlider(
                    value = durationState.floatValue,
                    onValueChange = { durationState.floatValue = it },
                    valueRange = 1500f..8000f,
                    steps = 12,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wheel_settings_speed),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.wheel_settings_speed_value,
                            speedState.floatValue
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GlassCustomSlider(
                    value = speedState.floatValue,
                    onValueChange = { speedState.floatValue = it },
                    valueRange = 0.6f..1.6f,
                    steps = 10,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    GlassTonalButton(
                        onClick = onDismiss,
                        enabled = true,
                        colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    GlassTonalButton(
                        onClick = {
                            if (!enabled) return@GlassTonalButton
                            onApply(durationState.floatValue.toInt(), speedState.floatValue)
                            onDismiss()
                        },
                        enabled = enabled,
                        colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    )
}
