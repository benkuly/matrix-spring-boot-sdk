package net.folivo.matrix.appservice.config

import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MatrixAppserviceExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleAllOtherExceptions(exception: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse("M_UNKNOWN", exception.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MatrixServerException::class)
    fun handleMatrixServerException(exception: MatrixServerException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(exception.errorResponse, exception.statusCode)
    }
}