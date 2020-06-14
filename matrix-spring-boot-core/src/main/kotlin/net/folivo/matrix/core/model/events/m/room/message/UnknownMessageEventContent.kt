package net.folivo.matrix.core.model.events.m.room.message

import com.fasterxml.jackson.annotation.JsonProperty

class UnknownMessageEventContent : MessageEvent.MessageEventContent {
    constructor(
            body: String?,
            @JsonProperty("msgtype")
            messageType: String?
    ) : super(
            body = body ?: "",
            messageType = messageType ?: ""
    )
}