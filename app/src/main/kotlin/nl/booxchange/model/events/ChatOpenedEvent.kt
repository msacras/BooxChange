package nl.booxchange.model.events

import nl.booxchange.model.entities.ChatModel

data class ChatOpenedEvent(
    val chatModel: ChatModel? = null,
    val chatId: String = chatModel?.id!!
)
