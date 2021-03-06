package net.folivo.matrix.appservice.config

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.web.reactive.function.server.ServerRequest


class ErrorResponseAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions?): MutableMap<String, Any> {
        val defaultErrorAttributes = super.getErrorAttributes(request, options)
        defaultErrorAttributes["errcode"] = defaultErrorAttributes["status"] ?: 500
        defaultErrorAttributes["error"] = determineErrorMessage(getError(request))
        return defaultErrorAttributes
    }

    private fun determineErrorMessage(exception: Throwable): String {
        println("have ${exception.message}")
        return if (exception is AccessDeniedException) {
            "M_UNAUTHORIZED"
        } else {
            exception.message ?: "UNKNOWN"
        }
    }
}