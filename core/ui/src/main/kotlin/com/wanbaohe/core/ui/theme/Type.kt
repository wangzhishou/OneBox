/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.wanbaohe.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiFontFamily

@Composable
fun rememberTypography(
    fontRes: UiFontFamily
): Typography {
    val language = LocalConfiguration.current.locales[0].language

    return remember(fontRes, language) {
        Typography(
            fontFamily = fontRes.fontFamily
        ).withLocalizedLineHeight(language)
    }
}

private fun Typography.withLocalizedLineHeight(language: String): Typography {
    if (language !in DenseScriptLanguages) return this

    return copy(
        displayLarge = displayLarge.withDenseScriptLineHeight(),
        displayMedium = displayMedium.withDenseScriptLineHeight(),
        displaySmall = displaySmall.withDenseScriptLineHeight(),
        headlineLarge = headlineLarge.withDenseScriptLineHeight(),
        headlineMedium = headlineMedium.withDenseScriptLineHeight(),
        headlineSmall = headlineSmall.withDenseScriptLineHeight(),
        titleLarge = titleLarge.withDenseScriptLineHeight(),
        titleMedium = titleMedium.withDenseScriptLineHeight(),
        titleSmall = titleSmall.withDenseScriptLineHeight(),
        bodyLarge = bodyLarge.withDenseScriptLineHeight(),
        bodyMedium = bodyMedium.withDenseScriptLineHeight(),
        bodySmall = bodySmall.withDenseScriptLineHeight(),
        labelLarge = labelLarge.withDenseScriptLineHeight(),
        labelMedium = labelMedium.withDenseScriptLineHeight(),
        labelSmall = labelSmall.withDenseScriptLineHeight(),
        displayLargeEmphasized = displayLargeEmphasized.withDenseScriptLineHeight(),
        displayMediumEmphasized = displayMediumEmphasized.withDenseScriptLineHeight(),
        displaySmallEmphasized = displaySmallEmphasized.withDenseScriptLineHeight(),
        headlineLargeEmphasized = headlineLargeEmphasized.withDenseScriptLineHeight(),
        headlineMediumEmphasized = headlineMediumEmphasized.withDenseScriptLineHeight(),
        headlineSmallEmphasized = headlineSmallEmphasized.withDenseScriptLineHeight(),
        titleLargeEmphasized = titleLargeEmphasized.withDenseScriptLineHeight(),
        titleMediumEmphasized = titleMediumEmphasized.withDenseScriptLineHeight(),
        titleSmallEmphasized = titleSmallEmphasized.withDenseScriptLineHeight(),
        bodyLargeEmphasized = bodyLargeEmphasized.withDenseScriptLineHeight(),
        bodyMediumEmphasized = bodyMediumEmphasized.withDenseScriptLineHeight(),
        bodySmallEmphasized = bodySmallEmphasized.withDenseScriptLineHeight(),
        labelLargeEmphasized = labelLargeEmphasized.withDenseScriptLineHeight(),
        labelMediumEmphasized = labelMediumEmphasized.withDenseScriptLineHeight(),
        labelSmallEmphasized = labelSmallEmphasized.withDenseScriptLineHeight()
    )
}

private fun TextStyle.withDenseScriptLineHeight(): TextStyle = copy(
    lineHeight = (lineHeight.value + fontSize.value * DenseScriptLineHeightRatio).sp
)

private val DenseScriptLanguages = setOf("zh", "ja", "ko")
private const val DenseScriptLineHeightRatio = 0.1f

@Composable
fun ProvideTypography(
    fontRes: UiFontFamily,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = rememberTypography(fontRes),
        content = content
    )
}
