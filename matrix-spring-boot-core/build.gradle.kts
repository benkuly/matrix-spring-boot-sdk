plugins {
    `maven-publish`
    signing
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-json")

    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

publishing {
    publications {
        create<MavenPublication>("matrix-spring-boot-core") {
            pom {
                artifactId = "matrix-spring-boot-core"

                from(components["java"])

                name.set("matrix-spring-boot-core")
                description.set("Spring Boot Starter for matrix-protocol client.")
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
    sign(publishing.publications["matrix-spring-boot-core"])
}