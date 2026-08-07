package com.shifenmiao.model.search

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class SuggestionModel(val tag: String) : Parcelable {
    @IgnoredOnParcel
    val id = tag.hashCode()
}
