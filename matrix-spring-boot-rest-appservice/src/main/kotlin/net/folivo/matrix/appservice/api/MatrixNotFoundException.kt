package net.folivo.matrix.appservice.api

import net.folivo.matrix.core.api.ErrorResponse
import net.folivo.matrix.core.api.MatrixServerException
import org.springframework.http.HttpStatus

class MatrixNotFoundException(message: String) : MatrixServerException(
        HttpStatus.NOT_FOUND,
        ErrorResponse("NET.FOLIVO.MATRIX_NOT_FOUND", message)
)