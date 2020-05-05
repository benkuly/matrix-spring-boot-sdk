package net.folivo.matrix.bot.appservice.user

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class AppserviceUser(
        @Id
        val userId: String
)