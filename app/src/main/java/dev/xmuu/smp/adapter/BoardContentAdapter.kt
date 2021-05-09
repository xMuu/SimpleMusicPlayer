package dev.xmuu.smp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.activity.PlayerActivity
import dev.xmuu.smp.databinding.ItemBoardContentHeaderBinding
import dev.xmuu.smp.databinding.ItemBoardContentNormalBinding
import dev.xmuu.smp.databinding.ItemEmptyListBinding
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.module.GlideApp
import dev.xmuu.smp.other.MyDrawableCrossFadeFactory
import jp.wasabeef.transformers.glide.BlurTransformation


private const val EMPTY = R.layout.item_empty_list
private const val HEADER = R.layout.item_board_content_header
private const val NORMAL = R.layout.item_board_content_normal


class BoardContentAdapter(private var boardBrief: BoardBrief, private var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var _list: MutableList<Song> = mutableListOf()
    var list: MutableList<Song>
        get() = _list
        set(value) {
            _list = value
        }

    override fun getItemViewType(position: Int): Int {
        return if (_list.isEmpty()) {
            EMPTY
        } else {
            if (position == 0) {
                HEADER
            } else {
                NORMAL
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
            HEADER -> HeaderViewHolder(view)
            NORMAL -> NormalViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bindData(boardBrief)
            is NormalViewHolder -> holder.bindData(_list[position])
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

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemBoardContentHeaderBinding by viewbind()

        fun bindData(boardBrief: BoardBrief) {
            binding.apply {
                boardContentHeaderTitle.text = boardBrief.name
                boardContentHeaderDescription.text = boardBrief.description
                GlideApp.with(context)
                    .load(boardBrief.coverUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(boardContentHeaderCover)
                GlideApp.with(context)
                    .load(boardBrief.coverUrl)
                    .apply(
                        RequestOptions.bitmapTransform(
                            BlurTransformation(
                                context,
                                75,
                                sampling = 12,
                                rs = false
                            )
                        )
                    )
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(object :
                        CustomViewTarget<ConstraintLayout?, Drawable?>(boardContentHeaderLayout) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            view!!.background =
                                ContextCompat.getDrawable(context, R.drawable.default_cover)
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            view!!.background =
                                ContextCompat.getDrawable(context, R.drawable.default_cover)
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable?>?
                        ) {
                            view!!.background = resource
                        }
                    })
            }
        }
    }

    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemBoardContentNormalBinding by viewbind()

        fun bindData(song: Song) {
            binding.apply {
                boardContentNormalTitle.text = song.name
                boardContentNormalSubtitle.text = song.getSubtitle()
                GlideApp.with(context)
                    .load(song.getSmallCoverUri())
                    .transition(DrawableTransitionOptions.withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(boardContentNormalCover)
                boardContentNormalLinearLayout.setOnClickListener {
                    App.musicController.value?.let {
                        it.setPlaylist(list)
                        it.play(song)
                        context.startActivity(Intent(context, PlayerActivity::class.java))
                    }
                }
            }
        }

    }

}