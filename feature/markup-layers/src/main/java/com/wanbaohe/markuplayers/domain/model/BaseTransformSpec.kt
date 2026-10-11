package com.wanbaohe.markuplayers.domain.model

data class BaseTransformSpec(
    val rotationSteps: Int = 0,
    val freeRotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropRect: NormalizedRect = NormalizedRect.Full,
)
