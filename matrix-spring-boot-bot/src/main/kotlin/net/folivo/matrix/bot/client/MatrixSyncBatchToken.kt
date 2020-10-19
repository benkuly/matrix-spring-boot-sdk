package net.folivo.matrix.bot.client

import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_sync_batch_token")
data class MatrixSyncBatchToken(
        @Id
        @Column("user_id")
        val userId: UserId,

        @Column("token")
        val token: String?,

        @Version
        @Column("version")
        val version: Int = 1
)