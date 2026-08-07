package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconSelector
import com.shifenmiao.base.ui.icon.IconSelectorDisplayMode
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.screenLogic.AddCategoryComponent
import com.shifenmiao.marktodo.screenLogic.AddCategoryUiEvent
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
fun AddCategoryScreen(
    component: AddCategoryComponent,
    onGoBack: () -> Unit
) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (component.editingCategoryId != null) R.string.dialog_edit_category_title
                    else R.string.dialog_add_category_title
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onGoBack) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { component.handleEvent(AddCategoryUiEvent.Submit) }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_confirm)
                )
            }
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            // Title input field
            OneBoxOutlinedTextField(
                value = uiState.title,
                onValueChange = { newTitle ->
                    component.handleEvent(
                        AddCategoryUiEvent.UpdateTitle(newTitle)
                    )
                },
                singleLine = true,
                placeholder = { Text(text = stringResource(R.string.dialog_add_category_hint)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.hasTitleError,
                supportingText = if (uiState.hasTitleError) {
                    { Text(text = stringResource(R.string.validation_category_title_required), style = MaterialTheme.typography.bodySmall) }
                } else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done)
            )

            // Icon selection
            Text(
                text = stringResource(R.string.dialog_add_category_icon),
                style = MaterialTheme.typography.labelLarge
            )

            IconSelector(
                selectedIconKey = uiState.iconKey,
                onIconSelected = { iconKey ->
                    component.handleEvent(AddCategoryUiEvent.UpdateIconKey(iconKey))
                },
                displayMode = IconSelectorDisplayMode.ROW
            )

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }

    BackHandler {
        onGoBack()
    }
}
