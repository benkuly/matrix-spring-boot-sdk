package net.folivo.matrix.bot.appservice.room

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("matrix_room")
data class MatrixRoom(
        @Id
        @Column("id")
        val id: String, // FIXME switch to extra type with conversion

        @Column("is_managed")
        val isManaged: Boolean = false,

        @Version
        @Column("version")
        val version: Int = 1
)