package net.folivo.spring.matrix.appservice

import net.folivo.spring.matrix.restclient.MatrixClientAutoconfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class MatrixAppserviceAutoconfigurationContextTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                MatrixAppserviceAutoconfiguration::class.java,
                MatrixClientAutoconfiguration::class.java,
                TestConfiguration::class.java
            )
        )

    @Test
    fun `default services`() {
        this.contextRunner
            .withPropertyValues(
                "matrix.client.homeServer.hostname=localhost",
                "matrix.client.token=test",
                "matrix.appservice.hsToken=token"
            )
            .run { context: AssertableApplicationContext ->
                assertThat(context).hasSingleBean(AppserviceApplicationEngine::class.java)
            }
    }
}