package net.folivo.matrix.bot.appservice.user

import net.folivo.matrix.bot.appservice.room.AppserviceRoom
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToMany

@Entity
class AppserviceUser(
        @Id
        val userId: String,

        @ManyToMany(mappedBy = "members")
        val rooms: MutableSet<AppserviceRoom> = HashSet()
)