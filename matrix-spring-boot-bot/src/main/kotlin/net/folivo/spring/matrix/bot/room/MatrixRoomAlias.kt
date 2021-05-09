package net.folivo.spring.matrix.bot.room

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room_alias")
data class MatrixRoomAlias(
    @Id
    @Column("alias")
    val alias: MatrixId.RoomAliasId,

    @Column("room_id")
    val roomId: MatrixId.RoomId,

    @Version
    @Column("version")
    val version: Int = 0
)