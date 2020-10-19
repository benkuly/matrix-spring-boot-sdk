package net.folivo.matrix.bot.membership

import net.folivo.matrix.core.model.MatrixId.RoomId
import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_membership")
class MatrixMembership(
        @Column("user_id")
        val userId: UserId,
        @Column("room_id")
        val roomId: RoomId,
        @Id
        @Column("id")
        val id: String = "${userId.full}-${roomId.full}",
        @Version
        @Column("version")
        val version: Int = 0
)