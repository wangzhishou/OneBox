package com.wanbaohe.devicestatus.screen

import android.content.Context
import android.text.format.Formatter
import com.wanbaohe.devicestatus.R
import com.wanbaohe.devicestatus.domain.BatteryFacts
import com.wanbaohe.devicestatus.domain.BatteryHealth
import com.wanbaohe.devicestatus.domain.BatteryStatus
import com.wanbaohe.devicestatus.domain.CarrierFacts
import com.wanbaohe.devicestatus.domain.DeviceFacts
import com.wanbaohe.devicestatus.domain.DisplayFacts
import com.wanbaohe.devicestatus.domain.NetworkSlice
import com.wanbaohe.devicestatus.domain.PhoneStatus
import com.wanbaohe.devicestatus.domain.PhoneType
import com.wanbaohe.devicestatus.domain.PowerSource
import com.wanbaohe.devicestatus.domain.PrivateDns
import com.wanbaohe.devicestatus.domain.SimState
import com.wanbaohe.devicestatus.domain.StorageFacts
import com.wanbaohe.devicestatus.domain.StorageKind
import com.wanbaohe.devicestatus.domain.SystemFacts
import com.wanbaohe.devicestatus.domain.TransportKind
import com.wanbaohe.devicestatus.domain.WifiFacts
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class FormattedRow(
    val label: String,
    val value: String,
    val copyText: String = value,
)

data class FormattedSection(
    val title: String,
    val rows: List<FormattedRow>,
)

data class FormattedPhoneStatus(
    val addresses: List<FormattedRow>,
    val sections: List<FormattedSection>,
    val footnote: String,
)

object PhoneStatusFormatter {
    fun format(context: Context, status: PhoneStatus): FormattedPhoneStatus {
        val sections = buildList {
            addAll(networkSections(context, status.networks))
            carrierSection(context, status.carrier)?.let(::add)
            deviceSection(context, status.device)?.let(::add)
            add(systemSection(context, status))
            displaySection(context, status.display)?.let(::add)
            memorySection(context, status)?.let(::add)
            storageSection(context, status.storage)?.let(::add)
            batterySection(context, status.battery)?.let(::add)
            appSection(context, status.app)?.let(::add)
        }
        return FormattedPhoneStatus(
            addresses = heroAddresses(context, status.networks),
            sections = sections,
            footnote = context.getString(R.string.status_footnote),
        )
    }

    private fun networkSections(context: Context, networks: List<NetworkSlice>): List<FormattedSection> =
        networks.mapNotNull { slice ->
            val rows = networkRows(context, slice)
            if (rows.isEmpty()) null else FormattedSection(networkTitle(context, slice), rows)
        }

    private fun networkTitle(context: Context, slice: NetworkSlice): String {
        val name = when {
            slice.transports == listOf(TransportKind.OTHER) && !slice.interfaceName.isNullOrBlank() ->
                slice.interfaceName
            else -> transportLabel(context, slice.transports)
        }
        return if (slice.isActive) context.getString(R.string.network_active_title, name) else name
    }

    private fun networkRows(context: Context, slice: NetworkSlice): List<FormattedRow> {
        val rows = RowBuilder(context)
        rows.addJoined(R.string.label_ipv4, slice.ipv4)
        rows.addJoined(R.string.label_ipv6, slice.ipv6)
        rows.addJoined(R.string.label_gateway, slice.gateways)
        rows.addJoined(R.string.label_dns, slice.dns)
        rows.add(R.string.label_interface, slice.interfaceName)
        slice.mtu?.let { rows.add(R.string.label_mtu, it.toString()) }
        when (val dns = slice.privateDns) {
            null -> Unit
            PrivateDns.Off -> rows.add(R.string.label_private_dns, context.getString(R.string.private_dns_off))
            PrivateDns.Automatic -> rows.add(R.string.label_private_dns, context.getString(R.string.private_dns_auto))
            is PrivateDns.Host -> rows.add(R.string.label_private_dns, dns.name)
        }
        rows.add(R.string.label_proxy, slice.proxy)
        slice.validated?.let { rows.add(R.string.label_validated, yesNo(context, it)) }
        slice.metered?.let { rows.add(R.string.label_metered, yesNo(context, it)) }
        if (slice.captivePortal) rows.add(R.string.label_captive, yesNo(context, true))
        slice.downlinkKbps?.let { rows.add(R.string.label_downlink, kbps(it)) }
        slice.uplinkKbps?.let { rows.add(R.string.label_uplink, kbps(it)) }
        slice.wifi?.let { wifiRows(context, rows, it) }
        return rows.build()
    }

    private fun wifiRows(context: Context, rows: RowBuilder, wifi: WifiFacts) {
        wifi.rssiDbm?.let { rows.add(R.string.label_wifi_rssi, context.getString(R.string.value_dbm, it)) }
        linkSpeed(context, wifi)?.let { rows.add(R.string.label_wifi_link, it) }
        wifi.frequencyMhz?.let { mhz ->
            val channel = wifiChannel(mhz)
            val text = if (channel == null) {
                context.getString(R.string.value_mhz, mhz)
            } else {
                context.getString(R.string.value_frequency, mhz, channel)
            }
            rows.add(R.string.label_wifi_frequency, text)
        }
        rows.add(R.string.label_wifi_standard, wifi.standardName)
    }

    private fun linkSpeed(context: Context, wifi: WifiFacts): String? {
        val rx = wifi.rxLinkSpeedMbps
        val tx = wifi.txLinkSpeedMbps
        if (rx != null && tx != null) {
            return context.getString(R.string.value_link_pair, rx, tx)
        }
        val single = wifi.linkSpeedMbps ?: rx ?: tx
        return single?.let { context.getString(R.string.value_mbps, it) }
    }

    private fun heroAddresses(context: Context, networks: List<NetworkSlice>): List<FormattedRow> {
        val lan = networks.filter { slice ->
            slice.transports.any { it == TransportKind.WIFI || it == TransportKind.ETHERNET }
        }
        val active = networks.filter { it.isActive }
        val v4 = addressRows(context, lan, R.string.label_ipv4) { it.ipv4 }
            .ifEmpty { addressRows(context, active, R.string.label_ipv4) { it.ipv4 } }
            .ifEmpty { addressRows(context, networks, R.string.label_ipv4) { it.ipv4 } }
        if (v4.isNotEmpty()) return v4
        return addressRows(context, lan, R.string.label_ipv6) { it.ipv6 }
            .ifEmpty { addressRows(context, active, R.string.label_ipv6) { it.ipv6 } }
            .ifEmpty { addressRows(context, networks, R.string.label_ipv6) { it.ipv6 } }
    }

    private fun addressRows(
        context: Context,
        slices: List<NetworkSlice>,
        labelRes: Int,
        pick: (NetworkSlice) -> List<String>,
    ): List<FormattedRow> {
        val label = context.getString(labelRes)
        return slices.flatMap { slice ->
            val titled = if (slice.transports.isEmpty() || slice.transports == listOf(TransportKind.OTHER)) {
                label
            } else {
                context.getString(R.string.label_ip_on, label, transportLabel(context, slice.transports))
            }
            pick(slice).map { FormattedRow(titled, it) }
        }
    }

    private fun carrierSection(context: Context, carrier: CarrierFacts?): FormattedSection? {
        if (carrier == null) return null
        val rows = RowBuilder(context)
        rows.add(R.string.label_carrier, carrier.networkOperatorName)
        rows.add(R.string.label_sim_carrier, carrier.simOperatorName)
        rows.add(R.string.label_operator_code, carrier.operatorCode)
        rows.add(R.string.label_country, carrier.countryIso)
        carrier.simState?.let { rows.add(R.string.label_sim_state, simState(context, it)) }
        carrier.roaming?.let { rows.add(R.string.label_roaming, yesNo(context, it)) }
        carrier.phoneType?.let { rows.add(R.string.label_phone_type, phoneType(context, it)) }
        return rows.build().ifEmpty { null }?.let {
            FormattedSection(context.getString(R.string.section_carrier), it)
        }
    }

    private fun deviceSection(context: Context, device: DeviceFacts): FormattedSection? {
        val rows = RowBuilder(context)
        rows.add(R.string.label_device_name, device.deviceName)
        rows.add(R.string.label_manufacturer, device.manufacturer)
        rows.add(R.string.label_brand, device.brand)
        rows.add(R.string.label_model, device.model)
        rows.add(R.string.label_device, device.device)
        rows.add(R.string.label_product, device.product)
        rows.add(R.string.label_hardware, device.hardware)
        rows.add(R.string.label_board, device.board)
        rows.add(R.string.label_soc, device.soc)
        rows.addJoined(R.string.label_abi, device.abis)
        rows.add(R.string.label_gles, device.glEsVersion)
        rows.add(R.string.label_baseband, device.baseband)
        rows.add(R.string.label_bootloader, device.bootloader)
        rows.add(R.string.label_android_id, device.androidId)
        rows.add(R.string.label_fingerprint, device.fingerprint)
        return rows.build().ifEmpty { null }?.let {
            FormattedSection(context.getString(R.string.section_device), it)
        }
    }

    private fun systemSection(context: Context, status: PhoneStatus): FormattedSection {
        val system = status.system
        val rows = RowBuilder(context)
        rows.add(
            R.string.label_android_version,
            context.getString(R.string.android_version_value, system.release, system.sdk),
        )
        rows.add(R.string.label_security_patch, system.securityPatch)
        rows.add(R.string.label_build, system.buildDisplay)
        rows.add(R.string.label_incremental, system.incremental)
        rows.add(R.string.label_kernel, system.kernel)
        rows.add(R.string.label_webview, system.webViewVersion)
        rows.add(R.string.label_locale, language(system))
        rows.add(R.string.label_timezone, timeZone(system))
        rows.add(R.string.label_uptime, uptime(context, system.uptimeMillis))
        system.bootCount?.let { rows.add(R.string.label_boot_count, it.toString()) }
        status.airplaneMode?.let { rows.add(R.string.label_airplane, yesNo(context, it)) }
        status.powerSave?.let { rows.add(R.string.label_power_save, yesNo(context, it)) }
        status.dataSaver?.let { rows.add(R.string.label_data_saver, yesNo(context, it)) }
        return FormattedSection(context.getString(R.string.section_system), rows.build())
    }

    private fun displaySection(context: Context, display: DisplayFacts): FormattedSection? {
        val rows = RowBuilder(context)
        if (display.widthPx > 0 && display.heightPx > 0) {
            rows.add(
                R.string.label_resolution,
                context.getString(R.string.value_resolution, display.widthPx, display.heightPx),
            )
        }
        if (display.densityDpi > 0) {
            rows.add(
                R.string.label_density,
                context.getString(R.string.value_density, display.densityDpi, scaleText(display.density)),
            )
        }
        rows.add(R.string.label_font_scale, scaleText(display.fontScale))
        display.refreshHz?.let { rows.add(R.string.label_refresh_rate, context.getString(R.string.value_hz, hzText(it))) }
        if (display.smallestWidthDp > 0) {
            rows.add(R.string.label_smallest_width, context.getString(R.string.value_dp, display.smallestWidthDp))
        }
        return rows.build().ifEmpty { null }?.let {
            FormattedSection(context.getString(R.string.section_display), it)
        }
    }

    private fun memorySection(context: Context, status: PhoneStatus): FormattedSection? {
        val memory = status.memory ?: return null
        val rows = RowBuilder(context)
        rows.add(R.string.label_ram_total, Formatter.formatFileSize(context, memory.totalBytes))
        rows.add(R.string.label_ram_avail, Formatter.formatFileSize(context, memory.availableBytes))
        rows.add(R.string.label_low_memory, yesNo(context, memory.lowMemory))
        return FormattedSection(context.getString(R.string.section_memory), rows.build())
    }

    private fun storageSection(context: Context, volumes: List<StorageFacts>): FormattedSection? {
        if (volumes.isEmpty()) return null
        val rows = RowBuilder(context)
        volumes.forEach { volume ->
            val label = when (volume.kind) {
                StorageKind.INTERNAL -> R.string.label_storage_internal
                StorageKind.SHARED -> R.string.label_storage_shared
            }
            rows.add(
                label,
                context.getString(
                    R.string.value_storage,
                    Formatter.formatFileSize(context, volume.availableBytes),
                    Formatter.formatFileSize(context, volume.totalBytes),
                ),
            )
        }
        return FormattedSection(context.getString(R.string.section_storage), rows.build())
    }

    private fun batterySection(context: Context, battery: BatteryFacts?): FormattedSection? {
        if (battery == null) return null
        val rows = RowBuilder(context)
        battery.percent?.let { rows.add(R.string.label_battery_level, context.getString(R.string.value_percent, it)) }
        battery.status?.let { rows.add(R.string.label_battery_status, batteryStatus(context, it)) }
        battery.source?.let { rows.add(R.string.label_battery_source, powerSource(context, it)) }
        battery.health?.let { rows.add(R.string.label_battery_health, batteryHealth(context, it)) }
        battery.temperatureC?.let {
            rows.add(R.string.label_battery_temp, String.format(Locale.getDefault(), "%.1f °C", it))
        }
        battery.voltageMv?.let {
            rows.add(R.string.label_battery_voltage, String.format(Locale.getDefault(), "%.2f V", it / 1000.0))
        }
        rows.add(R.string.label_battery_tech, battery.technology)
        battery.currentMicroAmps?.let {
            rows.add(R.string.label_battery_current, context.getString(R.string.value_current_ma, (it / 1000.0).roundToInt()))
        }
        battery.chargeTimeRemainingMs?.let { rows.add(R.string.label_charge_time, chargeTime(context, it)) }
        return rows.build().ifEmpty { null }?.let {
            FormattedSection(context.getString(R.string.section_battery), it)
        }
    }

    private fun appSection(context: Context, app: com.wanbaohe.devicestatus.domain.AppFacts): FormattedSection? {
        val rows = RowBuilder(context)
        rows.add(R.string.label_version, app.versionName)
        if (app.versionCode > 0L) rows.add(R.string.label_version_code, app.versionCode.toString())
        rows.add(R.string.label_flavor, app.flavor)
        rows.add(R.string.label_build_type, app.buildType)
        rows.add(R.string.label_package, app.packageName)
        rows.add(R.string.label_target_sdk, app.targetSdk.toString())
        rows.add(R.string.label_installed, timestamp(app.firstInstallTime))
        rows.add(R.string.label_updated, timestamp(app.lastUpdateTime))
        return rows.build().ifEmpty { null }?.let {
            FormattedSection(context.getString(R.string.section_app), it)
        }
    }

    private fun transportLabel(context: Context, transports: List<TransportKind>): String =
        transports.joinToString(" · ") { transport ->
            context.getString(
                when (transport) {
                    TransportKind.VPN -> R.string.transport_vpn
                    TransportKind.WIFI -> R.string.transport_wifi
                    TransportKind.CELLULAR -> R.string.transport_cellular
                    TransportKind.ETHERNET -> R.string.transport_ethernet
                    TransportKind.BLUETOOTH -> R.string.transport_bluetooth
                    TransportKind.USB -> R.string.transport_usb
                    TransportKind.OTHER -> R.string.transport_other
                },
            )
        }

    private fun language(system: SystemFacts): String =
        if (system.languageName.isBlank() || system.languageName == system.languageTag) {
            system.languageTag
        } else {
            "${system.languageName} (${system.languageTag})"
        }

    private fun timeZone(system: SystemFacts): String {
        val sign = if (system.timeZoneOffsetMinutes >= 0) "+" else "-"
        val absMinutes = abs(system.timeZoneOffsetMinutes)
        val hours = absMinutes / 60
        val minutes = absMinutes % 60
        val gmt = if (minutes == 0) {
            "GMT$sign$hours"
        } else {
            String.format(Locale.US, "GMT%s%d:%02d", sign, hours, minutes)
        }
        return "$gmt (${system.timeZoneId})"
    }

    private fun uptime(context: Context, millis: Long): String {
        val totalMinutes = millis / 60_000L
        val days = (totalMinutes / (60 * 24)).toInt()
        val hours = ((totalMinutes / 60) % 24).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return if (days > 0) {
            context.getString(R.string.uptime_days, days, hours, minutes)
        } else {
            context.getString(R.string.uptime_hours, hours, minutes)
        }
    }

    private fun chargeTime(context: Context, millis: Long): String {
        val minutes = ((millis + 59_999L) / 60_000L).toInt()
        val hours = minutes / 60
        val remain = minutes % 60
        return if (hours > 0) {
            context.getString(R.string.charge_time_hours, hours, remain)
        } else {
            context.getString(R.string.charge_time_minutes, minutes)
        }
    }

    private fun timestamp(millis: Long): String? {
        if (millis <= 0L) return null
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
    }

    private fun scaleText(value: Float): String {
        val nearest = value.roundToInt()
        return if (abs(value - nearest) < 0.001f) {
            nearest.toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", value)
        }
    }

    private fun hzText(value: Float): String {
        val nearest = value.roundToInt()
        return if (abs(value - nearest) < 0.05f) {
            nearest.toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }

    private fun kbps(value: Int): String {
        val locale = Locale.getDefault()
        return if (value >= 1000) {
            String.format(locale, "%.0f Mbps", value / 1000.0)
        } else {
            String.format(locale, "%d Kbps", value)
        }
    }

    private fun yesNo(context: Context, value: Boolean): String =
        context.getString(if (value) R.string.value_yes else R.string.value_no)

    private fun simState(context: Context, state: SimState): String = context.getString(
        when (state) {
            SimState.ABSENT -> R.string.sim_absent
            SimState.READY -> R.string.sim_ready
            SimState.PIN -> R.string.sim_pin
            SimState.PUK -> R.string.sim_puk
            SimState.NETWORK_LOCKED -> R.string.sim_network_locked
            SimState.NOT_READY -> R.string.sim_not_ready
            SimState.RESTRICTED -> R.string.sim_restricted
            SimState.UNKNOWN -> R.string.sim_unknown
        },
    )

    private fun phoneType(context: Context, type: PhoneType): String = context.getString(
        when (type) {
            PhoneType.GSM -> R.string.phone_gsm
            PhoneType.CDMA -> R.string.phone_cdma
            PhoneType.SIP -> R.string.phone_sip
            PhoneType.NONE -> R.string.phone_none
            PhoneType.UNKNOWN -> R.string.sim_unknown
        },
    )

    private fun batteryStatus(context: Context, status: BatteryStatus): String = context.getString(
        when (status) {
            BatteryStatus.CHARGING -> R.string.battery_charging
            BatteryStatus.DISCHARGING -> R.string.battery_discharging
            BatteryStatus.FULL -> R.string.battery_full
            BatteryStatus.NOT_CHARGING -> R.string.battery_not_charging
            BatteryStatus.UNKNOWN -> R.string.battery_unknown
        },
    )

    private fun powerSource(context: Context, source: PowerSource): String = context.getString(
        when (source) {
            PowerSource.AC -> R.string.plugged_ac
            PowerSource.USB -> R.string.plugged_usb
            PowerSource.WIRELESS -> R.string.plugged_wireless
            PowerSource.DOCK -> R.string.plugged_dock
            PowerSource.UNPLUGGED -> R.string.plugged_none
            PowerSource.UNKNOWN -> R.string.battery_unknown
        },
    )

    private fun batteryHealth(context: Context, health: BatteryHealth): String = context.getString(
        when (health) {
            BatteryHealth.GOOD -> R.string.health_good
            BatteryHealth.OVERHEAT -> R.string.health_overheat
            BatteryHealth.DEAD -> R.string.health_dead
            BatteryHealth.OVER_VOLTAGE -> R.string.health_over_voltage
            BatteryHealth.FAILURE -> R.string.health_failure
            BatteryHealth.COLD -> R.string.health_cold
            BatteryHealth.UNKNOWN -> R.string.health_unknown
        },
    )

    private fun wifiChannel(frequencyMhz: Int): Int? = when (frequencyMhz) {
        2484 -> 14
        in 2412..2472 -> (frequencyMhz - 2407) / 5
        in 5160..5885 -> (frequencyMhz - 5000) / 5
        in 5955..7115 -> (frequencyMhz - 5950) / 5
        else -> null
    }
}

private class RowBuilder(private val context: Context) {
    private val rows = mutableListOf<FormattedRow>()

    fun add(labelRes: Int, value: String?) {
        val text = value?.trim().orEmpty()
        if (text.isEmpty()) return
        rows += FormattedRow(context.getString(labelRes), text)
    }

    fun addJoined(labelRes: Int, values: List<String>) {
        if (values.isEmpty()) return
        add(labelRes, values.joinToString(", "))
    }

    fun build(): List<FormattedRow> = rows.toList()
}
