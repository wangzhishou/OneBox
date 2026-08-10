package com.wanbaohe.profile.settingItem

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Info
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getCurrentLocaleString
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getDisplayName
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getLanguages
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedRadioButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.enhancedVerticalScroll
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItemOverload
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import com.t8rin.imagetoolbox.core.utils.LocaleSwitchWatcher
import java.util.Locale

/**
 * 应用内语言选择底部弹窗（语言设置的唯一入口）。
 * 顶部带"切换后将重启"提醒横幅；选择语言后由 LocaleSwitchWatcher 冷重启进程，
 * 整体切换到该语言的 Room 分库与 MMKV 缓存。
 */
@Composable
fun ChangeLanguageSheet(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // "切换语言后将重启"提醒是否已被勾选不再提醒（全局偏好，不随语言隔离）
    var noticeDismissed by remember { mutableStateOf(AppSharedStorage.loadLanguageSwitchNoticeDismissed()) }

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
                    // 重启提醒横幅：切换语言会冷重启进程（数据源按语言隔离），
                    // 进 sheet 即提醒；点关闭按钮后永久不再提示
                    if (!noticeDismissed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.large
                                )
                                .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.language_switch_restart_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            EnhancedIconButton(
                                onClick = {
                                    AppSharedStorage.saveLanguageSwitchNoticeDismissed(true)
                                    noticeDismissed = true
                                },
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        }
                    }
                    entries.entries.forEachIndexed { index, locale ->
                        val isSelected =
                            selected == locale.value || (selected.isEmpty() && index == 0)
                        PreferenceItemOverload(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                // 切换语言后由 LocaleSwitchWatcher 冷重启进程；
                                // 点当前已选中的语言不处理
                                if (!isSelected) {
                                    context.setGlobalLocale(
                                        locale.key.takeIf { it.isNotBlank() }
                                            ?.let(Locale::forLanguageTag)
                                    )
                                }
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
                                        if (!isSelected) {
                                            context.setGlobalLocale(
                                                locale.key.takeIf { it.isNotBlank() }
                                                    ?.let(Locale::forLanguageTag)
                                            )
                                        }
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

    // API 33+：语言选择已被 LocaleManager 同步持久化，直接冷重启切到新语言数据源，
    // 不赌 onConfigurationChanged 回调（实测部分设备应用内改语言该回调不触发）。
    // 更低版本选择只在内存里（manifest 关闭了 autoStoreLocales 服务），杀进程会丢，
    // 仍走 LocaleSwitchWatcher 的回调路径兜底。
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val newTag = locale?.toLanguageTag()
            ?: Resources.getSystem().configuration.locales[0]?.toLanguageTag()
        LocaleSwitchWatcher.restartForLocaleSwitch(
            this,
            newTag?.takeIf { it.isNotBlank() } ?: "en"
        )
    }
}
