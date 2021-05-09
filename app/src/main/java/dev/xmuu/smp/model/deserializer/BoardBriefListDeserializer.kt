package dev.xmuu.smp.model.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import dev.xmuu.smp.model.BoardBrief
import dev.xmuu.smp.model.BoardBriefList
import java.lang.reflect.Type

class BoardBriefListDeserializer : JsonDeserializer<BoardBriefList> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BoardBriefList {
        val result = BoardBriefList()
        val root = json!!.asJsonObject
        result.code = root["code"].asInt
        val list = root["list"].asJsonArray
        val tmp = ArrayList<BoardBrief>()
        if (list.size() > 0) {
            for (element in list) {
                tmp.add(context!!.deserialize(element, BoardBrief::class.java))
            }
        }
        result.list = tmp
        return result
    }
}