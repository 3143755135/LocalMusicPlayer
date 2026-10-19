package com.localmusic.player.di

import android.content.Context
import com.localmusic.player.data.ArtworkLoader
import com.localmusic.player.data.LibraryRepository
import com.localmusic.player.data.MusicStoreRepository
import com.localmusic.player.playback.PlayerConnection

/**
 * 简单的手写依赖容器：整个 App 共用的单例都在这里创建。
 * 生命周期跟随 Application，所以播放状态、曲库、歌单不会随页面重建而丢失。
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val libraryRepository = LibraryRepository(appContext)
    val musicStore = MusicStoreRepository(appContext)
    val artworkLoader = ArtworkLoader(appContext)
    val playerConnection = PlayerConnection(appContext) { song ->
        musicStore.recordPlayed(song.id)
    }
}
