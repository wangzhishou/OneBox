package com.shifenmiao.app.functions

import androidx.compose.foundation.ComposeFoundationFlags.isPausableCompositionInPrefetchEnabled
import androidx.compose.material3.ComposeMaterial3Flags.isCheckboxStylingFixEnabled
import com.arkivanov.decompose.DecomposeSettings
import com.arkivanov.decompose.ExperimentalDecomposeApi

@OptIn(ExperimentalDecomposeApi::class)
internal fun setupFlags() {
    isCheckboxStylingFixEnabled = true
    DecomposeSettings.update { it.copy(duplicateConfigurationsEnabled = true) }
    isPausableCompositionInPrefetchEnabled = true
}