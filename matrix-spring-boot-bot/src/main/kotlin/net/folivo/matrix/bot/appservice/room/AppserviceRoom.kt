package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.bot.appservice.user.AppserviceUser
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class AppserviceRoom(
        @Id
        val roomId: String,

        val roomAlias: String? = null,

        @ManyToMany
        @JoinTable(name = "appserviceRoom_appserviceUser")
        val members: MutableSet<AppserviceUser> = HashSet()
)