package nl.booxchange.model

data class ChatOpenedEvent(
    val chatModel: ChatModel? = null,
    val chatId: String = chatModel?.id!!
)
