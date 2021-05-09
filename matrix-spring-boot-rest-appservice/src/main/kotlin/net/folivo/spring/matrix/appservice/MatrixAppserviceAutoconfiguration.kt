package net.folivo.spring.matrix.appservice

import net.folivo.trixnity.appservice.rest.AppserviceService
import net.folivo.trixnity.appservice.rest.DefaultAppserviceService
import net.folivo.trixnity.appservice.rest.event.AppserviceEventTnxService
import net.folivo.trixnity.appservice.rest.room.AppserviceRoomService
import net.folivo.trixnity.appservice.rest.user.AppserviceUserService
import net.folivo.trixnity.client.rest.MatrixClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(MatrixAppserviceConfigurationProperties::class)
class MatrixAppserviceAutoconfiguration {


    @Bean
    @ConditionalOnMissingBean
    fun defaultAppserviceService(
        matrixClient: MatrixClient,
        appserviceEventService: AppserviceEventTnxService,
        appserviceUserService: AppserviceUserService,
        appserviceRoomService: AppserviceRoomService,
    ): AppserviceService {
        return DefaultAppserviceService(
            appserviceEventService,
            appserviceUserService,
            appserviceRoomService,
        )
    }


    @Bean
    @ConditionalOnMissingBean
    fun appserviceApplication(
        properties: MatrixAppserviceConfigurationProperties,
        appserviceService: AppserviceService
    ): AppserviceApplicationEngine {
        return AppserviceApplicationEngine(properties, appserviceService)
    }

}