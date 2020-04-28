package net.folivo.matrix.appservice.api

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class MatrixNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND, "NET.FOLIVO.MATRIX_NOT_FOUND") {
}