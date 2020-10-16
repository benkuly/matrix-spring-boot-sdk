package net.folivo.matrix.bot.appservice.user

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_user")
data class MatrixUser(
        @Id
        @Column("id")
        val id: String,

        @Column("is_managed")
        val isManaged: Boolean = false,

        @Version
        @Column("version")
        val version: Int = 1
)