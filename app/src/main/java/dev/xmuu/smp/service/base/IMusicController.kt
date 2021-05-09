package dev.xmuu.smp.service.base

import androidx.lifecycle.LiveData
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.PlayerMode

interface IMusicController {

    /**
     * 开始播放
     */
    fun play()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 播放指定歌曲
     */
    fun play(song: Song)

    /**
     * 是否正在播放
     * @return true 正在播放，false 暂停
     */
    fun isPlaying(): Boolean

    /**
     * 播放上一首
     */
    fun playPrev()

    /**
     * 播放下一首
     */
    fun playNext()

    /**
     * 获取播放时长
     */
    fun getDuration(): Int

    /**
     * 设置播放进度
     */
    fun setProgress(newProgress: Int)

    /**
     * 获取播放进度
     */
    fun getProgress(): Int

    /**
     * 设置当前歌曲
     * @return song
     */
    fun setCurrentSong(song: Song)

    /**
     * 获取正在播放的歌曲
     * @return song
     */
    fun getCurrentSong(): Song

    /**
     * 设置播放列表
     * @param playlist 播放器列表
     */
    fun setPlaylist(playlist: MutableList<Song>)

    /**
     * 获取播放列表
     * @return LiveData 播放列表
     */
    fun getPlaylist(): MutableList<Song>

    /**
     * 设置播放模式
     * @param mode 播放模式
     */
    fun setPlayerMode(mode: PlayerMode)

    /**
     * 获取播放模式
     * @return mode 播放模式
     */
    fun getPlayerMode(): PlayerMode

}