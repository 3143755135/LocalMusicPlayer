package com.localmusic.player

import android.app.Application
import com.localmusic.player.di.AppContainer

class MusicApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
