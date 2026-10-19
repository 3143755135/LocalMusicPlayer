package com.localmusic.player.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.localmusic.player.data.model.Song
import com.localmusic.player.data.model.SongSort
import com.localmusic.player.ui.LocalAppContainer
import com.localmusic.player.ui.components.AddToPlaylistDialog
import com.localmusic.player.ui.components.CreatePlaylistDialog
import com.localmusic.player.ui.components.EmptyState
import com.localmusic.player.ui.components.SongActionsSheet
import com.localmusic.player.ui.components.SongRow
import com.localmusic.player.ui.components.commonSongActions
import com.localmusic.player.ui.viewmodel.LibraryUiState
import com.localmusic.player.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    uiState: LibraryUiState,
    store: StoreViewModel,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onSortChange: (SongSort) -> Unit,
) {
    val container = LocalAppContainer.current
    val player = container.playerConnection
    val playerState by player.state.collectAsStateWithLifecycle()
    val playlists by store.playlists.collectAsStateWithLifecycle()

    var sortMenuOpen by remember { mutableStateOf(false) }
    var actionsFor by remember { mutableStateOf<Song?>(null) }
    var addToPlaylistFor by remember { mutableStateOf<Song?>(null) }
    var newPlaylistFor by remember { mutableStateOf<Song?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(text = "歌曲", style = MaterialTheme.typography.titleLarge)
                    if (uiState.totalSongs > 0) {
                        Text(
                            text = "${uiState.totalSongs} 首",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Rounded.Search, contentDescription = "搜索")
                }
                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "排序")
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                    ) {
                        SongSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                trailingIcon = {
                                    if (option == uiState.sort) {
                                        Icon(Icons.Rounded.Check, contentDescription = null)
                                    }
                                },
                                onClick = {
                                    onSortChange(option)
                                    sortMenuOpen = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "重新扫描")
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        if (uiState.songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { player.play(uiState.songs, 0, false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("播放全部")
                }
                FilledTonalButton(
                    onClick = { player.play(uiState.songs, 0, true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("随机播放")
                }
            }
        }

        if (uiState.songs.isEmpty()) {
            EmptyState(
                title = if (uiState.isScanning) "正在扫描本地音乐…" else "还没有本地音乐",
                message = "把音频文件放进手机存储后点击下面的按钮重新扫描。" +
                    "支持 mp3、flac、m4a、wav、ogg 等系统能解码的格式。",
                actionLabel = "重新扫描",
                onAction = onRefresh,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(uiState.songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        isCurrent = song.id == playerState.currentSong?.id,
                        onClick = { player.play(uiState.songs, index, false) },
                        trailing = {
                            IconButton(onClick = { actionsFor = song }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "更多操作")
                            }
                        },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    actionsFor?.let { song ->
        SongActionsSheet(
            song = song,
            actions = commonSongActions(
                song = song,
                connection = player,
                store = store,
                onAddToPlaylist = { addToPlaylistFor = it },
            ),
            onDismiss = { actionsFor = null },
        )
    }

    addToPlaylistFor?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { addToPlaylistFor = null },
            onCreateNew = {
                addToPlaylistFor = null
                newPlaylistFor = song
            },
            onPick = { playlist ->
                store.addToPlaylist(playlist.id, song.id)
                addToPlaylistFor = null
            },
        )
    }

    newPlaylistFor?.let { song ->
        CreatePlaylistDialog(
            onDismiss = { newPlaylistFor = null },
            onConfirm = { name ->
                val playlist = store.createPlaylist(name)
                store.addToPlaylist(playlist.id, song.id)
                newPlaylistFor = null
            },
        )
    }
}
