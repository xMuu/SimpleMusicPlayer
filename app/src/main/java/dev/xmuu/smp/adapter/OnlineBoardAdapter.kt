package dev.xmuu.smp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.R
import dev.xmuu.smp.activity.BoardContentActivity
import dev.xmuu.smp.databinding.ItemEmptyListBinding
import dev.xmuu.smp.databinding.ItemBoardBriefFullBinding
import dev.xmuu.smp.databinding.ItemBoardBriefSimpleBinding
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.module.GlideApp
import dev.xmuu.smp.other.MyDrawableCrossFadeFactory

private const val EMPTY = R.layout.item_empty_list
private const val FULL = R.layout.item_board_brief_full
private const val SIMPLE = R.layout.item_board_brief_simple

class OnlineBoardAdapter(private var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var _list: MutableList<BoardBrief> = mutableListOf()
    var list: MutableList<BoardBrief>
        get() = _list
        set(value) {
            _list = value
        }

    override fun getItemViewType(position: Int): Int {
        return if (_list.isEmpty()) {
            EMPTY
        } else {
            if (_list[position].hasTracks) {
                FULL
            } else {
                SIMPLE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (_list.isEmpty()) {
            1
        } else {
            _list.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflateView(parent, viewType)
        return when (viewType) {
            FULL -> FullViewHolder(view)
            SIMPLE -> SimpleViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FullViewHolder -> holder.bindData(_list[position])
            is SimpleViewHolder -> holder.bindData(_list[position])
            is EmptyViewHolder -> holder.bindData("列表为空")
        }
    }

    private fun inflateView(viewGroup: ViewGroup, viewType: Int): View {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        return layoutInflater.inflate(viewType, viewGroup, false)
    }

    inner class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding: ItemEmptyListBinding by viewbind()

        fun bindData(text: String) {
            binding.apply {
                this.listEmptyTipText.text = text
            }
        }

    }

    inner class FullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemBoardBriefFullBinding by viewbind()

        fun bindData(boardBrief: BoardBrief) {
            binding.apply {
                onlineBoardItemTitle.text = boardBrief.name
                onlineBoardItem1StText.text = boardBrief.topTracks?.get(0) ?: ""
                onlineBoardItem2NdText.text = boardBrief.topTracks?.get(1) ?: ""
                onlineBoardItem3RdText.text = boardBrief.topTracks?.get(2) ?: ""
                GlideApp.with(context)
                    .load(boardBrief.coverUrl)
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(onlineBoardItemCover)
                onlineBoardItemLayout.setOnClickListener {
                    val intent = Intent(context, BoardContentActivity::class.java)
                    intent.putExtra("board_brief", boardBrief)
                    context.startActivity(intent)
                }
            }
        }

    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemBoardBriefSimpleBinding by viewbind()

        fun bindData(boardBrief: BoardBrief) {
            binding.apply {
                onlineBoardItemTitle.text = boardBrief.name
                onlineBoardItemDescription.text = boardBrief.description ?: ""
                GlideApp.with(context)
                    .load(boardBrief.coverUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(onlineBoardItemCover)
                onlineBoardItemLayout.setOnClickListener {
                    val intent = Intent(context, BoardContentActivity::class.java)
                    intent.putExtra("board_brief", boardBrief)
                    context.startActivity(intent)
                }
            }
        }

    }
}