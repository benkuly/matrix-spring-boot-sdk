package net.folivo.matrix.bot.config

import io.r2dbc.spi.ConnectionFactory
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.autoconfigure.r2dbc.EmbeddedDatabaseConnection
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableR2dbcRepositories(basePackages = ["net.folivo.matrix.bot"])
@EnableTransactionManagement
@EnableConfigurationProperties(MatrixBotProperties::class)
class MatrixBotDatabaseAutoconfiguration(
        private val botProperties: MatrixBotProperties,
        private val resourceLoader: ResourceLoader
) : AbstractR2dbcConfiguration() {

    @Bean("liquibaseDatasource")
    fun liquibaseDatasource(): DataSource {
        return botProperties.migration.initializeDataSourceBuilder().build();
    }

    @Bean
    fun liquibase(@Qualifier("liquibaseDatasource") liquibaseDatasource: DataSource): SpringLiquibase {
        return SpringLiquibase().apply {
            changeLog = "classpath:db/changelog/net.folivo.matrix.bot.changelog-master.yml"
            dataSource = liquibaseDatasource
        }
    }

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.of(botProperties.database) { EmbeddedDatabaseConnection.get(resourceLoader.classLoader) }
                .build()
    }

    override fun getCustomConverters(): MutableList<Any> {
        return mutableListOf(MatrixIdReadingConverter(), MatrixIdWritingConverter())
    }
}