package net.folivo.spring.matrix.bot.membership

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_membership")
data class MatrixMembership(
    @Column("user_id")
    val userId: MatrixId.UserId,
    @Column("room_id")
    val roomId: MatrixId.RoomId,
    @Id
    @Column("id")
    val id: String = "${userId.full}-${roomId.full}",
    @Version
    @Column("version")
    val version: Int = 0
)