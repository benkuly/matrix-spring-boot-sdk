package net.folivo.matrix.bot.appservice.membership

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table


@Table("Membership")
class MatrixMembership(
        @Column("fk_Membership_AppserviceUser")
        val userId: String,
        @Column("fk_Membership_AppserviceRoom")
        val roomId: String,
        @Id
        val id: String = "$userId-$roomId",
        @Version
        val version: Int = 1
)