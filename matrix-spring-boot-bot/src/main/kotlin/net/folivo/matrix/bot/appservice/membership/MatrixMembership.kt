package net.folivo.matrix.bot.appservice.membership

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_membership")
class MatrixMembership(
        @Column("user_id")
        val userId: String,
        @Column("room_id")
        val roomId: String,
        @Id
        @Column("id")
        val id: String = "$userId-$roomId",
        @Version
        @Column("version")
        val version: Int = 1
)