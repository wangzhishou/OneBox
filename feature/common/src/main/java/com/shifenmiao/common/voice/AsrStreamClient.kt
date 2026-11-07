package com.shifenmiao.common.voice

/** RemoteConfig.voiceInput.provider 的历史取值(仅 ≤139 老版本读它, 新版本用 `engine` 切换) */
const val VOICE_PROVIDER_IFLYTEK = "iflytek"

/**
 * 流式语音识别客户端契约。
 *
 * 两个实现:
 * - [IFlytekAsrClient]: App 持网关签名 URL 直连讯飞, 音频 base64 塞 JSON 帧;
 * - [FunAsrClient]: 连自家网关反代的 FunASR 2pass 服务, 音频走二进制帧。
 * 二者对上层([VoiceRecognizer] / UI)语义一致: 上行 PCM、回中间结果、可取消。
 */
interface AsrStreamClient {
    /** 上行一块 16k 单声道 PCM */
    fun sendPcm(chunk: ByteArray)

    /** 说完: 排空队列后发结束帧, 等服务端最终结果 */
    fun stop()

    /** 放弃: 立即断开 */
    fun cancel()
}
