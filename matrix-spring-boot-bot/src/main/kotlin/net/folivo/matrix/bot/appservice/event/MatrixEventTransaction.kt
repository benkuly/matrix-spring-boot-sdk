package net.folivo.matrix.bot.appservice.event

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("EventTransaction")
data class MatrixEventTransaction(
        @Column("tnxId")
        var tnxId: String,
        @Column("eventIdOrHash")
        var eventIdOrHash: String,
        @Id
        val id: UUID = UUID.randomUUID(),
        @Version
        val version: Int = 1
)