package com.localmusic.player.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.localmusic.player.data.model.Song
import com.localmusic.player.ui.LocalAppContainer
import com.localmusic.player.ui.components.AddToPlaylistDialog
import com.localmusic.player.ui.components.CreatePlaylistDialog
import com.localmusic.player.ui.components.EmptyState
import com.localmusic.player.ui.components.SheetAction
import com.localmusic.player.ui.components.SongActionsSheet
import com.localmusic.player.ui.components.SongRow
import com.localmusic.player.ui.components.commonSongActions
import com.localmusic.player.ui.viewmodel.StoreViewModel

/**
 * 通用的歌曲列表页：收藏、最近播放、专辑详情、艺术家详情、歌单详情都用它。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCollectionScreen(
    title: String,
    subtitle: String,
    songs: List<Song>,
    store: StoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "这里还没有歌曲。",
    topBarActions: @Composable RowScope.() -> Unit = {},
    additionalActions: (song: Song, index: Int) -> List<SheetAction> = { _, _ -> emptyList() },
) {
    val container = LocalAppContainer.current
    val player = container.playerConnection
    val playerState by player.state.collectAsStateWithLifecycle()
    val playlists by store.playlists.collectAsStateWithLifecycle()

    var actionsFor by remember { mutableStateOf<Pair<Song, Int>?>(null) }
    var addToPlaylistFor by remember { mutableStateOf<Song?>(null) }
    var newPlaylistFor by remember { mutableStateOf<Song?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
            },
            actions = topBarActions,
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { player.play(songs, 0, false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("播放全部")
                }
                FilledTonalButton(
                    onClick = { player.play(songs, 0, true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("随机播放")
                }
            }
        }

        if (songs.isEmpty()) {
            EmptyState(title = "空空如也", message = emptyMessage)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        isCurrent = song.id == playerState.currentSong?.id,
                        onClick = { player.play(songs, index, false) },
                        trailing = {
                            IconButton(onClick = { actionsFor = song to index }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "更多操作")
                            }
                        },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    actionsFor?.let { (song, index) ->
        SongActionsSheet(
            song = song,
            actions = commonSongActions(
                song = song,
                connection = player,
                store = store,
                onAddToPlaylist = { addToPlaylistFor = it },
            ) + additionalActions(song, index),
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
