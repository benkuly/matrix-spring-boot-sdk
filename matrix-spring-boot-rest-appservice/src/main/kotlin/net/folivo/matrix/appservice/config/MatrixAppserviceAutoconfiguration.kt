package net.folivo.matrix.appservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.folivo.matrix.appservice.api.*
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Mono


@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableWebFluxSecurity
@EnableWebFlux
@EnableConfigurationProperties(MatrixAppserviceProperties::class)
class MatrixAppserviceAutoconfiguration(private val matrixAppserviceProperties: MatrixAppserviceProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceHandler(
            matrixClient: MatrixClient,
            matrixAppserviceEventService: MatrixAppserviceEventService,
            matrixAppserviceUserService: MatrixAppserviceUserService,
            matrixAppserviceRoomService: MatrixAppserviceRoomService
    ): AppserviceHandler {
        return DefaultAppserviceHandler(
                matrixClient,
                matrixAppserviceEventService,
                matrixAppserviceUserService,
                matrixAppserviceRoomService
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun noOpMatrixAppserviceEventService(): MatrixAppserviceEventService {
        return NoOpMatrixAppserviceEventService()
    }

    @Bean
    @ConditionalOnMissingBean
    fun noopMatrixAppserviceUserService(): MatrixAppserviceUserService {
        return NoOpMatrixAppserviceUserService()
    }

    @Bean
    @ConditionalOnMissingBean
    fun noOpMatrixAppserviceRoomService(): MatrixAppserviceRoomService {
        return NoOpMatrixAppserviceRoomService()
    }

    @Bean
    fun appserviceController(appserviceHandler: AppserviceHandler): AppserviceController {
        return AppserviceController(appserviceHandler)
    }

    @Bean
    fun matrixAppserviceExceptionHandler(): MatrixAppserviceExceptionHandler {
        return MatrixAppserviceExceptionHandler()
    }

    @Bean
    fun springSecurityFilterChain(
            http: ServerHttpSecurity,
            matrixHomeServerAuthenticationManager: MatrixHomeServerAuthenticationManager,
            objectMapper: ObjectMapper
    ): SecurityWebFilterChain? {
        val authenticationWebFilter = AuthenticationWebFilter(matrixHomeServerAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(MatrixHomeServerAuthenticationConverter())
        authenticationWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        authenticationWebFilter.setAuthenticationFailureHandler { webFilterExchange, _ ->
            Mono.defer { Mono.just(webFilterExchange.exchange.response) }
                    .flatMap { response: ServerHttpResponse ->
                        response.statusCode = HttpStatus.FORBIDDEN
                        response.headers.contentType = MediaType.APPLICATION_JSON
                        val dataBufferFactory = response.bufferFactory()
                        val buffer: DataBuffer = dataBufferFactory.wrap(
                                objectMapper.writeValueAsBytes(ErrorResponse("403", "NET.FOLIVO.MATRIX_FORBIDDEN"))
                        )
                        response.writeWith(Mono.just(buffer))
                                .doOnError { DataBufferUtils.release(buffer) }
                    }
        }

        http.authorizeExchange()
                .pathMatchers("/_matrix/**").authenticated()
                .and()
                .csrf().disable()
                .requestCache().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling()
        return http.build()
    }

    @Bean
    fun matrixHomeServerAuthenticationManager(): ReactiveAuthenticationManager {
        return MatrixHomeServerAuthenticationManager(matrixAppserviceProperties.hsToken)
    }

    @Bean
    fun errorResponseAttributes(): ErrorResponseAttributes {
        return ErrorResponseAttributes()
    }

}