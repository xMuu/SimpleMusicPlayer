package dev.xmuu.smp.util

import android.content.Context
import android.provider.MediaStore
import dev.xmuu.smp.model.Song
import rxhttp.awaitString
import rxhttp.wrapper.param.RxHttp
import java.util.*

object MusicUtil {

    private const val contentOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER // 排列顺序
    private val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI // 内容的 URI 地址
    private val contentProjection = arrayOf( // 需要搜索信息的列
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.ArtistColumns.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.AlbumColumns.ALBUM
    )

    fun getSongList(context: Context): MutableList<Song> {
        val resolver = context.contentResolver
        val songList: MutableList<Song> = ArrayList<Song>()
        resolver.query(
            contentUri, contentProjection, null, null,
            contentOrder
        ).use { cursor ->
            cursor?.let {
                val id = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val name = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artist = it.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST)
                val albumId = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumName = it.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM)
                while (it.moveToNext()) {
                    val song = Song()
                    song.id = it.getString(id)
                    song.name = it.getString(name)
                    song.artist = it.getString(artist)
                    song.albumId = it.getLong(albumId)
                    song.albumName = it.getString(albumName)
                    songList.add(song)
                }
                it.close()
            }
        }
        return songList
    }

    suspend fun getSongInfo(isLocal: Boolean, id: String, context: Context): Song? {
        return if (isLocal) {
            getLocalSongInfo(context, id)
        } else {
            getOnlineSongInfo(id)
        }
    }

    private fun getLocalSongInfo(context: Context, songId: String): Song? {
        val songList: MutableList<Song> = ArrayList<Song>()
        val resolver = context.contentResolver
        val selection = MediaStore.Audio.Media._ID + " = ? "
        resolver.query(
            contentUri, contentProjection, selection, arrayOf(songId),
            contentOrder
        ).use { cursor ->
            cursor?.let {
                val id = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val name = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artist = it.getColumnIndexOrThrow(MediaStore.Audio.ArtistColumns.ARTIST)
                val albumId = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumName = it.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM)
                while (it.moveToNext()) {
                    val song = Song()
                    song.id = it.getString(id)
                    song.name = it.getString(name)
                    song.artist = it.getString(artist)
                    song.albumId = it.getLong(albumId)
                    song.albumName = it.getString(albumName)
                    songList.add(song)
                }
                it.close()
            }
        }
        return if (songList.isEmpty()) {
            null
        } else {
            songList.first()
        }
    }

    private suspend fun getOnlineSongInfo(id: String): Song? {
        val json = RxHttp.get("/song/detail?ids=$id").awaitString()
        val result = GsonUtil.parseSongList(json)
        return if (result.list.isEmpty()) {
            null
        } else {
            result.list.first()
        }
    }

}
