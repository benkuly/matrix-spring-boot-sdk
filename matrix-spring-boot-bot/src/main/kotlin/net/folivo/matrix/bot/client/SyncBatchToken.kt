package net.folivo.matrix.bot.client

import org.neo4j.springframework.data.core.schema.Id
import org.neo4j.springframework.data.core.schema.Node
import org.neo4j.springframework.data.core.schema.Property

@Node("SyncBatchToken")
data class SyncBatchToken(
        @Id
        val id: String,
        @Property("value")
        val value: String? = null
)