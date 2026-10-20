package com.t8rin.imagetoolbox.feature.scan_qr_code.domain

import com.wanbaohe.core.ui.utils.navigation.Screen

enum class ScanCodeContentType {
    INTERNAL_DEEPLINK,
    IMAGE,
    WEB,
    TEXT
}

interface ScanCodeResultResolver {
    fun classify(raw: String): ScanCodeContentType
    fun resolveScreen(raw: String): Screen?
}

