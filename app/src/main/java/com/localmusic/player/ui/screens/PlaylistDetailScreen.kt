package com.localmusic.player.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.localmusic.player.data.model.Song
import com.localmusic.player.ui.components.ConfirmDeleteDialog
import com.localmusic.player.ui.components.RenamePlaylistDialog
import com.localmusic.player.ui.components.SheetAction
import com.localmusic.player.ui.viewmodel.StoreViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    librarySongs: List<Song>,
    store: StoreViewModel,
    onBack: () -> Unit,
) {
    val playlists by store.playlists.collectAsStateWithLifecycle()
    val playlist = playlists.firstOrNull { it.id == playlistId }

    val songs = remember(playlist, librarySongs) {
        val indexed = librarySongs.associateBy { it.id }
        playlist?.songIds?.mapNotNull { indexed[it] }.orEmpty()
    }

    var menuOpen by remember { mutableStateOf(false) }
    var renameDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }

    if (playlist == null) {
        // 歌单已被删除，直接退出
        LaunchedEffect(Unit) { onBack() }
        return
    }

    SongCollectionScreen(
        title = playlist.name,
        subtitle = "${songs.size} 首",
        songs = songs,
        store = store,
        onBack = onBack,
        emptyMessage = "长按歌曲列表里的 ⋮ 可以把歌曲加进这个歌单。",
        topBarActions = {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "更多")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("重命名") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        renameDialog = true
                    },
                )
                DropdownMenuItem(
                    text = { Text("删除歌单") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                    onClick = {
                        menuOpen = false
                        deleteDialog = true
                    },
                )
            }
        },
        additionalActions = { song, index ->
            buildList {
                add(
                    SheetAction("上移", Icons.Rounded.ArrowUpward) {
                        store.moveInPlaylist(playlistId, index, index - 1)
                    },
                )
                add(
                    SheetAction("下移", Icons.Rounded.ArrowDownward) {
                        store.moveInPlaylist(playlistId, index, index + 1)
                    },
                )
                add(
                    SheetAction("从歌单中移除", Icons.Rounded.PlaylistRemove) {
                        store.removeFromPlaylist(playlistId, song.id)
                    },
                )
            }
        },
    )

    if (renameDialog) {
        RenamePlaylistDialog(
            currentName = playlist.name,
            onDismiss = { renameDialog = false },
            onConfirm = { newName ->
                store.renamePlaylist(playlistId, newName)
                renameDialog = false
            },
        )
    }

    if (deleteDialog) {
        ConfirmDeleteDialog(
            title = "删除歌单",
            message = "确定要删除「${playlist.name}」吗？歌单里的歌曲文件不会被删除。",
            onDismiss = { deleteDialog = false },
            onConfirm = {
                store.deletePlaylist(playlistId)
                deleteDialog = false
                onBack()
            },
        )
    }
}
