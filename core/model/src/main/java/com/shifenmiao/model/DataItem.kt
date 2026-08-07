package com.shifenmiao.model

import android.os.Parcelable
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DataItem(
    val id: Int = -1,
    val title: String? = "",
    val description: String? = "",
    val user: User? = User(),
    val createdAt: String? = "",
    val updatedAt: String? = "",
    val publishedAt: String? = "",
    val url: String? = "",
    val miniProgramId: String? = "",
    val data: String? = "",
    val placeholder: String? = "",
    val categories: List<Category>? = null,
    /** ListItemType.id 直接下发，避免再嵌套一层 ListType DTO */
    val listType: Int? = null,
    val icon: StrapiImage? = null,
    val iconName: String? = null,
    val recommend: Boolean = false,
    val agent: Agent? = null,
    val prompt: ChatPrompt? = null,
    val vipLevel: Int? = 0,
    /** 是否高亮显示（服务端下发，无数据时默认 false） */
    val isHighlighted: Boolean = false,
    /** 此功能需要联网才能使用（服务端下发，无数据时默认 false） */
    val isOnline: Boolean = false,
    /** 此功能需要 AI 支持（服务端下发，无数据时默认 false） */
    @SerialName("ai")
    val isAi: Boolean = false,
    /**
     * Strapi v5 文档级 cuid (24 字符).
     *
     * 与 [id] 不同: id 是 Strapi v4 的数字主键, documentId 是 v5 跨内容类型稳定的字符串标识.
     * 评论 / 关联表等 v5 接口统一使用 documentId; v4 数字 id 仍由 [id] 保留用于本地 / 旧版兼容.
     */
    @SerialName("documentId")
    val documentId: String? = null,
    /**
     * 评论总数 (一级 + 回复), 由 go-proxy 在 item-list / blog 列表接口侧附加.
     * 旧版本 / 没启用评论插件时为 null, UI 不展示角标.
     */
    @SerialName("commentCount")
    val commentCount: Int? = null,
) : Parcelable


data class DefaultDataItem(
    val customId: Int = 1,
    val customTitle: String = "示例标题",
    val customDescription: String = "这是一个示例描述",
    val customCreatedAt: String = "2023-01-01T00:00:00Z",
    val customUpdatedAt: String = "2023-01-02T00:00:00Z",
    val customPublishedAt: String = "2023-01-03T00:00:00Z",
    val customUrl: String = "http://example.com/data-item",
    val customMiniProgramId: String = "",
    var customCategory: List<Category> = listOf(
        Category(
            id = 0,
            name = "示例分类"
        )
    ),
    val customListType: Int = 0,
    val customIconPath: StrapiImage? = null
) {
    val dataItem: DataItem = DataItem(
        id = customId,
        title = customTitle,
        description = customDescription,
        createdAt = customCreatedAt,
        updatedAt = customUpdatedAt,
        publishedAt = customPublishedAt,
        url = customUrl,
        miniProgramId = customMiniProgramId,
        categories = customCategory,
        listType = customListType,
        icon = customIconPath
    )
}


// 创建一个默认的DataItem实例

@Parcelize
@Serializable
data class ListType(
    val id: Int,
    val typeId: Int,
    val typeName: String
) : Parcelable

@Parcelize
@Serializable
data class StrapiImage(
    val id: Int? = null,
    val name: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val formats: Formats? = null,
    val hash: String? = null,
    val ext: String? = null,
    val mime: String? = null,
    val size: Double? = null,
    val url: String = ""
) : Parcelable

@Parcelize
@Serializable
data class Formats(
    val thumbnail: Thumbnail? = null,
    val small: Small? = null
) : Parcelable

@Parcelize
@Serializable
data class Thumbnail(
    val ext: String = "",
    val url: String = "",
    val hash: String = "",
    val mime: String = "",
    val name: String = ""
) : Parcelable

@Parcelize
@Serializable
data class Small(
    val ext: String = "",
    val url: String = "",
    val hash: String = "",
    val mime: String = "",
    val name: String = ""
) : Parcelable
