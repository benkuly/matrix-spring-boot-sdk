plugins {
    `maven-publish`
    signing
}

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

publishing {
    publications {
        create<MavenPublication>("matrix-spring-boot-bot") {
            pom {
                artifactId = "matrix-spring-boot-bot"

                from(components["java"])

                name.set("matrix-spring-boot-bot")
                description.set("Spring Boot Starter for matrix-protocol bots.")
                url.set("https://github.com/benkuly/matrix-spring-boot-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("benkuly")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/benkuly/matrix-spring-boot-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/benkuly/matrix-spring-boot-sdk.git")
                    url.set("https://github.com/benkuly/matrix-spring-boot-sdk")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
            System.getenv("MAVEN_SIGN_KEY"),
            System.getenv("MAVEN_SIGN_PASSWORD")
    )
    sign(publishing.publications["matrix-spring-boot-bot"])
}