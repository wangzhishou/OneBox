package com.t8rin.imagetoolbox.feature.pdf_tools.presentation.router

import android.net.Uri
import androidx.compose.foundation.background
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as LazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.model.activity.ActivityLogEntry
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompress
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMergeType
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotate90

@Composable
internal fun PdfPickerLandingScreen(
    onPick: (Screen.PdfTools.Type) -> Unit,
    onGoBack: () -> Unit,
    recentDocs: List<ActivityLogEntry> = emptyList(),
    recentPdfs: List<RecentAccessEntity> = emptyList(),
    onRecordAccess: (Uri) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
) {
    val pdfPreviewPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.Preview(uri))
        }
    )
    val pdfToImagesPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.PdfToImages(uri))
        }
    )
    val imagesToPdfPicker = rememberImagePicker(
        onSuccess = { uri: Uri -> onPick(Screen.PdfTools.Type.ImagesToPdf(listOf(uri))) }
    )
    val mergePdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uris: List<Uri> ->
            uris.forEach(onRecordAccess)
            onPick(Screen.PdfTools.Type.Merge(uris))
        }
    )
    val splitPdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.Split(uri))
        }
    )
    val compressPdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.Compress(uri))
        }
    )
    val rotatePdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.Rotate(uri))
        }
    )
    val removePagesPdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.RemovePages(uri))
        }
    )
    val rearrangePdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            onRecordAccess(uri)
            onPick(Screen.PdfTools.Type.Rearrange(uri))
        }
    )

    val tools = remember {
        listOf(
            PdfToolEntry(
                titleRes = R.string.merge_pdf,
                subtitleRes = R.string.merge_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMergeType,
                launcher = { mergePdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.split_pdf,
                subtitleRes = R.string.split_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentCut,
                launcher = { splitPdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.compress_pdf,
                subtitleRes = R.string.compress_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompress,
                launcher = { compressPdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.rotate_pdf,
                subtitleRes = R.string.rotate_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRotate90,
                launcher = { rotatePdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.remove_pages_pdf,
                subtitleRes = R.string.remove_pages_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                launcher = { removePagesPdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.rearrange_pdf,
                subtitleRes = R.string.rearrange_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert,
                launcher = { rearrangePdfPicker.pickFile() }
            ),
            PdfToolEntry(
                titleRes = R.string.images_to_pdf,
                subtitleRes = R.string.images_to_pdf_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                launcher = { imagesToPdfPicker.pickImage() }
            ),
            PdfToolEntry(
                titleRes = R.string.pdf_to_images,
                subtitleRes = R.string.pdf_to_images_sub,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                launcher = { pdfToImagesPicker.pickFile() }
            ),
        )
    }

    BaseScreen(
        title = stringResource(R.string.pdf_tools),
        onGoBack = onGoBack,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
            }

            item(span = { GridItemSpan(2) }) {
                ImportDocumentCard(
                    onClick = pdfPreviewPicker::pickFile,
                    recentPdfs = recentPdfs,
                    onRecentPdfClick = { uri ->
                        onRecordAccess(uri)
                        onPick(Screen.PdfTools.Type.Preview(uri))
                    },
                )
            }

            items(tools) { entry ->
                ToolCard(
                    title = stringResource(entry.titleRes),
                    subtitle = stringResource(entry.subtitleRes),
                    icon = entry.icon,
                    onClick = entry.launcher,
                )
            }

            if (recentDocs.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = stringResource(R.string.recent_documents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                items(recentDocs) { doc ->
                    RecentDocCard(
                        entry = doc,
                        onClick = {
                            val payload = runCatching { JSONObject(doc.payload) }.getOrNull()
                            val fileUri = payload?.optString("fileUri")
                            if (!fileUri.isNullOrEmpty()) {
                                onPick(Screen.PdfTools.Type.Preview(fileUri.toUri()))
                            }
                        }
                    )
                }
            }
        }
    }
}

private data class PdfToolEntry(
    val titleRes: Int,
    val subtitleRes: Int,
    val icon: ImageVector,
    val launcher: () -> Unit,
)

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .flatGlassContainer(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                resultPadding = 0.dp,
            ),
        placeholder = {
            Text(
                text = stringResource(R.string.search_your_library),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = CircleShape,
    )
}

@Composable
private fun ImportDocumentCard(
    onClick: () -> Unit,
    recentPdfs: List<RecentAccessEntity>,
    onRecentPdfClick: (Uri) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .flatGlassContainer(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                resultPadding = 0.dp,
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.preview_pdf),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.preview_pdf_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (recentPdfs.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.recent_pdfs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LazyRowItems(
                    items = recentPdfs,
                    key = { it.uri },
                ) { entity ->
                    RecentPdfChip(
                        name = entity.displayName,
                        onClick = { onRecentPdfClick(entity.uri.toUri()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPdfChip(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .width(140.dp)
            .flatGlassContainer(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                resultPadding = 0.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .flatGlassContainer(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
            .height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentDocCard(
    entry: ActivityLogEntry,
    onClick: () -> Unit,
) {
    val fileName = remember(entry) {
        runCatching {
            JSONObject(entry.payload).optString("fileName").takeIf { it.isNotEmpty() }
        }.getOrNull() ?: entry.title
    }
    val timeAgo = remember(entry) {
        formatRelativeTime(entry.createdAt)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .flatGlassContainer(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .height(100.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Column {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 7 -> "${days}天前"
        days < 30 -> "${days / 7}周前"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}
