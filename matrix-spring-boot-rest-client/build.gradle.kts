dependencies {
    api("org.springframework.boot:spring-boot-starter")

    api("net.folivo:trixnity-rest-client:${Versions.trixnity}")
    implementation("io.ktor:ktor-client-java:${Versions.ktor}")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}