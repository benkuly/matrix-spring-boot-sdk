package net.folivo.matrix.appservice

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse


@SpringBootConfiguration
@EnableAutoConfiguration
class TestApplication {

    @Bean
    fun something(): RouterFunction<ServerResponse> {
        return route(GET("/_matrix/something").and(accept(APPLICATION_JSON)), HandlerFunction {
            ServerResponse.ok().build()
        })
    }

}