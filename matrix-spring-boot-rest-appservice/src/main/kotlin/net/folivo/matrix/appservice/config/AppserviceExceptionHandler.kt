package net.folivo.matrix.appservice.config

import net.folivo.matrix.appservice.api.MatrixNotFoundException
import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AppserviceExceptionHandler {

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllOtherExceptions(exception: Exception): ResponseEntity<ErrorResponse> {
        LOG.error("some unhandled exception occurred", exception)
        return ResponseEntity(ErrorResponse("M_UNKNOWN", exception.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MatrixServerException::class)
    fun handleMatrixServerException(exception: MatrixServerException): ResponseEntity<ErrorResponse> {
        LOG.warn("MatrixServerException occurred", exception)
        return ResponseEntity(exception.errorResponse, exception.statusCode)
    }

    @ExceptionHandler(MatrixNotFoundException::class)
    fun handleMatrixNotFoundException(exception: MatrixNotFoundException): ResponseEntity<ErrorResponse> {
        LOG.debug("MatrixNotFoundException occurred", exception)
        return ResponseEntity(exception.errorResponse, exception.statusCode)
    }
}