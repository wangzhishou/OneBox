package com.shifenmiao.base.ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.clickableWithoutRipple(enabled: Boolean = true, onClick: () -> Unit) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled
    ) {
        onClick()
    }
}

fun Modifier.onGloballyPositionedDebounced(
    delayMillis: Long = 200,
    onPositioned: (LayoutCoordinates) -> Unit
): Modifier = composed {
    val job = remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    this.onGloballyPositioned { coordinates ->
        job.value?.cancel()
        job.value = coroutineScope.launch {
            delay(delayMillis)
            onPositioned(coordinates)
        }
    }
}

