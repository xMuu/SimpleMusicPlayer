package dev.xmuu.smp.model.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.SongType
import java.lang.reflect.Type

class SongDeserializer : JsonDeserializer<Song> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Song {
        val song = Song()
        val root = json!!.asJsonObject
        song.type = SongType.ONLINE
        song.id = root["id"].asString
        song.name = root["name"].asString
        val artist = root["ar"].asJsonArray
        val stringBuilder = StringBuilder()
        for (element in artist) {
            val `object` = element.asJsonObject
            stringBuilder.append(`object`["name"].asString)
            stringBuilder.append("/")
        }
        stringBuilder.deleteCharAt(stringBuilder.length - 1)
        song.artist = stringBuilder.toString()
        val album = root["al"].asJsonObject
        song.albumName = album["name"].asString
        song.albumCoverUrl = album["picUrl"].asString
        return song
    }
}