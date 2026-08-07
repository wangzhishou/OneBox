/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.domain.model.QrType
import com.t8rin.imagetoolbox.core.domain.model.QrType.Wifi.EncryptionType
import com.t8rin.imagetoolbox.core.domain.utils.trimTrailingZero
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Latitude
import com.t8rin.imagetoolbox.core.resources.icons.Longitude
import com.t8rin.imagetoolbox.core.utils.getString
import java.text.DateFormat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineProfile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePublic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAtEmail
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBadge
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBusiness
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNumbers
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePassword
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePhone
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShortText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickyNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTopic

internal data class InfoEntry(
    val icon: ImageVector,
    val text: String,
    val canCopy: Boolean,
)

internal data class QrInfo(
    val title: String,
    val icon: ImageVector,
    val intent: Intent?,
    val data: List<InfoEntry>,
) {
    companion object
}

@Composable
internal fun rememberQrInfo(qrType: QrType.Complex): QrInfo {
    return when (qrType) {
        is QrType.Wifi -> wifiQrInfo(qrType)
        is QrType.Email -> emailQrInfo(qrType)
        is QrType.Geo -> geoQrInfo(qrType)
        is QrType.Phone -> phoneQrInfo(qrType)
        is QrType.Sms -> smsQrInfo(qrType)
        is QrType.Contact -> contactQrInfo(qrType)
        is QrType.Calendar -> calendarQrInfo(qrType)
    }
}

@Composable
private fun calendarQrInfo(
    qrType: QrType.Calendar
): QrInfo = qrInfoBuilder(qrType) {
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
            text = qrType.summary.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.summary.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
            text = qrType.description.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.description.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlace,
            text = qrType.location.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.location.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile,
            text = qrType.organizer.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.organizer.isNotBlank()
        )
    )
    val start = runCatching {
        qrType.start?.let {
            DateFormat.getDateTimeInstance().format(it)
        }?.removeSuffix(":00")
    }.getOrNull().orEmpty()

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStart,
            text = start.ifBlank { getString(R.string.not_specified) },
            canCopy = start.isNotBlank()
        )
    )

    val end = runCatching {
        qrType.end?.let {
            DateFormat.getDateTimeInstance().format(it)
        }?.removeSuffix(":00")
    }.getOrNull().orEmpty()

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
            text = end.ifBlank { getString(R.string.not_specified) },
            canCopy = end.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
            text = qrType.status.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.status.isNotBlank()
        )
    )
}

@Composable
private fun contactQrInfo(
    qrType: QrType.Contact
): QrInfo = qrInfoBuilder(qrType) {
    val formattedName = qrType.name.formattedName.replace("\\", "")
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile,
            text = formattedName.ifBlank { getString(R.string.not_specified) },
            canCopy = formattedName.isNotBlank()
        )
    )
    if (qrType.name.pronunciation.isNotBlank()) {
        entry(
            InfoEntry(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver,
                text = qrType.name.pronunciation,
                canCopy = true
            )
        )
    }
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBusiness,
            text = qrType.organization.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.organization.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBadge,
            text = qrType.title.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.title.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePhone,
            text = qrType.phones.joinToString("\n") { it.number }
                .ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.phones.isNotEmpty()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAtEmail,
            text = qrType.emails.joinToString("\n") { it.address }
                .ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.emails.isNotEmpty()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink,
            text = qrType.urls.joinToString("\n")
                .ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.urls.isNotEmpty()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
            text = qrType.addresses.joinToString("\n") { it.addressLines.joinToString(" ") }
                .ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.addresses.isNotEmpty()
        )
    )
}

@Composable
private fun smsQrInfo(
    qrType: QrType.Sms
): QrInfo = qrInfoBuilder(qrType) {
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStickyNote,
            text = qrType.message.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.message.isNotBlank()
        )
    )
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePhone,
            text = qrType.phoneNumber.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.phoneNumber.isNotBlank()
        )
    )
}

@Composable
private fun phoneQrInfo(
    qrType: QrType.Phone
): QrInfo = qrInfoBuilder(qrType) {
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNumbers,
            text = qrType.number.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.number.isNotBlank()
        )
    )
}

@Composable
private fun geoQrInfo(
    qrType: QrType.Geo
): QrInfo = qrInfoBuilder(qrType) {
    val latitude = qrType.latitude?.toString()?.trimTrailingZero().orEmpty()
    val longitude = qrType.longitude?.toString()?.trimTrailingZero().orEmpty()

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Latitude,
            text = latitude.ifBlank { getString(R.string.not_specified) },
            canCopy = latitude.isNotBlank()
        )
    )

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Longitude,
            text = longitude.ifBlank { getString(R.string.not_specified) },
            canCopy = longitude.isNotBlank()
        )
    )
}

@Composable
private fun emailQrInfo(
    qrType: QrType.Email
): QrInfo = qrInfoBuilder(qrType) {
    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAtEmail,
            text = qrType.address.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.address.isNotBlank()
        )
    )

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTopic,
            text = qrType.subject.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.subject.isNotBlank()
        )
    )

    entry(
        InfoEntry(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShortText,
            text = qrType.body.ifBlank { getString(R.string.not_specified) },
            canCopy = qrType.body.isNotBlank()
        )
    )
}

@Composable
private fun wifiQrInfo(
    qrType: QrType.Wifi
): QrInfo = qrInfoBuilder(qrType) {
    val ssid = InfoEntry(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
        text = qrType.ssid.ifBlank { getString(R.string.not_specified) },
        canCopy = qrType.ssid.isNotBlank()
    )
    when (qrType.encryptionType) {
        EncryptionType.OPEN -> {
            entry(
                InfoEntry(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePublic,
                    text = getString(R.string.open_network),
                    canCopy = false
                )
            )
            entry(ssid)
        }

        else -> {
            entry(
                InfoEntry(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSecurity,
                    text = qrType.encryptionType.toString(),
                    canCopy = false
                )
            )
            entry(ssid)
            entry(
                InfoEntry(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePassword,
                    text = qrType.password.ifBlank { getString(R.string.not_specified) },
                    canCopy = qrType.password.isNotBlank()
                )
            )
        }
    }
}