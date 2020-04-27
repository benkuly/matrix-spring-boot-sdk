package net.folivo.matrix.appservice.config

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.web.reactive.function.server.ServerRequest


class ErrorResponseAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, includeStackTrace: Boolean): MutableMap<String, Any> {
        val defaultErrorAttributes = super.getErrorAttributes(request, includeStackTrace) // TODO can be optimized
        val errorAttributes = HashMap<String, Any>()

        errorAttributes.put("errcode", defaultErrorAttributes["status"] ?: 500)
        errorAttributes.put("error", determineErrorMessage(getError(request)))
        return errorAttributes
    }

    private fun determineErrorMessage(exception: Throwable): String {
        return if (exception is AccessDeniedException) {
            "M_UNAUTHORIZED"
        } else {
            exception.message ?: "UNKNOWN"
        }
    }
}