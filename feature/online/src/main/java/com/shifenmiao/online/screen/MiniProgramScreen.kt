package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.base.ui.button.SecondaryButton
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.theme.AppTheme

@Composable
fun MiniProgramScreen(
    onGoBack: () -> Unit = {},
    dataItem: DataItem,
    appComponent: AppComponent
) {
    val uiState by appComponent.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var rememberChoice by remember { mutableStateOf(false) }

    val shouldRememberChoice = AppSharedStorage.loadMiniProgramRememberChoice()

    if (uiState.launchMiniProResp) {
        onGoBack.invoke()
        appComponent.resetLaunchMiniProResp()
    }

    LaunchedEffect(dataItem.miniProgramId) {
        if (dataItem.miniProgramId?.isNotEmpty() == true) {
            if (!Wechat.isEnabled) {
                onGoBack.invoke()
                return@LaunchedEffect
            }
            if (shouldRememberChoice) {
                Wechat.launchMiniProgram(dataItem.miniProgramId!!)
                onGoBack.invoke()
            } else {
                showDialog = true
            }
        }
    }

    BaseScreen(
        title = dataItem.title.orEmpty(),
        onGoBack = onGoBack
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(AppTheme.dimens.paddingLarge)
        ) {
            if (showDialog) {
                item {
                    Column(
                        modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingNormal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(com.shifenmiao.online.R.string.mini_program_jump_confirm),
                            style =  MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
                        Column(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(AppTheme.dimens.paddingLarge),
                        ) {
                            Text(
                                text = dataItem.title.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
                            Text(
                                text = dataItem.description.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberChoice,
                                onCheckedChange = {
                                    rememberChoice = it
                                },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.5f
                                    ),
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                )
                            )
                            Text(
                                text = stringResource(R.string.remember_choice),
                                style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.None)
                            )
                        }
                        Row {
                            PrimaryButton(
                                onClick = {
                                    if (rememberChoice) {
                                        AppSharedStorage.saveMiniProgramRememberChoice(true)
                                    }
                                    if (Wechat.isEnabled) {
                                        Wechat.launchMiniProgram(dataItem.miniProgramId!!)
                                    }
                                    onGoBack.invoke()
                                },
                                text = stringResource(id = R.string.button_confirm)
                            )
                            Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
                            SecondaryButton(
                                onClick = {
                                    onGoBack.invoke()
                                },
                                text = stringResource(id = R.string.button_cancel)
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(text = stringResource(com.shifenmiao.online.R.string.mini_program_redirecting))
                }
            }
        }
    }
    BackHandler { onGoBack() }
}