package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.core.model.MatrixId.RoomAliasId
import net.folivo.matrix.core.model.MatrixId.RoomId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room_alias")
data class MatrixRoomAlias(
        @Id
        @Column("alias")
        val alias: RoomAliasId,

        @Column("room_id")
        val roomId: RoomId,

        @Version
        @Column("version")
        val version: Int = 1
)