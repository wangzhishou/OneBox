package com.shifenmiao.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UpdateNicknameRequest(
    val nickname: String
) : Parcelable

@Parcelize
@Serializable
data class ChangePasswordRequest(
    val code: String,
    val newPassword: String
) : Parcelable

@Parcelize
@Serializable
data class SendBindEmailCodeRequest(
    val email: String
) : Parcelable

@Parcelize
@Serializable
data class BindEmailRequest(
    val email: String,
    val code: String
) : Parcelable
