package com.shifenmiao.ai.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifenmiao.ai.model.MessageUiModel

@Composable
fun rememberUniqueMessageLazyKeys(
    messageUiModels: List<MessageUiModel>
): List<String> = remember(messageUiModels) {
    val duplicateCounts = HashMap<String, Int>(messageUiModels.size)
    messageUiModels.map { item ->
        val duplicateIndex = duplicateCounts.getOrDefault(item.id, 0)
        duplicateCounts[item.id] = duplicateIndex + 1

        if (duplicateIndex == 0) {
            item.id
        } else {
            "${item.id}_dup_$duplicateIndex"
        }
    }
}
