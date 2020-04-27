package net.folivo.matrix.common.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MatrixMessageEventContent(val type: String)