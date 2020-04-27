package net.folivo.matrix.restclient.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MatrixEvent(val type: String)