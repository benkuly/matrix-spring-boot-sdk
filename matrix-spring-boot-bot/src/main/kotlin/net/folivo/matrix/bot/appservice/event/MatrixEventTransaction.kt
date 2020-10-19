package net.folivo.matrix.bot.appservice.event

import net.folivo.matrix.core.model.MatrixId.EventId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_event_transaction")
data class MatrixEventTransaction(
        @Column("tnx_id")
        var tnxId: String,
        @Column("event_id")
        var eventId: EventId,
        @Id
        @Column("id")
        val id: String = "$tnxId-$eventId",
        @Version
        @Column("version")
        val version: Int = 1
)