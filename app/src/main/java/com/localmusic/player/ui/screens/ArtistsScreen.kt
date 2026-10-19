package com.localmusic.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localmusic.player.data.model.Artist
import com.localmusic.player.ui.components.EmptyState
import com.localmusic.player.ui.components.SongArtwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    onOpenArtist: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "艺术家", style = MaterialTheme.typography.titleLarge) },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )
        if (artists.isEmpty()) {
            EmptyState(title = "还没有艺术家", message = "扫描到本地音乐后，艺术家会显示在这里。")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(artists, key = { it.name }) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenArtist(artist.name) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SongArtwork(
                            song = artist.songs.firstOrNull(),
                            modifier = Modifier.size(48.dp),
                            cornerRadius = 24.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${artist.songs.size} 首",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
