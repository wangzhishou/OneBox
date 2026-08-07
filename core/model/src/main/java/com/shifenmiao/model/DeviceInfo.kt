package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DeviceInfo(
    val deviceName: String? = null,
    val deviceBrand: String? = null,
    val deviceModel: String? = null,
    val deviceId: String? = null,
    val deviceSdkInt: Int? = null,
    var deviceNetType: String? = null,
    var channel: String? = null
) : Parcelable

fun DeviceInfo.toQueryString(): String {
    val parts = listOfNotNull(
        "clientInfo=android_${BuildConfig.VersionName}_${BuildConfig.BUILD_TYPE}_${BuildConfig.FLAVOR}",
        "clientVersion=${BuildConfig.VersionCode}",
        "os=android",
        "osVersion=${deviceSdkInt}",
        "netType=${deviceNetType}",
        "customInfo=${deviceBrand}_${deviceModel}_${deviceName}_$deviceId",
        "channel=${channel}",
    )
    return parts.joinToString("&")
}