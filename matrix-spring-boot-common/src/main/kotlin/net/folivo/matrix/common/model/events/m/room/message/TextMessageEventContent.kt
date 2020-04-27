package net.folivo.matrix.common.model.events.m.room.message

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.common.annotation.MatrixMessageEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-text">matrix spec</a>
 */
@MatrixMessageEventContent("m.text")
class TextMessageEventContent : MessageEvent.MessageEventContent {
    constructor(
            body: String,
            format: String? = null,
            formattedBody: String? = null
    ) : super(
            body = body,
            messageType = "m.text"
    ) {
        this.format = format
        this.formattedBody = formattedBody
    }

    @JsonProperty("format")
    val format: String?

    @JsonProperty("formatted_body")
    val formattedBody: String?
}