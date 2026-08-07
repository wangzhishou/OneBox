package com.shifenmiao.model.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BaiduConfig(
    val asrAppId: String = "65156192",
    val asrAppKey: String = "Y8tO30oPRP8dGKSb70cMdtm9",
    val asrAppSecret: String = "0Wa6DHpMMQYeLm7cdISBvxEcDX39bhgo",
    val ttsAppId: String = "65156192",
    val ttsAppKey: String = "Y8tO30oPRP8dGKSb70cMdtm9",
    val ttsAppSecret: String = "0Wa6DHpMMQYeLm7cdISBvxEcDX39bhgo"
) : Parcelable
