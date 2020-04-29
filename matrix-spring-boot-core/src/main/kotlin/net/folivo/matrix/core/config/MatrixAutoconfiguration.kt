package net.folivo.matrix.core.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.Module
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MatrixAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun matrixConfiguration(configurer: List<MatrixConfigurer>): MatrixConfiguration {
        val config = MatrixConfiguration()
        configurer.forEach {
            it.configure(config)
        }
        return config
    }

    @Bean
    fun defaultMatrixConfigurer(): DefaultMatrixConfigurer {
        return DefaultMatrixConfigurer()
    }

    @Bean
    fun matrixEventJacksonModule(config: MatrixConfiguration): Module {
        return MatrixEventJacksonModule(
                config.registeredEvents,
                config.registeredMessageEventContent
        )
    }

    @Bean
    fun modifyBuilder(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer {
            it.serializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}