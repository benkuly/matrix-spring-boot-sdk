package net.folivo.matrix.bot.config

import net.folivo.matrix.bot.MatrixBot
import net.folivo.matrix.bot.handler.MatrixMessageContentHandler
import net.folivo.matrix.bot.handler.MatrixMessageEventHandler
import net.folivo.matrix.bot.sync.PersistenceSyncBatchTokenService
import net.folivo.matrix.bot.sync.SyncBatchTokenRepository
import net.folivo.matrix.core.handler.MatrixEventHandler
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@Configuration
@EnableConfigurationProperties(MatrixBotProperties::class)
@EnableJpaRepositories("net.folivo.matrix.bot.sync")
@EntityScan("net.folivo.matrix.bot.sync")
class MatrixBotAutoconfiguration(private val botProperties: MatrixBotProperties) {

    @Bean
    @ConditionalOnProperty(name = ["matrix.bot.mode"], havingValue = "CLIENT")
    @ConditionalOnMissingBean
    fun matrixBot(matrixClient: MatrixClient, matrixEventHandler: List<MatrixEventHandler>): MatrixBot {
        val matrixBot = MatrixBot(matrixClient, matrixEventHandler, botProperties)
        matrixBot.start()
        return matrixBot
    }

    @Bean
    fun matrixMessageEventHandler(
            matrixMessageContentHandler: List<MatrixMessageContentHandler>,
            matrixClient: MatrixClient
    ): MatrixEventHandler {
        return MatrixMessageEventHandler(matrixMessageContentHandler, matrixClient)
    }

    @Bean
    @ConditionalOnMissingBean
    fun syncBatchTokenService(syncBatchTokenRepository: SyncBatchTokenRepository): SyncBatchTokenService {
        return PersistenceSyncBatchTokenService(syncBatchTokenRepository)
    }

}