package net.folivo.matrix.core.config

import net.folivo.matrix.core.model.events.Event
import net.folivo.matrix.core.model.events.UnknownEvent
import net.folivo.matrix.core.model.events.m.room.AliasesEvent
import net.folivo.matrix.core.model.events.m.room.CreateEvent
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.UnknownMessageEventContent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.JacksonTester

@SpringBootTest
@AutoConfigureJsonTesters
class MatrixAutoconfigurationJsonTest {
    @Autowired
    lateinit var json: JacksonTester<Event<*>>

    @Autowired
    lateinit var jsonContent: JacksonTester<MessageEvent.MessageEventContent>

    @Test
    fun `should create subtype from state event`() {
        val content = """
        {
            "content": {
                "aliases": [
                    "#somewhere:example.org",
                    "#another:example.org"
                ]
            },
            "event_id": "$143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "state_key": "example.org",
            "type": "m.room.aliases",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        if (result is AliasesEvent) {
            assertThat(result.content.aliases).contains("#somewhere:example.org", "#another:example.org")
            assertThat(result.unsigned?.age).isEqualTo(1234)
            assertThat(result.type).isEqualTo("m.room.aliases")
        } else {
            fail("result should be of type ${AliasesEvent::class}")
        }
    }

    @Test
    fun `should create subtype from room event`() {
        val content = """
        {
            "content": {
                "creator": "@example:example.org",
                "m.federate": true,
                "predecessor": {
                    "event_id": "something:example.org",
                    "room_id": "!oldroom:example.org"
                },
                "room_version": "1"
            },
            "event_id": "!143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "state_key": "",
            "type": "m.room.create",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        if (result is CreateEvent) {
            assertThat(result.content.creator).isEqualTo("@example:example.org")
            assertThat(result.unsigned?.age).isEqualTo(1234)
            assertThat(result.type).isEqualTo("m.room.create")
        } else {
            fail("result should be of type ${MessageEvent::class} but was ${result::class}")
        }
    }

    @Test
    fun `should create subtype from event even if it's type is unknown`() {
        val content = """
        {
            "content": {
                "something": "unicorn"
            },
            "event_id": "$143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "type": "unknownEventType",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        if (result is UnknownEvent) {
            assertThat(result.type).isEqualTo("unknownEventType")
        } else {
            fail("result should be of type ${UnknownEvent::class}")
        }
    }

    @Test
    fun `should create subtype from message event`() {
        val content = """
        {
            "content": {
                "body": "This is an example text message",
                "format": "org.matrix.custom.html",
                "formatted_body": "<b>This is an example text message</b>",
                "msgtype": "m.text"
            },
            "event_id": "$143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "type": "m.room.message",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        val resultContent = result.content
        if (result is MessageEvent<*> && resultContent is TextMessageEventContent) {
            assertThat(resultContent.format).isEqualTo("org.matrix.custom.html")
            assertThat(result.type).isEqualTo("m.room.message")
        } else {
            fail("resultContent should be of type ${TextMessageEventContent::class}")
        }
    }

    @Test
    fun `should create subtype from message event even if it's type is unknown`() {
        val content = """
        {
            "content": {
                "body": "This is an example text message",
                "format": "org.matrix.custom.html",
                "formatted_body": "<b>This is an example text message</b>",
                "msgtype": "m.unknown"
            },
            "event_id": "$143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "type": "m.room.message",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        val resultContent = result.content
        if (result is MessageEvent<*> && resultContent is UnknownMessageEventContent) {
            assertThat(resultContent.body).isEqualTo("This is an example text message")
            assertThat(resultContent.messageType).isEqualTo("m.unknown")
            assertThat(result.type).isEqualTo("m.room.message")
        } else {
            fail("resultContent should be of type ${UnknownMessageEventContent::class}")
        }
    }

    @Test
    fun `should create subtype from message event even if it's type is null`() {
        val content = """
        {
            "content": {
                "body": "This is an example text message",
                "format": "org.matrix.custom.html",
                "formatted_body": "<b>This is an example text message</b>",
                "msgtype": null
            },
            "event_id": "$143273582443PhrSn:example.org",
            "origin_server_ts": 1432735824653,
            "room_id": "!jEsUZKDJdhlrceRyVU:example.org",
            "sender": "@example:example.org",
            "type": "m.room.message",
            "unsigned": {
                "age": 1234
            }
        }
    """.trimIndent()
        val result = json.parseObject(content)
        val resultContent = result.content
        if (result is MessageEvent<*> && resultContent is UnknownMessageEventContent) {
            assertThat(resultContent.body).isEqualTo("This is an example text message")
            assertThat(resultContent.messageType).isEqualTo("")
            assertThat(result.type).isEqualTo("m.room.message")
        } else {
            fail("resultContent should be of type ${UnknownMessageEventContent::class}")
        }
    }

    @Test
    fun `should create valid matrix json from message event content`() {
        val result = jsonContent.write(TextMessageEventContent("test"))
        assertThat(result).extractingJsonPathStringValue("@.msgtype").isEqualTo("m.text")
        assertThat(result).extractingJsonPathStringValue("@.body").isEqualTo("test")
        println(result.json)
    }
}