package com.localmusic.player.data.model

/** 一首本地歌曲。id 为 MediaStore 中的音频 id。 */
data class Song(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val size: Long,
    val dateAdded: Long,
    val trackNumber: Int,
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songs: List<Song>,
) {
    val durationMs: Long get() = songs.sumOf { it.durationMs }
}

data class Artist(
    val name: String,
    val songs: List<Song>,
)

enum class SongSort(val label: String) {
    TITLE("标题"),
    ARTIST("艺术家"),
    ALBUM("专辑"),
    DATE_ADDED("最近添加"),
    DURATION("时长"),
}
