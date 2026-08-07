package com.shifenmiao.model.item


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 路由跳转 / 临时预览用的条目参数（与 DB 实体解耦）。
 * - DB 侧的真实数据走 ItemWithRelation 加载
 * - 这里只携带"打开该屏所需的最小字段"
 */
@Parcelize
@Serializable
data class ItemEntityParams(
    val id: Int? = null,
    val title: String = "",
    val description: String = "",
    val url: String? = null,
    val data: String? = null,
    val listType: Int = 0
) : Parcelable
