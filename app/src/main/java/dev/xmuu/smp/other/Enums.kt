package dev.xmuu.smp.other

enum class SongType(val value: Boolean) {
    LOCAL(true), ONLINE(false)
}

enum class PlayerMode(val value: Int) {
    LOOP(1), ONE(2), SHUFFLE(3)
}

enum class MessageType {
    STATUS_CHANGED, MODE_CHANGED
}