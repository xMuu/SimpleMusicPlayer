package dev.xmuu.smp.model.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import dev.xmuu.smp.model.BoardBrief
import java.lang.reflect.Type
import java.util.*

class BoardBriefDeserializer : JsonDeserializer<BoardBrief> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BoardBrief {
        val boardBrief = BoardBrief()
        val root = json!!.asJsonObject
        boardBrief.id = root["id"].asString
        boardBrief.name = root["name"].asString
        if (root["description"] !== JsonNull.INSTANCE) {
            boardBrief.description = root["description"].asString
        } else {
            boardBrief.description = ""
        }
        boardBrief.coverUrl = root["coverImgUrl"].asString
        val tracksArray = root["tracks"].asJsonArray
        val tracks: MutableList<String>?
        if (tracksArray.size() > 0) {
            boardBrief.hasTracks = true
            tracks = ArrayList()
            for (i in 0 until tracksArray.size()) {
                val trackObject = tracksArray[i].asJsonObject
                val first = trackObject["first"].asString
                val second = trackObject["second"].asString
                val track = "$first - $second"
                tracks.add(track)
            }
        } else {
            boardBrief.hasTracks = false
            tracks = null
        }
        boardBrief.topTracks = tracks
        return boardBrief
    }
}