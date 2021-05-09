package dev.xmuu.smp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.activity.PlayerActivity
import dev.xmuu.smp.databinding.ItemEmptyListBinding
import dev.xmuu.smp.databinding.ItemLocalMusicBinding
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.module.GlideApp
import dev.xmuu.smp.other.MyDrawableCrossFadeFactory

private const val EMPTY = R.layout.item_empty_list
private const val NORMAL = R.layout.item_local_music

class LocalMusicAdapter(private var context: Context) :
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
            NORMAL
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
        return if (viewType == EMPTY) {
            EmptyViewHolder(view)
        } else {
            NormalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmptyViewHolder) {
            holder.bindData("列表为空")
        }
        if (holder is NormalViewHolder) {
            holder.bindData(_list[position])
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

    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ItemLocalMusicBinding by viewbind()

        fun bindData(song: Song) {
            binding.apply {
                localMusicTitle.text = song.name
                localMusicSubtitle.text = song.getSubtitle()
                GlideApp.with(context)
                    .load(song.getSmallCoverUri())
                    .transition(DrawableTransitionOptions.withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(this.localMusicCover)
                localMusicLayout.setOnClickListener {
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
