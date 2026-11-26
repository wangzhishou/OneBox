package com.wanbaohe.engine.android.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

internal class GameHaptics(context: Context) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun tick(strength: Float) {
        if (!vibrator.hasVibrator()) return
        val amount = strength.coerceIn(0.05f, 1f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val primitive = if (amount > 0.65f) {
                VibrationEffect.Composition.PRIMITIVE_CLICK
            } else {
                VibrationEffect.Composition.PRIMITIVE_TICK
            }
            if (vibrator.arePrimitivesSupported(primitive).firstOrNull() == true) {
                vibrator.vibrate(
                    VibrationEffect.startComposition()
                        .addPrimitive(primitive, amount)
                        .compose()
                )
                return
            }
        }
        val amplitude = (amount * 255).toInt().coerceIn(1, 255)
        vibrator.vibrate(VibrationEffect.createOneShot(12L + (amount * 18).toLong(), amplitude))
    }

    fun cancel() = vibrator.cancel()
}
