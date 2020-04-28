package net.folivo.matrix.core.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MatrixMessageEventContent(val type: String)