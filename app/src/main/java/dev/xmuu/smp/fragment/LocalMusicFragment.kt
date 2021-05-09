package dev.xmuu.smp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.rxLifeScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.R
import dev.xmuu.smp.adapter.LocalMusicAdapter
import dev.xmuu.smp.databinding.FragmentLocalMusicBinding
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.CommonDIffCallback
import dev.xmuu.smp.util.MusicUtil

class LocalMusicFragment : Fragment(R.layout.fragment_local_music) {

    companion object {
        @JvmStatic
        fun newInstance() = LocalMusicFragment()
    }

    private lateinit var adapter: LocalMusicAdapter
    private val binding: FragmentLocalMusicBinding by viewbind()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initRecyclerView() {
        adapter = LocalMusicAdapter(requireContext())
        with(binding) {
            localMusicRecyclerView.adapter = adapter
            localMusicRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            loadListData()
        }
    }

    private fun initSwipeRefreshLayout() {
        with(binding) {
            localMusicSwipeRefreshLayout.apply {
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
                val newList = MusicUtil.getSongList(requireContext())
                if (oldList.isEmpty()) oldList.add(Song())
                adapter.list = newList
                DiffUtil.calculateDiff(CommonDIffCallback(oldList, newList)).also {
                    it.dispatchUpdatesTo(adapter)
                }
            },
            {
                Toast.makeText(requireContext(), "发生错误，请稍后重试", Toast.LENGTH_SHORT).show()
            },
            {
                binding.localMusicSwipeRefreshLayout.isRefreshing = true
            },
            {
                binding.localMusicSwipeRefreshLayout.isRefreshing = false
            }
        )
    }
}