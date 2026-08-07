package com.shifenmiao.interfaces.player

interface AudioPlayStatusListener {
    fun onPlayStart()
    fun onPlayProgress(progress: Int)
    fun onPlayStop()
    fun onPlayPause()
    fun onPlayResume()
    fun onPlayLoading()
    fun onPlayLoadingFinish(isSuccess: Boolean)
}