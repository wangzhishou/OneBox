package com.shifenmiao.model.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 祈福墙单个 tab 的文案配置，由远程下发。
 *
 * 出于合规（宗教类文案）考虑，APK 内置仅为中性兜底文案；
 * 字段为 `null` 或 blank 时，消费端回退到本地兜底文案。
 *
 * 服务端下发示例：
 * ```json
 * {
 *   "blessingWallTabTexts": [
 *     {
 *       "type": "woodenFish",
 *       "title": "敲木鱼",
 *       "subtitle": "静心一敲 · 烦恼全消",
 *       "buttonText": "点我一下，好运 +1",
 *       "statTitle": "今日敲木鱼次数"
 *     }
 *   ]
 * }
 * ```
 */
@Parcelize
@Serializable
data class BlessingWallTabText(
    /**
     * tab 类型标识，对应客户端 BlessingType.key：
     * woodenFish / wealthGod / guanyin / incense
     */
    val type: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val buttonText: String? = null,
    val statTitle: String? = null,
) : Parcelable
