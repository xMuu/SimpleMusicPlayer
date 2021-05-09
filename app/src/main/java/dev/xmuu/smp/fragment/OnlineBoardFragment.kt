package dev.xmuu.smp.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.rxLifeScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.R
import dev.xmuu.smp.adapter.OnlineBoardAdapter
import dev.xmuu.smp.databinding.FragmentOnlineBoardBinding
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.other.CommonDIffCallback
import dev.xmuu.smp.util.GsonUtil
import rxhttp.awaitString
import rxhttp.wrapper.param.RxHttp

class OnlineBoardFragment : Fragment(R.layout.fragment_online_board) {

    companion object {
        @JvmStatic
        fun newInstance() = OnlineBoardFragment()
    }

    private lateinit var adapter: OnlineBoardAdapter
    private val binding: FragmentOnlineBoardBinding by viewbind()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initRecyclerView() {
        adapter = OnlineBoardAdapter(requireContext())
        with(binding) {
            onlineBoardRecyclerView.adapter = adapter
            onlineBoardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            loadListData()
        }
    }

    private fun initSwipeRefreshLayout() {
        with(binding) {
            onlineListSwipeRefreshLayout.apply {
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
                val newList = getBoardBriefList()
                if (oldList.isEmpty()) oldList.add(BoardBrief())
                adapter.list = newList
                DiffUtil.calculateDiff(CommonDIffCallback(oldList, newList)).also {
                    it.dispatchUpdatesTo(adapter)
                }
            },
            {
                Log.d("OnlineBoardFragment", "loadListData: $it")
                Toast.makeText(requireContext(), "网络请求失败，请稍后重试", Toast.LENGTH_SHORT).show()
            },
            {
                binding.onlineListSwipeRefreshLayout.isRefreshing = true
            },
            {
                binding.onlineListSwipeRefreshLayout.isRefreshing = false
            }
        )
    }

    private suspend fun getBoardBriefList(): MutableList<BoardBrief> {
        val json = RxHttp.get("toplist/detail/")
            .awaitString()
        return GsonUtil.parseBoardBriefList(json).list
    }

}