package com.localmusic.player.playback

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.localmusic.player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val ready: Boolean = false,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playbackSpeed: Float = 1f,
    val sleepTimerEndsAt: Long? = null,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)

data class PlaybackProgress(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
)

/**
 * 界面与 PlaybackService 之间的唯一桥梁。
 * 只暴露状态流和少量指令，界面不直接接触 ExoPlayer。
 */
class PlayerConnection(
    context: Context,
    private val onSongStarted: (Song) -> Unit = {},
) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val _progress = MutableStateFlow(PlaybackProgress())
    val progress: StateFlow<PlaybackProgress> = _progress.asStateFlow()

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState()
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        val token = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java),
        )
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { connected ->
                        controller = connected
                        connected.addListener(listener)
                        syncState()
                        startTicker()
                    }
                    .onFailure { Log.e(TAG, "连接播放服务失败", it) }
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }

    fun release() {
        tickerJob?.cancel()
        tickerJob = null
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        controller?.removeListener(listener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }

    // ------------------------------------------------------------ 播放控制

    /** 用一批歌曲替换当前播放队列并开始播放。 */
    fun play(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val c = controller ?: return
        if (songs.isEmpty()) return
        val index = startIndex.coerceIn(0, songs.lastIndex)
        c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
        c.shuffleModeEnabled = shuffle
        c.prepare()
        c.play()
    }

    fun playSingle(song: Song) = play(listOf(song), 0)

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun pause() {
        controller?.pause()
    }

    fun seekToPosition(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun next() {
        val c = controller ?: return
        if (c.hasNextMediaItem()) c.seekToNextMediaItem()
    }

    fun previous() {
        val c = controller ?: return
        // 播放超过 3 秒时，先回到本曲开头
        if (c.currentPosition > 3_000L) {
            c.seekTo(0L)
            return
        }
        if (c.hasPreviousMediaItem()) c.seekToPreviousMediaItem() else c.seekTo(0L)
    }

    fun playQueueIndex(index: Int) {
        val c = controller ?: return
        if (index !in 0 until c.mediaItemCount) return
        c.seekTo(index, 0L)
        c.play()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    fun cycleRepeatMode() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    // -------------------------------------------------------------- 队列

    fun playNext(song: Song) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            playSingle(song)
            return
        }
        val target = (c.currentMediaItemIndex + 1).coerceIn(0, c.mediaItemCount)
        c.addMediaItem(target, song.toMediaItem())
    }

    fun addToQueue(song: Song) {
        val c = controller ?: return
        if (c.mediaItemCount == 0) {
            playSingle(song)
            return
        }
        c.addMediaItem(song.toMediaItem())
    }

    fun removeFromQueue(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val c = controller ?: return
        if (fromIndex !in 0 until c.mediaItemCount) return
        if (toIndex !in 0 until c.mediaItemCount) return
        c.moveMediaItem(fromIndex, toIndex)
    }

    // ---------------------------------------------------------- 睡眠定时

    /** minutes 传 null 表示取消定时。 */
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minutes == null || minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerEndsAt = null)
            return
        }
        val endsAt = System.currentTimeMillis() + minutes * 60_000L
        _state.value = _state.value.copy(sleepTimerEndsAt = endsAt)
        sleepTimerJob = scope.launch {
            delay(minutes * 60_000L)
            controller?.pause()
            _state.value = _state.value.copy(sleepTimerEndsAt = null)
        }
    }

    // ------------------------------------------------------------ 内部

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (isActive) {
                val c = controller
                if (c != null) {
                    val rawDuration = c.duration
                    val duration = if (rawDuration > 0L) rawDuration
                    else _state.value.currentSong?.durationMs ?: 0L
                    _progress.value = PlaybackProgress(
                        positionMs = c.currentPosition.coerceAtLeast(0L),
                        durationMs = duration,
                        bufferedMs = c.bufferedPosition.coerceAtLeast(0L),
                    )
                }
                delay(if (c?.isPlaying == true) 500L else 1_000L)
            }
        }
    }

    private fun syncState() {
        val c = controller
        if (c == null) {
            _state.value = PlayerUiState()
            return
        }
        val queue = (0 until c.mediaItemCount).map { c.getMediaItemAt(it).toSong() }
        val currentIndex = c.currentMediaItemIndex
        val current = queue.getOrNull(currentIndex)

        if (current != null && current.id != _state.value.currentSong?.id) {
            onSongStarted(current)
        }

        _state.value = _state.value.copy(
            ready = true,
            currentSong = current,
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            queue = queue,
            currentIndex = if (currentIndex in queue.indices) currentIndex else -1,
            shuffleEnabled = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
            playbackSpeed = c.playbackParameters.speed,
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem(),
        )
    }

    private companion object {
        const val TAG = "PlayerConnection"
    }
}
