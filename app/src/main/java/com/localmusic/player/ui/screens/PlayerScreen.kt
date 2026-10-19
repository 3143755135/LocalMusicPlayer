package com.localmusic.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.localmusic.player.ui.LocalAppContainer
import com.localmusic.player.ui.components.EmptyState
import com.localmusic.player.ui.components.SongArtwork
import com.localmusic.player.ui.util.formatDuration
import com.localmusic.player.ui.viewmodel.StoreViewModel

private val SPEED_OPTIONS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
private val SLEEP_OPTIONS = listOf(15, 30, 45, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    store: StoreViewModel,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val player = container.playerConnection
    val state by player.state.collectAsStateWithLifecycle()
    val playback by player.progress.collectAsStateWithLifecycle()

    var speedMenuOpen by remember { mutableStateOf(false) }
    var sleepDialogOpen by remember { mutableStateOf(false) }
    var queueOpen by remember { mutableStateOf(false) }
    var dragPositionSec by remember { mutableStateOf<Float?>(null) }

    val song = state.currentSong
    if (song == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("正在播放") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
            )
            EmptyState(
                title = "还没有正在播放的歌曲",
                message = "去“歌曲”里挑一首开始播放吧。",
                actionLabel = "返回",
                onAction = onBack,
            )
        }
        return
    }

    val durationSec = (playback.durationMs / 1000f).coerceAtLeast(1f)
    val sliderValue = (dragPositionSec ?: (playback.positionMs / 1000f)).coerceIn(0f, durationSec)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "正在播放",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "收起")
                }
            },
            actions = {
                Box {
                    TextButton(onClick = { speedMenuOpen = true }) {
                        Text("${trimSpeed(state.playbackSpeed)}x")
                    }
                    DropdownMenu(
                        expanded = speedMenuOpen,
                        onDismissRequest = { speedMenuOpen = false },
                    ) {
                        SPEED_OPTIONS.forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${trimSpeed(speed)}x") },
                                onClick = {
                                    player.setPlaybackSpeed(speed)
                                    speedMenuOpen = false
                                },
                            )
                        }
                    }
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SongArtwork(
                    song = song,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    cornerRadius = 20.dp,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${song.artist} · ${song.album}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(20.dp))

            Slider(
                value = sliderValue,
                onValueChange = { dragPositionSec = it },
                onValueChangeFinished = {
                    dragPositionSec?.let { player.seekToPosition((it * 1000f).toLong()) }
                    dragPositionSec = null
                },
                valueRange = 0f..durationSec,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatDuration(playback.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatDuration(playback.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { player.setShuffleEnabled(!state.shuffleEnabled) }) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "随机播放",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { player.previous() }) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "上一首",
                    modifier = Modifier.size(36.dp),
                )
            }
            FilledIconButton(
                onClick = { player.togglePlayPause() },
                modifier = Modifier.size(68.dp),
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Rounded.Pause
                    else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(34.dp),
                )
            }
            IconButton(onClick = { player.next() }) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "下一首",
                    modifier = Modifier.size(36.dp),
                )
            }
            IconButton(onClick = { player.cycleRepeatMode() }) {
                Icon(
                    imageVector = if (state.repeatMode == Player.REPEAT_MODE_ONE) {
                        Icons.Rounded.RepeatOne
                    } else {
                        Icons.Rounded.Repeat
                    },
                    contentDescription = "循环模式",
                    tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val favorite = store.isFavorite(song.id)
            IconButton(onClick = { store.toggleFavorite(song.id) }) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Favorite
                    else Icons.Filled.FavoriteBorder,
                    contentDescription = if (favorite) "取消收藏" else "收藏",
                    tint = if (favorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { sleepDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = "睡眠定时",
                    tint = if (state.sleepTimerEndsAt != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { queueOpen = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = "播放队列",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }

    if (sleepDialogOpen) {
        AlertDialog(
            onDismissRequest = { sleepDialogOpen = false },
            title = { Text("睡眠定时") },
            text = {
                Column {
                    Text(
                        text = if (state.sleepTimerEndsAt != null) {
                            "定时结束后会自动暂停播放。"
                        } else {
                            "选择一个时长，到点后自动暂停。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    SLEEP_OPTIONS.forEach { minutes ->
                        Text(
                            text = "$minutes 分钟",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    player.setSleepTimer(minutes)
                                    sleepDialogOpen = false
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                    if (state.sleepTimerEndsAt != null) {
                        HorizontalDivider()
                        Text(
                            text = "关闭定时",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    player.setSleepTimer(null)
                                    sleepDialogOpen = false
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sleepDialogOpen = false }) { Text("取消") }
            },
        )
    }

    if (queueOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { queueOpen = false },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "播放队列",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${state.queue.size} 首",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider()
                LazyColumn {
                    itemsIndexed(state.queue) { index, queueSong ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { player.playQueueIndex(index) }
                                .background(
                                    if (index == state.currentIndex) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                )
                                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SongArtwork(
                                song = queueSong,
                                modifier = Modifier.size(40.dp),
                                cornerRadius = 6.dp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = queueSong.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (index == state.currentIndex) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = queueSong.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { player.moveQueueItem(index, index - 1) }) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = "上移")
                            }
                            IconButton(onClick = { player.moveQueueItem(index, index + 1) }) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = "下移")
                            }
                            IconButton(onClick = { player.removeFromQueue(index) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "移出队列")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun trimSpeed(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
