package dev.xmuu.smp.model.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.model.SongList
import java.lang.reflect.Type
import java.util.*

class SongListDeserializer : JsonDeserializer<SongList> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SongList {
        val songList = SongList()
        val list = ArrayList<Song>()
        val root = json!!.asJsonObject
        if (root.has("songs")) {
            val tracks = root["songs"].asJsonArray
            for (element in tracks) {
                list.add(context!!.deserialize(element, Song::class.java))
            }
        }
        if (root.has("playlist")) {
            val playlist = root["playlist"].asJsonObject
            songList.id = playlist["id"].asString
            val tracks = playlist["tracks"].asJsonArray
            for (element in tracks) {
                list.add(context!!.deserialize(element, Song::class.java))
            }
        }
        songList.list = list
        return songList
    }
}