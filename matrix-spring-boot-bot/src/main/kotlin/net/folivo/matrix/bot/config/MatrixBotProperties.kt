package net.folivo.matrix.bot.config

import net.folivo.matrix.core.model.MatrixId.UserId
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.bot")
@ConstructorBinding
data class MatrixBotProperties(
        val autoJoin: AutoJoinMode = AutoJoinMode.RESTRICTED,
        val trackMembership: TrackMembershipMode = TrackMembershipMode.ALL,
        val serverName: String,
        val username: String,
        val botUserId: UserId = UserId(username, serverName),
        val displayName: String? = null,
        val mode: BotMode = BotMode.CLIENT,
        val database: R2dbcProperties,
        val migration: DataSourceProperties
) {
    enum class BotMode {
        APPSERVICE, CLIENT
    }

    enum class AutoJoinMode {
        ENABLED, DISABLED, RESTRICTED
    }

    enum class TrackMembershipMode {
        ALL, MANAGED, NONE
    }
}
