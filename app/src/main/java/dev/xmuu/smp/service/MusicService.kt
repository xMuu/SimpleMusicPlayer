package dev.xmuu.smp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.*
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.tencent.mmkv.MMKV
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.activity.MainActivity
import dev.xmuu.smp.activity.PlayerActivity
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.MessageEvent
import dev.xmuu.smp.other.MessageType
import dev.xmuu.smp.other.PlayerMode
import dev.xmuu.smp.service.base.IMusicController
import dev.xmuu.smp.util.dp
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val CHANNEL_ID = "SMP_NOTIFICATION_CHANNEL"
const val FOREGROUND_SERVICE_ID = 1102

const val CODE_PREVIOUS = 1
const val CODE_CONTROL = 2
const val CODE_NEXT = 3

const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
        or PlaybackStateCompat.ACTION_PAUSE
        or PlaybackStateCompat.ACTION_PLAY_PAUSE
        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        or PlaybackStateCompat.ACTION_STOP
        or PlaybackStateCompat.ACTION_SEEK_TO)

open class MusicService : Service() {

    private val musicController by lazy { MusicController() }
    private var notificationManager: NotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionCallback: MediaSessionCompat.Callback? = null
    private var mediaController: MediaControllerCompat? = null

    private lateinit var audioManager: AudioManager
    private lateinit var audioAttributes: AudioAttributes
    private lateinit var audioFocusRequest: AudioFocusRequest

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return musicController
    }

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        super.onCreate()
        initEventBus()
        initNotificationChannel()
        initMediaSession()
        initAudioFocus()
        updateNotification(musicController.getCurrentSong())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getIntExtra("int_code", 0)) {
            CODE_PREVIOUS -> musicController.playPrev()
            CODE_CONTROL -> {
                if (musicController.isPlaying()) {
                    musicController.pause()
                } else {
                    musicController.play()
                }
            }
            CODE_NEXT -> musicController.playNext()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().register(this)
        mediaSession?.let {
            it.setCallback(null)
            it.release()
        }
        player?.release()
    }

    private fun initEventBus() {
        EventBus.getDefault().register(this)
    }

    private fun initAudioFocus() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> postLostAudioFocusEvent()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> postLostAudioFocusEvent()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> postLostAudioFocusEvent()
                    }
                }.build()
            audioManager.requestAudioFocus(audioFocusRequest)
        }
    }

    private fun postLostAudioFocusEvent() {
        musicController.pause()
        EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "Lost Audio Focus"))
    }

    private fun initMediaSession() {
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        var myNoisyAudioStreamReceiverTag = false
        val myNoisyAudioStreamReceiver = NoisyReceiver()
        mediaSessionCallback = object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                if (!myNoisyAudioStreamReceiverTag) {
                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                    myNoisyAudioStreamReceiverTag = true
                }
                mediaSession?.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            (App.musicController.value?.getProgress() ?: 0).toLong(),
                            1f
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                )
            }
        }
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(mediaSessionCallback, Handler(Looper.getMainLooper()))
            if (!isActive) {
                isActive = true
            }
        }
    }

    inner class NoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            postNoisyReceivedEvent()
        }
    }

    private fun postNoisyReceivedEvent() {
        musicController.pause()
        EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "Noisy Received"))
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SMP Notification"
            val descriptionText = "SMP 正在提供音乐服务。"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(song: Song, cover: Bitmap?) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_music_note)
            setLargeIcon(cover)
            setContentTitle(song.name ?: "")
            setContentText(song.getSubtitle())
            setContentIntent(getPendingIntentToOpenActivity())
            addAction(
                R.drawable.ic_baseline_skip_previous,
                "Previous",
                getPendingIntentToPrevSong()
            )
            addAction(getControlIcon(), "Control Player", getPendingIntentToControlPlayer())
            addAction(
                R.drawable.ic_baseline_skip_next,
                "Next Song",
                getPendingIntentToNextSong()
            )
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            setOngoing(true)
        }.build()

        mediaSession?.apply {
            setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.name ?: "Not Playing")
                    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                    putLong(
                        MediaMetadata.METADATA_KEY_DURATION,
                        (App.musicController.value?.getDuration() ?: 0).toLong()
                    )
                }.build()
            )
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        (App.musicController.value?.getProgress() ?: 0).toLong(),
                        1f
                    )
                    .setActions(MEDIA_SESSION_ACTIONS)
                    .build()
            )
            setCallback(mediaSessionCallback)
            isActive = true
        }
        if (!musicController.isPlaying()) {
            mediaSessionCallback?.onPause()
        }
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    private fun updateNotification(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = getSongCover(song)
            CoroutineScope(Dispatchers.Main).launch {
                showNotification(song, bitmap)
            }
        }
    }

    private suspend fun getSongCover(song: Song): Bitmap {
        return suspendCoroutine {
            val request = ImageRequest.Builder(this)
                .size(128.dp())
                .data(song.getLargeCoverUri())
                .error(R.drawable.default_cover)
                .target(
                    onStart = {
                    },
                    onSuccess = { result ->
                        it.resume(result.toBitmap())
                    },
                    onError = { _ ->
                        ContextCompat.getDrawable(this@MusicService, R.drawable.default_cover)
                            ?.let { it1 ->
                                it.resume(it1.toBitmap(128.dp(), 128.dp()))
                            }
                    }).build()
            Coil.imageLoader(this).enqueue(request)
        }
    }

    private fun getControlIcon(): Int {
        return if (musicController.isPlaying()) {
            R.drawable.ic_baseline_pause
        } else {
            R.drawable.ic_baseline_play_arrow
        }
    }

    private fun getPendingIntentToOpenActivity(): PendingIntent {
        val intentMain = Intent(this, MainActivity::class.java)
        val intentPlayer = Intent(this, PlayerActivity::class.java)
        val intents = arrayOf(intentMain, intentPlayer)
        return PendingIntent.getActivities(this, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getPendingIntentToPrevSong(): PendingIntent {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("int_code", CODE_PREVIOUS)
        return PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getPendingIntentToControlPlayer(): PendingIntent {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("int_code", CODE_CONTROL)
        return PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getPendingIntentToNextSong(): PendingIntent {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("int_code", CODE_NEXT)
        return PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        Log.d("MusicService", "MessageEvent Received: ${event.type}")
    }

    inner class MusicController : Binder(), IMusicController, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private var currentSong = Song()

        private var isPrepared = false

        private var playerMode = PlayerMode.LOOP

        override fun onPrepared(mp: MediaPlayer?) {
            isPrepared = true
            mediaController = mediaSession?.sessionToken?.let { it ->
                MediaControllerCompat(
                    this@MusicService,
                    it
                )
            }
            play()
            updateNotification(currentSong)
        }

        override fun onCompletion(mp: MediaPlayer?) {
            if (playerMode == PlayerMode.ONE) {
                setProgress(0)
                play()
            } else {
                playNext()
            }
        }

        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            Toast.makeText(applicationContext, "播放失败，可能是 VIP 歌曲", Toast.LENGTH_SHORT).show()
            return true
        }

        override fun play() {
            player?.start()
            mediaSessionCallback?.onPlay()
            EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "开始播放"))
            updateNotification(currentSong)
        }

        override fun play(song: Song) {
            currentSong = song
            updateNotification(currentSong)
            PlaylistQueue.to(song)
            if (player != null) {
                player?.reset()
                player?.release()
                player = null
            }
            player = MediaPlayer().apply {
                currentSong.getSongUri {
                    when (it) {
                        is Uri -> {
                            setDataSource(applicationContext, it)
                        }
                        is String -> {
                            setDataSource(it)
                        }
                    }
                }
                setOnPreparedListener(this@MusicController)
                setOnCompletionListener(this@MusicController)
                setOnErrorListener(this@MusicController)
                isPrepared = false
                prepareAsync()
            }
        }

        override fun pause() {
            player?.pause()
            EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "暂停播放"))
            updateNotification(currentSong)
        }

        override fun isPlaying(): Boolean {
            return player?.isPlaying ?: false
        }

        override fun playPrev() {
            PlaylistQueue.previous()?.let {
                play(it)
                EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "播放上一首"))
            }
        }

        override fun playNext() {
            PlaylistQueue.next()?.let {
                play(it)
                EventBus.getDefault().post(MessageEvent(MessageType.STATUS_CHANGED, "播放下一首"))
            }
        }

        override fun getDuration(): Int {
            return if (isPrepared) {
                player?.duration ?: 0
            } else {
                0
            }
        }

        override fun setProgress(newProgress: Int) {
            if (isPrepared) {
                player?.seekTo(newProgress)
                mediaSessionCallback?.onPlay()
            }
        }

        override fun getProgress(): Int {
            return if (isPrepared) {
                player?.currentPosition ?: 0
            } else {
                0
            }
        }

        override fun setCurrentSong(song: Song) {
            currentSong = song
        }

        override fun getCurrentSong(): Song {
            return currentSong
        }

        override fun setPlaylist(playlist: MutableList<Song>) {
            PlaylistQueue.setPlaylist(playlist)
            PlaylistQueue.reloadPlaylist(playerMode)
        }

        override fun getPlaylist(): MutableList<Song> {
            val result = mutableListOf<Song>()
            PlaylistQueue.currentList.toCollection(result)
            return result
        }

        override fun setPlayerMode(mode: PlayerMode) {
            EventBus.getDefault().post(MessageEvent(MessageType.MODE_CHANGED, "切换播放模式"))
            playerMode = mode
            PlaylistQueue.reloadPlaylist(mode)
            val mmkv = MMKV.defaultMMKV()
            mmkv?.encode("player_mode", mode.value)
        }

        override fun getPlayerMode(): PlayerMode {
            return playerMode
        }
    }

}