package net.folivo.matrix.restclient.model.events.m.room.message

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.restclient.annotation.MatrixMessageEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-notice">matrix spec</a>
 */
@MatrixMessageEventContent("m.notice")
class NoticeMessageEventContent : MessageEvent.MessageEventContent {
    constructor(
            body: String,
            format: String? = null,
            formattedBody: String? = null
    ) : super(
            body = body,
            messageType = "m.notice"
    ) {
        this.format = format
        this.formattedBody = formattedBody
    }

    @JsonProperty("format")
    val format: String?

    @JsonProperty("formatted_body")
    val formattedBody: String?
}