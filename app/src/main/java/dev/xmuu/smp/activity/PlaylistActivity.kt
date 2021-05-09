package dev.xmuu.smp.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.rxLifeScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.adapter.PlaylistAdapter
import dev.xmuu.smp.databinding.ActivityPlaylistBinding
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.CommonDIffCallback
import dev.xmuu.smp.other.MessageEvent
import dev.xmuu.smp.other.MessageType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PlaylistActivity : AppCompatActivity() {

    private lateinit var adapter: PlaylistAdapter
    private val binding: ActivityPlaylistBinding by viewbind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.playlistToolbar)
        title = "播放列表"
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    private fun initRecyclerView() {
        adapter = PlaylistAdapter(this)
        with(binding) {
            playlistRecyclerView.adapter = adapter
            playlistRecyclerView.layoutManager = LinearLayoutManager(this@PlaylistActivity)
            loadListData()
        }
    }

    private fun initSwipeRefreshLayout() {
        with(binding) {
            playlistSwipeRefreshLayout.apply {
                setColorSchemeResources(R.color.red)
                setOnRefreshListener {
                    loadListData()
                }
            }
        }
    }

    private fun loadListData() {
        rxLifeScope.launch(
            {
                adapter.list = App.musicController.value?.getPlaylist()!!
                adapter.notifyDataSetChanged()
            },
            {
                Toast.makeText(this, "发生错误，请稍后重试", Toast.LENGTH_SHORT).show()
            },
            {
//                binding.playlistSwipeRefreshLayout.isRefreshing = true
            },
            {
                binding.playlistSwipeRefreshLayout.isRefreshing = false
            }
        )
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.STATUS_CHANGED -> {
                loadListData()
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
        loadListData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}