package com.shifenmiao.base.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.shifenmiao.interfaces.player.AudioPlayStatusListener
import java.io.File

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayStatusListener: AudioPlayStatusListener? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressTask = object : Runnable {
        override fun run() {
            if (audioPlayStatusListener != null) {
                val progress = mediaPlayer?.currentPosition ?: 0
                audioPlayStatusListener?.onPlayProgress(progress)
                handler.postDelayed(this, 1000) // update every second
            }
        }
    }

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            audioPlayStatusListener?.onPlayStop()
        }
    }

    fun setPlayStatusListener(listener: AudioPlayStatusListener) {
        this.audioPlayStatusListener = listener
    }

    fun startPlaying(file: File) {
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(file.absolutePath)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        handler.post(updateProgressTask) // start updating progress
        audioPlayStatusListener?.onPlayStart()
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        handler.removeCallbacks(updateProgressTask) // stop updating progress
        audioPlayStatusListener?.onPlayStop()
    }

    fun pausePlaying() {
        mediaPlayer?.pause()
        handler.removeCallbacks(updateProgressTask) // stop updating progress
        audioPlayStatusListener?.onPlayPause()
    }

    fun resumePlaying() {
        mediaPlayer?.start()
        handler.post(updateProgressTask) // start updating progress
        audioPlayStatusListener?.onPlayResume()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateProgressTask) // stop updating progress
    }
}