package net.folivo.matrix.appservice

import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import org.springframework.http.HttpStatus.I_AM_A_TEAPOT
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class TestRestController {
    @GetMapping("/someError")
    fun somethingWithError(): Mono<String> {
        return Mono.error(MatrixServerException(I_AM_A_TEAPOT, ErrorResponse("TEA_PLEASE")))
    }
}