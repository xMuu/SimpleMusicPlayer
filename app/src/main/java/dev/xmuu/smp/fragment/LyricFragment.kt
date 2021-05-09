package dev.xmuu.smp.fragment

import androidx.fragment.app.Fragment
import com.hi.dhl.binding.viewbind
import dev.xmuu.smp.R
import dev.xmuu.smp.databinding.FragmentLyricBinding

class LyricFragment : Fragment(R.layout.fragment_lyric) {

    companion object {
        @JvmStatic
        fun newInstance() = LyricFragment()
    }

    private val binding: FragmentLyricBinding by viewbind()

}