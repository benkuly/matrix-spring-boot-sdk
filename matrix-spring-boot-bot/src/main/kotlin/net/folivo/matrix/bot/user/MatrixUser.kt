package net.folivo.matrix.bot.user

import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_user")
data class MatrixUser(
        @Id
        @Column("id")
        val id: UserId,

        @Column("is_managed")
        val isManaged: Boolean = false,

        @Version
        @Column("version")
        val version: Int = 0
)