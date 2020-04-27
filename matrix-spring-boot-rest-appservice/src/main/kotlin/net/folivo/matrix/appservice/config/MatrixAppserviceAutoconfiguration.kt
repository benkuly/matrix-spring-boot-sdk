package net.folivo.matrix.appservice.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


@Configuration
@Import(MatrixAppserviceWebfluxConfiguration::class)
@EnableConfigurationProperties(MatrixAppserviceProperties::class)
class MatrixAppserviceAutoconfiguration(private val matrixAppserviceProperties: MatrixAppserviceProperties) {


}