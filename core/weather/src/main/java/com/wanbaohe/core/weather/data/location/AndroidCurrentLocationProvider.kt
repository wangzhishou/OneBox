/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.core.weather.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.wanbaohe.core.weather.domain.repository.CurrentLocationProvider
import com.wanbaohe.core.weather.domain.repository.CurrentLocationSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidCurrentLocationProvider(
    context: Context,
) : CurrentLocationProvider {
    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    override fun lastKnownLocation(): CurrentLocationSnapshot? {
        if (!hasLocationPermission()) return null
        val manager = locationManager ?: return null
        return listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        ).mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull(Location::getTime)?.toSnapshot()
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): CurrentLocationSnapshot? {
        if (!hasLocationPermission()) return null
        val manager = locationManager ?: return null
        val provider = when {
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
                LocationManager.GPS_PROVIDER
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
                LocationManager.NETWORK_PROVIDER
            else -> return lastKnownLocation()
        }
        return withTimeoutOrNull(12_000L) {
            suspendCancellableCoroutine<CurrentLocationSnapshot?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (continuation.isActive) continuation.resume(location.toSnapshot())
                    }
                }
                continuation.invokeOnCancellation {
                    runCatching { manager.removeUpdates(listener) }
                }
                runCatching {
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }.onFailure {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        } ?: lastKnownLocation()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun Location.toSnapshot(): CurrentLocationSnapshot {
        val verticalAccuracy = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasVerticalAccuracy()
        return CurrentLocationSnapshot(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitude.takeIf { hasAltitude() },
            accuracyMeters = accuracy.takeIf { hasAccuracy() },
            verticalAccuracyMeters = if (verticalAccuracy) verticalAccuracyMeters else null,
            capturedAt = time,
        )
    }
}
