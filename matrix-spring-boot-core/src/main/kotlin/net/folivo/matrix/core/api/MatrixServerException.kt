package net.folivo.matrix.core.api

import org.springframework.http.HttpStatus

open class MatrixServerException(
        val statusCode: HttpStatus,
        val errorResponse: ErrorResponse
) : Exception("status: $statusCode; errorCode: ${errorResponse.errorCode}; errorMessage: ${errorResponse.errorMessage}")