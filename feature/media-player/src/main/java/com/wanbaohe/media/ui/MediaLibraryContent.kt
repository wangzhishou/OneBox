package com.wanbaohe.media.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.NoResults
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMusicNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOpenWith
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSearchTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaFilter
import com.wanbaohe.media.component.MediaPlayerComponent
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaMetadata

/**
 * 媒体库页正文（原型"视频库 / 音乐库"）。
 *
 * 数据有两层："全部"是本机媒体库（MediaStore），"最近"是 App 打开过的记录。
 * 没有读媒体权限时只能看最近记录，页面顶部会给一条授权提示；空态按钮直接拉起
 * 系统文件选择器，对应原型里"去添加"。
 */
@Composable
fun MediaLibraryContent(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 播放进度每 500ms 改一次 UiState；列表只在真正影响列表的字段变化时重算 O(n)
    val visible = remember(
        state.library,
        state.recentEntries,
        state.favorites,
        state.filter,
        state.query,
    ) { state.visibleLibrary }
    val highlights = remember(
        state.recentEntries,
        state.favorites,
        state.filter,
        state.query,
    ) { state.recentHighlights }
    val recentTitle = stringResource(
        if (state.kind == MediaKind.VIDEO) R.string.media_recent_videos else R.string.media_recent_audio
    )
    val allTitle = stringResource(
        if (state.kind == MediaKind.VIDEO) R.string.media_all_videos else R.string.media_all_audio
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.isSearchOpen) {
            item {
                GlassSearchTextField(
                    value = state.query,
                    onValueChange = component::setQuery,
                    placeholder = stringResource(R.string.media_search),
                    style = GlassStyle.Medium,
                )
            }
        }

        if (state.hasLibrary) {
            item {
                FilterChips(
                    selected = state.filter,
                    onSelect = component::setFilter,
                )
            }
        }

        if (!state.hasMediaPermission) {
            item {
                PermissionBanner(onGrant = component::requestMediaPermission)
            }
        }

        if (!state.hasLibrary) {
            item {
                if (state.isLoading) {
                    // 首次读库还没回来时别急着喊"还没有音乐"，否则会闪一下空态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    EmptyMediaState(
                        kind = state.kind,
                        isLoading = false,
                        onAdd = onAddFile,
                    )
                }
            }
        } else {
            if (highlights.isNotEmpty()) {
                item {
                    RecentMediaCard(
                        title = recentTitle,
                        entries = highlights,
                        kind = state.kind,
                        loadArtwork = component::artwork,
                        loadMetadata = component::metadata,
                        onSelect = component::play,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MediaSectionHeader(
                        title = allTitle,
                        trailing = visible.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    if (visible.isNotEmpty()) {
                        TextButton(onClick = component::playAll) {
                            Text(text = stringResource(R.string.media_play_all))
                        }
                    }
                }
            }

            if (visible.isEmpty()) {
                item { NoResults() }
            } else {
                items(visible, key = { it.uri }) { entry ->
                    MediaListRow(
                        entry = entry,
                        kind = state.kind,
                        isCurrent = entry.uri == state.current?.uri,
                        isFavorite = entry.uri in state.favorites,
                        loadArtwork = component::artwork,
                        loadMetadata = component::metadata,
                        onClick = { component.play(entry) },
                        onFavorite = { component.toggleFavorite(entry) },
                        onShare = { component.share(entry) },
                        onOpenWith = { component.openWithOtherApp(entry) },
                    )
                }
            }
        }
    }
}

/** 顶部筛选胶囊：全部 / 收藏 / 最近播放。 */
@Composable
private fun FilterChips(
    selected: MediaFilter,
    onSelect: (MediaFilter) -> Unit,
) {
    val filters = listOf(
        MediaFilter.ALL to stringResource(R.string.media_filter_all),
        MediaFilter.FAVORITES to stringResource(R.string.media_filter_favorites),
        MediaFilter.RECENT to stringResource(R.string.media_filter_recent),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters, key = { it.first.name }) { (filter, label) ->
            GlassFilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(text = label) },
            )
        }
    }
}

/** 没有读媒体权限时的提示：说明当前只能看最近记录，并给一个授权入口。 */
@Composable
private fun PermissionBanner(onGrant: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        style = GlassStyle.Thin,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.LineFolder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.media_permission_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onGrant) {
                Text(text = stringResource(R.string.media_permission_action))
            }
        }
    }
}

/**
 * 空态：一块圆角玻璃底 + 图标 + 标题/说明 + 主按钮。
 *
 * 图标用文件夹轮廓（原型是"文件夹里有个播放箭头"），在音乐库下换成音符。
 */
@Composable
fun EmptyMediaState(
    kind: MediaKind,
    isLoading: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassSurface(
            shape = RoundedCornerShape(36.dp),
            style = GlassStyle.Thin,
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (kind == MediaKind.VIDEO) {
                        Icons.Outlined.LineFolder
                    } else {
                        Icons.Outlined.LineMusicNote
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(
                if (kind == MediaKind.VIDEO) {
                    R.string.media_empty_videos_title
                } else {
                    R.string.media_empty_audio_title
                }
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(
                if (kind == MediaKind.VIDEO) {
                    R.string.media_empty_videos_subtitle
                } else {
                    R.string.media_empty_audio_subtitle
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAdd,
            enabled = !isLoading,
        ) {
            Text(text = stringResource(R.string.media_add))
        }
    }
}

/**
 * 列表行：缩略图 + 标题/副标题 + 溢出菜单。
 *
 * 整行可点（点开即播）；当前正在播的那一行标题前加一个小播放标记。
 */
@Composable
fun MediaListRow(
    entry: MediaEntry,
    kind: MediaKind,
    isCurrent: Boolean,
    isFavorite: Boolean,
    loadArtwork: suspend (MediaEntry, Int) -> android.graphics.Bitmap?,
    loadMetadata: suspend (MediaEntry) -> MediaMetadata,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onOpenWith: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 媒体库条目已经带 title/时长时就不必再跑 MediaMetadataRetriever
    val needsMetadata = entry.durationMs <= 0L || entry.mediaTitle == null
    val metadata = rememberMediaMetadata(entry, enabled = needsMetadata, load = loadMetadata)
    val typeLabel = stringResource(
        if (kind == MediaKind.VIDEO) R.string.media_video_type else R.string.media_audio_type
    )
    val duration = metadata.durationMs.takeIf { it > 0L } ?: entry.durationMs
    val subtitle = buildString {
        append(metadata.displayArtist(entry.subtitleHint ?: typeLabel))
        if (duration > 0L) {
            append(" · ")
            append(formatMediaDuration(duration))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassRegular(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            MediaArtwork(
                entry = entry,
                kind = kind,
                maxSize = 240,
                loadArtwork = loadArtwork,
                modifier = Modifier
                    .width(if (kind == MediaKind.VIDEO) 96.dp else 56.dp)
                    .aspectRatio(if (kind == MediaKind.VIDEO) 1.5f else 1f),
                shape = RoundedCornerShape(10.dp),
                iconSize = 22.dp,
            )
            DurationBadge(duration)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Outlined.LinePlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = metadata.title?.takeIf { it.isNotBlank() } ?: entry.bestTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        RowOverflowMenu(
            isFavorite = isFavorite,
            onFavorite = onFavorite,
            onShare = onShare,
            onOpenWith = onOpenWith,
        )
    }
}

@Composable
private fun RowOverflowMenu(
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onOpenWith: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.LineMore,
                contentDescription = stringResource(R.string.media_more),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (isFavorite) R.string.media_unfavorite else R.string.media_favorite
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LineFavorite,
                        contentDescription = null,
                    )
                },
                trailingIcon = if (isFavorite) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.LineFavorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                } else {
                    null
                },
                onClick = {
                    expanded = false
                    onFavorite()
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.media_share)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LineShare,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onShare()
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.media_open_with)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LineOpenWith,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onOpenWith()
                },
            )
        }
    }
}
