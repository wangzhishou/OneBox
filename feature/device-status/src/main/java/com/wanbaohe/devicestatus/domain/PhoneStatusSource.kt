package com.wanbaohe.devicestatus.domain

import kotlinx.coroutines.flow.Flow

interface PhoneStatusSource {
    fun snapshots(): Flow<PhoneStatus>
    fun refresh()
}
