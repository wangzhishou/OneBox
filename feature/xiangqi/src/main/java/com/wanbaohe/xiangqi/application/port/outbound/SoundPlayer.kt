package com.wanbaohe.xiangqi.application.port.outbound

import java.io.File

interface SoundPlayer {
    suspend fun playLocalFile(file: File)
    suspend fun playEffect(url: String)
    suspend fun playBackground(url: String)
    fun stopBackground()
    suspend fun playBeep()
}
