package net.folivo.spring.matrix.bot.event

import net.folivo.trixnity.core.model.MatrixId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("matrix_sync_batch_token")
data class MatrixSyncBatchToken(
    @Id
    @Column("user_id")
    val userId: MatrixId.UserId,

    @Column("token")
    val token: String?,

    @Version
    @Column("version")
    val version: Int = 0
)