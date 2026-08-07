package com.shifenmiao.feature.document.component

sealed class OcrTaskListUiEvent {
    data class Toast(val message: String) : OcrTaskListUiEvent()
}

