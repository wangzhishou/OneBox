package com.shifenmiao.network

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.image.QwenImageRequest
import com.shifenmiao.model.ai.image.QwenImageResponse
import com.shifenmiao.network.api.QwenImageService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 可供任意功能模块复用的千问图像 3.0 网关。
 *
 * 路由规则与产品约定保持一致：用户填写百炼 API Token 时直连 DashScope；未填写时
 * 请求应用后端代理。请求体和响应体在两条路由上完全一致，后端可以直接透明转发。
 */
@Singleton
class QwenImageGateway @Inject constructor(
    @Named("DirectQwenImageService") private val directService: QwenImageService,
    @Named("ProxyQwenImageService") private val proxyService: QwenImageService,
) {
    suspend fun generateOrEdit(
        engine: AiEngine,
        request: QwenImageRequest,
        proxyPath: String = UrlConstants.ALIBABA_QWEN_IMAGE_PROXY_PATH,
    ): Response<QwenImageResponse> {
        request.validate()
        val endpoint = QwenImageEndpointResolver.resolve(engine, proxyPath)
        return when (endpoint.route) {
            QwenImageRoute.DIRECT -> directService.generateOrEdit(
                url = endpoint.url,
                authorization = endpoint.authorization,
                request = request,
            )
            QwenImageRoute.PROXY -> proxyService.generateOrEdit(
                url = endpoint.url,
                request = request,
            )
        }
    }
}

enum class QwenImageRoute {
    DIRECT,
    PROXY,
}

data class QwenImageEndpoint(
    val route: QwenImageRoute,
    val url: String,
    val authorization: String? = null,
)

object QwenImageEndpointResolver {
    fun resolve(
        engine: AiEngine,
        proxyPath: String = UrlConstants.ALIBABA_QWEN_IMAGE_PROXY_PATH,
    ): QwenImageEndpoint {
        val token = engine.authorizationCode.trim()
        return if (token.isNotEmpty()) {
            QwenImageEndpoint(
                route = QwenImageRoute.DIRECT,
                url = AiRequestUrlResolver.joinUrl(
                    baseUrl = AiRequestUrlResolver.normalizeBaseUrl(
                        baseUrl = engine.requestUrl,
                        fallbackBaseUrl = UrlConstants.Q_WEN_AI_BASE_URL,
                    ),
                    path = UrlConstants.Q_WEN_IMAGE_GENERATION_ENDPOINT,
                ),
                authorization = "Bearer $token",
            )
        } else {
            QwenImageEndpoint(
                route = QwenImageRoute.PROXY,
                url = AiRequestUrlResolver.joinUrl(
                    baseUrl = AiRequestUrlResolver.normalizeBaseUrl(
                        baseUrl = engine.proxyUrl,
                        fallbackBaseUrl = engine.proxyUrl.ifBlank { NetworkBuilder.getBaseUrl() },
                    ),
                    path = proxyPath,
                ),
            )
        }
    }
}
