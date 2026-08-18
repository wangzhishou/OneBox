package com.shifenmiao.model.imageprocess

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 百度 AI 修图(retouching,异步任务制)参数。
 *
 * 采用通用 key-value 结构:key 为接口文档中的功能名(如 face_smooth、skin_white),
 * value 为程度值(多为 [0,1],面部重塑类为 [-1,1]);未设置的 key 不下发。
 * 请求体为 application/json,下发时按 [ALL_HUMAN_KEYS] 自动拆分为
 * PartialHumanOptions / AllHumanOptions 两个嵌套 JSON 对象(见官方请求示例,
 * struct 参数以 form 字段 JSON 字符串方式提交会被拒:error_code=216100);
 * 枚举/妆容类型类参数(makeup_*_id 等)暂未覆盖。
 *
 * 该结构对所有调用方开放,各 feature 可自行定义参数目录与预设。
 */
@Parcelize
@Serializable
data class RetouchParams(
    val values: Map<String, Float> = emptyMap()
) : Parcelable {

    fun isEmpty(): Boolean = values.isEmpty()

    operator fun get(key: String): Float? = values[key]

    /** 设置/清除单项(value 为 null 时移除该 key),返回新实例 */
    fun withValue(key: String, value: Float?): RetouchParams = copy(
        values = if (value == null) values - key else values + (key to value)
    )

    /** PartialHumanOptions 参数集(空返回 null,表示不下发该字段) */
    fun partialHumanValues(): Map<String, Float>? =
        values.filterKeys { it !in ALL_HUMAN_KEYS }.takeIf { it.isNotEmpty() }

    /** AllHumanOptions 参数集(空返回 null,表示不下发该字段) */
    fun allHumanValues(): Map<String, Float>? =
        values.filterKeys { it in ALL_HUMAN_KEYS }.takeIf { it.isNotEmpty() }

    companion object {
        val EMPTY = RetouchParams()

        /** 属于 AllHumanOptions(全图人体美化)的 key,其余归入 PartialHumanOptions */
        private val ALL_HUMAN_KEYS = setOf(
            "leg_long",
            "body_heighten",
            "all_skin_color_same",
            "remove_pure_bg_flaw",
            "remove_bg_flaw",
        )
    }
}

/**
 * AI 修图创建任务请求体(application/json,struct 参数为嵌套 JSON 对象)
 */
data class RetouchCreateRequest(
    @SerializedName("image")
    val image: String,
    @SerializedName("PartialHumanOptions")
    val partialHumanOptions: Map<String, Float>? = null,
    @SerializedName("AllHumanOptions")
    val allHumanOptions: Map<String, Float>? = null,
)

/**
 * AI 修图查询任务请求体(application/json)
 */
data class RetouchQueryRequest(
    @SerializedName("task_id")
    val taskId: String,
)

/**
 * AI 修图创建任务响应:成功时 [RetouchTaskResult.taskId] 用于轮询查询
 */
@Parcelize
@Serializable
data class RetouchCreateResponse(
    @SerializedName("log_id")
    val logId: Long? = null,
    @SerializedName("result")
    val result: RetouchTaskResult? = null,
    @SerializedName("error_code")
    val errorCode: Int? = null,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

@Parcelize
@Serializable
data class RetouchTaskResult(
    @SerializedName("task_id")
    val taskId: String? = null,
) : Parcelable

/**
 * AI 修图查询任务响应。
 *
 * 防御性解析:官方文档参数表写 result.status 为字符串
 * (pending/processing/success/failed),示例却给 task_status 整型,
 * 两种字段都接;判定以 [RetouchQueryResult.dlink] 非空为成功,
 * [RetouchQueryResult.taskErrcode] 非 0 或 status==failed 为失败,其余继续轮询。
 */
@Parcelize
@Serializable
data class RetouchQueryResponse(
    @SerializedName("log_id")
    val logId: Long? = null,
    @SerializedName("result")
    val result: RetouchQueryResult? = null,
    @SerializedName("error_code")
    val errorCode: Int? = null,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

@Parcelize
@Serializable
data class RetouchQueryResult(
    @SerializedName("task_id")
    val taskId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("task_status")
    val taskStatus: Int? = null,
    @SerializedName("task_errcode")
    val taskErrcode: Int? = null,
    /** 结果图下载链接(有效期 8 小时) */
    @SerializedName("dlink")
    val dlink: String? = null,
) : Parcelable
