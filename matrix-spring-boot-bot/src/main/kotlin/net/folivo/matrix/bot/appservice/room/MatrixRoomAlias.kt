package net.folivo.matrix.bot.appservice.room

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room_alias")
data class MatrixRoomAlias(
        @Id
        @Column("alias")
        val alias: MatrixRoomAliasId,

        @Column("room_id")
        val roomId: MatrixRoomId,

        @Version
        @Column("version")
        val version: Int = 1
)