/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.model.imageprocess

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 百度图像处理统一响应(经 Go 网关代理):
 * 成功时结果图 base64 在 [image](智能抠图在 [foreground]);
 * 失败时有 [errorCode]/[errorMsg]
 */
@Parcelize
@Serializable
data class ImageProcessResponse(
    @SerialName("image")
    val image: String? = null,
    @SerialName("foreground")
    val foreground: String? = null,
    @SerialName("log_id")
    val logId: Long? = null,
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

/**
 * 智能抠图请求体(application/json;其余图像处理接口均为 form-urlencoded)。
 * method=auto 自动识别主体;return_form=rgba 返回带透明通道的 PNG
 */
@Parcelize
@Serializable
data class ImageSegmentRequest(
    @SerialName("image")
    val image: String,
    @SerialName("method")
    val method: String = "auto",
    @SerialName("refine_mask")
    val refineMask: String = "true",
    @SerialName("return_form")
    val returnForm: String = "rgba",
) : Parcelable
