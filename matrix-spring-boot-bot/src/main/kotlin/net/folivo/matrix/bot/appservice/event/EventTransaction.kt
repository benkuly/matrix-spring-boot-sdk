package net.folivo.matrix.bot.appservice.event

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class EventTransaction(
        var tnxId: String,
        var eventIdOrType: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set
}