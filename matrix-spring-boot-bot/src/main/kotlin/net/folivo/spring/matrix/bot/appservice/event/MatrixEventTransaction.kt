package net.folivo.spring.matrix.bot.appservice.event

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_event_transaction")
data class MatrixEventTransaction(
    @Id
    @Column("id")
    val id: String,
    @Version
    @Column("version")
    val version: Int = 0
)