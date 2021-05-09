package dev.xmuu.smp.model

import java.io.Serializable

class BoardBrief : Serializable {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var coverUrl: String? = null
    var topTracks: List<String>? = null
    var hasTracks = false
}