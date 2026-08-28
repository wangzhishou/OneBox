package com.wanbaohe.profile.settingItem

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.network.update.OpenSourceReleaseEntryPoint
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getCurrentLocaleString
import com.t8rin.imagetoolbox.core.ui.utils.helper.toUiPath
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxThemedIconBadge
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.wanbaohe.profile.model.ProfileSetting
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BaseSettingItem(
    modifier: Modifier,
    setting: ProfileSetting,
    themeIndex: Int = 0,
    onclick: () -> Unit = {},
    settingsComponent: SettingsComponent,
    appComponent: AppComponent? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val context = LocalContext.current
    var subtitle = stringResource(setting.subtitle)
    val settingsState = LocalSettingsState.current
    val currentFolderUri = settingsState.saveFolderUri
    when (setting) {
        ProfileSetting.FontFamilySetting -> {
            subtitle = settingsState.font.name ?: stringResource(R.string.system)
        }

        ProfileSetting.ClearCache -> {
            subtitle = settingsComponent.getReadableCacheSize()
        }

        ProfileSetting.FolderSetting -> {
            subtitle = currentFolderUri?.toUiPath(
                context = context,
                default = stringResource(R.string.unspecified)
            )
                ?: stringResource(R.string.default_folder)
        }

        ProfileSetting.LanguageSetting -> {
            subtitle = context.getCurrentLocaleString()
        }

        ProfileSetting.AIModelWorkSettings -> {
            val engine = appComponent?.aiEngineManager?.currentAIEngine?.collectAsState()
            subtitle = engine?.value?.let {
                val modelName = it.model.name
                if (modelName.isNotBlank() && modelName != it.title) {
                    "${it.title} · $modelName"
                } else {
                    it.title
                }
            } ?: ""
        }

        ProfileSetting.TTSSettings -> {
            val config = appComponent?.ttsService?.observeConfig()?.collectAsState(null)
            subtitle = config?.value?.let {
                val provider = when (it.providerType) {
                    com.shifenmiao.model.tts.TTSProviderType.OPENAI_COMPATIBLE -> "OpenAI"
                    com.shifenmiao.model.tts.TTSProviderType.MIMO -> "Mimo"
                }
                "$provider · ${it.model} · ${it.defaultVoice}"
            } ?: ""
        }

        ProfileSetting.OpenSource -> {
            val release = remember {
                EntryPointAccessors.fromApplication(
                    context = context.applicationContext,
                    entryPoint = OpenSourceReleaseEntryPoint::class.java,
                ).openSourceReleaseChecker()
            }.latestRelease.collectAsState().value
            release?.let {
                subtitle = if (it.isNewer) {
                    stringResource(CoreR.string.profile_item_open_source_update, it.tag)
                } else {
                    stringResource(CoreR.string.profile_item_open_source_latest)
                }
            }
        }

        else -> {

        }
    }
    OneBoxListItem(
        modifier = modifier,
        onClick = onclick,
        headlineContent = {
            Text(
                text = stringResource(setting.title),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = if (subtitle.isNotEmpty()) {
            {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            null
        },
        trailingContent = trailingContent ?: {
            Icon(
                modifier = Modifier
                    .size(14.dp),
                imageVector = setting.trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f)
            )
        },
        leadingContent = {
            if (setting.drawableIcon != 0) {
                OneBoxThemedIconBadge(
                    icon = ImageVector.vectorResource(id = setting.drawableIcon),
                    themeIndex = themeIndex
                )
            } else {
                OneBoxThemedIconBadge(
                    icon = setting.icon,
                    themeIndex = themeIndex
                )
            }
        }
    )
}
