package net.folivo.matrix.bot.appservice.room

import net.folivo.matrix.core.model.MatrixId.RoomId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room")
data class MatrixRoom(
        @Id
        @Column("id")
        val id: RoomId,

        @Column("is_managed")
        val isManaged: Boolean = false,

        @Version
        @Column("version")
        val version: Int = 1
)