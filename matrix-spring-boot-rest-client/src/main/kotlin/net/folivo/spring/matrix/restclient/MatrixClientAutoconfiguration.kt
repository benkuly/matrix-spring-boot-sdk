package net.folivo.spring.matrix.restclient

import io.ktor.client.*
import io.ktor.client.engine.java.*
import net.folivo.trixnity.client.rest.MatrixClient
import net.folivo.trixnity.client.rest.api.sync.InMemorySyncBatchTokenService
import net.folivo.trixnity.client.rest.api.sync.SyncBatchTokenService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(MatrixClientConfigurationProperties::class)
class MatrixClientAutoconfiguration {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun matrixClient(
        properties: MatrixClientConfigurationProperties,
        syncBatchTokenService: SyncBatchTokenService
    ): MatrixClient {
        return MatrixClient(HttpClient(Java), properties.toMatrixClientProperties(), syncBatchTokenService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun inMemorySyncBatchTokenService(): SyncBatchTokenService {
        LOG.info("you should implement a persistent SyncBatchTokenService if you use the sync api. Currently used: ${InMemorySyncBatchTokenService::class.simpleName}")
        return InMemorySyncBatchTokenService
    }
}