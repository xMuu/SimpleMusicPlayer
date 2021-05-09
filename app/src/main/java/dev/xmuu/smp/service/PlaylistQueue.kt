package dev.xmuu.smp.service

import dev.xmuu.smp.model.Song
import dev.xmuu.smp.other.PlayerMode
import java.util.*

object PlaylistQueue {

    var currentList: Deque<Song> = LinkedList()

    private var normalList: Deque<Song> = LinkedList()

    fun setPlaylist(playlist: MutableList<Song>) {
        normalList.clear()
        normalList.addAll(playlist)
    }

    fun to(song: Song) {
        while (currentList.first != song) {
            val temp = currentList.pollFirst()
            currentList.addLast(temp)
        }
    }

    fun next(): Song? {
        if (currentList.isEmpty()) return null
        val temp = currentList.pollFirst()
        currentList.addLast(temp)
        return currentList.first
    }

    fun previous(): Song? {
        if (currentList.isEmpty()) return null
        val temp = currentList.pollLast()
        currentList.addFirst(temp)
        return currentList.first
    }

    fun reloadPlaylist(mode: PlayerMode) {
        when (mode) {
            PlayerMode.ONE -> loadLoopModeList()
            PlayerMode.LOOP -> loadLoopModeList()
            PlayerMode.SHUFFLE -> loadShuffleModeList()
        }
    }

    private fun loadLoopModeList() {
        currentList = normalList
    }

    private fun loadShuffleModeList() {
        val temp = mutableListOf<Song>()
        normalList.toCollection(temp)
        temp.shuffle()
        currentList.clear()
        currentList.addAll(temp)
    }

}