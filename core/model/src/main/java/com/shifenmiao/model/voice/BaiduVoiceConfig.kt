package com.shifenmiao.model.voice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaiduVoiceConfig(
    var asrAppId:String,
    val asrApiKey:String,
    val secretKey:String
): Parcelable
