package com.shifenmiao.model.webview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
enum class WebViewType(val title: String) : Parcelable {
    DEFAULT("Default"),
    EXTERNAL("External"),
    PREVIEW("Preview"),
    COLUMN("Column"),
    MARKDOWN_RENDER("MarkdownRender")
}
