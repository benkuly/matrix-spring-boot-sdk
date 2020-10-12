package net.folivo.matrix.bot.appservice.event

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("EventTransaction")
data class MatrixEventTransaction(
        @Column("tnxId")
        var tnxId: String,
        @Column("eventIdOrHash")
        var eventIdOrHash: String,
        @Id
        val id: String = "$tnxId-$eventIdOrHash",
        @Version
        val version: Int = 1
)