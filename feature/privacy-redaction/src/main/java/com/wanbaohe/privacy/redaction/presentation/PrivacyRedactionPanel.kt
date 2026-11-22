/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButtonGroup
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSlider
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.privacy.redaction.R
import com.wanbaohe.privacy.redaction.domain.model.PrivacyCategory
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument
import com.wanbaohe.privacy.redaction.domain.model.PrivacyMask
import com.wanbaohe.privacy.redaction.presentation.model.PrivacyAnalysisStage
import com.wanbaohe.privacy.redaction.presentation.model.PrivacyAnalysisState
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.R as CoreR

@Composable
fun PrivacyRedactionPanel(
    document: PrivacyDocument,
    analysisState: PrivacyAnalysisState,
    selectedRegionId: String?,
    isDrawing: Boolean,
    onRescan: () -> Unit,
    onSelectRegion: (String?) -> Unit,
    onToggleRegion: (String, Boolean) -> Unit,
    onDeleteRegion: (String) -> Unit,
    onDrawingChange: (Boolean) -> Unit,
    onMaskChange: (PrivacyMask) -> Unit,
    onStrengthChange: (Float) -> Unit,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.privacy_regions_found, document.regions.size),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            EnhancedIconButton(
                onClick = onRescan,
                enabled = !analysisState.isRunning,
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.privacy_rescan),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        when {
            analysisState.isRunning -> AnalysisProgress(analysisState)
            analysisState.stage == PrivacyAnalysisStage.FAILED -> Text(
                text = stringResource(R.string.privacy_analysis_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            document.regions.isEmpty() -> Text(
                text = stringResource(R.string.privacy_no_regions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        EnhancedButtonGroup(
            items = listOf(
                stringResource(R.string.privacy_mosaic),
                stringResource(R.string.privacy_solid),
            ),
            selectedIndex = if (document.mask == PrivacyMask.MOSAIC) 0 else 1,
            onIndexChange = { index ->
                onMaskChange(if (index == 0) PrivacyMask.MOSAIC else PrivacyMask.SOLID)
            },
        )
        if (document.mask == PrivacyMask.MOSAIC) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.privacy_strength),
                    style = MaterialTheme.typography.bodyMedium,
                )
                EnhancedSlider(
                    value = document.strength,
                    onValueChange = onStrengthChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
                Text(
                    text = "${(document.strength * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ConfirmButton(
            text = stringResource(
                if (isDrawing) R.string.privacy_finish_drawing else R.string.privacy_add_region
            ),
            onClick = { onDrawingChange(!isDrawing) },
            modifier = Modifier.fillMaxWidth(),
        )
        document.regions.forEachIndexed { index, region ->
            val categoryIndex = document.regions.take(index + 1).count {
                it.category == region.category
            }
            GlassCard(
                onClick = { onSelectRegion(region.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (selectedRegionId == region.id) {
                            Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primaryContainer,
                                ShapeDefaults.large,
                            )
                        } else Modifier
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(region.category.titleRes())} $categoryIndex",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedRegionId == region.id) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GlassSwitch(
                        checked = region.isEnabled,
                        onCheckedChange = { onToggleRegion(region.id, it) },
                    )
                    EnhancedIconButton(
                        onClick = { onDeleteRegion(region.id) },
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(CoreR.string.delete),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
        OneSecondaryButton(
            text = stringResource(R.string.privacy_disable),
            onClick = onDisable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AnalysisProgress(state: PrivacyAnalysisState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val progress = state.progress
        if (state.stage == PrivacyAnalysisStage.PREPARING_MODEL && progress != null) {
            GlassLinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            GlassLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Text(
            text = when {
                state.stage == PrivacyAnalysisStage.PREPARING_MODEL && progress != null ->
                    stringResource(R.string.privacy_model_percent, (progress * 100).roundToInt())
                state.stage == PrivacyAnalysisStage.PREPARING_MODEL ->
                    stringResource(R.string.privacy_preparing_model)
                else -> stringResource(R.string.privacy_recognizing)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun PrivacyCategory.titleRes(): Int = when (this) {
    PrivacyCategory.PHONE -> R.string.privacy_category_phone
    PrivacyCategory.ID_CARD -> R.string.privacy_category_id
    PrivacyCategory.BANK_CARD -> R.string.privacy_category_bank
    PrivacyCategory.EMAIL -> R.string.privacy_category_email
    PrivacyCategory.MANUAL -> R.string.privacy_category_manual
}
