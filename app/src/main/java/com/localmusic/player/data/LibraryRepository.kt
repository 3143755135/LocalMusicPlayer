package com.localmusic.player.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.localmusic.player.data.model.Album
import com.localmusic.player.data.model.Artist
import com.localmusic.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 通过 MediaStore 读取设备上的本地音频。不涉及任何网络请求。
 */
class LibraryRepository(private val context: Context) {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    suspend fun refresh() {
        _isScanning.value = true
        try {
            _songs.value = withContext(Dispatchers.IO) { queryMediaStore() }
        } finally {
            _isScanning.value = false
        }
    }

    private fun queryMediaStore(): List<Song> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val songs = ArrayList<Song>()

        val cursor = runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder,
            )
        }.getOrNull() ?: return songs

        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val duration = c.getLong(durationIndex)
                if (id <= 0L || duration <= 0L) continue

                val artist = c.getString(artistIndex).orEmpty().trim()
                    .takeUnless { it.isEmpty() || it == "<unknown>" }
                    ?: UNKNOWN_ARTIST
                val album = c.getString(albumIndex).orEmpty().trim()
                    .takeUnless { it.isEmpty() || it == "<unknown>" }
                    ?: UNKNOWN_ALBUM

                songs += Song(
                    id = id,
                    uri = ContentUris
                        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        .toString(),
                    title = c.getString(titleIndex).orEmpty().trim().ifBlank { "未知曲目" },
                    artist = artist,
                    album = album,
                    albumId = c.getLong(albumIdIndex),
                    durationMs = duration,
                    size = c.getLong(sizeIndex),
                    dateAdded = c.getLong(dateAddedIndex),
                    // MediaStore 的 TRACK 高位可能是 CD 号，这里只取曲目号
                    trackNumber = c.getInt(trackIndex) % 1000,
                )
            }
        }
        return songs
    }

    companion object {
        const val UNKNOWN_ARTIST = "未知艺术家"
        const val UNKNOWN_ALBUM = "未知专辑"

        fun albumsOf(songs: List<Song>): List<Album> =
            songs.groupBy { it.albumId }
                .map { (albumId, items) ->
                    Album(
                        id = albumId,
                        name = items.first().album,
                        artist = dominantArtist(items),
                        songs = items.sortedBy { it.trackNumber },
                    )
                }
                .sortedBy { it.name.lowercase() }

        fun artistsOf(songs: List<Song>): List<Artist> =
            songs.groupBy { it.artist }
                .map { (name, items) -> Artist(name = name, songs = items) }
                .sortedBy { it.name.lowercase() }

        private fun dominantArtist(songs: List<Song>): String =
            songs.groupingBy { it.artist }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?: UNKNOWN_ARTIST
    }
}
