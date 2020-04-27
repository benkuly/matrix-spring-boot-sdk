package net.folivo.matrix.appservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFluxSecurity
class MatrixAppserviceWebfluxConfiguration(private val matrixAppserviceProperties: MatrixAppserviceProperties) : WebFluxConfigurer {

    @Bean
    fun springSecurityFilterChain(
            http: ServerHttpSecurity,
            matrixHomeServerAuthenticationManager: MatrixHomeServerAuthenticationManager
    ): SecurityWebFilterChain? {
        val authenticationWebFilter = AuthenticationWebFilter(matrixHomeServerAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(MatrixHomeServerAuthenticationConverter())
        authenticationWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance())

        http.authorizeExchange()
                .pathMatchers("/_matrix/**").authenticated()
                .and()
                .csrf().disable()
                .requestCache().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
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