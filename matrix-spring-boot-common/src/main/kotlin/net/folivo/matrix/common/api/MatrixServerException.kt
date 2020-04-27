package net.folivo.matrix.common.api

open class MatrixServerException(
        val statusCode: Int,
        val errorResponse: ErrorResponse
) : Exception(errorResponse.errorMessage)