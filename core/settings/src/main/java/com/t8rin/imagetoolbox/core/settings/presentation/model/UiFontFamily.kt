/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.t8rin.imagetoolbox.core.settings.presentation.model

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import java.io.File

sealed class UiFontFamily(
    val name: String?,
    private val variable: Boolean,
    val type: FontType? = null
) {
    val isVariable: Boolean?
        get() = variable.takeIf {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

    val fontFamily: FontFamily
        get() = type?.let {
            when (it) {
                is FontType.File -> fontFamilyFromFile(file = File(it.path))
                is FontType.Resource -> fontFamilyResource(resId = it.resId)
            }
        } ?: FontFamily.Default

    constructor(
        name: String?,
        variable: Boolean,
        fontRes: Int
    ) : this(
        name = name,
        variable = variable,
        type = FontType.Resource(fontRes)
    )

    constructor(
        name: String?,
        variable: Boolean,
        filePath: String
    ) : this(
        name = name,
        variable = variable,
        type = FontType.File(filePath)
    )

    operator fun component1() = fontFamily
    operator fun component2() = name
    operator fun component3() = isVariable
    operator fun component4() = type

    data object Montserrat : UiFontFamily(
        fontRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            R.font.montserrat_variable
        } else R.font.montserrat_regular,
        name = "Montserrat",
        variable = true
    )

    data object System : UiFontFamily(
        name = null,
        variable = true
    )

    class Custom(
        name: String?,
        val filePath: String
    ) : UiFontFamily(
        name = name,
        variable = false,
        filePath = filePath
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is Custom) return false

            return filePath == other.filePath
        }

        override fun hashCode(): Int {
            return filePath.hashCode()
        }

        override fun toString(): String {
            return "Custom(name = $name, filePath = $filePath)"
        }
    }

    fun asDomain(): DomainFontFamily {
        return when (this) {
            System -> DomainFontFamily.System
            Montserrat -> DomainFontFamily.Montserrat
            is Custom -> DomainFontFamily.Custom(name, filePath)
        }
    }

    companion object {

        val entries: List<UiFontFamily>
            @Composable
            get() = defaultEntries + customEntries

        val defaultEntries: List<UiFontFamily> by lazy {
            listOf(
                Montserrat,
                System
            ).sortedBy { it.name }
        }

        val customEntries: List<Custom>
            @Composable
            get() {
                val customFonts = LocalSettingsState.current.customFonts

                return remember(customFonts) {
                    derivedStateOf {
                        customFonts.sortedBy { it.name }
                    }
                }.value
            }
    }
}

@Composable
fun FontType?.toUiFont(): UiFontFamily {
    val entries = UiFontFamily.entries

    return remember(entries, this) {
        derivedStateOf {
            when (this) {
                is FontType.File -> UiFontFamily.Custom(
                    name = File(path).nameWithoutExtension.replace("[:\\-_.,]".toRegex(), " "),
                    filePath = path
                )

                is FontType.Resource -> entries.find { it.type == this } ?: UiFontFamily.System
                null -> UiFontFamily.System
            }
        }
    }.value
}

fun FontType?.asUi(): UiFontFamily {
    val entries = UiFontFamily.defaultEntries

    return when (this) {
        is FontType.File -> UiFontFamily.Custom(
            name = File(path).nameWithoutExtension.replace("[:\\-_.,]".toRegex(), " "),
            filePath = path
        )

        is FontType.Resource -> entries.find { it.type == this } ?: UiFontFamily.System
        null -> UiFontFamily.System
    }
}

fun FontType?.asDomain(): DomainFontFamily = this?.asUi()?.asDomain() ?: DomainFontFamily.System

fun DomainFontFamily?.asFontType(): FontType? = this?.toUiFont()?.type

fun DomainFontFamily.toUiFont(): UiFontFamily = when (this) {
    DomainFontFamily.System -> UiFontFamily.System
    DomainFontFamily.Montserrat -> UiFontFamily.Montserrat
    is DomainFontFamily.Custom -> UiFontFamily.Custom(
        name = name,
        filePath = filePath
    )
    else -> UiFontFamily.System
}

private fun fontFamilyResource(resId: Int) = FontFamily(
    Font(
        resId = resId,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Light,
            style = FontStyle.Normal
        )
    ),
    Font(
        resId = resId,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        )
    ),
    Font(
        resId = resId,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Medium,
            style = FontStyle.Normal
        )
    ),
    Font(
        resId = resId,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.SemiBold,
            style = FontStyle.Normal
        )
    ),
    Font(
        resId = resId,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        )
    )
)

private fun fontFamilyFromFile(file: File) = FontFamily(
    Font(
        file = file,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Light,
            style = FontStyle.Normal
        )
    ),
    Font(
        file = file,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Normal,
            style = FontStyle.Normal
        )
    ),
    Font(
        file = file,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Medium,
            style = FontStyle.Normal
        )
    ),
    Font(
        file = file,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.SemiBold,
            style = FontStyle.Normal
        )
    ),
    Font(
        file = file,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            weight = FontWeight.Bold,
            style = FontStyle.Normal
        )
    )
)