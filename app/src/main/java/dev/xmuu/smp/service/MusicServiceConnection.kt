package dev.xmuu.smp.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.tencent.mmkv.MMKV
import dev.xmuu.smp.App
import dev.xmuu.smp.App.Companion.musicController
import dev.xmuu.smp.other.PlayerMode
import dev.xmuu.smp.util.MusicUtil

class MusicServiceConnection : ServiceConnection {

    private val mmkv = MMKV.defaultMMKV()

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        musicController.value = service as MusicService.MusicController
        val localPlayList = MusicUtil.getSongList(App.context)
        when (mmkv?.decodeInt("player_mode")) {
            PlayerMode.ONE.value -> musicController.value?.setPlayerMode(PlayerMode.ONE)
            PlayerMode.SHUFFLE.value -> musicController.value?.setPlayerMode(PlayerMode.SHUFFLE)
            else -> musicController.value?.setPlayerMode(PlayerMode.LOOP)
        }
        musicController.value?.setPlaylist(localPlayList)
        if (!musicController.value?.getPlaylist().isNullOrEmpty()) {
            musicController.value?.getPlaylist()?.first()?.let {
                musicController.value?.setCurrentSong(
                    it
                )
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicController.value = null
    }
}