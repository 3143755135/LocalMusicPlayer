package com.localmusic.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.localmusic.player.ui.LocalAppContainer
import com.localmusic.player.ui.components.EmptyState
import com.localmusic.player.ui.components.SongArtwork
import com.localmusic.player.ui.components.SongRow
import com.localmusic.player.ui.viewmodel.LibraryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    library: LibraryUiState,
    onBack: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val player = container.playerConnection
    val playerState by player.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    val keyword = query.trim().lowercase()
    val songHits = remember(keyword, library.songs) {
        if (keyword.isEmpty()) emptyList() else library.songs
            .filter {
                it.title.lowercase().contains(keyword) ||
                    it.artist.lowercase().contains(keyword) ||
                    it.album.lowercase().contains(keyword)
            }
            .take(60)
    }
    val albumHits = remember(keyword, library.albums) {
        if (keyword.isEmpty()) emptyList() else library.albums
            .filter { it.name.lowercase().contains(keyword) || it.artist.lowercase().contains(keyword) }
            .take(12)
    }
    val artistHits = remember(keyword, library.artists) {
        if (keyword.isEmpty()) emptyList() else library.artists
            .filter { it.name.lowercase().contains(keyword) }
            .take(12)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "搜索", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("搜索歌曲、专辑、艺术家") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = "清空")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (keyword.isEmpty()) {
            EmptyState(title = "输入关键词开始搜索", message = "可以搜索本机音乐的标题、艺术家或专辑。")
        } else if (songHits.isEmpty() && albumHits.isEmpty() && artistHits.isEmpty()) {
            EmptyState(title = "没有匹配的结果", message = "换个关键词试试。")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (albumHits.isNotEmpty()) {
                    item { SectionTitle("专辑") }
                    itemsIndexed(albumHits, key = { _, album -> "album-${album.id}" }) { _, album ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAlbum(album.id) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SongArtwork(
                                song = album.songs.firstOrNull(),
                                modifier = Modifier.size(48.dp),
                                cornerRadius = 8.dp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = album.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${album.artist} · ${album.songs.size} 首",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                if (artistHits.isNotEmpty()) {
                    item { SectionTitle("艺术家") }
                    itemsIndexed(artistHits, key = { _, artist -> "artist-${artist.name}" }) { _, artist ->
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

                if (songHits.isNotEmpty()) {
                    item { SectionTitle("歌曲") }
                    itemsIndexed(songHits, key = { _, song -> "song-${song.id}" }) { index, song ->
                        SongRow(
                            song = song,
                            isCurrent = song.id == playerState.currentSong?.id,
                            onClick = { player.play(songHits, index, false) },
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
