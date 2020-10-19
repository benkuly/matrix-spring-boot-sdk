package net.folivo.matrix.bot.appservice.membership

import net.folivo.matrix.bot.appservice.room.MatrixRoomId
import net.folivo.matrix.core.model.MatrixUserId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_membership")
class MatrixMembership(
        @Column("user_id")
        val userId: MatrixUserId,
        @Column("room_id")
        val roomId: MatrixRoomId,
        @Id
        @Column("id")
        val id: String = "${userId.full}-${roomId.full}",
        @Version
        @Column("version")
        val version: Int = 1
)