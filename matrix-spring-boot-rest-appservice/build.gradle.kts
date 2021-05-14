dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    api(project(":matrix-spring-boot-rest-client"))
    api("net.folivo:trixnity-rest-appservice:${Versions.trixnity}")
    implementation("io.ktor:ktor-server-cio:${Versions.ktor}")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}