package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.bot.appservice.user.AppserviceUser
import org.neo4j.springframework.data.core.schema.Id
import org.neo4j.springframework.data.core.schema.Node
import org.neo4j.springframework.data.core.schema.Property
import org.neo4j.springframework.data.core.schema.Relationship
import org.neo4j.springframework.data.core.schema.Relationship.Direction.INCOMING

@Node("AppserviceRoom")
data class AppserviceRoom(
        @Id
        val roomId: String,

        @Property("roomAlias")
        val roomAlias: String? = null,

        @Relationship(type = "MEMBER_OF", direction = INCOMING)
        val members: MutableSet<AppserviceUser> = HashSet()
)