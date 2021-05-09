package dev.xmuu.smp.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.App
import dev.xmuu.smp.R
import dev.xmuu.smp.databinding.FragmentAlbumImageBinding
import dev.xmuu.smp.module.GlideApp
import dev.xmuu.smp.other.MessageEvent
import dev.xmuu.smp.other.MessageType
import dev.xmuu.smp.other.MyDrawableCrossFadeFactory
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AlbumImageFragment : Fragment(R.layout.fragment_album_image) {

    companion object {
        @JvmStatic
        fun newInstance() = AlbumImageFragment()
    }

    private val binding: FragmentAlbumImageBinding by viewbind()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAlbumCover()
    }

    private fun loadAlbumCover() {
        with(binding) {
            val currentSong = App.musicController.value?.getCurrentSong()
            if (currentSong != null) {
                GlideApp.with(requireContext())
                    .load(currentSong.getLargeCoverUri())
                    .transition(withCrossFade(MyDrawableCrossFadeFactory.INSTANCE))
                    .error(R.drawable.default_cover)
                    .placeholder(R.drawable.default_cover)
                    .into(playerAlbumCover)
            } else {
                GlideApp.with(requireContext())
                    .load(R.drawable.default_cover)
                    .into(playerAlbumCover)
            }
        }
    }

    @Subscribe(sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.STATUS_CHANGED -> loadAlbumCover()
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
        loadAlbumCover()
    }

}