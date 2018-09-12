package nl.booxchange.model.events

import nl.booxchange.model.entities.MessageModel

data class MessageReceivedEvent(val messageModel: MessageModel)
