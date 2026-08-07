package com.shifenmiao.model.ai

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


/**
 * @see https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=F32EA2A561F1886CD8D606513512D547&refer=outter
 * 隐式标识扩展字段的值,应为符合以下格式的字符串。
 * {"AIGC": {"Label":"value1","ContentProducer":"value2","ProduceID":"
 * value3","ReservedCode1":"value4","ContentPropagator":"value5","PropagateID":
 * "value6","ReservedCode2":"value7"}}
 * 各要素的值应主要由 GB18030—2022中码位为0x21、0x23~0x5B、0x5D~0x7E 的
 * 字符以及\"构成。
 */
@Parcelize
@Serializable
data class AIGCInfo(
    /**
     * 生成合成标签要素由 Label表示,取值为value1,应符合以下要求。
     * 1) 存储内容属于、可能、疑似为人工智能生成合成的属性信息:属于人工智能生成合成内容
     * 的,value1 的值取1;可能为人工智能生成合成内容的,value1 的值取2;疑似为人工智
     * 能生成合成内容的,value1 的值取3。
     */
    @SerializedName("Label")
    val label: String,
    /**
     * 生成合成服务提供者要素由 ContentProducer表示,取值为value2,应符合以下要求:
     * 1) 存储生成合成服务提供者的名称或编码;
     */
    @SerializedName("ContentProducer")
    var contentProducer: String = "",
    /**
     * 内容制作编号要素由 ProduceID表示,取值为value3,应符合以下要求:
     * 1) 存储生成合成服务提供者对该内容的唯一编号
     */
    @SerializedName("ProduceID")
    val produceID: String,
    /**
     * 预留字段1由 ReservedCode1表示,取值为value4,要求如下:
     * 1) 可存储用于生成合成服务提供者自主开展安全防护,保护内容、标识完整性的信息;
     */
    @SerializedName("ReservedCode1")
    val reservedCode1: String = "",
    /**
     * 内容传播服务提供者要素由 ContentPropagator表示,取值为value5,应符合以下要求:
     * 1) 存储内容传播服务提供者的名称或编码;
     */
    @SerializedName("ContentPropagator")
    val contentPropagator: String,
    /**
     * 内容传播编号要素由 PropagateID表示,取值为value6,应符合以下要求:
     * 1) 存储内容传播服务提供者对该内容的唯一编号;
     */
    @SerializedName("PropagateID")
    val propagateID: String,
    /**
     * 预留字段2由 ReservedCode2表示,取值为value7,要求如下:
     * 1) 可存储用于内容传播服务提供者自主开展安全防护,保护内容、标识完整性的信息;
     */
    @SerializedName("ReservedCode2")
    val reservedCode2: String
) : Parcelable