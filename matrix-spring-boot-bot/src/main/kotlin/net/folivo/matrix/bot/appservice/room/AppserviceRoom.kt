package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.bot.appservice.user.AppserviceUser
import javax.persistence.*

@Entity
class AppserviceRoom(
        @Id
        val roomId: String,

        val roomAlias: String? = null,

        @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
        @JoinTable(name = "appserviceRoom_appserviceUser")
        val members: MutableSet<AppserviceUser> = HashSet()
)