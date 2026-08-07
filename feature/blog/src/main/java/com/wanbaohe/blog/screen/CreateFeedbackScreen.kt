package com.wanbaohe.blog.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.LabelText
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.components.LoadingOverlay
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.upload.rememberImageUploader
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.DataMultipleSelector
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.blog.logic.CreateFeedbackComponent
import com.wanbaohe.blog.ui.ImageUploadGallery
import com.shifenmiao.common.upload.UploadingImage
import kotlinx.coroutines.launch

@Composable
fun CreateFeedbackScreen(
    createFeedbackComponent: CreateFeedbackComponent,
    appComponent: AppComponent
) {
    val feedbackRequest by createFeedbackComponent.feedbackRequest.collectAsState()
    val tags by createFeedbackComponent.tags.collectAsState()
    val selectTags by createFeedbackComponent.selectedTags.collectAsState()
    val isDataChange by createFeedbackComponent.isDataChange.collectAsState()
    val isCheckError by createFeedbackComponent.isCheckError.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var feedbackTitle by rememberSaveable { mutableStateOf(feedbackRequest.title) }
    val onBack = {
        if (isDataChange) {
            showExitDialog = true
        } else {
            createFeedbackComponent.onGoBack()
        }
    }
    val markdownCode = remember {
        mutableStateOf(TextFieldValue(feedbackRequest.content))
    }

    val localActivity = LocalComponentActivity.current
    val uploadingImages = remember { mutableStateListOf<UploadingImage>() }

    val imageFilePickerAndUploader = rememberImageUploader(
        localActivity = localActivity,
        commonComponent = appComponent as CommonComponent,
        onPickerSuccess = { files ->
            files.forEachIndexed { index, uri ->
                val newUploadingImage = UploadingImage(localUri = uri.toString())
                uploadingImages.add(index, newUploadingImage)
            }
        },
        onUploadFailure = { index, msg ->
            if (index >= 0) {
                uploadingImages[index] = uploadingImages[index].copy(isError = true)
            } else {
                ActionUtils.showError(msg)
            }
        },
        onImageUploaded = { index, strapiImages ->
            strapiImages.firstOrNull()?.let { strapiImage ->
                uploadingImages[index] = uploadingImages[index].copy(
                    id = strapiImage.id,
                    progress = 1f,
                    strapiImage = strapiImage,
                    isUploaded = true,
                    isError = false
                )
                strapiImage.id?.let { imageId ->
                    createFeedbackComponent.addImage(imageId)
                }
            }
        },
        onProgressUpdate = { index, progress ->
            if (index >= 0) {
                uploadingImages[index] = uploadingImages[index].copy(progress = progress)
            }
        }
    )

    BaseScreen(
        title = stringResource(id = R.string.feedback_create),
        onGoBack = onBack,
        supportGlassEffect = true,
        actions = {}
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DataMultipleSelector(
                    showEdit = false,
                    modifier = Modifier,
                    value = selectTags,
                    color = Color.Unspecified,
                    selectedItemColor = MaterialTheme.colorScheme.secondaryContainer,
                    onValueChange = {
                        createFeedbackComponent.setSelected(it)
                    },
                    entries = tags,
                    title = stringResource(R.string.select_groups),
                    titleIcon = null,
                    itemContentText = {
                        it.name
                    },
                    onNewItemChange = {
                    },
                    onDeleteChange = {
                    },
                    onEditItemChange = { _, _ ->
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.feedback_title_hint),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "*",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                GlassOutlinedTextField(
                    value = feedbackTitle,
                    onValueChange = {
                        feedbackTitle = it
                        createFeedbackComponent.updateTitle(it)
                    },
                    placeholder = {
                        LabelText(
                            text = stringResource(R.string.feedback_title_placeholder)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isCheckError && feedbackRequest.title.isEmpty(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    shape = AppTheme.shapes.getTextFieldShape()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.feedback_content_hint),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "*",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                GlassOutlinedTextField(
                    isError = isCheckError && feedbackRequest.content.isEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    value = markdownCode.value,
                    onValueChange = {
                        markdownCode.value = it
                        createFeedbackComponent.updateContent(it.text)
                    },
                    placeholder = {
                        LabelText(
                            text = stringResource(R.string.feedback_placeholder)
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    shape = AppTheme.shapes.getTextFieldShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                )

                if (isCheckError) {
                    Text(
                        text = stringResource(id = R.string.fields_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.feedback_upload_image),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ImageUploadGallery(
                    modifier = Modifier.fillMaxWidth(),
                    images = uploadingImages,
                    onAddClick = {
                        imageFilePickerAndUploader.pickFile()
                    },
                    onImageRemove = { strapiImage ->
                        uploadingImages.removeAll {
                            it.localUri == strapiImage.localUri
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            createFeedbackComponent.submitFeedback(
                                onFailed = {
                                    isLoading = false
                                    AppToastHost.showFailureToast(getString(R.string.feedback_failed))
                                },
                                onSuccess = {
                                    AppToastHost.showToast(
                                        message = getString(R.string.feedback_onSuccess),
                                        duration = ToastDuration.Long
                                    )
                                    isLoading = false
                                    createFeedbackComponent.onGoBack()
                                }
                            )
                        }
                    },
                    text = stringResource(id = R.string.feedback_submit)
                )
            }

        }

        if (isLoading) {
            LoadingOverlay()
        }
    }

    ExitWithoutSavingDialog(
        onExit = createFeedbackComponent.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}