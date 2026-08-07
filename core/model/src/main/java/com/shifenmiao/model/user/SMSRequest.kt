package com.shifenmiao.model.user

import kotlinx.serialization.Serializable

@Serializable
data class SMSRequest(
    val phone: String
)

@Serializable
data class VerifyCodeRequest(
    val phone: String,
    val code: String
)