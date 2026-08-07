package com.shifenmiao.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import com.shifenmiao.theme.shapes.TopArrowBubbleShape

class AppShapes {
    @Composable
    fun getSuggestionChipShape(): CornerBasedShape {
        return MaterialTheme.shapes.large
    }

    @Composable
    fun getSmallShape(): CornerBasedShape {
        return MaterialTheme.shapes.medium
    }

    @Composable
    fun getMediumShape(): CornerBasedShape {
        return MaterialTheme.shapes.medium
    }

    @Composable
    fun getLargeShape(): CornerBasedShape {
        return MaterialTheme.shapes.medium
    }

    @Composable
    fun getTextFieldShape(): Shape {
        return MaterialTheme.shapes.medium
    }

    @Composable
    fun getTopArrowBubbleShape(): TopArrowBubbleShape {
        return TopArrowBubbleShape()
    }
}

val appShapes = AppShapes()

val LocalAppShapes = staticCompositionLocalOf {
    appShapes
}