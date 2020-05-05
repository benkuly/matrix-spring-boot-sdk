package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.client.MatrixClientBot
import net.folivo.matrix.bot.client.PersistenceSyncBatchTokenService
import net.folivo.matrix.bot.client.SyncBatchTokenRepository
import net.folivo.matrix.bot.handler.MatrixEventHandler
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@Configuration
@ConditionalOnProperty(prefix = "matrix.bot", name = ["mode"], havingValue = "CLIENT", matchIfMissing = true)
@EnableJpaRepositories("net.folivo.matrix.bot.client")
@EntityScan("net.folivo.matrix.bot.client")
class MatrixClientBotAutoconfiguration(private val botProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun matrixClientBot(matrixClient: MatrixClient, matrixEventHandler: List<MatrixEventHandler>): MatrixClientBot {
        val matrixClientBot = MatrixClientBot(
                matrixClient,
                matrixEventHandler,
                botProperties
        )
        matrixClientBot.start()
        return matrixClientBot
    }

    @Bean
    @ConditionalOnMissingBean
    fun syncBatchTokenService(syncBatchTokenRepository: SyncBatchTokenRepository): SyncBatchTokenService {
        return PersistenceSyncBatchTokenService(syncBatchTokenRepository)
    }

}