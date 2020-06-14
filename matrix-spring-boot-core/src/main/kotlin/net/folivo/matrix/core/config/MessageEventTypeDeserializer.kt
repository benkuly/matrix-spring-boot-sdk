package net.folivo.matrix.core.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import net.folivo.matrix.core.model.events.RoomEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.UnknownMessageEventContent


class MessageEventTypeDeserializer(
        private val eventContentTypes: Map<String, Class<out MessageEvent.MessageEventContent>>
) : JsonDeserializer<MessageEvent<*>>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): MessageEvent<*> {
        val node: JsonNode = context.readTree(parser)
        val msgtype = node.get("content")?.get("msgtype")?.asText()

        val javaType = eventContentTypes[msgtype] ?: UnknownMessageEventContent::class.java

        val contentParser = node.get("content").traverse()
        contentParser.nextToken()
        val unsignedParser = node.get("unsigned").traverse()
        unsignedParser.nextToken()

        return MessageEvent(
                content = context.readValue(contentParser, javaType),
                id = node.get("event_id").asText(),
                sender = node.get("sender").asText(),
                originTimestamp = node.get("origin_server_ts").asLong(),
                roomId = node.get("room_id")?.asText(),
                unsigned = context.readValue(unsignedParser, RoomEvent.UnsignedData::class.java)
        )
    }
}