package net.folivo.matrix.appservice.config

import net.folivo.matrix.common.api.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MatrixAppserviceExceptionHandler {

    @ExceptionHandler(Exception::class)//FIXME
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse("500", exception.message ?: ""), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}