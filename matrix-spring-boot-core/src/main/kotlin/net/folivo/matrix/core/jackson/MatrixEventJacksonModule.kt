package net.folivo.matrix.core.jackson;

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleSerializers
import net.folivo.matrix.core.model.MatrixId
import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent


class MatrixEventJacksonModule(
        private val registeredEvents: Map<String, Class<out Event<*>>>,
        private val registeredMessageEventContent: Map<String, Class<out MessageEvent.MessageEventContent>>
) : Module() {
    override fun getModuleName(): String {
        return "MatrixEventJacksonModule";
    }

    override fun version(): Version {
        return Version.unknownVersion();
    }

    override fun setupModule(context: SetupContext) {
        context.registerSubtypes(*registeredEvents.map { NamedType(it.value, it.key) }.toTypedArray())
        context.addDeserializers(
                SimpleDeserializers(
                        mapOf(
                                MessageEvent::class.java to MessageEventTypeDeserializer(
                                        registeredMessageEventContent
                                ),
                                MatrixId::class.java to MatrixIdDeserializer()
                        )
                )
        )
        context.addSerializers(SimpleSerializers(listOf(MatrixIdSerializer())))
    }
}
