package net.folivo.matrix.restclient.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.Module
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.ErrorResponse
import net.folivo.matrix.restclient.api.MatrixServerException
import net.folivo.matrix.restclient.api.sync.InMemorySyncBatchTokenService
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono

@Configuration
@EnableConfigurationProperties(MatrixClientProperties::class)
@AutoConfigureAfter(value = [WebClientAutoConfiguration::class, JacksonAutoConfiguration::class])
class MatrixClientAutoconfiguration() {

    private val logger = LoggerFactory.getLogger(MatrixClientAutoconfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun matrixClientConfiguration(configurer: List<MatrixClientConfigurer>): MatrixClientConfiguration {
        val config = MatrixClientConfiguration()
        configurer.forEach {
            it.configure(config)
        }
        return config
    }

    @Bean
    fun defaultMatrixClientConfigurer(): DefaultMatrixClientConfigurer {
        return DefaultMatrixClientConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean
    fun matrixClient(
            @Qualifier("matrixWebClient")
            matrixWebClient: WebClient,
            syncBatchTokenService: SyncBatchTokenService
    ): MatrixClient {
        return MatrixClient(matrixWebClient, syncBatchTokenService)
    }

    @Bean
    @ConditionalOnMissingBean
    fun inMemorySyncBatchTokenService(): SyncBatchTokenService {
        logger.warn("you should implement a persistent SyncBatchTokenService. Currently used: InMemorySyncBatchTokenService")
        return InMemorySyncBatchTokenService()
    }

    @Bean("matrixWebClient")
    @ConditionalOnMissingBean
    fun matrixWebClient(
            config: MatrixClientProperties,
            webClientBuilder: WebClient.Builder
    ): WebClient {
        return webClientBuilder
                .baseUrl(
                        DefaultUriBuilderFactory().builder()
                                .scheme(if (config.homeServer.secure) "https" else "http")
                                .host(config.homeServer.hostname)
                                .port(config.homeServer.port)
                                .path("/_matrix/client").build().toASCIIString()
                )
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${config.token}")
                .filter(ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono<ErrorResponse>()
                                .flatMap {
                                    Mono.error<ClientResponse>(
                                            MatrixServerException(
                                                    clientResponse.statusCode()
                                                            .value(), it
                                            )
                                    )
                                }
                    } else {
                        Mono.just(clientResponse)
                    }
                })
                .build();
    }

    @Bean
    fun matrixEventJacksonModule(config: MatrixClientConfiguration): Module {
        return MatrixEventJacksonModule(
                config.registeredEvents,
                config.registeredMessageEventContent
        )
    }

    @Bean
    fun modifyBuilder(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer {
            it.serializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

}