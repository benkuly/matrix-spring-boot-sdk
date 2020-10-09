package net.folivo.matrix.bot.appservice.room

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("MatrixRoom")
data class MatrixRoom(
        @Id
        val id: String,

        @Column("isManaged")
        val isManaged: Boolean = false,

        @Version
        val version: Int = 1
)