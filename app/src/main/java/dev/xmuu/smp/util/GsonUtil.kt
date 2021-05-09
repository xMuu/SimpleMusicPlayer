package dev.xmuu.smp.util

import com.google.gson.GsonBuilder
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.model.BoardBriefList
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.model.SongList
import dev.xmuu.smp.model.deserializer.*

object GsonUtil {

    private var builder: GsonBuilder = GsonBuilder()

    init {
        builder.serializeNulls()
    }

    fun parseBoardBriefList(json: String?): BoardBriefList {
        builder.registerTypeAdapter(BoardBrief::class.java, BoardBriefDeserializer())
        builder.registerTypeAdapter(BoardBriefList::class.java, BoardBriefListDeserializer())
        return builder.create().fromJson(json, BoardBriefList::class.java)
    }

    fun parseSongList(json: String?): SongList {
        builder.registerTypeAdapter(Song::class.java, SongDeserializer())
        builder.registerTypeAdapter(SongList::class.java, SongListDeserializer())
        return builder.create().fromJson(json, SongList::class.java)
    }
}
