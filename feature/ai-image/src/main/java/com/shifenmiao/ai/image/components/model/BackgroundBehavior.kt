package com.shifenmiao.ai.image.components.model

sealed class BackgroundBehavior {
    data object None : BackgroundBehavior()

    data object Image : BackgroundBehavior()

    data class Color(
        val width: Int,
        val height: Int,
        val color: Int
    ) : BackgroundBehavior()
}