package net.folivo.matrix.restclient.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MatrixMessageEventContent(val type: String)