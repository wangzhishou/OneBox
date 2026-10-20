package com.wanbaohe.setting.skill.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.SkillFrontMatterParser
import com.shifenmiao.database.ai.entity.SkillEntity
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.text.EditorUiDefaults
import com.wanbaohe.markdown.edit.webview.WebViewMarkdownEditor
import com.wanbaohe.markdown.edit.webview.rememberWebViewMarkdownEditorState
import com.wanbaohe.setting.skill.component.SkillDetailComponent
import com.wanbaohe.settings.R as SettingsR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 技能全屏正文编辑页（照 SystemPromptDetail 模式）。
 *
 * 本页只管正文：顶栏标题 = 技能名，描述只读展示；body 用 WebViewMarkdownEditor
 * 编辑剥掉 frontmatter 的正文，保存时由 Component 用库里最新的
 * name/description 拼装 SKILL.md 并走校验管线。
 * name/description 的编辑在列表页的元数据弹窗里完成。
 */
@Composable
fun SkillDetailScreen(component: SkillDetailComponent) {
    val skill by component.skill.collectAsState()
    val loaded by component.loaded.collectAsState()
    val editable = skill?.source == SkillEntity.SOURCE_LOCAL

    var isDirty by remember { mutableStateOf(false) }
    val showExitDialog = rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val editorState = rememberWebViewMarkdownEditorState()

    val saveSuccessText = stringResource(R.string.save_success)
    val saveInvalidText = stringResource(SettingsR.string.skill_save_invalid)
    val slugImmutableText = stringResource(SettingsR.string.skill_save_slug_immutable)
    val copyFailedText = stringResource(SettingsR.string.skill_copy_failed)
    val notFoundText = stringResource(SettingsR.string.skill_detail_not_found)

    // 技能行不存在（已删除 / 空 id）→ 提示并返回
    LaunchedEffect(loaded, skill) {
        if (loaded && skill == null) {
            AppToastHost.showToast(notFoundText)
            component.onGoBack()
        }
    }

    // 切换技能（含另存副本）后重置脏状态；正文经 key + initialValue 注入（见下）
    LaunchedEffect(skill) {
        isDirty = false
    }

    val onBack = {
        if (isDirty && editable) {
            showExitDialog.value = true
        } else {
            component.onGoBack()
        }
    }

    fun handleSave() {
        scope.launch {
            component.saveEdit(editorState.getContent()) { result ->
                scope.launch(Dispatchers.Main) {
                    when (result) {
                        SkillDetailComponent.SaveResult.SUCCESS -> {
                            editorState.clearDraft()
                            AppToastHost.showToast(saveSuccessText)
                            component.onGoBack()
                        }

                        SkillDetailComponent.SaveResult.SLUG_IMMUTABLE ->
                            AppToastHost.showToast(slugImmutableText)

                        SkillDetailComponent.SaveResult.INVALID ->
                            AppToastHost.showToast(saveInvalidText)
                    }
                }
            }
        }
    }

    BaseScreen(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = skill?.name ?: "",
        onGoBack = onBack,
        supportGlassEffect = false,
        actions = {
            if (editable) {
                IconButton(onClick = ::handleSave) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.done_button),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // BUNDLED / REMOTE 只读：另存副本，成功后本页切换为副本编辑态
                TextButton(
                    onClick = {
                        component.saveAsCopy { copy ->
                            scope.launch(Dispatchers.Main) {
                                if (copy == null) {
                                    AppToastHost.showToast(copyFailedText)
                                }
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(SettingsR.string.skill_save_as_copy))
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 正文编辑器（剥掉 frontmatter；只读态 readOnly）。
            // key + initialValue：技能加载完成后重建编辑器，让组件在 onEditorReady 时
            // 用 initialValue 一次性注入正文——不用 state.setContent，
            // 避免"setContent 早于 ready 存入暂存 → ready 后组件又用空 initialValue 覆盖"
            // 的时序竞争把正文抹空。
            key(component.skillId, skill?.id) {
                WebViewMarkdownEditor(
                    initialValue = skill?.let { SkillFrontMatterParser.extractBody(it.body) }.orEmpty(),
                    state = editorState,
                    readOnly = !editable,
                    textStyle = EditorUiDefaults.contentTextStyle(),
                    storageKey = "skill_detail_${component.skillId}",
                    onContentChanged = { if (editable) isDirty = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

    BackHandler(onBack = onBack)

    ExitWithoutSavingDialog(
        onExit = { component.onGoBack() },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value
    )
}
