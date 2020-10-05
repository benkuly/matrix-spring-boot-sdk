package net.folivo.matrix.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("matrix.bot")
@ConstructorBinding
data class MatrixBotProperties(
        val autoJoin: AutoJoinMode = AutoJoinMode.RESTRICTED,
        val trackMembership: TrackMembershipMode = TrackMembershipMode.NONE,
        val serverName: String,
        val username: String,
        val mode: BotMode = BotMode.CLIENT
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
