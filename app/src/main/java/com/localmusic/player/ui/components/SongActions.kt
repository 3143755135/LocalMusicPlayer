package com.localmusic.player.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import com.localmusic.player.data.model.Song
import com.localmusic.player.playback.PlayerConnection
import com.localmusic.player.ui.viewmodel.StoreViewModel

/** 歌曲操作面板里最常用的一组操作。 */
fun commonSongActions(
    song: Song,
    connection: PlayerConnection,
    store: StoreViewModel,
    onAddToPlaylist: (Song) -> Unit,
): List<SheetAction> {
    val favorite = store.isFavorite(song.id)
    return buildList {
        add(
            SheetAction("下一首播放", Icons.AutoMirrored.Rounded.PlaylistPlay) {
                connection.playNext(song)
            },
        )
        add(
            SheetAction("添加到播放队列", Icons.AutoMirrored.Rounded.QueueMusic) {
                connection.addToQueue(song)
            },
        )
        add(SheetAction("添加到歌单", Icons.Rounded.Add) { onAddToPlaylist(song) })
        add(
            SheetAction(
                label = if (favorite) "取消收藏" else "收藏",
                icon = if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            ) { store.toggleFavorite(song.id) },
        )
    }
}
