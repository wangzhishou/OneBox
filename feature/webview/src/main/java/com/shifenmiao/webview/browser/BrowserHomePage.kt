package com.shifenmiao.webview.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil3.compose.SubcomposeAsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.resources.icons.Language

private data class PresetSite(val title: String, val url: String, val faviconUrl: String)

private val presetSites = listOf(
    PresetSite("百度", "https://www.baidu.com", "https://www.baidu.com".toFaviconUrl()),
    PresetSite("哔哩哔哩", "https://www.bilibili.com", "https://www.bilibili.com".toFaviconUrl()),
    PresetSite("知乎", "https://www.zhihu.com", "https://www.zhihu.com".toFaviconUrl()),
    PresetSite("微博", "https://www.weibo.com", "https://www.weibo.com".toFaviconUrl()),
    PresetSite("京东", "https://www.jd.com", "https://www.jd.com".toFaviconUrl()),
    PresetSite("淘宝", "https://www.taobao.com", "https://www.taobao.com".toFaviconUrl()),
    PresetSite("腾讯新闻", "https://news.qq.com", "https://news.qq.com".toFaviconUrl()),
    PresetSite("网易", "https://www.163.com", "https://www.163.com".toFaviconUrl()),
)

@Composable
fun BrowserHomePage(
    onSiteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            containerAlpha = 0.15f,
            borderWidth = 0.5.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.browser_popular_sites),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(presetSites, key = { it.url }) { site ->
                        PresetSiteItem(site = site, onClick = { onSiteClick(site.url) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetSiteItem(site: PresetSite, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        SubcomposeAsyncImage(
            model = site.faviconUrl,
            contentDescription = site.title,
            modifier = Modifier.size(40.dp),
            loading = { FallbackGlobeIcon() },
            error = { FallbackGlobeIcon() }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = site.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun FallbackGlobeIcon() {
    Icon(
        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
