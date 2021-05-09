package dev.xmuu.smp.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.rxLifeScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.R
import dev.xmuu.smp.adapter.BoardContentAdapter
import dev.xmuu.smp.databinding.ActivityBoardContentBinding
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.CommonDIffCallback
import dev.xmuu.smp.util.GsonUtil
import rxhttp.awaitString
import rxhttp.wrapper.param.RxHttp

class BoardContentActivity : AppCompatActivity() {

    private lateinit var brief: BoardBrief
    private lateinit var adapter: BoardContentAdapter
    private val binding: ActivityBoardContentBinding by viewbind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        brief = intent.getSerializableExtra("board_brief") as BoardBrief
        initToolbar()
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.boardListToolbar)
        title = brief.name
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }
    }

    private fun initRecyclerView() {
        adapter = BoardContentAdapter(brief, this)
        with(binding) {
            boardListRecyclerView.adapter = adapter
            boardListRecyclerView.layoutManager = LinearLayoutManager(this@BoardContentActivity)
            loadListData()
        }
    }

    private fun initSwipeRefreshLayout() {
        with(binding) {
            boardListSwipeRefreshLayout.apply {
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
                val oldList = adapter.list
                val newList = getContentList(brief.id)
                if (oldList.isEmpty()) oldList.add(Song())
                adapter.list = newList
                DiffUtil.calculateDiff(CommonDIffCallback(oldList, newList)).also {
                    it.dispatchUpdatesTo(adapter)
                }
            },
            {
                Toast.makeText(this, "网络请求失败，请稍后重试", Toast.LENGTH_SHORT).show()
            },
            {
                binding.boardListSwipeRefreshLayout.isRefreshing = true
            },
            {
                binding.boardListSwipeRefreshLayout.isRefreshing = false
            }
        )
    }

    private suspend fun getContentList(id: String?): MutableList<Song> {
        return if (id == null) {
            mutableListOf()
        } else {
            val json = RxHttp.get("/playlist/detail?id=${id}").awaitString()
            GsonUtil.parseSongList(json).list
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}