package com.shifenmiao.model.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SearchHistory(
    val data: List<SuggestionModel>,
    val size: Int = 30
) : Parcelable
