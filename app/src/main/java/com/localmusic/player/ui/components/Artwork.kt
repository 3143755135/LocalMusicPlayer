package com.localmusic.player.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localmusic.player.data.model.Song
import com.localmusic.player.ui.LocalAppContainer

/**
 * 歌曲封面。显示尺寸完全交给 modifier（例如 Modifier.size(48.dp)
 * 或 Modifier.fillMaxWidth().aspectRatio(1f)）。
 *
 * 取图顺序：系统缩略图 -> 音频内嵌图片 -> 占位图标。
 */
@Composable
fun SongArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val container = LocalAppContainer.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val sidePx = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
        var bitmap by remember(song?.uri, sidePx) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(song?.uri, sidePx) {
            bitmap = song?.let { container.artworkLoader.load(it.uri, sidePx) }
        }

        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(0.42f),
            )
        }
    }
}
