package com.shifenmiao.model.pay

data class PayUIState(
    val payState: PayState = PayState.INITIALIZING,
)

enum class PayState {
    DEFAULT,
    SUCCESS,
    FAILURE,
    LOADING,
    INCOMPLETE,
    INITIALIZING
}