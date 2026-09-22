package com.shifenmiao.network

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType

object AiRequestUrlResolver {

    enum class RequestRoute {
        DIRECT,
        PROXY,
        UNAVAILABLE,
    }

    fun resolveRequestRoute(engine: AiEngine): RequestRoute {
        return when {
            // Jev: 与其他引擎同一套路由语义 —— 已验证直连(或 Google 渠道自带 token)
            // 且官网地址已配置时直连, 否则走 App 代理兜底(代理路径有兜底常量, 始终可用)
            engine.requestProtocol == AiRequestProtocol.JEV -> {
                if (engine.canChatDirectly() && engine.requestUrl.isNotBlank()) {
                    RequestRoute.DIRECT
                } else {
                    RequestRoute.PROXY
                }
            }
            engine.canChatDirectly() -> RequestRoute.DIRECT
            engine.hasProxyRouteConfigured() -> RequestRoute.PROXY
            else -> RequestRoute.UNAVAILABLE
        }
    }

    fun hasAvailableRoute(engine: AiEngine): Boolean {
        return resolveRequestRoute(engine) != RequestRoute.UNAVAILABLE
    }

    fun shouldUseDirectRequest(engine: AiEngine): Boolean {
        return resolveRequestRoute(engine) == RequestRoute.DIRECT
    }

    fun shouldUseProxyRequest(engine: AiEngine): Boolean {
        return resolveRequestRoute(engine) == RequestRoute.PROXY
    }

    fun resolveRequestUrl(engine: AiEngine): String {
        if (!hasAvailableRoute(engine)) {
            error("No available AI engine route configured for ${engine.identityKey()}")
        }
        val route = resolveRequestRoute(engine)

        return when (engine.requestProtocol) {
            AiRequestProtocol.JEV -> {
                if (route == RequestRoute.DIRECT) {
                    joinUrl(
                        baseUrl = normalizeBaseUrl(
                            baseUrl = engine.requestUrl,
                            fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(engine)
                        ),
                        path = engine.requestPath.ifBlank { UrlConstants.JEV_SYSTEMONE_ENDPOINT },
                    )
                } else {
                    joinUrl(
                        baseUrl = engine.proxyUrl.ifBlank { NetworkBuilder.getBaseUrl() },
                        path = engine.proxyPath.ifBlank { UrlConstants.JEV_PROXY_PATH },
                    )
                }
            }
            AiRequestProtocol.RESPONSES_COMPATIBLE -> {
                if (route == RequestRoute.DIRECT) {
                    val baseUrl = normalizeBaseUrl(
                        baseUrl = engine.requestUrl,
                        fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(engine)
                    )
                    joinUrl(baseUrl, engine.requestPath.ifBlank { "/v1/responses" })
                } else {
                    joinUrl(
                        baseUrl = resolveProxyBaseUrl(engine),
                        path = resolveProxyPath(engine)
                    )
                }
            }
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                if (route == RequestRoute.DIRECT) {
                    val baseUrl = normalizeBaseUrl(
                        baseUrl = engine.requestUrl,
                        fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(engine)
                    )
                    joinUrl(baseUrl, "/v1/messages")
                } else {
                    joinUrl(
                        baseUrl = resolveProxyBaseUrl(engine),
                        path = resolveProxyPath(engine)
                    )
                }
            }
            AiRequestProtocol.LOCAL_ON_DEVICE -> {
                // 端侧本地推理不走 HTTP；调用方若到达此处说明误用了云端 URL 解析。
                error("Local on-device engine has no request URL: ${engine.identityKey()}")
            }
            else -> {
                if (shouldUseDirectRequest(engine)) {
                    joinUrl(
                        baseUrl = normalizeBaseUrl(
                            baseUrl = engine.requestUrl,
                            fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(engine)
                        ),
                        path = engine.requestPath
                    )
                } else if (shouldUseProxyRequest(engine)) {
                    joinUrl(
                        baseUrl = resolveProxyBaseUrl(engine),
                        path = resolveProxyPath(engine)
                    )
                } else {
                    error("No available AI engine route configured for ${engine.identityKey()}")
                }
            }
        }
    }

    fun resolveAuthorizationHeader(engine: AiEngine): String? {
        return when (engine.requestProtocol) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                null
            }
            // Jev: 直连时带用户官网 key; 代理路由不带(由 OkHttp AuthInterceptor 注入 App JWT)
            AiRequestProtocol.JEV -> {
                engine.authorizationCode
                    .takeIf { shouldUseDirectRequest(engine) && it.isNotBlank() }
                    ?.let { "Bearer $it" }
            }
            AiRequestProtocol.RESPONSES_COMPATIBLE -> {
                engine.authorizationCode
                    .takeIf { shouldUseDirectRequest(engine) && it.isNotBlank() }
                    ?.let { "Bearer $it" }
            }
            AiRequestProtocol.OPENAI_COMPATIBLE,
            AiRequestProtocol.OWN_PROXY -> {
                engine.authorizationCode
                    .takeIf { shouldUseDirectRequest(engine) && it.isNotBlank() }
                    ?.let { "Bearer $it" }
            }
            // 端侧本地推理不发 HTTP 头，无 Authorization 概念。
            AiRequestProtocol.LOCAL_ON_DEVICE -> null
        }
    }

    fun resolveApiKey(engine: AiEngine): String? {
        return engine.authorizationCode
            .takeIf { shouldUseDirectRequest(engine) && it.isNotBlank() }
            ?.let { apiKey ->
                when (engine.authType) {
                    AuthType.API_KEY -> apiKey
                    AuthType.BEARER -> apiKey  // Bearer token 也可以用作 api-key
                    AuthType.NONE -> null
                }
            }
    }

    private fun resolveProxyPath(engine: AiEngine): String {
        return engine.proxyPath
            .takeIf { it.isNotBlank() }
            ?: defaultProxyPathFor(engine.name)
    }

    private fun resolveProxyBaseUrl(engine: AiEngine): String {
        return normalizeBaseUrl(
            baseUrl = engine.proxyUrl,
            fallbackBaseUrl = NetworkBuilder.getBaseUrl()
        )
    }

    private fun defaultProxyPathFor(engineName: String): String {
        return when (engineName.lowercase()) {
            AiProvider.QWen.value -> UrlConstants.ALIBABA_AI_PROXY_PATH
            AiProvider.Kimi.value -> UrlConstants.KIM_AI_PROXY_PATH
            AiProvider.DouBao.value -> UrlConstants.DOU_BAO_AI_PROXY_PATH
            AiProvider.Tencent.value -> UrlConstants.TENCENT_AI_PROXY_PATH
            AiProvider.DeepSeek.value -> UrlConstants.DEEP_SEEK_AI_PROXY_PATH
            AiProvider.Baidu.value -> UrlConstants.BAIDU_AI_PROXY_PATH
            AiProvider.Mimo.value -> UrlConstants.XIAOMI_AI_PROXY_PATH
            AiProvider.ZhiPu.value -> UrlConstants.ZHIPU_AI_PROXY_PATH
            AiProvider.MinMax.value.lowercase() -> UrlConstants.MINIMAX_AI_PROXY_PATH
            AiProvider.Jev.value -> UrlConstants.JEV_PROXY_PATH
            else -> UrlConstants.OPENAI_TEXT_COMPLETIONS_ENDPOINT
        }
    }

    fun normalizeBaseUrl(baseUrl: String, fallbackBaseUrl: String): String {
        val resolvedBaseUrl = baseUrl.trim().ifBlank {
            fallbackBaseUrl.trim().ifBlank { NetworkBuilder.getBaseUrl().trim() }
        }
        return when {
            resolvedBaseUrl.startsWith("http://") || resolvedBaseUrl.startsWith("https://") -> resolvedBaseUrl
            resolvedBaseUrl.isBlank() -> NetworkBuilder.getBaseUrl().trim()
            else -> "https://$resolvedBaseUrl"
        }
    }

    fun joinUrl(baseUrl: String, path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val normalizedBaseUrl = when {
            baseUrl.isBlank() -> NetworkBuilder.getBaseUrl().trimEnd('/')
            baseUrl.endsWith("/") -> baseUrl.trimEnd('/')
            else -> baseUrl
        }
        val normalizedPath = path.trimStart('/')
        return if (normalizedPath.isBlank()) {
            "$normalizedBaseUrl/"
        } else {
            "$normalizedBaseUrl/$normalizedPath"
        }
    }
}

