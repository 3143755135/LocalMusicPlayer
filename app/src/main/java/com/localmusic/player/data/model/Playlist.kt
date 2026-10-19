package com.localmusic.player.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long> = emptyList(),
    val createdAt: Long = 0L,
)

@Serializable
data class LibrarySnapshot(
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<Long> = emptyList(),
    val recentIds: List<Long> = emptyList(),
)
