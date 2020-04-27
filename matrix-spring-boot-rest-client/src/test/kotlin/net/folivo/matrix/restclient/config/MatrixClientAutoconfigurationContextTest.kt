package net.folivo.matrix.restclient.config

import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.reactive.function.client.WebClient


class MatrixClientAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(
                            MatrixClientAutoconfiguration::class.java,
                            WebClientAutoConfiguration::class.java
                    )
            )

    @Test
    fun `default services`() {
        this.contextRunner
                .withPropertyValues("matrix.homeServer.hostname=localhost", "matrix.token=test")
                .run { context: AssertableApplicationContext ->
                    assertThat(context).hasSingleBean(MatrixClient::class.java)
                    assertThat(context).hasSingleBean(WebClient::class.java)
                    assertThat(context).hasSingleBean(SyncBatchTokenService::class.java)
                }
    }
}