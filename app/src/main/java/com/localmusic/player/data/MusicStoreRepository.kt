package com.localmusic.player.data

import android.content.Context
import com.localmusic.player.data.model.LibrarySnapshot
import com.localmusic.player.data.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * 歌单 / 收藏 / 最近播放的本地存储，序列化成 filesDir 下的一个 json 文件。
 */
class MusicStoreRepository(context: Context) {

    private val file = File(context.filesDir, "music_store.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var persistJob: Job? = null

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _favoriteIds = MutableStateFlow<List<Long>>(emptyList())
    val favoriteIds: StateFlow<List<Long>> = _favoriteIds.asStateFlow()

    private val _recentIds = MutableStateFlow<List<Long>>(emptyList())
    val recentIds: StateFlow<List<Long>> = _recentIds.asStateFlow()

    init {
        load()
    }

    // ---------------------------------------------------------------- 歌单

    fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "新建歌单" },
            createdAt = System.currentTimeMillis(),
        )
        _playlists.value = _playlists.value + playlist
        schedulePersist()
        return playlist
    }

    fun renamePlaylist(playlistId: String, name: String) {
        val newName = name.trim()
        if (newName.isEmpty()) return
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId) it.copy(name = newName) else it
        }
        schedulePersist()
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filterNot { it.id == playlistId }
        schedulePersist()
    }

    fun addSongsToPlaylist(playlistId: String, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id != playlistId) {
                playlist
            } else {
                val appended = songIds.filterNot { playlist.songIds.contains(it) }
                playlist.copy(songIds = playlist.songIds + appended)
            }
        }
        schedulePersist()
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id != playlistId) playlist
            else playlist.copy(songIds = playlist.songIds.filterNot { it == songId })
        }
        schedulePersist()
    }

    fun moveSongInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id != playlistId) return@map playlist
            val ids = playlist.songIds.toMutableList()
            if (fromIndex !in ids.indices) return@map playlist
            val target = toIndex.coerceIn(0, ids.lastIndex)
            val moved = ids.removeAt(fromIndex)
            ids.add(target, moved)
            playlist.copy(songIds = ids)
        }
        schedulePersist()
    }

    // ---------------------------------------------------------------- 收藏

    fun isFavorite(songId: Long): Boolean = _favoriteIds.value.contains(songId)

    /** 返回切换后的状态：true 表示已收藏。 */
    fun toggleFavorite(songId: Long): Boolean {
        val currentlyFavorite = _favoriteIds.value.contains(songId)
        _favoriteIds.value = if (currentlyFavorite) {
            _favoriteIds.value.filterNot { it == songId }
        } else {
            _favoriteIds.value + songId
        }
        schedulePersist()
        return !currentlyFavorite
    }

    // ------------------------------------------------------------ 最近播放

    fun recordPlayed(songId: Long) {
        _recentIds.value = (listOf(songId) + _recentIds.value.filterNot { it == songId })
            .take(MAX_RECENT)
        schedulePersist()
    }

    // ---------------------------------------------------------------- 存储

    private fun load() {
        if (!file.exists()) return
        runCatching {
            json.decodeFromString(LibrarySnapshot.serializer(), file.readText())
        }.onSuccess { snapshot ->
            _playlists.value = snapshot.playlists
            _favoriteIds.value = snapshot.favorites
            _recentIds.value = snapshot.recentIds
        }
    }

    /** 合并短时间内的高频写入，避免每点一次收藏都落盘。 */
    private fun schedulePersist() {
        persistJob?.cancel()
        persistJob = scope.launch {
            delay(200)
            val snapshot = LibrarySnapshot(
                playlists = _playlists.value,
                favorites = _favoriteIds.value,
                recentIds = _recentIds.value,
            )
            runCatching {
                file.writeText(json.encodeToString(LibrarySnapshot.serializer(), snapshot))
            }
        }
    }

    private companion object {
        const val MAX_RECENT = 200
    }
}
