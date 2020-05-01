package net.folivo.matrix.restclient.config

import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import net.folivo.matrix.restclient.MatrixClient
import net.folivo.matrix.restclient.api.sync.InMemorySyncBatchTokenService
import net.folivo.matrix.restclient.api.sync.SyncBatchTokenService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
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
class MatrixClientAutoconfiguration {

    private val logger = LoggerFactory.getLogger(MatrixClientAutoconfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun matrixClientConfiguration(config: MatrixClientProperties): MatrixClientConfiguration {
        return MatrixClientConfiguration(config)
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

    @Bean
    fun webClientTokenAuthorizationFilter(matrixClientConfiguration: MatrixClientConfiguration): WebClientTokenAuthorizationFilter {
        return WebClientTokenAuthorizationFilter(matrixClientConfiguration)
    }

    @Bean("matrixWebClient")
    @ConditionalOnMissingBean
    fun matrixWebClient(
            config: MatrixClientProperties,
            webClientBuilder: WebClient.Builder,
            webClientTokenAuthorizationFilter: WebClientTokenAuthorizationFilter
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
                .filter(ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
                    if (clientResponse.statusCode().isError) {
                        clientResponse.bodyToMono<ErrorResponse>()
                                .flatMap {
                                    Mono.error<ClientResponse>(MatrixServerException(clientResponse.statusCode(), it))
                                }
                    } else {
                        Mono.just(clientResponse)
                    }
                })
                .filter(webClientTokenAuthorizationFilter)
                .build();
    }
}