package com.shifenmiao.model.imageprocess

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 百度图像处理统一响应(经 Go 网关代理):
 * 成功时结果图 base64 在 [image](智能抠图在 [foreground]);
 * 失败时有 [errorCode]/[errorMsg]
 */
@Parcelize
@Serializable
data class ImageProcessResponse(
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("foreground")
    val foreground: String? = null,
    @SerializedName("log_id")
    val logId: Long? = null,
    @SerializedName("error_code")
    val errorCode: Int? = null,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

/**
 * 智能抠图请求体(application/json;其余图像处理接口均为 form-urlencoded)。
 * method=auto 自动识别主体;return_form=rgba 返回带透明通道的 PNG
 */
@Parcelize
@Serializable
data class ImageSegmentRequest(
    @SerializedName("image")
    val image: String,
    @SerializedName("method")
    val method: String = "auto",
    @SerializedName("refine_mask")
    val refineMask: String = "true",
    @SerializedName("return_form")
    val returnForm: String = "rgba",
) : Parcelable
