package com.shifenmiao.webview.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import androidx.compose.foundation.layout.PaddingValues
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePublic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDevices

@Composable
fun BrowserSettingsPage(component: BrowserComponent) {
    val state by component.state.collectAsState()
    val settings = state.settings
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showSearchEngineDialog by remember { mutableStateOf(false) }
    var showUserAgentDialog by remember { mutableStateOf(false) }
    var showCustomUaDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.browser_settings),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle(stringResource(R.string.browser_section_general))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerAlpha = 0.2f,
            borderWidth = 0.5.dp
        ) {
            Column {
                SettingsItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePublic,
                    title = stringResource(R.string.browser_search_engine),
                    subtitle = settings.searchEngineName,
                    onClick = { showSearchEngineDialog = true }
                )
                SettingsItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDevices,
                    title = stringResource(R.string.browser_user_agent),
                    subtitle = UserAgentPreset.byId(settings.userAgentPresetId).displayName,
                    onClick = { showUserAgentDialog = true }
                )
                SettingsItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                    title = stringResource(R.string.browser_text_size),
                    subtitle = "${settings.textSize}%",
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle(stringResource(R.string.browser_section_privacy))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerAlpha = 0.2f,
            borderWidth = 0.5.dp
        ) {
            Column {
                SettingsItemWithSwitch(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                    title = stringResource(R.string.browser_privacy_mode),
                    subtitle = stringResource(R.string.browser_privacy_mode_subtitle),
                    checked = settings.enablePrivacyMode,
                    onCheckedChange = { component.updateSettings(settings.copy(enablePrivacyMode = it)) }
                )
                SettingsItemWithSwitch(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSecurity,
                    title = stringResource(R.string.browser_clear_on_exit),
                    subtitle = stringResource(R.string.browser_clear_on_exit_subtitle),
                    checked = settings.clearCacheOnExit,
                    onCheckedChange = { component.updateSettings(settings.copy(clearCacheOnExit = it)) }
                )
                SettingsItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    title = stringResource(R.string.browser_clear_browsing_data),
                    subtitle = stringResource(R.string.browser_clear_browsing_data_subtitle),
                    onClick = { showClearCacheDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle(stringResource(R.string.browser_section_about))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerAlpha = 0.2f,
            borderWidth = 0.5.dp
        ) {
            Column {
                SettingsItem(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                    title = stringResource(R.string.browser_about),
                    subtitle = stringResource(R.string.browser_version, "1.0.0"),
                    onClick = { }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showClearCacheDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.browser_clear_browsing_data)) },
            text = { Text(stringResource(R.string.browser_clear_data_confirm)) },
            confirmButton = {
                Text(
                    text = stringResource(R.string.browser_clear),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            component.clearCache()
                            component.clearHistory()
                            showClearCacheDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = stringResource(R.string.browser_cancel),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showClearCacheDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        )
    }

    if (showSearchEngineDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showSearchEngineDialog = false },
            title = { Text(stringResource(R.string.browser_select_search_engine)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SearchEngine.all.forEach { engine ->
                        val isSelected = engine.name == settings.searchEngineName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    component.updateSettings(settings.copy(searchEngineName = engine.name))
                                    showSearchEngineDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = engine.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePublic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Text(
                    text = stringResource(R.string.browser_cancel),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSearchEngineDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        )
    }

    if (showUserAgentDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showUserAgentDialog = false },
            title = { Text(stringResource(R.string.browser_ua_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    UserAgentPreset.all.forEach { preset ->
                        val isSelected = preset.id == settings.userAgentPresetId &&
                                preset.id != BrowserSettings.CUSTOM_PRESET_ID
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    component.updateSettings(settings.copy(userAgentPresetId = preset.id))
                                    if (preset.id != BrowserSettings.CUSTOM_PRESET_ID) {
                                        showUserAgentDialog = false
                                    } else {
                                        showCustomUaDialog = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (preset.userAgent.isNotEmpty()) {
                                    Text(
                                        text = preset.userAgent.take(60) + "…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    val isCustomSelected = settings.userAgentPresetId == BrowserSettings.CUSTOM_PRESET_ID
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                component.updateSettings(settings.copy(userAgentPresetId = BrowserSettings.CUSTOM_PRESET_ID))
                                showCustomUaDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.browser_custom),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isCustomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isCustomSelected && settings.customUserAgent.isNotEmpty()) {
                                Text(
                                    text = settings.customUserAgent.take(60) + "…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        if (isCustomSelected) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Text(
                    text = stringResource(R.string.browser_cancel),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showUserAgentDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        )
    }

    if (showCustomUaDialog) {
        var customUa by remember { mutableStateOf(settings.customUserAgent) }
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showCustomUaDialog = false },
            title = { Text(stringResource(R.string.browser_custom_ua)) },
            text = {
                GlassOutlinedTextField(
                    value = customUa,
                    onValueChange = { customUa = it },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    style = GlassStyle.Thin,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.browser_custom_ua_hint), style = MaterialTheme.typography.bodySmall) }
                )
            },
            confirmButton = {
                Text(
                    text = stringResource(R.string.browser_confirm),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            component.updateSettings(
                                settings.copy(
                                    userAgentPresetId = BrowserSettings.CUSTOM_PRESET_ID,
                                    customUserAgent = customUa
                                )
                            )
                            showCustomUaDialog = false
                            showUserAgentDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = stringResource(R.string.browser_cancel),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCustomUaDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        GlassSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

