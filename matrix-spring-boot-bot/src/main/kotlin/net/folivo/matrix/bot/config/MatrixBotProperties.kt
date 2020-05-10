package net.folivo.matrix.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("matrix.bot")
@ConstructorBinding
data class MatrixBotProperties(
        @DefaultValue("RESTRICTED")
        val autoJoin: AutoJoinMode = AutoJoinMode.RESTRICTED,
        val serverName: String,
        @DefaultValue("CLIENT")
        val mode: BotMode = BotMode.CLIENT
) {
    enum class BotMode {
        APPSERVICE, CLIENT
    }

    enum class AutoJoinMode {
        ENABLED, DISABLED, RESTRICTED
    }
}
