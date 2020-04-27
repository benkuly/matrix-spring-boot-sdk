package net.folivo.matrix.restclient.api

open class MatrixServerException(
        val statusCode: Int,
        val errorResponse: ErrorResponse
) : Exception(errorResponse.errorMessage)