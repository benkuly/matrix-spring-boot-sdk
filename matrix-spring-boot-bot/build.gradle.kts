description = "Spring Boot Starter for [Matrix] bots and appservices based on Trixnity."

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    api(project(":matrix-spring-boot-rest-client"))
    api(project(":matrix-spring-boot-rest-appservice"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.michael-bull.kotlin-retry:kotlin-retry:${Versions.kotlinRetry}")


    api("org.springframework.boot:spring-boot-starter-data-r2dbc")

    api("org.liquibase:liquibase-core")
    implementation("org.springframework.data:spring-data-jdbc")
    implementation("com.zaxxer:HikariCP")

    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("com.h2database:h2")

    testImplementation("io.projectreactor:reactor-test")
}