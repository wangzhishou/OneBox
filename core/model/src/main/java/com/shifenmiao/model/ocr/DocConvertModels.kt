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

package com.shifenmiao.model.ocr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DocConvertRequestResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("log_id")
    val logId: String? = null,
    @SerialName("result")
    val result: DocConvertTaskResult? = null,
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

@Parcelize
@Serializable
data class DocConvertTaskResult(
    @SerialName("task_id")
    val taskId: String? = null
) : Parcelable

@Parcelize
@Serializable
data class DocConvertQueryResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("log_id")
    val logId: String? = null,
    @SerialName("result")
    val result: DocConvertQueryResult? = null,
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error_code")
    val errorCode: Int? = null,
    @SerialName("error_msg")
    val errorMsg: String? = null,
) : Parcelable

@Parcelize
@Serializable
data class DocConvertQueryResult(
    @SerialName("task_id")
    val taskId: String? = null,
    @SerialName("ret_code")
    val retCode: Int? = null,
    @SerialName("ret_msg")
    val retMsg: String? = null,
    @SerialName("percent")
    val percent: Int? = null,
    @SerialName("result_data")
    val resultData: DocConvertResultData? = null,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null
) : Parcelable

@Parcelize
@Serializable
data class DocConvertResultData(
    @SerialName("word")
    val word: String? = null,
    @SerialName("excel")
    val excel: String? = null
) : Parcelable

