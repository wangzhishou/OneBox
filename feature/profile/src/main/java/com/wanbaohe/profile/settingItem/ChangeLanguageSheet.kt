package com.wanbaohe.profile.settingItem

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getCurrentLocaleString
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getDisplayName
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getLanguages
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedRadioButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.enhancedVerticalScroll
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItemOverload
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import java.util.Locale

/**
 * 应用内语言选择底部弹窗。当系统级语言设置入口不可用（API < 33 或拉起失败）时，
 * 作为兜底入口让用户在受支持的语言之间切换。
 */
@Composable
fun ChangeLanguageSheet(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    EnhancedModalBottomSheet(
        onDismiss = {
            if (!it) onDismiss()
        },
        title = {
            TitleItem(
                text = stringResource(R.string.language),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language
            )
        },
        sheetContent = {
            val entries = remember { context.getLanguages() }
            val selected = remember { context.getCurrentLocaleString() }
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .enhancedVerticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    entries.entries.forEachIndexed { index, locale ->
                        val isSelected =
                            selected == locale.value || (selected.isEmpty() && index == 0)
                        PreferenceItemOverload(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                context.setGlobalLocale(
                                    locale.key.takeIf { it.isNotBlank() }
                                        ?.let(Locale::forLanguageTag)
                                )
                            },
                            resultModifier = Modifier.padding(
                                start = 16.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                            color = animateColorAsState(
                                if (isSelected) MaterialTheme
                                    .colorScheme
                                    .secondaryContainer
                                else EnhancedBottomSheetDefaults.contentContainerColor
                            ).value,
                            shape = ShapeDefaults.byIndex(
                                index = index,
                                size = entries.size
                            ),
                            endIcon = {
                                EnhancedRadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        context.setGlobalLocale(
                                            locale.key.takeIf { it.isNotBlank() }
                                                ?.let(Locale::forLanguageTag)
                                        )
                                    }
                                )
                            },
                            title = locale.value,
                            subtitle = remember(locale) {
                                getDisplayName(
                                    lang = locale.key,
                                    useDefaultLocale = true
                                )
                            }.takeIf { locale.value != it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onDismiss
            ) {
                AutoSizeText(stringResource(R.string.close))
            }
        },
        visible = visible
    )
}

@Suppress("DEPRECATION")
private fun Context.setGlobalLocale(locale: Locale?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSystemService(LocaleManager::class.java).applicationLocales =
            locale?.let {
                LocaleList.forLanguageTags(it.toLanguageTag())
            } ?: LocaleList.getEmptyLocaleList()
    } else {
        val newLocale = locale ?: Resources.getSystem().configuration.locales[0]
        Locale.setDefault(newLocale)

        val configuration = resources.configuration
        configuration.setLocale(newLocale)

        resources.updateConfiguration(
            configuration,
            resources.displayMetrics
        )
    }

    AppCompatDelegate.setApplicationLocales(
        locale?.let {
            LocaleListCompat.forLanguageTags(it.toLanguageTag())
        } ?: LocaleListCompat.getEmptyLocaleList()
    )
}
