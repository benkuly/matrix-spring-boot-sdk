package net.folivo.matrix.appservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.folivo.matrix.core.api.ErrorResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class MatrixAppserviceWebfluxConfiguration(private val matrixAppserviceProperties: MatrixAppserviceProperties) : WebFluxConfigurer {

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
    fun matrixHomeServerAuthenticationManager(): MatrixHomeServerAuthenticationManager {
        return MatrixHomeServerAuthenticationManager(matrixAppserviceProperties.hsToken)
    }

    @Bean
    fun errorResponseAttributes(): ErrorResponseAttributes {
        return ErrorResponseAttributes()
    }

}