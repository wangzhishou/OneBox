package com.shifenmiao.model.user.event

data class BindPhoneEvent(
    var source:String = "BindPhoneEvent",
    var onSuccess: () -> Unit = {},
    var onError: (String) -> Unit = {},
)
