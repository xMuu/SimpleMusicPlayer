package dev.xmuu.smp.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.databinding.ActivityMainBinding
import dev.xmuu.smp.fragment.LocalMusicFragment
import dev.xmuu.smp.fragment.OnlineBoardFragment
import dev.xmuu.smp.module.GlideApp
import dev.xmuu.smp.other.MessageEvent
import dev.xmuu.smp.other.MessageType
import dev.xmuu.smp.other.MyDrawableCrossFadeFactory
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val binding: ActivityMainBinding by viewbind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTabAndViewpagerWithPermissionCheck()
        initButtons()
        initBottomBar()
        loadSongInfo()
        refreshButtonStatus()
    }

    @NeedsPermission(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE)
    fun initTabAndViewpager() {
        with(binding) {
            mainViewpager.adapter =
                object : FragmentStateAdapter(this@MainActivity) {
                override fun createFragment(position: Int): Fragment {
                    return if (position > 0) {
                        OnlineBoardFragment.newInstance()
                    } else {
                        LocalMusicFragment.newInstance()
                    }
                }

                override fun getItemCount(): Int {
                    return 2
                }
            }
            TabLayoutMediator(
                mainTabLayout, mainViewpager
            ) { tab: TabLayout.Tab, position: Int ->
                if (position > 0) {
                    tab.text = "在线音乐"
                } else {
                    tab.text = "本地音乐"
                }
            }.attach()
        }
    }

    private fun initButtons() {
        with(binding) {
            arrayListOf<View>(mainBtnControl, mainBtnNext, mainBtnPlayList).forEach {
                it.setOnClickListener(this@MainActivity)
            }
        }
    }

    private fun initBottomBar() {
        with(binding) {
            mainBottomControlBar.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View) {
        with(binding) {
            when (v) {
                mainBtnControl -> {
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
                mainBtnNext -> {
                    App.musicController.value?.playNext()
                }
                mainBtnPlayList -> {
                    startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
                }
                mainBottomControlBar -> {
                    startActivity(Intent(this@MainActivity, PlayerActivity::class.java))
                }
                else -> {
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun loadSongInfo() {
        val currentSong = App.musicController.value?.getCurrentSong()
        with(binding) {
            if (currentSong != null) {
                this.mainSongTitle.text = currentSong.name
                this.mainSongSubtitle.text = currentSong.getSubtitle()
                GlideApp.with(this@MainActivity)
                    .load(currentSong.getSmallCoverUri())
                    .transition(withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(mainAlbumImage)
            } else {
                this.mainSongTitle.text = "暂无歌曲"
                this.mainSongSubtitle.text = "选择歌曲开始播放"
            }
        }
    }

    private fun refreshButtonStatus() {
        with(binding) {
            if (App.musicController.value?.isPlaying() == true) {
                mainBtnControl.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_play_bar_btn_pause
                    )
                )
            } else {
                mainBtnControl.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_play_bar_btn_play
                    )
                )
            }
        }
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.STATUS_CHANGED -> {
                loadSongInfo()
                refreshButtonStatus()
            }
            else -> {
            }
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

}