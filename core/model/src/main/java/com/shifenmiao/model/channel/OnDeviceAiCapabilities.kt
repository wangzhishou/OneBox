package com.shifenmiao.model.channel

data class OnDeviceAiCapabilities(
    val isLocalChatSupported: Boolean,
    val isLocalTranslationSupported: Boolean,
) {
    val isLocalModelManagementSupported: Boolean
        get() = isLocalChatSupported || isLocalTranslationSupported

    companion object {
        fun fromFlavor(
            flavorType: FlavorType = FlavorType.fromName(),
        ): OnDeviceAiCapabilities = when (flavorType) {
            FlavorType.GOOGLE, FlavorType.FOSS -> OnDeviceAiCapabilities(
                isLocalChatSupported = true,
                isLocalTranslationSupported = true,
            )

            else -> OnDeviceAiCapabilities(
                isLocalChatSupported = false,
                isLocalTranslationSupported = false,
            )
        }
    }
}
