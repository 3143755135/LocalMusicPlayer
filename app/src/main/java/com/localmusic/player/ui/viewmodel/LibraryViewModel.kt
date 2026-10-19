package com.localmusic.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.localmusic.player.data.LibraryRepository
import com.localmusic.player.data.model.Album
import com.localmusic.player.data.model.Artist
import com.localmusic.player.data.model.Song
import com.localmusic.player.data.model.SongSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val totalSongs: Int = 0,
    val isScanning: Boolean = false,
    val sort: SongSort = SongSort.TITLE,
    val query: String = "",
)

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val sortOption = MutableStateFlow(SongSort.TITLE)
    private val keyword = MutableStateFlow("")

    val uiState: StateFlow<LibraryUiState> =
        combine(repository.songs, repository.isScanning, sortOption, keyword) { songs, scanning, sort, query ->
            val sorted = sortSongs(songs, sort)
            val filtered = if (query.isBlank()) sorted else sorted.filter { it.matches(query) }
            LibraryUiState(
                songs = filtered,
                albums = LibraryRepository.albumsOf(filtered),
                artists = LibraryRepository.artistsOf(filtered),
                totalSongs = songs.size,
                isScanning = scanning,
                sort = sort,
                query = query,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(),
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }

    fun setSort(sort: SongSort) {
        sortOption.value = sort
    }

    fun setQuery(query: String) {
        keyword.value = query
    }

    companion object {
        fun factory(repository: LibraryRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LibraryViewModel(repository) }
        }
    }
}

private fun sortSongs(songs: List<Song>, sort: SongSort): List<Song> = when (sort) {
    SongSort.TITLE -> songs.sortedBy { it.title.lowercase() }
    SongSort.ARTIST -> songs.sortedWith(
        compareBy({ it.artist.lowercase() }, { it.album.lowercase() }, { it.trackNumber }),
    )
    SongSort.ALBUM -> songs.sortedWith(
        compareBy({ it.album.lowercase() }, { it.trackNumber }, { it.title.lowercase() }),
    )
    SongSort.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
    SongSort.DURATION -> songs.sortedByDescending { it.durationMs }
}

private fun Song.matches(query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    return title.lowercase().contains(q) ||
        artist.lowercase().contains(q) ||
        album.lowercase().contains(q)
}
