package net.folivo.matrix.bot.appservice.user

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("MatrixUser")
data class MatrixUser(
        @Id
        val id: String,

        @Column("isManaged")
        val isManaged: Boolean = false,

        @Version
        val version: Int = 1
)