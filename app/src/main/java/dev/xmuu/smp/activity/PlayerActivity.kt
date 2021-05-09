package dev.xmuu.smp.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hi.dhl.binding.viewbind
import com.zackratos.ultimatebarx.ultimatebarx.UltimateBarX
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.databinding.ActivityPlayerBinding
import dev.xmuu.smp.fragment.AlbumImageFragment
import dev.xmuu.smp.fragment.LyricFragment
import dev.xmuu.smp.other.MessageEvent
import dev.xmuu.smp.other.MessageType
import dev.xmuu.smp.other.PlayerMode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PlayerActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val DELAY_MILLIS = 500L
        private const val MSG_UPDATE_PROGRESS = 0
    }

    private val binding: ActivityPlayerBinding by viewbind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBars()
        initTabAndViewpager()
        initButtons()
        initSeekBar()
        loadSongInfo()
        refreshButtonStatus()
        refreshSeekBar()
    }

    private fun initBars() {
        with(binding) {
            UltimateBarX.with(this@PlayerActivity)
                .transparent()
                .applyStatusBar()
            UltimateBarX.with(this@PlayerActivity)
                .transparent()
                .applyNavigationBar()
            UltimateBarX.addStatusBarTopPadding(playerTopBar)
            UltimateBarX.addNavigationBarBottomPadding(playerControlArea)
        }
    }

    private fun initTabAndViewpager() {
        with(binding) {
            playerViewpager.adapter = object : FragmentStateAdapter(this@PlayerActivity) {
                override fun createFragment(position: Int): Fragment {
                    return if (position > 0) {
                        LyricFragment.newInstance()
                    } else {
                        AlbumImageFragment.newInstance()
                    }
                }

                override fun getItemCount(): Int {
                    return 2
                }
            }
            TabLayoutMediator(
                playerTabLayout, playerViewpager
            ) { _: TabLayout.Tab?, _: Int -> }.attach()
        }
    }

    private fun initButtons() {
        with(binding) {
            arrayListOf<View>(
                playerBtnLike,
                playerBtnPrev,
                playerBtnControl,
                playerBtnNext,
                playerBtnMode,
                playerBtnDown
            ).forEach {
                it.setOnClickListener(this@PlayerActivity)
            }
        }
    }

    private fun initSeekBar() {
        with(binding) {
            playerControlSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        App.musicController.value?.setProgress(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
    }

    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                playerBtnPrev -> {
                    App.musicController.value?.playPrev()
                    playerControlSeekBar.progress = 0
                }
                playerBtnControl -> {
                    if (App.musicController.value?.isPlaying() == true) {
                        App.musicController.value!!.pause()
                    } else {
                        if (App.musicController.value?.getDuration()!! > 0) {
                            App.musicController.value!!.play()
                        } else {
                            App.musicController.value!!.play(
                                App.musicController.value!!.getCurrentSong()
                            )
                        }
                    }
                    refreshButtonStatus()
                }
                playerBtnNext -> {
                    App.musicController.value?.playNext()
                    playerControlSeekBar.progress = 0
                }
                playerBtnMode -> {
                    when (App.musicController.value?.getPlayerMode()) {
                        PlayerMode.ONE -> {
                            App.musicController.value?.setPlayerMode(PlayerMode.SHUFFLE)
                        }
                        PlayerMode.LOOP -> {
                            App.musicController.value?.setPlayerMode(PlayerMode.ONE)
                        }
                        PlayerMode.SHUFFLE -> {
                            App.musicController.value?.setPlayerMode(PlayerMode.LOOP)
                        }
                    }
                    refreshButtonStatus()
                }
                playerBtnDown -> onBackPressed()
                else -> {
                }
            }
        }
    }

    private fun loadSongInfo() {
        val currentSong = App.musicController.value?.getCurrentSong()
        with(binding) {
            if (currentSong != null) {
                this.playerSongTitle.text = currentSong.name
                this.playerSongSubtitle.text = currentSong.getSubtitle()
            } else {
                this.playerSongTitle.text = "暂无歌曲"
                this.playerSongSubtitle.text = "选择歌曲开始播放"
            }
        }
    }

    private fun refreshButtonStatus() {
        with(binding) {
            if (App.musicController.value?.isPlaying() == true) {
                playerBtnControl.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PlayerActivity,
                        R.drawable.ic_play_btn_pause_selector
                    )
                )
                handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, DELAY_MILLIS)
            } else {
                playerBtnControl.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PlayerActivity,
                        R.drawable.ic_play_btn_play_selector
                    )
                )
                handler.removeMessages(MSG_UPDATE_PROGRESS)
            }
            when (App.musicController.value?.getPlayerMode()) {
                PlayerMode.ONE -> {
                    playerBtnMode.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@PlayerActivity,
                            R.drawable.ic_play_btn_one_selector
                        )
                    )
                }
                PlayerMode.LOOP -> {
                    playerBtnMode.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@PlayerActivity,
                            R.drawable.ic_play_btn_loop_selector
                        )
                    )
                }
                PlayerMode.SHUFFLE -> {
                    playerBtnMode.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@PlayerActivity,
                            R.drawable.ic_play_btn_shuffle_selector
                        )
                    )
                }
            }
        }
    }

    private fun refreshSeekBar() {
        with(binding) {
            App.musicController.value?.getDuration()?.let {
                playerControlSeekBar.max = it
                playerTotalTimeText.text = intToTimeText(it)
            }
            App.musicController.value?.getProgress()?.let {
                playerControlSeekBar.progress = it
                playerPlayedTimeText.text = intToTimeText(it)
            }
        }
        handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, DELAY_MILLIS)
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.STATUS_CHANGED -> {
                loadSongInfo()
                refreshButtonStatus()
            }
            MessageType.MODE_CHANGED -> {
                refreshButtonStatus()
            }
        }
    }

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                refreshSeekBar()
            }
        }
    }

    private fun intToTimeText(time: Int): String {
        val h = 60 * 60 * 1000
        val m = 60 * 1000
        val s = 1000

        val hour = time / h
        val min = time % h / m
        val sec = time % m / s
        return if (hour == 0) {
            String.format("%02d:%02d", min, sec)
        } else {
            String.format("%02d:%02d:%02d", hour, min, sec)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        loadSongInfo()
        refreshButtonStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}