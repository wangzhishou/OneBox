package com.shifenmiao.core.constants

/**
 * URL / token / 备案号等渠道相关常量统一入口。
 *
 * 实际值由 flavor sourceSet 决定：
 *   - src/domestic/.../UrlConstantsFlavor.kt   国内渠道（onebox / xiaomi / yyb / oppo / vivo / huawei）
 *   - src/google/.../UrlConstantsFlavor.kt     Google Play 渠道
 *
 * 想看/改哪个渠道的配置，直接打开对应 sourceSet 下的 UrlConstantsFlavor.kt 即可。
 */
object UrlConstants {
    /**
     * 用于WebView加载空白页面的URL
     */
    const val ABOUT_BLANK = "about:blank"

    /**
     * 内部Screen路由URL前缀,老的接口废弃一下
     */
    const val APP_URL_PREFIX = "app://"

    /**
     * 内部Screen路由URL前缀
     */
    const val DEEP_LINKS_PREFIX = "onebox://"

    const val DEEP_LINK_HOST_SCREEN = "screen"
    const val DEEP_LINK_HOST_ACTION = "action"
    const val DEEP_LINK_HOST_ITEM = "item"

    const val DEEP_LINK_QUERY_ACTION = "action"
    const val DEEP_LINK_QUERY_ITEM_ID = "item_id"
    const val DEEP_LINK_QUERY_ROUTE_KEY = "route_key"

    /**
     * WebView Base URl
     */
    const val WEB_VIEW_BASE_URL = "https://appassets.androidplatform.net/"

    const val NEED_CACHE_PARAM_NAME = "_needCache"

    /**
     * 正式发布环境网址
     */
    val RELEASE_URL = UrlConstantsFlavor.RELEASE_URL

    /**
     * 测试环境网址
     */
    val DEBUG_URL = UrlConstantsFlavor.DEBUG_URL

    /**
     * 用户隐私协议
     */
    const val USER_AGREEMENT_URL = UrlConstantsFlavor.USER_AGREEMENT_URL
    const val PRIVACY_POLICY_URL = UrlConstantsFlavor.PRIVACY_POLICY_URL

    val ACCESS_TOKEN = UrlConstantsFlavor.ACCESS_TOKEN

    /**
     * RemoteConfig.accessToken 的完整默认 token，按渠道隔离（国内 / google 各一份）。
     */
    val REMOTE_CONFIG_ACCESS_TOKEN = UrlConstantsFlavor.REMOTE_CONFIG_ACCESS_TOKEN

    const val OFFICIAL_WEBSITE = UrlConstantsFlavor.OFFICIAL_WEBSITE

    /** 联系邮箱, 按渠道隔离(国内 / google 各一份) */
    const val EMAIL = UrlConstantsFlavor.EMAIL

    /**
     * 微信客服链接
     */
    const val WECHAT_CUSTOMER_SERVICE = UrlConstantsFlavor.WECHAT_CUSTOMER_SERVICE

    /** 是否显示备案号/备案查询入口，按渠道隔离（国内 true / google false） */
    const val SHOW_BEI_AN_ENTRY = UrlConstantsFlavor.SHOW_BEI_AN_ENTRY

    const val BEI_AN_NUMBER = UrlConstantsFlavor.BEI_AN_NUMBER
    const val BEI_AN_QUERY: String = UrlConstantsFlavor.BEI_AN_QUERY
    const val BEI_AN_AI_QUERY: String = UrlConstantsFlavor.BEI_AN_AI_QUERY
    const val BEI_AN_BAIDU_NUMBER = UrlConstantsFlavor.BEI_AN_BAIDU_NUMBER
    const val BEI_AN_ALI_NUMBER = UrlConstantsFlavor.BEI_AN_ALI_NUMBER
    const val BEI_AN_KIMI_NUMBER = UrlConstantsFlavor.BEI_AN_KIMI_NUMBER
    const val BEI_AN_DOUBAO_NUMBER = UrlConstantsFlavor.BEI_AN_DOUBAO_NUMBER
    const val BEI_AN_TENCENT_NUMBER = UrlConstantsFlavor.BEI_AN_TENCENT_NUMBER
    const val BEI_AN_ONEBOX_NUMBER = UrlConstantsFlavor.BEI_AN_ONEBOX_NUMBER
    const val BEI_AN_DEEPSEEK_NUMBER = UrlConstantsFlavor.BEI_AN_DEEPSEEK_NUMBER

    const val ALIBABA_AI_PROXY_PATH = "openai/alibaba/v1/chat/completions"
    const val KIM_AI_PROXY_PATH = "openai/kimi/v1/chat/completions"
    const val DOU_BAO_AI_PROXY_PATH = "/openai/doubao/v1/chat/completions"
    const val XIAOMI_AI_PROXY_PATH = "/openai/xiaomi/v1/chat/completions"
    const val TENCENT_AI_PROXY_PATH = "/openai/tencent/v1/chat/completions"
    const val DEEP_SEEK_AI_PROXY_PATH = "/openai/deepseek/v1/chat/completions"

    const val BAIDU_AI_PROXY_PATH = "/openai/baidu/v2/chat/completions"

    const val MIMO_TTS_PROXY_PATH = "tts/mimo/v1/chat/completions"


    /**
     * 千问API
     */
    const val Q_WEN_AI_BASE_URL = "https://dashscope.aliyuncs.com/"
    const val Q_WEN_AI_TEXT_COMPLETIONS_ENDPOINT = "compatible-mode/v1/chat/completions"

    /**
     * Open AI
     */
    const val OPENAI_BASE_URL: String = "https://api.openai.com/"
    const val OPENAI_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * Google Gemini(OpenAI 兼容端点)
     */
    const val GEMINI_AI_BASE_URL: String = "https://generativelanguage.googleapis.com/"
    const val GEMINI_TEXT_COMPLETIONS_ENDPOINT = "v1beta/openai/chat/completions"

    /**
     * xAI Grok(OpenAI 兼容协议)
     */
    const val GROK_AI_BASE_URL: String = "https://api.x.ai/"
    const val GROK_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * Anthropic Claude(OpenAI 兼容端点)
     */
    const val CLAUDE_AI_BASE_URL: String = "https://api.anthropic.com/"
    const val CLAUDE_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * OpenRouter 聚合平台(OpenAI 兼容协议)
     */
    const val OPENROUTER_AI_BASE_URL: String = "https://openrouter.ai/api/"
    const val OPENROUTER_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * KIMI AI
     */
    const val KIMI_AI_BASE_URL: String = "https://api.moonshot.cn/"
    const val KIMI_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * DouBao AI
     */
    const val DOUBAO_AI_BASE_URL: String = "https://ark.cn-beijing.volces.com/"
    const val DOUBAO_TEXT_COMPLETIONS_ENDPOINT = "api/v3/chat/completions"

    /**
     * XiaoMi AI
     */
    const val XIAOMI_AI_BASE_URL: String = "https://token-plan-cn.xiaomimimo.com/"
    const val XIAOMI_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * 腾讯 AI
     */
    const val TENCENT_AI_BASE_URL: String = "https://api.hunyuan.cloud.tencent.com/"
    const val TENCENT_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * DeepSeek AI
     */
    const val DEEP_SEEK_AI_BASE_URL: String = "https://api.deepseek.com/"
    const val DEEP_SEEK_TEXT_COMPLETIONS_ENDPOINT = "v1/chat/completions"

    /**
     * 百度 PaddleOCR-VL 文档解析 API
     * 异步接口：先提交任务获取 task_id，再轮询结果
     * @see <a href="https://ai.baidu.com/ai-doc/OCR/Ym5h0hkwt">API文档</a>
     *
     *     const val BAIDU_OCR_BASE_URL = "https://aip.baidubce.com/"
     *     const val BAIDU_OCR_SUBMIT_TASK_PATH = "rest/2.0/brain/online/v2/paddle-vl-parser/task"
     *     const val BAIDU_OCR_QUERY_RESULT_PATH = "rest/2.0/brain/online/v2/paddle-vl-parser/task/query"
     */
    val BAIDU_OCR_BASE_URL = UrlConstantsFlavor.BAIDU_OCR_BASE_URL
    const val BAIDU_OCR_SUBMIT_TASK_PATH = "ai/paddle-vl-parser/task"
    const val BAIDU_OCR_QUERY_RESULT_PATH = "ai/paddle-vl-parser/task/query"
    // rest/2.0/ocr/v1/doc_convert/request
    const val BAIDU_DOC_CONVERT_PATH = "ai/doc_convert/request"
    // rest/2.0/ocr/v1/doc_convert/get_request_result
    const val BAIDU_DOC_CONVERT_REQUEST_RESULT_PATH = "ai/doc_convert/get_request_result"

    /**
     * 百度图像处理 API(经 Go 网关代理,网关注入真实 token):
     * 直连路径为 rest/2.0/image-process/v1/<op>,网关映射为 ai/image-process/<op>
     */
    // 图像去雾
    const val BAIDU_IMAGE_PROCESS_DEHAZE_PATH = "ai/image-process/dehaze"
    // 对比度增强
    const val BAIDU_IMAGE_PROCESS_CONTRAST_ENHANCE_PATH = "ai/image-process/contrast_enhance"
    // 图像无损放大
    const val BAIDU_IMAGE_PROCESS_QUALITY_ENHANCE_PATH = "ai/image-process/image_quality_enhance"
    // 拉伸图像恢复
    const val BAIDU_IMAGE_PROCESS_STRETCH_RESTORE_PATH = "ai/image-process/stretch_restore"
    // 图像修复(需传 rectangle 矩形区域)
    const val BAIDU_IMAGE_PROCESS_INPAINTING_PATH = "ai/image-process/inpainting"
    // 图像清晰度增强
    const val BAIDU_IMAGE_PROCESS_DEFINITION_ENHANCE_PATH = "ai/image-process/image_definition_enhance"
    // 图像色彩增强
    const val BAIDU_IMAGE_PROCESS_COLOR_ENHANCE_PATH = "ai/image-process/color_enhance"
    // 图片去摩尔纹
    const val BAIDU_IMAGE_PROCESS_REMOVE_MOIRE_PATH = "ai/image-process/remove_moire"
    // 文档图片去底纹
    const val BAIDU_IMAGE_PROCESS_DOC_REPAIR_PATH = "ai/image-process/doc_repair"
    // 智能抠图(application/json,结果在 foreground 字段)
    const val BAIDU_IMAGE_PROCESS_SEGMENT_PATH = "ai/image-process/segment"
    // AI修图(异步任务制:先创建任务拿 task_id,再轮询查询结果,结果在 result.dlink)
    const val BAIDU_IMAGE_PROCESS_RETOUCHING_CREATE_PATH = "ai/image-process/retouching/create_task"
    const val BAIDU_IMAGE_PROCESS_RETOUCHING_QUERY_PATH = "ai/image-process/retouching/query_task"

}
