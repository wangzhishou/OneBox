package com.shifenmiao.base.ui


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.utils.getString

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        modifier = modifier,
        text = text,
        textAlign = textAlign,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
fun ErrorTextInputField(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
fun getDefaultShareText(): String {
    val shareText = try {
        val appName = getString(R.string.app_name)
        val appDescription = getString(R.string.app_description)
        val appDownLoadDescription = getString(R.string.app_description_download_des)
        "$appName--$appDescription $appDownLoadDescription"
    } catch (_: Throwable) {
        // Handle the error here
        ""
    }
    return shareText
}