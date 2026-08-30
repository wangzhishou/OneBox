package com.wanbaohe.profile.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.MiniEdit
import com.t8rin.imagetoolbox.core.resources.icons.Github
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClearCache
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCoffee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDonate
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFont
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOutputDir
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQq
import com.t8rin.imagetoolbox.core.resources.icons.line.LineServerModels
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWechat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkModels
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.t8rin.imagetoolbox.core.resources.icons.line.LineToggle
import kotlinx.parcelize.RawValue
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText

sealed class ProfileSetting(
    val id: Int = 0,
    val title: Int = R.string.empty_string,
    val subtitle: Int = R.string.empty_string,
    val groupTitle: Int = R.string.empty_string,
    val icon: @RawValue ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
    val drawableIcon: Int = 0,
    val trailingIcon: @RawValue ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
    val settingsList: List<ProfileSetting>? = null,
    val isDebug: Boolean = false,
    var expanded: Boolean = false
) {
    data object SettingGroup : ProfileSetting(
        groupTitle = R.string.profile_group_support,
        settingsList = listOf(
            Help,
            Donate,
            Community
        )
    )

    data object OperateGroup : ProfileSetting(
        groupTitle = R.string.profile_group_system,
        settingsList = listOf(
            AuthCode,
            DisplaySetting,
            LanguageSetting,
            ClearCache,
            FolderSetting,
            AnalyticsSetting
        )
    )

    data object AIGroup : ProfileSetting(
        groupTitle = R.string.profile_group_ai_settings,
        settingsList = listOf(
            AIModelSettings,
            AIModelWorkSettings,
            AITokenUsage,
            TTSSettings,
            ImageGenerationSettings,
            AIFeatureSettings,
            SystemPromptManagement
        )
    )

    data object AboutUsGroup : ProfileSetting(
        groupTitle = R.string.profile_group_about,
        settingsList = listOf(
            AboutUsSetting,
            UpdateSetting,
            OpenSource,
            SupportDeveloper,
            RateAppSetting,
            ShareAppSetting
        )
    )

    data object Help : ProfileSetting(
        id = 1,
        title = R.string.profile_setting_help,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp
    )

    data object QQGroup : ProfileSetting(
        id = 2,
        title = R.string.profile_item_qq_group,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQq,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew
    )

    data object Wechat : ProfileSetting(
        id = 3,
        title = R.string.profile_item_wechat,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWechat,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy
    )

    data object Donate : ProfileSetting(
        id = 4,
        title = R.string.profile_item_donate,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDonate
    )

    data object ClearCache : ProfileSetting(
        id = 5,
        title = R.string.profile_item_clear,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineClearCache
    )

    data object FolderSetting : ProfileSetting(
        id = 6,
        title = R.string.profile_item_folder,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOutputDir
    )

    data object FontFamilySetting : ProfileSetting(
        id = 8,
        title = R.string.profile_item_font_family,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.MiniEdit
    )

    data object FontSizeSetting : ProfileSetting(
        id = 9,
        title = R.string.profile_item_font_size,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont
    )

    data object AboutUsSetting : ProfileSetting(
        id = 10,
        title = R.string.profile_item_about,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo
    )

    data object UpdateSetting : ProfileSetting(
        id = 11,
        title = R.string.profile_item_check_update,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSync
    )

    data object OpenSource : ProfileSetting(
        id = 26,
        title = R.string.profile_item_open_source,
        subtitle = R.string.profile_item_open_source_sub,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Github,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew
    )

    // 仅海外渠道(google / foss)展示的 Ko-fi 打赏入口, 可见性在 ProfileScreen 按 isOverseas 过滤
    data object SupportDeveloper : ProfileSetting(
        id = 27,
        title = R.string.profile_item_support_dev,
        subtitle = R.string.profile_item_support_dev_sub,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCoffee,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew
    )

    data object ShareAppSetting : ProfileSetting(
        id = 12,
        title = R.string.profile_item_share_app,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare
    )

    data object RateAppSetting : ProfileSetting(
        id = 20,
        title = R.string.profile_item_rate_app,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
    )

    data object DisplaySetting : ProfileSetting(
        id = 13,
        title = R.string.profile_item_dispaly,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme
    )

    data object LanguageSetting : ProfileSetting(
        id = 23,
        title = com.t8rin.imagetoolbox.core.resources.R.string.language,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.MiniEdit
    )

    data object AIModelSettings : ProfileSetting(
        id = 15,
        title = R.string.profile_item_ai_service_and_models,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineServerModels
    )

    data object AIFeatureSettings : ProfileSetting(
        id = 21,
        title = R.string.profile_item_ai_feature_settings,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures
    )

    data object AIModelWorkSettings : ProfileSetting(
        id = 16,
        title = R.string.profile_item_ai_workflow_models,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkModels
    )

    data object AITokenUsage : ProfileSetting(
        id = 17,
        title = R.string.profile_item_ai_usage,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBarChart
    )

    data object TTSSettings : ProfileSetting(
        id = 18,
        title = R.string.profile_item_tts_settings,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMic
    )

    data object ImageGenerationSettings : ProfileSetting(
        id = 25,
        title = R.string.profile_item_image_generation_settings,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures
    )

    data object SystemPromptManagement : ProfileSetting(
        id = 19,
        title = R.string.profile_item_ai_reply_style,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt
    )

    data object Community : ProfileSetting(
        id = 19,
        title = R.string.profile_item_community,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGroup
    )

    data object AuthCode : ProfileSetting(
        id = 22,
        title = R.string.profile_item_auth_code,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock
    )

    data object AnalyticsSetting : ProfileSetting(
        id = 24,
        title = R.string.profile_item_analytics,
        subtitle = R.string.profile_item_analytics_sub,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineToggle
    )

    companion object {
        val entries: List<ProfileSetting> by lazy {
            listOf(
                AIGroup,
                SettingGroup,
                OperateGroup,
                AboutUsGroup
            )
        }
    }
}