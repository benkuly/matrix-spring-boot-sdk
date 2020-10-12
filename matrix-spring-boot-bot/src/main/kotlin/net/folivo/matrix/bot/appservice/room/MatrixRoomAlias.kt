package net.folivo.matrix.bot.appservice.room

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("MatrixRoomAlias")
data class MatrixRoomAlias(
        @Id
        val alias: String,

        @Column("fk_MatrixRoomAlias_MatrixRoom")
        val roomId: String,

        @Version
        val version: Int = 1
)