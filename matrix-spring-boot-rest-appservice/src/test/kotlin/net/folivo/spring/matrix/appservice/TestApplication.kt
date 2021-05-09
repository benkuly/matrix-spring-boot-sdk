package net.folivo.spring.matrix.appservice

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import


@SpringBootConfiguration
@EnableAutoConfiguration
@Import(MatrixAppserviceAutoconfiguration::class, TestConfiguration::class)
class TestApplication {
    
}