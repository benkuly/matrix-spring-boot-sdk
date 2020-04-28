package net.folivo.matrix.core.model.events.m.room

import com.fasterxml.jackson.annotation.JsonProperty
import net.folivo.matrix.core.annotation.MatrixEvent
import net.folivo.matrix.core.model.events.StandardStateEvent
import net.folivo.matrix.core.model.events.StateEventContent

/**
 * @see <a href="https://matrix.org/docs/spec/client_server/r0.6.0#m-room-avatar">matrix spec</a>
 */
@MatrixEvent("m.room.avatar")
class AvatarEvent : StandardStateEvent<AvatarEvent.AvatarEventContent> {

    constructor(
            content: AvatarEventContent,
            id: String,
            sender: String,
            originTimestamp: Long,
            roomId: String? = null,
            unsigned: UnsignedData,
            previousContent: AvatarEventContent? = null
    ) : super(
            type = "m.room.avatar",
            content = content,
            id = id,
            sender = sender,
            originTimestamp = originTimestamp,
            roomId = roomId,
            unsigned = unsigned,
            stateKey = "",
            previousContent = previousContent
    ) {
    }

    data class AvatarEventContent(
            @JsonProperty("url")
            val url: String,
            @JsonProperty("info")
            val info: ImageInfo? = null
    ) : StateEventContent {
        data class ImageInfo(
                @JsonProperty("h")
                val h: Int? = null,
                @JsonProperty("w")
                val w: Int? = null,
                @JsonProperty("mimetype")
                val mimeType: String? = null,
                @JsonProperty("size")
                val size: Int? = null,
                @JsonProperty("thumbnail_url")
                val thumbnailUrl: String? = null,
//                @JsonProperty("thumbnail_file") //TODO encryption
//                val thumbnailFile: EncryptedFile? = null,
                @JsonProperty("thumbnail_info")
                val thumbnailUnfo: ThumbnailInfo? = null
        ) {
            data class ThumbnailInfo(
                    @JsonProperty("h")
                    val h: Int? = null,
                    @JsonProperty("w")
                    val w: Int? = null,
                    @JsonProperty("mimetype")
                    val mimeType: String? = null,
                    @JsonProperty("size")
                    val size: Int? = null
            )
        }
    }
}