package com.localmusic.player.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.localmusic.player.di.AppContainer

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer 没有提供")
}
