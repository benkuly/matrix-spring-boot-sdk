package net.folivo.matrix.bot.config

import io.mockk.every
import io.mockk.mockk
import net.folivo.matrix.bot.client.MatrixClientBot
import net.folivo.matrix.bot.client.PersistenceSyncBatchTokenService
import net.folivo.matrix.bot.client.SyncBatchTokenRepository
import net.folivo.matrix.bot.handler.MatrixMessageEventHandler
import net.folivo.matrix.restclient.MatrixClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux

@Disabled("no high priority to solve issues")
class MatrixClientBotAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(
                            MatrixClientBotAutoconfigurationContextTestConfiguration::class.java,
                            MatrixBotAutoconfiguration::class.java,
                            MatrixClientBotAutoconfiguration::class.java
                    )
            )

    @Configuration
    class MatrixClientBotAutoconfigurationContextTestConfiguration {
        @Bean
        fun matrixClient(): MatrixClient {
            return mockk(relaxed = true) {
                every { syncApi } returns mockk(relaxed = true) {
                    every { syncLoop() } returns Flux.empty()
                }
            }
        }

        @Bean
        fun syncBatchTokenRepository(): SyncBatchTokenRepository {
            return mockk(relaxed = true)
        }
    }

    @Test
    fun `default services`() {
        this.contextRunner
                .withPropertyValues(
                        "matrix.bot.mode=CLIENT",
                        "matrix.homeServer.hostname=localhost",
                        "matrix.token=test"
                )
                .run { context: AssertableApplicationContext ->
                    assertThat(context).hasSingleBean(MatrixClientBot::class.java)
                    assertThat(context).hasSingleBean(MatrixMessageEventHandler::class.java)
                    assertThat(context).hasSingleBean(PersistenceSyncBatchTokenService::class.java)
                }
    }
}