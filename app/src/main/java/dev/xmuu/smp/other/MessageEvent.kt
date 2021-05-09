package dev.xmuu.smp.other

data class MessageEvent(
    val type: MessageType,
    val message: String?
)