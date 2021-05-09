package net.folivo.spring.matrix.bot.room

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room")
data class MatrixRoom(
    @Id
    @Column("id")
    val id: MatrixId.RoomId,

    @Column("is_managed")
    val isManaged: Boolean = false,

    @Version
    @Column("version")
    val version: Int = 0
)