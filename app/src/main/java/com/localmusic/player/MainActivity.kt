package com.localmusic.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.localmusic.player.ui.AppRoot
import com.localmusic.player.ui.theme.LocalMusicTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MusicApp).container
        container.playerConnection.connect()

        setContent {
            LocalMusicTheme {
                AppRoot(container)
            }
        }
    }
}
