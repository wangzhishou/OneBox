package com.shifenmiao.network.service

import com.shifenmiao.model.moderation.SensitiveWordCheckField
import com.shifenmiao.model.moderation.SensitiveWordCheckRequest
import com.shifenmiao.model.moderation.SensitiveWordCheckResponse
import com.shifenmiao.network.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SensitiveWordCheckOutcome {
    data object Safe : SensitiveWordCheckOutcome

    data class Hit(val message: String, val hits: List<SensitiveWordCheckField>) :
        SensitiveWordCheckOutcome

    data class Failed(val cause: String) : SensitiveWordCheckOutcome
}

@Singleton
class SensitiveWordChecker @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * 对 [fields] 中声明的文本进行敏感词检测。
     *
     * - [Safe] 表示未命中;
     * - [Hit] 表示命中, 携带服务端消息以及非 release 模式下命中的字段详情;
     * - [Failed] 表示网络 / 协议异常, 调用方可按业务策略选择放行或拦截。
     */
    suspend fun check(
        scene: String,
        fields: List<SensitiveWordCheckField>
    ): SensitiveWordCheckOutcome {
        if (fields.isEmpty() || fields.all { it.text.isBlank() }) {
            return SensitiveWordCheckOutcome.Safe
        }
        val request = SensitiveWordCheckRequest(scene = scene, fields = fields)
        return runCatching {
            val response = apiService.checkSensitiveWords(request)
            val body: SensitiveWordCheckResponse? = response.body()
            if (!response.isSuccessful || body == null) {
                SensitiveWordCheckOutcome.Failed(
                    "http ${response.code()}: ${response.message().ifBlank { "unknown" }}"
                )
            } else {
                if (body.hit) {
                    SensitiveWordCheckOutcome.Hit(
                        message = body.message.orEmpty(),
                        hits = body.hits.map { SensitiveWordCheckField(it.key, it.words.joinToString()) }
                    )
                } else {
                    SensitiveWordCheckOutcome.Safe
                }
            }
        }.getOrElse { SensitiveWordCheckOutcome.Failed(it.message ?: "unknown error") }
    }
}
