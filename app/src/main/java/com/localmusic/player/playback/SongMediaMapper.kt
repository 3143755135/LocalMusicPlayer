package com.localmusic.player.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.localmusic.player.data.model.Song

private const val EXTRA_ID = "extra_song_id"
private const val EXTRA_URI = "extra_song_uri"
private const val EXTRA_TITLE = "extra_song_title"
private const val EXTRA_ARTIST = "extra_song_artist"
private const val EXTRA_ALBUM = "extra_song_album"
private const val EXTRA_ALBUM_ID = "extra_song_album_id"
private const val EXTRA_DURATION = "extra_song_duration"
private const val EXTRA_SIZE = "extra_song_size"
private const val EXTRA_DATE_ADDED = "extra_song_date_added"
private const val EXTRA_TRACK = "extra_song_track"

/**
 * 歌曲模型的完整字段挂在 MediaMetadata.extras 上，
 * 这样播放队列经过 MediaSession 传到界面（甚至服务被系统重启）后依然能还原成 Song。
 */
fun Song.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putLong(EXTRA_ID, id)
        putString(EXTRA_URI, uri)
        putString(EXTRA_TITLE, title)
        putString(EXTRA_ARTIST, artist)
        putString(EXTRA_ALBUM, album)
        putLong(EXTRA_ALBUM_ID, albumId)
        putLong(EXTRA_DURATION, durationMs)
        putLong(EXTRA_SIZE, size)
        putLong(EXTRA_DATE_ADDED, dateAdded)
        putInt(EXTRA_TRACK, trackNumber)
    }
    return MediaItem.Builder()
        .setMediaId(uri)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(Uri.parse(uri))
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .setExtras(extras)
                .build(),
        )
        .build()
}

fun MediaItem.toSong(): Song {
    val extras = mediaMetadata.extras
    val extraId = extras?.getLong(EXTRA_ID) ?: 0L
    return Song(
        id = if (extraId != 0L) extraId else mediaId.hashCode().toLong(),
        uri = extras?.getString(EXTRA_URI)
            ?: localConfiguration?.uri?.toString().orEmpty(),
        title = extras?.getString(EXTRA_TITLE) ?: mediaMetadata.title?.toString().orEmpty(),
        artist = extras?.getString(EXTRA_ARTIST) ?: mediaMetadata.artist?.toString().orEmpty(),
        album = extras?.getString(EXTRA_ALBUM) ?: mediaMetadata.albumTitle?.toString().orEmpty(),
        albumId = extras?.getLong(EXTRA_ALBUM_ID) ?: 0L,
        durationMs = extras?.getLong(EXTRA_DURATION) ?: 0L,
        size = extras?.getLong(EXTRA_SIZE) ?: 0L,
        dateAdded = extras?.getLong(EXTRA_DATE_ADDED) ?: 0L,
        trackNumber = extras?.getInt(EXTRA_TRACK) ?: 0,
    )
}
