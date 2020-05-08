package net.folivo.matrix.bot.appservice.event

import org.neo4j.springframework.data.core.schema.GeneratedValue
import org.neo4j.springframework.data.core.schema.Id
import org.neo4j.springframework.data.core.schema.Node
import org.neo4j.springframework.data.core.schema.Property
import org.springframework.data.annotation.Version

@Node("EventTransaction")
data class EventTransaction(
        @Property("tnxId")
        var tnxId: String,
        @Property("eventIdElseType")
        var eventIdElseType: String,
        @Id
        @GeneratedValue
        val id: Long? = null // TODO maybe not in constructor to prevent duplicated keys due to wrong usage
) {
    @Version
    val version: Long = 0
}