package dev.xmuu.smp.other

import com.bumptech.glide.request.transition.DrawableCrossFadeFactory

object MyDrawableCrossFadeFactory {
    val INSTANCE = DrawableCrossFadeFactory.Builder(500).setCrossFadeEnabled(true).build()
}