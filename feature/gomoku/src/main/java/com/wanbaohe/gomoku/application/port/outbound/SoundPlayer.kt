package com.wanbaohe.gomoku.application.port.outbound

import java.io.File

interface SoundPlayer {
    suspend fun playLocalFile(file: File)
    suspend fun playEffect(url: String)
    fun stopEffect()
    suspend fun playBackground(url: String)
    fun pauseBackground()
    fun stopBackground()
    suspend fun playBeep()
}
