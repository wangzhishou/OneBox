package com.shifenmiao.feature.document.component

sealed class DocConvertTaskListUiEvent {
    data class Toast(val message: String) : DocConvertTaskListUiEvent()
}

