package com.wanbaohe.translator.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.ui.VoiceInputSheet
import com.shifenmiao.ai.ui.rememberVoiceInputLauncher
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.History
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.wanbaohe.core.ui.utils.content_pickers.ImagePickerMode
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.translator.R
import com.wanbaohe.translator.component.HistoryFilter
import com.wanbaohe.translator.component.TranslatorComponent
import com.wanbaohe.translator.component.TranslatorTab
import com.wanbaohe.translator.domain.TranslationLanguage
import com.wanbaohe.translator.domain.TranslationModelMode

@Composable
fun TranslatorScreen(component: TranslatorComponent) {
    val state by component.uiState.collectAsState()
    var showVoiceInput by remember { mutableStateOf(false) }
    var showClearHistory by remember { mutableStateOf(false) }
    val voiceInput = rememberVoiceInputLauncher(
        onResult = component::updateInput,
        onShowIflytekSheet = { showVoiceInput = true },
    )
    val camera = rememberImagePicker(mode = ImagePickerMode.CameraCapture) { uris ->
        uris.firstOrNull()?.let(component::recognizeImage)
    }

    BaseScreen(
        title = stringResource(
            if (state.currentTab == TranslatorTab.HISTORY) R.string.translation_history
            else R.string.translation_title
        ),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
        actions = {
            ModelSelector(
                selected = state.modelMode,
                modes = state.supportedModelModes,
                onSelect = component::selectModel,
            )
            if (state.currentTab == TranslatorTab.HISTORY && state.history.isNotEmpty()) {
                IconButton(onClick = { showClearHistory = true }) {
                    Icon(
                        Icons.Outlined.LineClear,
                        contentDescription = stringResource(R.string.translation_clear_history),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (state.currentTab) {
                TranslatorTab.TRANSLATE -> TranslationPane(
                    state = state,
                    onInputChange = component::updateInput,
                    onSourceLanguage = component::selectSourceLanguage,
                    onTargetLanguage = component::selectTargetLanguage,
                    onSwap = component::swapLanguages,
                    onTranslate = component::translate,
                    onVoiceInput = voiceInput,
                    onCameraInput = camera::pickImage,
                    onClear = component::clearInput,
                    onSpeak = component::speakOutput,
                    onFavorite = { state.currentRecordId?.let(component::toggleFavorite) },
                    onOpenLocalModels = component.onOpenLocalModels,
                    modifier = Modifier.weight(1f),
                )
                TranslatorTab.HISTORY -> TranslationHistoryPane(
                    state = state,
                    records = component.filteredHistory(),
                    onQueryChange = component::updateHistoryQuery,
                    onFilterChange = component::selectHistoryFilter,
                    onOpen = component::openHistory,
                    onSpeak = { component.openHistory(it); component.speakOutput() },
                    onFavorite = { component.toggleFavorite(it.id) },
                    modifier = Modifier.weight(1f),
                )
            }
            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        id = TranslatorTab.TRANSLATE.name,
                        label = stringResource(R.string.translation_title),
                        icon = Icons.Rounded.Language,
                    ),
                    BottomNavItem(
                        id = TranslatorTab.HISTORY.name,
                        label = stringResource(R.string.translation_history),
                        icon = Icons.Outlined.History,
                    ),
                ),
                selectedItemId = state.currentTab.name,
                onItemClick = { item ->
                    runCatching { TranslatorTab.valueOf(item.id) }.getOrNull()?.let(component::selectTab)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    VoiceInputSheet(
        visible = showVoiceInput,
        recognizer = component.voiceRecognizer,
        onResult = component::updateInput,
        onDismiss = { showVoiceInput = false },
    )

    if (showClearHistory) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearHistory = false },
            title = { Text(stringResource(R.string.translation_clear_history)) },
            text = { Text(stringResource(R.string.translation_clear_history_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    component.clearHistory()
                    showClearHistory = false
                }) {
                    Text(stringResource(com.shifenmiao.core.R.string.button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistory = false }) {
                    Text(stringResource(com.shifenmiao.core.R.string.button_cancel))
                }
            },
        )
    }
}

@Composable
private fun ModelSelector(
    selected: TranslationModelMode,
    modes: List<TranslationModelMode>,
    onSelect: (TranslationModelMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Rounded.Language,
                contentDescription = stringResource(selected.labelRes()),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        EnhancedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(mode.labelRes()),
                            color = if (mode == selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    },
                    trailingIcon = {
                        if (mode == selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            Spacer(Modifier.size(24.dp))
                        }
                    },
                    onClick = {
                        onSelect(mode)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
internal fun TranslationModelMode.labelRes(): Int = when (this) {
    TranslationModelMode.FAST -> R.string.translation_model_fast
    TranslationModelMode.DEFAULT -> R.string.translation_model_default
    TranslationModelMode.LOCAL -> R.string.translation_model_local
}

@Composable
internal fun TranslationLanguage.labelRes(): Int = when (this) {
    TranslationLanguage.AUTO -> R.string.translation_language_auto
    TranslationLanguage.CHINESE -> R.string.translation_language_zh
    TranslationLanguage.CHINESE_TRADITIONAL -> R.string.translation_language_zh_hant
    TranslationLanguage.ENGLISH -> R.string.translation_language_en
    TranslationLanguage.JAPANESE -> R.string.translation_language_ja
    TranslationLanguage.KOREAN -> R.string.translation_language_ko
    TranslationLanguage.FRENCH -> R.string.translation_language_fr
    TranslationLanguage.GERMAN -> R.string.translation_language_de
    TranslationLanguage.SPANISH -> R.string.translation_language_es
    TranslationLanguage.PORTUGUESE -> R.string.translation_language_pt
    TranslationLanguage.ITALIAN -> R.string.translation_language_it
    TranslationLanguage.DUTCH -> R.string.translation_language_nl
    TranslationLanguage.RUSSIAN -> R.string.translation_language_ru
    TranslationLanguage.UKRAINIAN -> R.string.translation_language_uk
    TranslationLanguage.ARABIC -> R.string.translation_language_ar
    TranslationLanguage.HINDI -> R.string.translation_language_hi
    TranslationLanguage.INDONESIAN -> R.string.translation_language_in
    TranslationLanguage.VIETNAMESE -> R.string.translation_language_vi
    TranslationLanguage.THAI -> R.string.translation_language_th
    TranslationLanguage.TURKISH -> R.string.translation_language_tr
    TranslationLanguage.FILIPINO -> R.string.translation_language_fil
}

internal fun HistoryFilter.labelRes(): Int = when (this) {
    HistoryFilter.ALL -> R.string.translation_filter_all
    HistoryFilter.TODAY -> R.string.translation_filter_today
    HistoryFilter.WEEK -> R.string.translation_filter_week
    HistoryFilter.MONTH -> R.string.translation_filter_month
}
