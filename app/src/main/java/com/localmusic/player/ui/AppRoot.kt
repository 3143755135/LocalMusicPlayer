package com.localmusic.player.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.localmusic.player.di.AppContainer
import com.localmusic.player.ui.components.MiniPlayer
import com.localmusic.player.ui.screens.AlbumsScreen
import com.localmusic.player.ui.screens.ArtistsScreen
import com.localmusic.player.ui.screens.PlayerScreen
import com.localmusic.player.ui.screens.PlaylistDetailScreen
import com.localmusic.player.ui.screens.PlaylistsScreen
import com.localmusic.player.ui.screens.SearchScreen
import com.localmusic.player.ui.screens.SongCollectionScreen
import com.localmusic.player.ui.screens.SongsScreen
import com.localmusic.player.ui.viewmodel.LibraryViewModel
import com.localmusic.player.ui.viewmodel.StoreViewModel

object Routes {
    const val SONGS = "songs"
    const val ALBUMS = "albums"
    const val ARTISTS = "artists"
    const val PLAYLISTS = "playlists"
    const val FAVORITES = "favorites"
    const val RECENT = "recent"
    const val SEARCH = "search"
    const val PLAYER = "player"
    const val ALBUM_DETAIL = "album/{albumId}"
    const val ARTIST_DETAIL = "artist/{artistName}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"

    fun album(id: Long) = "album/$id"
    fun artist(name: String) = "artist/${Uri.encode(name)}"
    fun playlist(id: String) = "playlist/${Uri.encode(id)}"
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val TABS = listOf(
    TabItem(Routes.SONGS, "歌曲", Icons.Rounded.LibraryMusic),
    TabItem(Routes.ALBUMS, "专辑", Icons.Rounded.Album),
    TabItem(Routes.ARTISTS, "艺术家", Icons.Rounded.Person),
    TabItem(Routes.PLAYLISTS, "歌单", Icons.AutoMirrored.Rounded.PlaylistPlay),
)

private val TOP_LEVEL_ROUTES = TABS.map { it.route }.toSet()

@Composable
fun AppRoot(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        AudioPermissionGate {
            MusicAppContent(container)
        }
    }
}

@Composable
private fun MusicAppContent(container: AppContainer) {
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = remember { LibraryViewModel.factory(container.libraryRepository) },
    )
    val storeViewModel: StoreViewModel = viewModel(
        factory = remember {
            StoreViewModel.factory(container.musicStore, container.libraryRepository)
        },
    )

    val library by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val favorites by storeViewModel.favorites.collectAsStateWithLifecycle()
    val recents by storeViewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val playerState by container.playerConnection.state.collectAsStateWithLifecycle()
    val playback by container.playerConnection.progress.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBars = currentRoute != null && currentRoute in TOP_LEVEL_ROUTES

    Scaffold(
        bottomBar = {
            Column {
                if (showBars && playerState.currentSong != null) {
                    MiniPlayer(
                        state = playerState,
                        progress = playback,
                        onExpand = {
                            navController.navigate(Routes.PLAYER) { launchSingleTop = true }
                        },
                        onTogglePlay = { container.playerConnection.togglePlayPause() },
                        onNext = { container.playerConnection.next() },
                    )
                }
                if (showBars) {
                    NavigationBar {
                        TABS.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    if (currentRoute != tab.route) {
                                        navController.navigate(tab.route) {
                                            popUpTo(Routes.SONGS) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SONGS,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.SONGS) {
                SongsScreen(
                    uiState = library,
                    store = storeViewModel,
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onRefresh = { libraryViewModel.refresh() },
                    onSortChange = { libraryViewModel.setSort(it) },
                )
            }

            composable(Routes.ALBUMS) {
                AlbumsScreen(
                    albums = library.albums,
                    onOpenAlbum = { navController.navigate(Routes.album(it)) },
                )
            }

            composable(Routes.ARTISTS) {
                ArtistsScreen(
                    artists = library.artists,
                    onOpenArtist = { navController.navigate(Routes.artist(it)) },
                )
            }

            composable(Routes.PLAYLISTS) {
                PlaylistsScreen(
                    store = storeViewModel,
                    onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
                    onOpenRecent = { navController.navigate(Routes.RECENT) },
                    onOpenPlaylist = { navController.navigate(Routes.playlist(it)) },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    library = library,
                    onBack = { navController.popBackStack() },
                    onOpenAlbum = { navController.navigate(Routes.album(it)) },
                    onOpenArtist = { navController.navigate(Routes.artist(it)) },
                )
            }

            composable(Routes.PLAYER) {
                PlayerScreen(
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.FAVORITES) {
                SongCollectionScreen(
                    title = "我的收藏",
                    subtitle = "${favorites.size} 首",
                    songs = favorites,
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                    emptyMessage = "在歌曲的 ⋮ 菜单里点“收藏”，歌曲就会出现在这里。",
                )
            }

            composable(Routes.RECENT) {
                SongCollectionScreen(
                    title = "最近播放",
                    subtitle = "${recents.size} 首",
                    songs = recents,
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                    emptyMessage = "播放过的歌曲会按时间倒序排列在这里。",
                )
            }

            composable(
                route = Routes.ALBUM_DETAIL,
                arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
            ) { entry ->
                val albumId = entry.arguments?.getLong("albumId") ?: 0L
                val album = library.albums.firstOrNull { it.id == albumId }
                SongCollectionScreen(
                    title = album?.name ?: "专辑",
                    subtitle = album?.let { "${it.artist} · ${it.songs.size} 首" }.orEmpty(),
                    songs = album?.songs.orEmpty(),
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                    emptyMessage = "这张专辑里的歌曲似乎已经不在设备上了。",
                )
            }

            composable(
                route = Routes.ARTIST_DETAIL,
                arguments = listOf(navArgument("artistName") { type = NavType.StringType }),
            ) { entry ->
                val artistName = Uri.decode(entry.arguments?.getString("artistName").orEmpty())
                val artist = library.artists.firstOrNull { it.name == artistName }
                SongCollectionScreen(
                    title = artist?.name ?: artistName,
                    subtitle = artist?.let { "${it.songs.size} 首" }.orEmpty(),
                    songs = artist?.songs.orEmpty(),
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                    emptyMessage = "这位艺术家的歌曲似乎已经不在设备上了。",
                )
            }

            composable(
                route = Routes.PLAYLIST_DETAIL,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) { entry ->
                PlaylistDetailScreen(
                    playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                    librarySongs = library.songs,
                    store = storeViewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
