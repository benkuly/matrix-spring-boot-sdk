package net.folivo.matrix.bot.sync

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class SyncBatchToken(
        @Id
        val id: Int,
        var value: String? = null
)