package net.folivo.matrix.bot.appservice.membership

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("MatrixMembership")
class MatrixMembership(
        @Column("fk_MatrixMembership_MatrixUser")
        val userId: String,
        @Column("fk_MatrixMembership_MatrixRoom")
        val roomId: String,
        @Id
        val id: String = "$userId-$roomId",
        @Version
        val version: Int = 1
)