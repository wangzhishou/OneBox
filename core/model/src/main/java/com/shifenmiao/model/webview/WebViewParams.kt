package com.shifenmiao.model.webview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class WebViewParams(
    val url: String? = null,
    val baseUrl: String? = null,
    val htmlData: String? = null,
    val title: String = "",
    val mimeType: String = "text/html",
    val isHtml: Boolean = false,
    val type: WebViewType = WebViewType.DEFAULT,
    val enableSlowWholeDocumentDraw: Boolean = false,
    val enableShare: Boolean = true,
    val enableCustomTouch: Boolean = true,
    val ignoreSslError: Boolean = true,
    val aIgcInfo: String = ""
) : Parcelable
