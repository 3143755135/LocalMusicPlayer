package com.localmusic.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.localmusic.player.data.LibraryRepository
import com.localmusic.player.data.MusicStoreRepository
import com.localmusic.player.data.model.Playlist
import com.localmusic.player.data.model.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StoreViewModel(
    private val store: MusicStoreRepository,
    private val library: LibraryRepository,
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = store.playlists
    val favoriteIds: StateFlow<List<Long>> = store.favoriteIds

    val favorites: StateFlow<List<Song>> = combine(store.favoriteIds, library.songs) { ids, songs ->
        val indexed = songs.associateBy { it.id }
        ids.mapNotNull { indexed[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = combine(store.recentIds, library.songs) { ids, songs ->
        val indexed = songs.associateBy { it.id }
        ids.mapNotNull { indexed[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createPlaylist(name: String): Playlist = store.createPlaylist(name)

    fun renamePlaylist(playlistId: String, name: String) = store.renamePlaylist(playlistId, name)

    fun deletePlaylist(playlistId: String) = store.deletePlaylist(playlistId)

    fun addToPlaylist(playlistId: String, songId: Long) =
        store.addSongsToPlaylist(playlistId, listOf(songId))

    fun removeFromPlaylist(playlistId: String, songId: Long) =
        store.removeSongFromPlaylist(playlistId, songId)

    fun moveInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) =
        store.moveSongInPlaylist(playlistId, fromIndex, toIndex)

    fun isFavorite(songId: Long): Boolean = store.isFavorite(songId)

    fun toggleFavorite(songId: Long): Boolean = store.toggleFavorite(songId)

    companion object {
        fun factory(
            store: MusicStoreRepository,
            library: LibraryRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { StoreViewModel(store, library) }
        }
    }
}
