package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.bot.appservice.room.AppserviceRoom
import org.neo4j.springframework.data.core.schema.Id
import org.neo4j.springframework.data.core.schema.Node
import org.neo4j.springframework.data.core.schema.Relationship
import org.neo4j.springframework.data.core.schema.Relationship.Direction.OUTGOING

@Node("AppserviceUser")
data class AppserviceUser(
        @Id
        val userId: String,

        @Relationship(type = "MEMBER_OF", direction = OUTGOING)
        val rooms: MutableSet<AppserviceRoom> = HashSet()
)