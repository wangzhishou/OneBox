package com.shifenmiao.core.constants

import androidx.compose.ui.unit.dp

object Constants {
    /**
     * 智能体更新间隔日期
     */
    const val AI_AGENT_UPDATE_INTERVAL = 1000 * 60 * 60 * 24 * 7

    /**
     * QQ群的key
     */
    const val QQ_GROUP_KEY = "1JOfn5KCue56UhXT1fRe6NgCLJB5sHFO"

    /**
     * 常用接口网络缓存时间 TimeUnit.MINUTES
     */
    const val NET_CACHE_TIME = 10

    const val MAX_PAGE_SIZE = 10

    // 将不需要频繁更新的参数提取为常量
    val DRAWER_WIDTH = 320.dp

    /**
     * callbackFlow的默认缓冲区大小是BUFFERED，在Kotlin的Channels库中，
     * BUFFERED的值是16。这意味着如果你没有明确设置缓冲区大小，那么callbackFlow的缓冲区默认可以存储16个元素。
     *
     * callbackFlow有一个默认的缓冲区，如果这个缓冲区已满，trySend将无法发送更多的数据。
     * 你可以通过在callbackFlow中使用buffer运算符来增加缓冲区的大小。
     */
    const val TYPE_DELAY = 16L

    /**
     * SSE 流式 chunk 发射间隔：callbackFlow 每发送一个 chunk 后 delay 30ms，
     * 降低下游消费频率，避免 chunk 在 buffer 中堆积后一次性涌出造成 UI 卡顿。
     */
    const val CALLBACK_TRY_SEND_DELAY = 30L
    /**
     * 打字机效果基础批处理大小
     */
    const val AI_BASE_BATCH_SIZE = 20

    /**
     * 打字机效果批处理大小增长因子 - 每增加100个字符增加的批处理大小
     */
    const val AI_BATCH_SIZE_GROWTH_FACTOR = 0.5f

    /**
     * 打字机效果批处理大小上限
     */
    const val AI_MAX_BATCH_SIZE = 1
    /**
     * 打字机效果更新阈值 - 当积累超过此数量的字符,太长了就不打字机效果了
     */
    const val AI_TYPEWRITER_THRESHOLD = 500

    const val AI_MODEL_DEFAULT_TITLE = "自定义"

    /**
     *
     */
    const val BASE_POINTS_NUM = 0.1F

    /**
     * 倒计时时间， 秒
     */
    const val COUNT_DOWN_TIME = 8

    /**
     * 隐私策略弹窗版本号
     */
    const val PRIVACY_POLICY_VERSION = 1

    /**
     * 默认日期格式
     */
    const val CHINESE_DATE_FORMATTER = "yyyy年MM月dd日"

    /**
     * Prompt 相关常量
     */
    const val PROMPT_DEFAULT_ID = 0

    /**
     * 分页大小
     */
    const val PAGE_SIZE = 20

    /**
     * 文件解析积分消耗数量
     */
    const val FILE_POINTS_CONSUME_NUM = 500
}
