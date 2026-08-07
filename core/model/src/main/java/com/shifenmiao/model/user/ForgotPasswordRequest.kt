package com.shifenmiao.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ForgotPasswordRequest(
    val email: String
) : Parcelable

@Parcelize
@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
) : Parcelable
