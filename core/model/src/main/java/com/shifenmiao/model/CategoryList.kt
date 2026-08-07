package com.shifenmiao.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class CategoryList(
    var data: List<Category>,
    val meta: com.shifenmiao.model.common.Meta? = null,
) : Parcelable {
    companion object {
        fun createDefaultCategoryList(): CategoryList {
            return CategoryList(
                data = listOf(
                    Category(
                        id = 0,
                        name = "全部"
                    )
                )
            )
        }
    }
}
