package dev.xmuu.smp.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import dev.xmuu.smp.baseUrl
import dev.xmuu.smp.other.SongType
import dev.xmuu.smp.util.GsonUtil
import okhttp3.*
import rxhttp.wrapper.param.RxHttp
import java.io.IOException

class Song {
    var type: SongType = SongType.LOCAL
    var id: String? = null
    var name: String? = null
    var artist: String? = null
    var albumId: Long = 0
    var albumName: String? = null
    var albumCoverUrl: String? = null

    inline fun getSongUri(crossinline result: (Any?) -> Unit) {
        if (type == SongType.LOCAL) {
            result.invoke(
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id?.toLong() ?: -1
                )
            )
        } else {
            result.invoke("https://music.163.com/song/media/outer/url?id=${id}.mp3")
        }
    }

    fun getSubtitle(): String {
        return "$artist - $albumName"
    }

    fun getSmallCoverUri(): Uri {
        return if (type == SongType.LOCAL) {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
        } else {
            Uri.parse("$albumCoverUrl?param=100y100")
        }
    }

    fun getLargeCoverUri(): Uri {
        return if (type == SongType.LOCAL) {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
        } else {
            Uri.parse("$albumCoverUrl?param=300y300")
        }
    }

}