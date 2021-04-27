package net.folivo.matrix.appservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.folivo.matrix.appservice.api.AppserviceController
import net.folivo.matrix.appservice.api.AppserviceHandler
import net.folivo.matrix.appservice.api.AppserviceHandlerHelper
import net.folivo.matrix.appservice.api.DefaultAppserviceHandler
import net.folivo.matrix.appservice.api.event.AppserviceEventService
import net.folivo.matrix.appservice.api.room.AppserviceRoomService
import net.folivo.matrix.appservice.api.user.AppserviceUserService
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.restclient.MatrixClient
import org.springframework.beans.factory.annotation.Qualifier
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
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableWebFluxSecurity
@EnableWebFlux
@EnableConfigurationProperties(AppserviceProperties::class)
class AppserviceAutoconfiguration(private val appserviceProperties: AppserviceProperties) {

    @Bean
    @ConditionalOnMissingBean
    fun appserviceHandlerHelper(
        matrixClient: MatrixClient,
        @Qualifier("matrixWebClient") webClient: WebClient,
        appserviceUserService: AppserviceUserService,
        appserviceRoomService: AppserviceRoomService
    ): AppserviceHandlerHelper {
        return AppserviceHandlerHelper(
            matrixClient,
            webClient,
            appserviceUserService,
            appserviceRoomService
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceHandler(
        matrixClient: MatrixClient,
        appserviceEventService: AppserviceEventService,
        appserviceUserService: AppserviceUserService,
        appserviceRoomService: AppserviceRoomService,
        helper: AppserviceHandlerHelper
    ): AppserviceHandler {
        return DefaultAppserviceHandler(
            appserviceEventService,
            appserviceUserService,
            appserviceRoomService,
            helper
        )
    }

    @Bean
    fun appserviceController(appserviceHandler: AppserviceHandler): AppserviceController {
        return AppserviceController(appserviceHandler)
    }

    @Bean
    fun matrixAppserviceExceptionHandler(): AppserviceExceptionHandler {
        return AppserviceExceptionHandler()
    }

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        homeServerAuthenticationManager: HomeServerAuthenticationManager,
        objectMapper: ObjectMapper
    ): SecurityWebFilterChain? {
        val authenticationWebFilter = AuthenticationWebFilter(homeServerAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(HomeServerAuthenticationConverter())
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
            .pathMatchers("/**").authenticated()
            .and()
            .cors().disable()
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
        return HomeServerAuthenticationManager(appserviceProperties.hsToken)
    }

    @Bean
    fun errorResponseAttributes(): ErrorResponseAttributes {
        return ErrorResponseAttributes()
    }

}