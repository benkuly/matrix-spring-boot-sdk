package net.folivo.matrix.core.api

open class MatrixServerException(
        val statusCode: Int,
        val errorResponse: ErrorResponse
) : Exception(errorResponse.errorMessage)