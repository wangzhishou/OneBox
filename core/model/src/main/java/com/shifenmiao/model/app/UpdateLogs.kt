package com.shifenmiao.model.app

import android.os.Parcelable
import com.shifenmiao.model.common.Meta
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * {
 *     "data": [
 *         {
 *             "id": 1,
 *             "versionName": "V1.0.1",
 *             "versionCode": 101,
 *             "changeLog": "nuxt: Flag async data promise as cancelled only if defined (#27690)\n",
 *             "channel": 1,
 *             "createdAt": "2024-07-08T06:10:11.718Z",
 *             "updatedAt": "2024-07-08T06:13:30.784Z",
 *             "publishedAt": "2024-07-08T06:13:30.740Z"
 *         }
 *     ],
 *     "meta": {
 *         "pagination": {
 *             "page": 1,
 *             "pageSize": 25,
 *             "pageCount": 1,
 *             "total": 1
 *         }
 *     }
 * }
 */

@Parcelize
@Serializable
data class UpdateLogs(
    val data: List<UpdateLog>,
    val meta: Meta
) : Parcelable


@Parcelize
@Serializable
data class UpdateLog(
    val id: Int,
    val versionName: String = "",
    val versionCode: Int = 100,
    val changeLog: String = "",
    val channel: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val publishedAt: String = "",
    val downloadUrl: String? = null
) : Parcelable