package com.wanbaohe.core.ui.utils.navigation

enum class LandscapeShellMode {
    Standard,
    ContentOnly
}

val Screen.landscapeShellMode: LandscapeShellMode
    get() = when (this) {
        is Screen.ImagePreview,
        Screen.MeasurementTools -> LandscapeShellMode.ContentOnly

        else -> LandscapeShellMode.Standard
    }
