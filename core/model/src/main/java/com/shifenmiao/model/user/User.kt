package com.shifenmiao.model.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class User(
    val id: Int = 0,
    val username: String? = "",
    val nickname: String? = "",
    val avatar: String? = null,
    val email: String? = null,
    val provider: String? = null,
    val openid: String? = null,
    val points: Int? = 0,
    val invitationCode: String? = null,
    val phone: String? = null,
    val vipLevel: Int? = 0,
    val totalRechargeAmount: Double? = 0.0,
    /** 邮箱是否已验证:邮箱注册为 false,Google/微信登录为 true;旧缓存缺省按未验证处理 */
    val confirmed: Boolean = false,
) : Parcelable

@Parcelize
@Serializable
data class WechatLoginRequest(
    val app_id: String,
    val code: String
) : Parcelable

@Parcelize
@Serializable
data class GoogleLoginRequest(
    val id_token: String
) : Parcelable

@Parcelize
@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
) : Parcelable

@Parcelize
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
) : Parcelable

@Parcelize
@Serializable
data class Login(
    val jwt: String,
    val user: User
) : Parcelable

@Parcelize
@Serializable
data class WechatUserInfo(
    @SerializedName("openid") val openid: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("sex") val sex: Int,
    @SerializedName("province") val province: String,
    @SerializedName("city") val city: String,
    @SerializedName("country") val country: String,
    @SerializedName("headimgurl") val headimgurl: String,
    @SerializedName("privilege") val privilege: List<String> = emptyList(), // Provide default value
    @SerializedName("unionid") val unionid: String
) : Parcelable