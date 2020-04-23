package net.folivo.matrix.bot.config

import io.mockk.every
import io.mockk.mockk
import net.folivo.matrix.bot.MatrixBot
import net.folivo.matrix.bot.handler.MatrixMessageEventHandler
import net.folivo.matrix.bot.sync.PersistenceSyncBatchTokenService
import net.folivo.matrix.bot.sync.SyncBatchTokenRepository
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncApiClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux

class MatrixBotAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(
                            MatrixBotAutoconfiguration::class.java,
                            MatrixBotAutoconfigurationContextTestConfiguration::class.java
                    )
            )

    @Configuration
    class MatrixBotAutoconfigurationContextTestConfiguration {
        @Bean
        fun matrixClient(): MatrixClient {
            val mockMatrixClient = mockk<MatrixClient>()
            val syncApiMock = mockk<SyncApiClient>()
            every { mockMatrixClient.syncApi }.returns(syncApiMock)
            every { syncApiMock.syncLoop() }.returns(Flux.empty())
            return mockMatrixClient
        }

        @Bean
        fun syncBatchTokenRepository(): SyncBatchTokenRepository {
            return mockk<SyncBatchTokenRepository>()
        }
    }

    @Test
    fun `default services`() {
        this.contextRunner
                .withPropertyValues("matrix.homeServer.hostname=localhost", "matrix.token=test")
                .run { context: AssertableApplicationContext ->
                    Assertions.assertThat(context).hasSingleBean(MatrixBot::class.java)
                    Assertions.assertThat(context).hasSingleBean(MatrixMessageEventHandler::class.java)
                    Assertions.assertThat(context).hasSingleBean(PersistenceSyncBatchTokenService::class.java)
                }
    }
}